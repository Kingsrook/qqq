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

package com.kingsrook.qqq.backend.core.processes.utils;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/*******************************************************************************
 ** Utility methods for working with QQQ records and table actions inside user -
 ** defined QQQ processes steps.
 *******************************************************************************/
public class GeneralProcessUtils
{

   /*******************************************************************************
    ** For a list of sourceRecords,
    ** lookup records in the foreignTableName,
    ** that have their foreignTablePrimaryKeyName in the sourceTableForeignKeyFieldName on the sourceRecords.
    **
    ** e.g., for a list of orders (with a clientId field), build a map of client.id => client record
    ** via getForeignRecordMap(input, orderList, "clientId", "client", "id")
    *******************************************************************************/
   public static Map<Serializable, QRecord> getForeignRecordMap(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTablePrimaryKeyName) throws QException
   {
      Map<Serializable, QRecord> foreignRecordMap = new HashMap<>();
      QueryInput                 queryInput       = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(foreignTableName);
      List<Serializable> foreignIds = new ArrayList<>(sourceRecords.stream().map(r -> r.getValue(sourceTableForeignKeyFieldName)).toList());

      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(foreignTablePrimaryKeyName, QCriteriaOperator.IN, foreignIds)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      for(QRecord foreignRecord : queryOutput.getRecords())
      {
         foreignRecordMap.put(foreignRecord.getValue(foreignTablePrimaryKeyName), foreignRecord);
      }
      return foreignRecordMap;
   }



   /*******************************************************************************
    ** For a list of sourceRecords,
    ** lookup records in the foreignTableName,
    ** that have their foreignTableForeignKeyName in the sourceTableForeignKeyFieldName on the sourceRecords.
    **
    ** e.g., for a list of orders, build a ListingHash of order.id => List(OrderLine records)
    ** via getForeignRecordListingHashMap(input, orderList, "id", "orderLine", "orderId")
    *******************************************************************************/
   public static ListingHash<Serializable, QRecord> getForeignRecordListingHashMap(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTableForeignKeyName) throws QException
   {
      ListingHash<Serializable, QRecord> foreignRecordMap = new ListingHash<>();
      QueryInput                         queryInput       = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(foreignTableName);
      List<Serializable> foreignIds = new ArrayList<>(sourceRecords.stream().map(r -> r.getValue(sourceTableForeignKeyFieldName)).toList());

      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(foreignTableForeignKeyName, QCriteriaOperator.IN, foreignIds)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      for(QRecord foreignRecord : queryOutput.getRecords())
      {
         foreignRecordMap.add(foreignRecord.getValue(foreignTableForeignKeyName), foreignRecord);
      }
      return foreignRecordMap;
   }



   /*******************************************************************************
    ** For a list of sourceRecords,
    ** lookup records in the foreignTableName,
    ** that have their foreignTablePrimaryKeyName in the sourceTableForeignKeyFieldName on the sourceRecords.
    ** and set those foreign records as a value in the sourceRecords.
    **
    ** e.g., for a list of orders (with a clientId field), setValue("client", QRecord(client));
    ** via addForeignRecordsToRecordList(input, orderList, "clientId", "client", "id")
    *******************************************************************************/
   public static void addForeignRecordsToRecordList(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTablePrimaryKeyName) throws QException
   {
      Map<Serializable, QRecord> foreignRecordMap = getForeignRecordMap(parentActionInput, sourceRecords, sourceTableForeignKeyFieldName, foreignTableName, foreignTablePrimaryKeyName);
      for(QRecord sourceRecord : sourceRecords)
      {
         QRecord foreignRecord = foreignRecordMap.get(sourceRecord.getValue(sourceTableForeignKeyFieldName));
         sourceRecord.setValue(foreignTableName, foreignRecord);
      }
   }



   /*******************************************************************************
    ** For a list of sourceRecords,
    ** lookup records in the foreignTableName,
    ** that have their foreignTableForeignKeyName in the sourceTableForeignKeyFieldName on the sourceRecords.
    **
    ** e.g., for a list of orders, setValue("orderLine", List(QRecord(orderLine)))
    ** via addForeignRecordsListToRecordList(input, orderList, "id", "orderLine", "orderId")
    *******************************************************************************/
   public static void addForeignRecordsListToRecordList(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTableForeignKeyName) throws QException
   {
      ListingHash<Serializable, QRecord> foreignRecordMap = getForeignRecordListingHashMap(parentActionInput, sourceRecords, sourceTableForeignKeyFieldName, foreignTableName, foreignTableForeignKeyName);
      for(QRecord sourceRecord : sourceRecords)
      {
         List<QRecord> foreignRecordList = foreignRecordMap.get(sourceRecord.getValue(sourceTableForeignKeyFieldName));
         if(foreignRecordList != null)
         {
            if(foreignRecordList instanceof Serializable s)
            {
               sourceRecord.setValue(foreignTableName, s);
            }
            else
            {
               sourceRecord.setValue(foreignTableName, new ArrayList<>(foreignRecordList));
            }
         }
      }
   }



