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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSInsertAction extends AbstractRDBMSAction implements InsertInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertResult execute(InsertRequest insertRequest) throws QException
   {
      try
      {
         InsertResult rs = new InsertResult();
         QTableMetaData table = insertRequest.getTable();

         List<QFieldMetaData> insertableFields = table.getFields().values().stream()
            .filter(field -> !field.getName().equals("id")) // todo - intent here is to avoid non-insertable fields.
            .toList();

         String columns = insertableFields.stream()
            .map(this::getColumnName)
            .collect(Collectors.joining(", "));
         String questionMarks = insertableFields.stream()
            .map(x -> "?")
            .collect(Collectors.joining(", "));

         String tableName = getTableName(table);
         StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(").append(columns).append(") VALUES");
         List<Object> params = new ArrayList<>();

         int recordIndex = 0;
         for(QRecord record : insertRequest.getRecords())
         {
            if(recordIndex++ > 0)
            {
               sql.append(",");
            }
            sql.append("(").append(questionMarks).append(")");
            for(QFieldMetaData field : insertableFields)
            {
               params.add(record.getValue(field.getName()));
            }
         }

         // todo sql customization - can edit sql and/or param list

         ConnectionManager connectionManager = new ConnectionManager();
         Connection        connection        = connectionManager.getConnection((RDBMSBackendMetaData) insertRequest.getBackend());

         // QueryResult rs = new QueryResult();
         // List<QRecord> records = new ArrayList<>();
         // rs.setRecords(records);

         // todo - non-serial-id style tables
         // todo - other generated values, e.g., createDate...  maybe need to re-select?
         List<Integer> idList = QueryManager.executeInsertForGeneratedIds(connection, sql.toString(), params);
         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);
         int index = 0;
         for(QRecord record : insertRequest.getRecords())
         {
            Integer id = idList.get(index++);
            QRecordWithStatus recordWithStatus = new QRecordWithStatus(record);
            recordWithStatus.setValue(table.getPrimaryKeyField(), id);
            recordsWithStatus.add(recordWithStatus);
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }

}
