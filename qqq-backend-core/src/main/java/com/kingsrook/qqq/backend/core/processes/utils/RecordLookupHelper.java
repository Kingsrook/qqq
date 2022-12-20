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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Utility to help processes lookup records.  Caches lookups - and potentially
 ** can pre-load entire tables or subsets of tables.
 **
 *******************************************************************************/
public class RecordLookupHelper
{
   private final AbstractActionInput actionInput;

   private Map<String, Map<Serializable, QRecord>> recordMaps    = new HashMap<>();
   private Set<String>                             preloadedKeys = new HashSet<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RecordLookupHelper(AbstractActionInput actionInput)
   {
      this.actionInput = actionInput;
   }



   /*******************************************************************************
    ** Fetch a record from a table by a key field (doesn't have to be its primary key).
    *******************************************************************************/
   public QRecord getRecordByKey(String tableName, String keyFieldName, Serializable key) throws QException
   {
      String                     mapKey    = tableName + "." + keyFieldName;
      Map<Serializable, QRecord> recordMap = recordMaps.computeIfAbsent(mapKey, (k) -> new HashMap<>());

      if(!recordMap.containsKey(key))
      {
         Optional<QRecord> optRecord = GeneralProcessUtils.getRecordByField(actionInput, tableName, keyFieldName, key);
         recordMap.put(key, optRecord.orElse(null));
      }

      return (recordMap.get(key));
   }



   /*******************************************************************************
    ** Optimization - to pre-load the records in a single query, which would otherwise
    ** have to be looked up one-by-one.
    **
    ** Note that when this method is called for a given pair of params (table/field),
    ** a flag is set to avoid ever re-loading this pair (e.g., subsequent calls to this
    ** method w/ a given input pair does a noop).
    *******************************************************************************/
   public void preloadRecords(String tableName, String keyFieldName) throws QException
   {
      String mapKey = tableName + "." + keyFieldName;
      if(!preloadedKeys.contains(mapKey))
      {
         Map<Serializable, QRecord> recordMap = GeneralProcessUtils.loadTableToMap(actionInput, tableName, keyFieldName);
         recordMaps.put(mapKey, recordMap);
         preloadedKeys.add(mapKey);
      }
   }



   /*******************************************************************************
    ** Optimization - to pre-load some records in a single query, which would otherwise
    ** have to be looked up one-by-one.
    **
    ** Note that this method is different from the overload that doesn't take a filter,
    ** in that it doesn't set any flags to avoid re-running (the idea being, you'd pass
    ** a unique filter in each time, so you'd always want it to re-run).
    *******************************************************************************/
   public void preloadRecords(String tableName, String keyFieldName, QQueryFilter filter) throws QException
   {
      String                     mapKey   = tableName + "." + keyFieldName;
      Map<Serializable, QRecord> tableMap = recordMaps.computeIfAbsent(mapKey, s -> new HashMap<>());
      tableMap.putAll(GeneralProcessUtils.loadTableToMap(actionInput, tableName, keyFieldName, filter));
   }



   /*******************************************************************************
    ** Get a value from a record, by doing a lookup on the specified keyFieldName,
    ** for the specified key value.
    **
    *******************************************************************************/
   public Serializable getRecordValue(String tableName, String requestedField, String keyFieldName, Serializable key) throws QException
   {
      QRecord record = getRecordByKey(tableName, keyFieldName, key);
      if(record == null)
      {
         return (null);
      }

      return (record.getValue(requestedField));
   }



   /*******************************************************************************
    ** Get a value from a record, in the requested type, by doing a lookup on the
    ** specified keyFieldName, for the specified key value.
    **
    *******************************************************************************/
   public <T extends Serializable> T getRecordValue(String tableName, String requestedField, String keyFieldName, Serializable key, Class<T> type) throws QException
   {
      Serializable value = getRecordValue(tableName, requestedField, keyFieldName, key);
      return (ValueUtils.getValueAsType(type, value));
   }



   /*******************************************************************************
    ** Get the id (primary key) value from a record, by doing a lookup on the
    ** specified keyFieldName, for the specified key value.
    **
    *******************************************************************************/
   public Serializable getRecordId(String tableName, String keyFieldName, Serializable key) throws QException
   {
      String primaryKeyField = actionInput.getInstance().getTable(tableName).getPrimaryKeyField();
      return (getRecordValue(tableName, primaryKeyField, keyFieldName, key));
   }



   /*******************************************************************************
    ** Get the id (primary key) value from a record, in the requested type, by doing
    ** a lookup on the specified keyFieldName, for the specified key value.
    **
    *******************************************************************************/
   public <T extends Serializable> T getRecordId(String tableName, String keyFieldName, Serializable key, Class<T> type) throws QException
   {
      Serializable value = getRecordId(tableName, keyFieldName, key);
      return (ValueUtils.getValueAsType(type, value));
   }

}
