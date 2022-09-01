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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.memory;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 ** Storage provider for the MemoryBackendModule
 *******************************************************************************/
public class MemoryRecordStore
{
   private static MemoryRecordStore instance;

   private Map<String, Map<Serializable, QRecord>> data;
   private Map<String, Integer>                    nextSerials;

   private static boolean collectStatistics = false;

   private static final Map<String, Integer> statistics = Collections.synchronizedMap(new HashMap<>());

   public static final String STAT_QUERIES_RAN = "queriesRan";



   /*******************************************************************************
    ** private singleton constructor
    *******************************************************************************/
   private MemoryRecordStore()
   {
      data = new HashMap<>();
      nextSerials = new HashMap<>();
   }



   /*******************************************************************************
    ** Forget all data in the memory store...
    *******************************************************************************/
   public void reset()
   {
      data.clear();
      nextSerials.clear();
   }



   /*******************************************************************************
    ** singleton accessor
    *******************************************************************************/
   public static MemoryRecordStore getInstance()
   {
      if(instance == null)
      {
         instance = new MemoryRecordStore();
      }
      return (instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<Serializable, QRecord> getTableData(QTableMetaData table)
   {
      if(!data.containsKey(table.getName()))
      {
         data.put(table.getName(), new HashMap<>());
      }
      return (data.get(table.getName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> query(QueryInput input)
   {
      incrementStatistic(STAT_QUERIES_RAN);

      Map<Serializable, QRecord> tableData = getTableData(input.getTable());
      List<QRecord>              records   = new ArrayList<>();

      for(QRecord qRecord : tableData.values())
      {
         boolean recordMatches = doesRecordMatch(input.getFilter(), qRecord);

         if(recordMatches)
         {
            records.add(qRecord);
         }
      }

      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean doesRecordMatch(QQueryFilter filter, QRecord qRecord)
   {
      boolean recordMatches = true;
      if(filter != null && filter.getCriteria() != null)
      {
         for(QFilterCriteria criterion : filter.getCriteria())
         {
            String       fieldName = criterion.getFieldName();
            Serializable value     = qRecord.getValue(fieldName);

            switch(criterion.getOperator())
            {
               case EQUALS:
               {
                  recordMatches = testEquals(criterion, value);
                  break;
               }
               case NOT_EQUALS:
               {
                  recordMatches = !testEquals(criterion, value);
                  break;
               }
               case IN:
               {
                  recordMatches = testIn(criterion, value);
                  break;
               }
               case NOT_IN:
               {
                  recordMatches = !testIn(criterion, value);
                  break;
               }
               case CONTAINS:
               {
                  recordMatches = testContains(criterion, fieldName, value);
                  break;
               }
               case NOT_CONTAINS:
               {
                  recordMatches = !testContains(criterion, fieldName, value);
                  break;
               }
               case GREATER_THAN:
               {
                  recordMatches = testGreaterThan(criterion, value);
                  break;
               }
               case GREATER_THAN_OR_EQUALS:
               {
                  recordMatches = testGreaterThan(criterion, value) || testEquals(criterion, value);
                  break;
               }
               case LESS_THAN:
               {
                  recordMatches = !testGreaterThan(criterion, value) && !testEquals(criterion, value);
                  break;
               }
               case LESS_THAN_OR_EQUALS:
               {
                  recordMatches = !testGreaterThan(criterion, value);
                  break;
               }
               default:
               {
                  throw new NotImplementedException("Operator [" + criterion.getOperator() + "] is not yet implemented in the Memory backend.");
               }
            }
            if(!recordMatches)
            {
               break;
            }
         }
      }
      return recordMatches;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean testGreaterThan(QFilterCriteria criterion, Serializable value)
   {
      Serializable criterionValue = criterion.getValues().get(0);
      if(criterionValue == null)
      {
         throw (new IllegalArgumentException("Missing criterion value in query"));
      }

      if (value == null)
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // a database would say 'false' for if a null column is > a value, so do the same. //
         /////////////////////////////////////////////////////////////////////////////////////
         return (false);
      }

      if(value instanceof LocalDate valueDate && criterionValue instanceof LocalDate criterionValueDate)
      {
         return (valueDate.isAfter(criterionValueDate));
      }

      if(value instanceof Number valueNumber && criterionValue instanceof Number criterionValueNumber)
      {
         return (valueNumber.doubleValue() > criterionValueNumber.doubleValue());
      }

      throw (new NotImplementedException("Greater/Less Than comparisons are not (yet?) implemented for the supplied types [" + value.getClass().getSimpleName() + "][" + criterionValue.getClass().getSimpleName() + "]"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean testIn(QFilterCriteria criterion, Serializable value)
   {
      if(!criterion.getValues().contains(value))
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean testEquals(QFilterCriteria criterion, Serializable value)
   {
      if(value == null)
      {
         return (false);
      }

      if(!value.equals(criterion.getValues().get(0)))
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean testContains(QFilterCriteria criterion, String fieldName, Serializable value)
   {
      String stringValue    = getStringFieldValue(value, fieldName, criterion);
      String criterionValue = getFirstStringCriterionValue(criterion);

      if(!stringValue.contains(criterionValue))
      {
         return (false);
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFirstStringCriterionValue(QFilterCriteria criteria)
   {
      if(CollectionUtils.nullSafeIsEmpty(criteria.getValues()))
      {
         throw new IllegalArgumentException("Missing value for [" + criteria.getOperator() + "] criteria on field [" + criteria.getFieldName() + "]");
      }
      Serializable value = criteria.getValues().get(0);
      if(value == null)
      {
         return "";
      }

      if(!(value instanceof String stringValue))
      {
         throw new ClassCastException("Value [" + value + "] for criteria [" + criteria.getFieldName() + "] is not a String, which is required for the [" + criteria.getOperator() + "] operator.");
      }

      return (stringValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getStringFieldValue(Serializable value, String fieldName, QFilterCriteria criterion)
   {
      if(value == null)
      {
         return "";
      }

      if(!(value instanceof String stringValue))
      {
         throw new ClassCastException("Value [" + value + "] in field [" + fieldName + "] is not a String, which is required for the [" + criterion.getOperator() + "] operator.");
      }

      return (stringValue);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer count(CountInput input)
   {
      QueryInput queryInput = new QueryInput(input.getInstance());
      queryInput.setSession(input.getSession());
      queryInput.setTableName(input.getTableName());
      queryInput.setFilter(input.getFilter());
      List<QRecord> queryResult = query(queryInput);

      return (queryResult.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> insert(InsertInput input, boolean returnInsertedRecords)
   {
      if(input.getRecords() == null)
      {
         return (new ArrayList<>());
      }

      QTableMetaData             table     = input.getTable();
      Map<Serializable, QRecord> tableData = getTableData(table);

      ////////////////////////////////////////
      // grab the next unique serial to use //
      ////////////////////////////////////////
      Integer nextSerial = nextSerials.get(table.getName());
      if(nextSerial == null)
      {
         nextSerial = 1;
      }

      while(tableData.containsKey(nextSerial))
      {
         nextSerial++;
      }

      List<QRecord>  outputRecords   = new ArrayList<>();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      for(QRecord record : input.getRecords())
      {
         /////////////////////////////////////////////////
         // set the next serial in the record if needed //
         /////////////////////////////////////////////////
         if(record.getValue(primaryKeyField.getName()) == null && primaryKeyField.getType().equals(QFieldType.INTEGER))
         {
            record.setValue(primaryKeyField.getName(), nextSerial++);
         }

         ///////////////////////////////////////////////////////////////////////////////////////////////////
         // make sure that if the user supplied a serial, greater than the one we had, that we skip ahead //
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         if(primaryKeyField.getType().equals(QFieldType.INTEGER) && record.getValueInteger(primaryKeyField.getName()) > nextSerial)
         {
            nextSerial = record.getValueInteger(primaryKeyField.getName()) + 1;
         }

         tableData.put(record.getValue(primaryKeyField.getName()), record);
         if(returnInsertedRecords)
         {
            outputRecords.add(record);
         }
      }

      nextSerials.put(table.getName(), nextSerial);

      return (outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> update(UpdateInput input, boolean returnUpdatedRecords)
   {
      if(input.getRecords() == null)
      {
         return (new ArrayList<>());
      }

      QTableMetaData             table     = input.getTable();
      Map<Serializable, QRecord> tableData = getTableData(table);

      List<QRecord>  outputRecords   = new ArrayList<>();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      for(QRecord record : input.getRecords())
      {
         Serializable primaryKeyValue = record.getValue(primaryKeyField.getName());
         if(tableData.containsKey(primaryKeyValue))
         {
            QRecord recordToUpdate = tableData.get(primaryKeyValue);
            for(Map.Entry<String, Serializable> valueEntry : record.getValues().entrySet())
            {
               recordToUpdate.setValue(valueEntry.getKey(), valueEntry.getValue());
            }

            if(returnUpdatedRecords)
            {
               outputRecords.add(record);
            }
         }
      }

      return (outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int delete(DeleteInput input)
   {
      if(input.getPrimaryKeys() == null)
      {
         return (0);
      }

      QTableMetaData             table       = input.getTable();
      Map<Serializable, QRecord> tableData   = getTableData(table);
      int                        rowsDeleted = 0;
      for(Serializable primaryKeyValue : input.getPrimaryKeys())
      {
         if(tableData.containsKey(primaryKeyValue))
         {
            tableData.remove(primaryKeyValue);
            rowsDeleted++;
         }
      }

      return (rowsDeleted);
   }



   /*******************************************************************************
    ** Setter for collectStatistics
    **
    *******************************************************************************/
   public static void setCollectStatistics(boolean collectStatistics)
   {
      MemoryRecordStore.collectStatistics = collectStatistics;
   }



   /*******************************************************************************
    ** Increment a statistic
    **
    *******************************************************************************/
   public static void incrementStatistic(String statName)
   {
      if(collectStatistics)
      {
         statistics.putIfAbsent(statName, 0);
         statistics.put(statName, statistics.get(statName) + 1);
      }
   }



   /*******************************************************************************
    ** clear the map of statistics
    **
    *******************************************************************************/
   public static void resetStatistics()
   {
      statistics.clear();
   }



   /*******************************************************************************
    ** Getter for statistics
    **
    *******************************************************************************/
   public static Map<String, Integer> getStatistics()
   {
      return statistics;
   }

}
