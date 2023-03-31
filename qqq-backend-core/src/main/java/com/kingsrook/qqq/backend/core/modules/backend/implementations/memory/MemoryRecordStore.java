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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ValidateRecordSecurityLockHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Storage provider for the MemoryBackendModule
 *******************************************************************************/
public class MemoryRecordStore
{
   private static final QLogger LOG = QLogger.getLogger(MemoryRecordStore.class);

   private static MemoryRecordStore instance;

   private Map<String, Map<Serializable, QRecord>> data;
   private Map<String, Integer>                    nextSerials;

   private static boolean collectStatistics = false;

   public static final String STAT_QUERIES_RAN = "queriesRan";
   public static final String STAT_INSERTS_RAN = "insertsRan";

   private static final Map<String, Integer> statistics = Collections.synchronizedMap(new HashMap<>());

   public static final ListingHash<Class<? extends AbstractActionInput>, AbstractActionInput> actionInputs = new ListingHash<>();



   /*******************************************************************************
    ** private singleton constructor
    *******************************************************************************/
   private MemoryRecordStore()
   {
      data = new HashMap<>();
      nextSerials = new HashMap<>();
   }



   /*******************************************************************************
    ** forget all data AND statistics
    *******************************************************************************/
   public static void fullReset()
   {
      getInstance().reset();
      resetStatistics();
      setCollectStatistics(false);
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
   public List<QRecord> query(QueryInput input) throws QException
   {
      incrementStatistic(input);

      Collection<QRecord> tableData = getTableData(input.getTable()).values();
      List<QRecord>       records   = new ArrayList<>();

      if(CollectionUtils.nullSafeHasContents(input.getQueryJoins()))
      {
         tableData = buildJoinCrossProduct(input);
      }

      for(QRecord qRecord : tableData)
      {
         boolean recordMatches = BackendQueryFilterUtils.doesRecordMatch(input.getFilter(), qRecord);

         if(recordMatches)
         {
            qRecord.setErrors(new ArrayList<>());
            ValidateRecordSecurityLockHelper.validateSecurityFields(input.getTable(), List.of(qRecord), ValidateRecordSecurityLockHelper.Action.SELECT);
            if(CollectionUtils.nullSafeHasContents(qRecord.getErrors()))
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////////
               // security error!  no record for you.  but remove the error, so future generations won't see it... //
               //////////////////////////////////////////////////////////////////////////////////////////////////////
               qRecord.setErrors(new ArrayList<>());
               LOG.trace("Error selecting record (presumably security?): " + qRecord.getErrors());
            }
            else
            {
               //////////////////////////////////////////////////////////////////////////////////
               // make sure we're not giving back records that are all full of associations... //
               //////////////////////////////////////////////////////////////////////////////////
               QRecord recordToReturn = new QRecord(qRecord);
               recordToReturn.setAssociatedRecords(new HashMap<>());
               records.add(recordToReturn);
            }
         }
      }

      BackendQueryFilterUtils.sortRecordList(input.getFilter(), records);
      records = BackendQueryFilterUtils.applySkipAndLimit(input, records);

      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Collection<QRecord> buildJoinCrossProduct(QueryInput input)
   {
      List<QRecord>  crossProduct = new ArrayList<>();
      QTableMetaData leftTable    = input.getTable();
      for(QRecord record : getTableData(leftTable).values())
      {
         QRecord productRecord = new QRecord();
         addRecordToProduct(productRecord, record, null);
         crossProduct.add(productRecord);
      }

      for(QueryJoin queryJoin : input.getQueryJoins())
      {
         QTableMetaData      nextTable        = QContext.getQInstance().getTable(queryJoin.getJoinTable());
         Collection<QRecord> nextTableRecords = getTableData(nextTable).values();

         List<QRecord> nextLevelProduct = new ArrayList<>();
         for(QRecord productRecord : crossProduct)
         {
            boolean matchFound = false;
            for(QRecord nextTableRecord : nextTableRecords)
            {
               if(joinMatches(productRecord, nextTableRecord, queryJoin))
               {
                  QRecord joinRecord = new QRecord(productRecord);
                  addRecordToProduct(joinRecord, nextTableRecord, queryJoin.getJoinTableOrItsAlias());
                  nextLevelProduct.add(joinRecord);
                  matchFound = true;
               }
            }

            if(!matchFound)
            {
               // todo - Left & Right joins
            }
         }

         crossProduct = nextLevelProduct;
      }

      return (crossProduct);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean joinMatches(QRecord productRecord, QRecord nextTableRecord, QueryJoin queryJoin)
   {
      for(JoinOn joinOn : queryJoin.getJoinMetaData().getJoinOns())
      {
         Serializable leftValue = productRecord.getValues().containsKey(queryJoin.getBaseTableOrAlias() + "." + joinOn.getLeftField())
            ? productRecord.getValue(queryJoin.getBaseTableOrAlias() + "." + joinOn.getLeftField())
            : productRecord.getValue(joinOn.getLeftField());
         Serializable rightValue = nextTableRecord.getValue(joinOn.getRightField());
         if(!Objects.equals(leftValue, rightValue))
         {
            return (false);
         }
      }

      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecordToProduct(QRecord productRecord, QRecord record, String tableNameOrAlias)
   {
      for(Map.Entry<String, Serializable> entry : record.getValues().entrySet())
      {
         productRecord.withValue(tableNameOrAlias == null ? entry.getKey() : tableNameOrAlias + "." + entry.getKey(), entry.getValue());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer count(CountInput input) throws QException
   {
      QueryInput queryInput = new QueryInput();
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
      incrementStatistic(input);

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
         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            outputRecords.add(record);
            continue;
         }

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
         Serializable primaryKeyValue = ValueUtils.getValueAsFieldType(primaryKeyField.getType(), record.getValue(primaryKeyField.getName()));

         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            outputRecords.add(record);
            continue;
         }

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

      QTableMetaData             table           = input.getTable();
      QFieldMetaData             primaryKeyField = table.getField(table.getPrimaryKeyField());
      Map<Serializable, QRecord> tableData       = getTableData(table);
      int                        rowsDeleted     = 0;
      for(Serializable primaryKeyValue : input.getPrimaryKeys())
      {
         primaryKeyValue = ValueUtils.getValueAsFieldType(primaryKeyField.getType(), primaryKeyValue);
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
   public static void incrementStatistic(AbstractActionInput input)
   {
      if(collectStatistics)
      {
         actionInputs.add(input.getClass(), input);
         if(input instanceof QueryInput)
         {
            incrementStatistic(STAT_QUERIES_RAN);
         }
         else if(input instanceof InsertInput)
         {
            incrementStatistic(STAT_INSERTS_RAN);
         }
      }
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
      actionInputs.clear();
   }



   /*******************************************************************************
    ** Getter for statistics
    **
    *******************************************************************************/
   public static Map<String, Integer> getStatistics()
   {
      return statistics;
   }



   /*******************************************************************************
    ** Getter for the actionInputs that were recorded - only while collectStatistics
    ** was true.
    *******************************************************************************/
   public static ListingHash<Class<? extends AbstractActionInput>, AbstractActionInput> getActionInputs()
   {
      return (actionInputs);
   }

}
