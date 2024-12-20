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
import java.util.Set;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
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
   /***************************************************************************
    **
    ***************************************************************************/
   public static BulkLoadTableStructure buildTableStructure(String tableName)
   {
      return (buildTableStructure(tableName, null, null));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static BulkLoadTableStructure buildTableStructure(String tableName, Association association, String parentAssociationPath)
   {
      QTableMetaData table = QContext.getQInstance().getTable(tableName);

      BulkLoadTableStructure tableStructure = new BulkLoadTableStructure();
      tableStructure.setTableName(tableName);
      tableStructure.setLabel(table.getLabel());

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

      fields.sort(Comparator.comparing(f -> ObjectUtils.requireNonNullElse(f.getLabel(), f.getName(), "")));

      for(Association childAssociation : CollectionUtils.nonNullList(table.getAssociations()))
      {
         BulkLoadTableStructure associatedStructure = buildTableStructure(childAssociation.getAssociatedTableName(), childAssociation, parentAssociationPath);
         tableStructure.addAssociation(associatedStructure);
      }

      return (tableStructure);
   }
}
