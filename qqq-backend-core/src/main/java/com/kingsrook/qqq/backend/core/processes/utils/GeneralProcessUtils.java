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
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/*******************************************************************************
 **
 *******************************************************************************/
public class GeneralProcessUtils
{

   /*******************************************************************************
    **
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
    **
    *******************************************************************************/
   public static ListingHash<Serializable, QRecord> getForeignRecordListingHashMap(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTablePrimaryKeyName) throws QException
   {
      ListingHash<Serializable, QRecord> foreignRecordMap = new ListingHash<>();
      QueryInput                         queryInput       = new QueryInput(parentActionInput.getInstance());
      queryInput.setSession(parentActionInput.getSession());
      queryInput.setTableName(foreignTableName);
      List<Serializable> foreignIds = new ArrayList<>(sourceRecords.stream().map(r -> r.getValue(sourceTableForeignKeyFieldName)).toList());

      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria(foreignTablePrimaryKeyName, QCriteriaOperator.IN, foreignIds)));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      for(QRecord foreignRecord : queryOutput.getRecords())
      {
         foreignRecordMap.add(foreignRecord.getValue(foreignTablePrimaryKeyName), foreignRecord);
      }
      return foreignRecordMap;
   }



   /*******************************************************************************
    **
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
    **
    *******************************************************************************/
   public static void addForeignRecordsListToRecordList(AbstractActionInput parentActionInput, List<QRecord> sourceRecords, String sourceTableForeignKeyFieldName, String foreignTableName, String foreignTablePrimaryKeyName) throws QException
   {
      ListingHash<Serializable, QRecord> foreignRecordMap = getForeignRecordListingHashMap(parentActionInput, sourceRecords, sourceTableForeignKeyFieldName, foreignTableName, foreignTablePrimaryKeyName);
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
    **
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
    **
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
    **
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
    ** Note - null values from the key field are NOT put in the map.
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
