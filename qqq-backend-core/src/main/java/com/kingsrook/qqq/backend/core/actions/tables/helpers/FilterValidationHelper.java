/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.metadata.personalization.TableMetaDataPersonalizerAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.personalization.TableMetaDataPersonalizerInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryOrCountInputInterface;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * Utility to help query action validate the fieldNames in a filter.
 *******************************************************************************/
public class FilterValidationHelper
{

   /***************************************************************************
    * throw an exception if a filter contains any field names (in its criteria
    * or orderBys) that aren't in the input table (which may be user-personalized).
    ***************************************************************************/
   public static void validateFieldNamesInFilter(QueryOrCountInputInterface input) throws QException
   {
      if(input.getFilter() == null)
      {
         ///////////////////////////////////////
         // if no filter, nothing to validate //
         ///////////////////////////////////////
         return;
      }

      List<String> unrecognizedFieldNames = new ArrayList<>();

      validateFieldNamesInFilterInner(input, input.getFilter(), input.getTable(), new HashMap<>(), unrecognizedFieldNames);

      if(!unrecognizedFieldNames.isEmpty())
      {
         throw (new QUserFacingException("Query Filter contained " + unrecognizedFieldNames.size() + " unrecognized field name" + StringUtils.plural(unrecognizedFieldNames) + ": " + StringUtils.join(",", unrecognizedFieldNames)));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void validateFieldNamesInFilterInner(QueryOrCountInputInterface queryOrCountInputInterface, QQueryFilter filter, QTableMetaData mainTable, Map<String, QTableMetaData> joinTables, List<String> unrecognizedFieldNames)
   {
      for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         validateFieldNameFromFilter(criteria.getFieldName(), queryOrCountInputInterface, mainTable, joinTables, unrecognizedFieldNames);
         validateFieldNameFromFilter(criteria.getOtherFieldName(), queryOrCountInputInterface, mainTable, joinTables, unrecognizedFieldNames);
      }

      for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(filter.getOrderBys()))
      {
         validateFieldNameFromFilter(orderBy.getFieldName(), queryOrCountInputInterface, mainTable, joinTables, unrecognizedFieldNames);
      }

      for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
      {
         validateFieldNamesInFilterInner(queryOrCountInputInterface, subFilter, mainTable, joinTables, unrecognizedFieldNames);
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void validateFieldNameFromFilter(String fieldName, QueryOrCountInputInterface input, QTableMetaData mainTable, Map<String, QTableMetaData> joinTables, List<String> unrecognizedFieldNames)
   {
      if(StringUtils.hasContent(fieldName))
      {
         boolean found = false;
         try
         {
            FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(mainTable, fieldName, input.getQueryJoins());
            if(fieldAndJoinTable.joinTable().getName().equals(mainTable.getName()))
            {
               found = mainTable.getFields().containsKey(fieldAndJoinTable.field().getName());
            }
            else
            {
               QTableMetaData joinTable = joinTables.computeIfAbsent(fieldAndJoinTable.joinTable().getName(), joinTableName ->
               {
                  QTableMetaData table = fieldAndJoinTable.joinTable();
                  try
                  {
                     return TableMetaDataPersonalizerAction.execute(new TableMetaDataPersonalizerInput().withTableMetaData(table).withInputSource(input.getInputSource()));
                  }
                  catch(QException e)
                  {
                     return table;
                  }
               });

               found = joinTable.getFields().containsKey(fieldAndJoinTable.field().getName());
            }
         }
         catch(Exception e)
         {
            ///////////////////////
            // leave found false //
            ///////////////////////
         }

         if(!found)
         {
            unrecognizedFieldNames.add(fieldName);
         }
      }
   }

}