   /*******************************************************************************
    ** Run a query on tableName, for where fieldName equals fieldValue, and return
    ** the list of QRecords.
    *******************************************************************************/
   public static List<QRecord> getRecordListByField(AbstractActionInput parentActionInput, String tableName, String fieldName, Serializable fieldValue) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.EQUALS, List.of(fieldValue))));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords());
   }



   /*******************************************************************************
    ** Query to get one record by a unique key value.  That field can be the primary
    ** key, or any other field on the table.  Note, if multiple rows do match the value,
    ** only 1 (determined in an unspecified way) is returned.
    *******************************************************************************/
   public static Optional<QRecord> getRecordById(AbstractActionInput parentActionInput, String tableName, String fieldName, Serializable fieldValue) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.EQUALS, List.of(fieldValue))));
      queryInput.setLimit(1);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords().stream().findFirst());
   }



   /*******************************************************************************
    ** Load all rows from a table.
    **
    ** Note, this is inherently unsafe, if you were to call it on a table with
    ** too many rows...  Caveat emptor.
    *******************************************************************************/
   public static List<QRecord> loadTable(AbstractActionInput parentActionInput, String tableName) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords());
   }



   /*******************************************************************************
    ** Load all rows from a table as a RecordEntity.
    **
    ** Note, this is inherently unsafe, if you were to call it on a table with
    ** too many rows...  Caveat emptor.
    *******************************************************************************/
   public static <T extends QRecordEntity> List<T> loadTable(AbstractActionInput parentActionInput, String tableName, Class<T> entityClass) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      List<T> rs = new ArrayList<>();
      for(QRecord record : queryOutput.getRecords())
      {
         rs.add(QRecordEntity.fromQRecord(entityClass, record));
      }

      return (rs);
   }



   /*******************************************************************************
    ** Load all rows from a table, into a map, keyed by the keyFieldName.
    **
    ** Note - null values from the key field are NOT put in the map.
    **
    ** If multiple values are found for the key, they'll squash each other, and only
    ** one random value will appear.
    **
    ** Also, note, this is inherently unsafe, if you were to call it on a table with
    ** too many rows...  Caveat emptor.
    *******************************************************************************/
   public static Map<Serializable, QRecord> loadTableToMap(AbstractActionInput parentActionInput, String tableName, String keyFieldName) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      QueryOutput   queryOutput = new QueryAction().execute(queryInput);
      List<QRecord> records     = queryOutput.getRecords();

      Map<Serializable, QRecord> map = new HashMap<>();
      for(QRecord record : records)
      {
         Serializable value = record.getValue(keyFieldName);
         if(value != null)
         {
            map.put(value, record);
         }
      }
      return (map);
   }



   /*******************************************************************************
    ** Note - null values from the key field are NOT put in the map.
    *******************************************************************************/
   public static <T extends QRecordEntity> Map<Serializable, T> loadTableToMap(AbstractActionInput parentActionInput, String tableName, String keyFieldName, Class<T> entityClass) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      QueryOutput   queryOutput = new QueryAction().execute(queryInput);
      List<QRecord> records     = queryOutput.getRecords();

      Map<Serializable, T> map = new HashMap<>();
      for(QRecord record : records)
      {
         Serializable value = record.getValue(keyFieldName);
         if(value != null)
         {
            map.put(value, QRecordEntity.fromQRecord(entityClass, record));
         }
      }
      return (map);
   }



   /*******************************************************************************
    ** Load all rows from a table, into a ListingHash, keyed by the keyFieldName.
    **
    ** Note - null values from the key field are NOT put in the map.
    **
    ** The ordering of the records is not specified.
    **
    ** Also, note, this is inherently unsafe, if you were to call it on a table with
    ** too many rows...  Caveat emptor.
    *******************************************************************************/
   public static ListingHash<Serializable, QRecord> loadTableToListingHash(AbstractActionInput parentActionInput, String tableName, String keyFieldName) throws QException
   {
      QueryInput queryInput = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(tableName);
      QueryOutput   queryOutput = new QueryAction().execute(queryInput);
      List<QRecord> records     = queryOutput.getRecords();

      ListingHash<Serializable, QRecord> map = new ListingHash<>();
      for(QRecord record : records)
      {
         Serializable value = record.getValue(keyFieldName);
         if(value != null)
         {
            map.add(value, record);
         }
      }
      return (map);
   }

}