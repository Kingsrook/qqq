/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSUpdateAction extends AbstractRDBMSAction implements UpdateInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateResult execute(UpdateRequest updateRequest) throws QException
   {
      try
      {
         UpdateResult rs = new UpdateResult();
         QTableMetaData table = updateRequest.getTable();

         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);

         // todo - sql batch for performance
         // todo - if setting a bunch of records to have the same value, a single update where id IN?
         int recordIndex = 0;
         for(QRecord record : updateRequest.getRecords())
         {
            List<QFieldMetaData> updateableFields = table.getFields().values().stream()
               .filter(field -> !field.getName().equals("id")) // todo - intent here is to avoid non-updateable fields.
               .filter(field -> record.getValues().containsKey(field.getName()))
               .toList();

            String columns = updateableFields.stream()
               .map(f -> this.getColumnName(f) + " = ?")
               .collect(Collectors.joining(", "));

            String tableName = table.getName();
            StringBuilder sql = new StringBuilder("UPDATE ").append(tableName)
               .append(" SET ").append(columns)
               .append(" WHERE ").append(getColumnName(table.getField(table.getPrimaryKeyField()))).append(" = ?");

            // todo sql customization - can edit sql and/or param list

            ConnectionManager connectionManager = new ConnectionManager();
            Connection connection = connectionManager.getConnection(new RDBMSBackendMetaData(updateRequest.getBackend()));

            QRecordWithStatus recordWithStatus = new QRecordWithStatus(record);
            recordsWithStatus.add(recordWithStatus);

            try
            {
               List<Object> params = new ArrayList<>();
               for(QFieldMetaData field : updateableFields)
               {
                  params.add(record.getValue(field.getName()));
               }
               params.add(record.getValue(table.getPrimaryKeyField()));
               QueryManager.executeUpdate(connection, sql.toString(), params);
               // todo - auto-updated values, e.g., modifyDate...  maybe need to re-select?
            }
            catch(Exception e)
            {
               recordWithStatus.setErrors(new ArrayList<>(List.of(e)));
            }
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }

}
