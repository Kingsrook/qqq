/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.bulk.TableKeyFieldsPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** utility to build BulkLoadTableStructure objects for a QQQ Table.
 *******************************************************************************/
public class BulkLoadTableStructureBuilder
{
   private static final QLogger LOG = QLogger.getLogger(BulkLoadTableStructureBuilder.class);



   /***************************************************************************
    **
    ***************************************************************************/
   public static BulkLoadTableStructure buildTableStructure(String tableName)
   {
      return (buildTableStructure(tableName, null, null, false));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static BulkLoadTableStructure buildTableStructure(String tableName, Boolean isBulkEdit)
   {
      return (buildTableStructure(tableName, null, null, isBulkEdit));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static BulkLoadTableStructure buildTableStructure(String tableName, Association association, String parentAssociationPath, Boolean isBulkEdit)
   {
      QTableMetaData table = QContext.getQInstance().getTable(tableName);

      BulkLoadTableStructure tableStructure = new BulkLoadTableStructure();
      tableStructure.setTableName(tableName);
      tableStructure.setLabel(table.getLabel());
      tableStructure.setIsBulkEdit(isBulkEdit);

      Set<String> associationJoinFieldNamesToExclude = new HashSet<>();

      if(association == null)
      {
         tableStructure.setIsMain(true);
         tableStructure.setIsMany(false);
         tableStructure.setAssociationPath(null);
      }
      else
      {
         tableStructure.setIsMain(false);

         QJoinMetaData join = QContext.getQInstance().getJoin(association.getJoinName());
         if(join.getType().equals(JoinType.ONE_TO_MANY) || join.getType().equals(JoinType.MANY_TO_ONE))
         {
            tableStructure.setIsMany(true);
         }

         for(JoinOn joinOn : join.getJoinOns())
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////
            // don't allow the user to map the "join field" from a child up to its parent                 //
            // (e.g., you can't map lineItem.orderId -- that'll happen automatically via the association) //
            ////////////////////////////////////////////////////////////////////////////////////////////////
            if(join.getLeftTable().equals(tableName))
            {
               associationJoinFieldNamesToExclude.add(joinOn.getLeftField());
            }
            else if(join.getRightTable().equals(tableName))
            {
               associationJoinFieldNamesToExclude.add(joinOn.getRightField());
            }
         }

         if(!StringUtils.hasContent(parentAssociationPath))
         {
            tableStructure.setAssociationPath(association.getName());
         }
         else
         {
            tableStructure.setAssociationPath(parentAssociationPath + "." + association.getName());
         }
      }

      ArrayList<QFieldMetaData> fields = new ArrayList<>();
      tableStructure.setFields(fields);
      for(QFieldMetaData field : table.getFields().values())
      {
         if(field.getIsEditable() && !associationJoinFieldNamesToExclude.contains(field.getName()))
         {
            fields.add(field);
         }
      }

      ////////////////////////////////////////////////////////
      // for bulk edit, users can use the primary key field //
      ////////////////////////////////////////////////////////
      if(isBulkEdit)
      {
         fields.add(table.getField(table.getPrimaryKeyField()));

         //////////////////////////////////////////////////////////////////////
         // also make available what key fields are available for this table //
         //////////////////////////////////////////////////////////////////////
         try
         {
            SearchPossibleValueSourceInput input = new SearchPossibleValueSourceInput()
               .withPossibleValueSourceName("tableKeyFields")
               .withPathParamMap(Map.of("processName", tableName + ".bulkEditWithFile"));
            List<QPossibleValue<String>> search = new TableKeyFieldsPossibleValueSource().search(input);
            tableStructure.setPossibleKeyFields(new ArrayList<>(search.stream().map(QPossibleValue::getId).toList()));
         }
         catch(QException qe)
         {
            LOG.warn("Unable to retrieve possible key fields for table [" + tableName + "]", qe);
         }
      }

      fields.sort(Comparator.comparing(f -> ObjectUtils.requireNonNullElse(f.getLabel(), f.getName(), "")));

      for(Association childAssociation : CollectionUtils.nonNullList(table.getAssociations()))
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // at this time, we are not prepared to handle 3-level deep associations, so, only process them from the top level... //
         // main challenge being, wide-mode.  so, maybe we should just only support 3-level+ associations for tall?            //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(association == null)
         {
            String nextLevelPath =
               (StringUtils.hasContent(parentAssociationPath) ? parentAssociationPath + "." : "")
                  + (association != null ? association.getName() : "");
            BulkLoadTableStructure associatedStructure = buildTableStructure(childAssociation.getAssociatedTableName(), childAssociation, nextLevelPath, isBulkEdit);
            tableStructure.addAssociation(associatedStructure);
         }
      }

      return (tableStructure);
   }
}
