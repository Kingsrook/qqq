/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Methods to help with unique key checks.
 *******************************************************************************/
public class UniqueKeyHelper
{
   private static Integer pageSize = 1000;

   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<List<Serializable>, Serializable> getExistingKeys(QBackendTransaction transaction, QTableMetaData table, List<QRecord> recordList, UniqueKey uniqueKey, boolean allowNullKeyValuesToEqual) throws QException
   {
      List<String>                          ukFieldNames    = uniqueKey.getFieldNames();
      Map<List<Serializable>, Serializable> existingRecords = new HashMap<>();
      if(ukFieldNames != null)
      {
         for(List<QRecord> page : CollectionUtils.getPages(recordList, pageSize))
         {
            QueryInput queryInput = new QueryInput();
            queryInput.setTableName(table.getName());
            queryInput.setTransaction(transaction);

            QQueryFilter filter = new QQueryFilter();
            if(ukFieldNames.size() == 1)
            {
               List<Serializable> values = page.stream()
                  .filter(r -> CollectionUtils.nullSafeIsEmpty(r.getErrors()))
                  .map(r -> r.getValue(ukFieldNames.get(0)))
                  .collect(Collectors.toList());

               if(values.isEmpty())
               {
                  continue;
               }

               filter.addCriteria(new QFilterCriteria(ukFieldNames.get(0), QCriteriaOperator.IN, values));
            }
            else
            {
               filter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
               for(QRecord record : page)
               {
                  if(CollectionUtils.nullSafeHasContents(record.getErrors()))
                  {
                     continue;
                  }

                  QQueryFilter subFilter = new QQueryFilter();
                  filter.addSubFilter(subFilter);
                  for(String fieldName : ukFieldNames)
                  {
                     Serializable value = record.getValue(fieldName);
                     if(value == null)
                     {
                        subFilter.addCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.IS_BLANK));
                     }
                     else
                     {
                        subFilter.addCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.EQUALS, value));
                     }
                  }
               }

               if(CollectionUtils.nullSafeIsEmpty(filter.getSubFilters()))
               {
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // if we didn't build any sub-filters (because all records have errors in them), don't run a query w/ no clauses - continue to next page //
                  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  continue;
               }
            }

            queryInput.setFilter(filter);

            /////////////////////////////////////////////////////////////////////////////////////////
            // in case the table we're working with is a CacheOf - tell the QueryActionCacheHelper //
            // not to do queries on the table behind the cache table (e.g., for cache misses or    //
            // expired records).  we just want to look at the cache table itself.                  //
            /////////////////////////////////////////////////////////////////////////////////////////
            queryInput.withFlag(QueryActionCacheHelper.CacheActionFlags.DO_NOT_QUERY_SOURCE_TABLE);

            QueryOutput queryOutput = new QueryAction().execute(queryInput);
            for(QRecord record : queryOutput.getRecords())
            {
               Optional<List<Serializable>> keyValues = getKeyValues(table, uniqueKey, record, allowNullKeyValuesToEqual);
               if(keyValues.isPresent())
               {
                  existingRecords.put(keyValues.get(), record.getValue(table.getPrimaryKeyField()));
               }
            }
         }
      }

      return (existingRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<List<Serializable>, Serializable> getExistingKeys(QBackendTransaction transaction, QTableMetaData table, List<QRecord> recordList, UniqueKey uniqueKey) throws QException
   {
      return (getExistingKeys(transaction, table, recordList, uniqueKey, false));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<List<Serializable>> getKeyValues(QTableMetaData table, UniqueKey uniqueKey, QRecord record, boolean allowNullKeyValuesToEqual)
   {
      try
      {
         List<Serializable> keyValues = new ArrayList<>();
         for(String fieldName : uniqueKey.getFieldNames())
         {
            QFieldMetaData field      = table.getField(fieldName);
            Serializable   value      = record.getValue(fieldName);
            Serializable   typedValue = ValueUtils.getValueAsFieldType(field.getType(), value);

            ///////////////////////////////////////////////////////////////////////////////////
            // if null value, look at flag to determine if a null should be used (which will //
            // allow keys to match), or a NullUniqueKeyValue, (which will never match)       //
            ///////////////////////////////////////////////////////////////////////////////////
            if(typedValue == null)
            {
               keyValues.add(allowNullKeyValuesToEqual ? null : new NullUniqueKeyValue());
            }
            else
            {
               keyValues.add(typedValue);
            }
         }
         return (Optional.of(keyValues));
      }
      catch(Exception e)
      {
         return (Optional.empty());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<List<Serializable>> getKeyValues(QTableMetaData table, UniqueKey uniqueKey, QRecord record)
   {
      return (getKeyValues(table, uniqueKey, record, false));
   }



   /*******************************************************************************
    ** To make a list of unique key values here behave like they do in an RDBMS
    ** (which is what we're trying to mimic - which is - 2 null values in a field
    ** aren't considered the same, so they don't violate a unique key) (at least, that's
    ** how some RDBMS's work, right??) - use this value instead of nulls in the
    ** output of getKeyValues - where interestingly, this class always returns
    ** false in it equals method... Unclear how bad this is, e.g., if it's violating
    ** the contract for equals and hashCode...
    *******************************************************************************/
   public static class NullUniqueKeyValue implements Serializable
   {
      @Override
      public boolean equals(Object obj)
      {
         return (false);
      }
   }



   /*******************************************************************************
    ** Getter for pageSize
    **
    *******************************************************************************/
   public static Integer getPageSize()
   {
      return pageSize;
   }



   /*******************************************************************************
    ** Setter for pageSize
    **
    *******************************************************************************/
   public static void setPageSize(Integer pageSize)
   {
      UniqueKeyHelper.pageSize = pageSize;
   }

}
