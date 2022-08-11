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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Storage provider for the MemoryBackendModule
 *******************************************************************************/
public class MemoryRecordStore
{
   private static MemoryRecordStore instance;

   private Map<String, Map<Serializable, QRecord>> data;
   private Map<String, Integer>                    nextSerials;



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
      Map<Serializable, QRecord> tableData = getTableData(input.getTable());
      List<QRecord>              records   = new ArrayList<>(tableData.values());
      // todo - filtering
      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer count(CountInput input)
   {
      Map<Serializable, QRecord> tableData = getTableData(input.getTable());
      List<QRecord>              records   = new ArrayList<>(tableData.values());
      // todo - filtering
      return (records.size());
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

      QTableMetaData             table      = input.getTable();
      Map<Serializable, QRecord> tableData  = getTableData(table);
      Integer                    nextSerial = nextSerials.get(table.getName());
      if(nextSerial == null)
      {
         nextSerial = 1;
         while(tableData.containsKey(nextSerial))
         {
            nextSerial++;
         }
      }

      List<QRecord>  outputRecords   = new ArrayList<>();
      QFieldMetaData primaryKeyField = table.getField(table.getPrimaryKeyField());
      for(QRecord record : input.getRecords())
      {
         if(record.getValue(primaryKeyField.getName()) == null && primaryKeyField.getType().equals(QFieldType.INTEGER))
         {
            record.setValue(primaryKeyField.getName(), nextSerial++);
         }

         tableData.put(record.getValue(primaryKeyField.getName()), record);
         if(returnInsertedRecords)
         {
            outputRecords.add(record);
         }
      }

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
         else
         {
            outputRecords.add(record);
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
}
