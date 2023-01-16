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


import java.io.Serializable;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSInsertAction extends AbstractRDBMSAction implements InsertInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSInsertAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      InsertOutput rs = new InsertOutput();

      if(CollectionUtils.nullSafeIsEmpty(insertInput.getRecords()))
      {
         LOG.debug("Insert request called with 0 records.  Returning with no-op");
         rs.setRecords(new ArrayList<>());
         return (rs);
      }

      QTableMetaData table = insertInput.getTable();
      Instant        now   = Instant.now();

      for(QRecord record : insertInput.getRecords())
      {
         ///////////////////////////////////////////
         // todo .. better (not hard-coded names) //
         ///////////////////////////////////////////
         setValueIfTableHasField(record, table, "createDate", now);
         setValueIfTableHasField(record, table, "modifyDate", now);
      }

      try
      {
         List<QFieldMetaData> insertableFields = table.getFields().values().stream()
            .filter(field -> !field.getName().equals("id")) // todo - intent here is to avoid non-insertable fields.
            .toList();

         String columns = insertableFields.stream()
            .map(this::getColumnName)
            .collect(Collectors.joining(", "));
         String questionMarks = insertableFields.stream()
            .map(x -> "?")
            .collect(Collectors.joining(", "));

         List<QRecord> outputRecords = new ArrayList<>();
         rs.setRecords(outputRecords);

         Connection connection;
         boolean    needToCloseConnection = false;
         if(insertInput.getTransaction() != null && insertInput.getTransaction() instanceof RDBMSTransaction rdbmsTransaction)
         {
            LOG.debug("Using connection from insertInput [" + rdbmsTransaction.getConnection() + "]");
            connection = rdbmsTransaction.getConnection();
         }
         else
         {
            connection = getConnection(insertInput);
            needToCloseConnection = true;
         }

         try
         {
            for(List<QRecord> page : CollectionUtils.getPages(insertInput.getRecords(), QueryManager.PAGE_SIZE))
            {
               String        tableName   = escapeIdentifier(getTableName(table));
               StringBuilder sql         = new StringBuilder("INSERT INTO ").append(tableName).append("(").append(columns).append(") VALUES");
               List<Object>  params      = new ArrayList<>();
               int           recordIndex = 0;

               //////////////////////////////////////////////////////
               // for each record in the page:                     //
               // - if it has errors, skip it                      //
               // - else add a "(?,?,...,?)," clause to the INSERT //
               // - then add all fields into the params list       //
               //////////////////////////////////////////////////////
               for(QRecord record : page)
               {
                  if(CollectionUtils.nullSafeHasContents(record.getErrors()))
                  {
                     continue;
                  }

                  if(recordIndex++ > 0)
                  {
                     sql.append(",");
                  }
                  sql.append("(").append(questionMarks).append(")");

                  for(QFieldMetaData field : insertableFields)
                  {
                     Serializable value = record.getValue(field.getName());
                     value = scrubValue(field, value, true);
                     params.add(value);
                  }
               }

               ////////////////////////////////////////////////////////////////////////////////////////
               // if all records had errors, copy them to the output, and continue w/o running query //
               ////////////////////////////////////////////////////////////////////////////////////////
               if(recordIndex == 0)
               {
                  for(QRecord record : page)
                  {
                     QRecord outputRecord = new QRecord(record);
                     outputRecords.add(outputRecord);
                  }
                  continue;
               }

               ///////////////////////////////////////////////////////////
               // execute the insert, then foreach record in the input, //
               // add it to the output, and set its generated id too.   //
               ///////////////////////////////////////////////////////////
               // todo sql customization - can edit sql and/or param list
               // todo - non-serial-id style tables
               // todo - other generated values, e.g., createDate...  maybe need to re-select?
               List<Integer> idList = QueryManager.executeInsertForGeneratedIds(connection, sql.toString(), params);
               int           index  = 0;
               for(QRecord record : page)
               {
                  QRecord outputRecord = new QRecord(record);
                  outputRecords.add(outputRecord);

                  if(CollectionUtils.nullSafeIsEmpty(record.getErrors()))
                  {
                     Integer id = idList.get(index++);
                     outputRecord.setValue(table.getPrimaryKeyField(), id);
                  }
               }
            }
         }
         finally
         {
            if(needToCloseConnection)
            {
               connection.close();
            }
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }

}
