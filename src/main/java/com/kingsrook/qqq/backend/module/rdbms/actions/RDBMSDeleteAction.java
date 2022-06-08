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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSDeleteAction extends AbstractRDBMSAction implements DeleteInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteResult execute(DeleteRequest deleteRequest) throws QException
   {
      try
      {
         DeleteResult rs = new DeleteResult();
         QTableMetaData table = deleteRequest.getTable();

         String tableName = table.getName();
         String primaryKeyName = getColumnName(table.getField(table.getPrimaryKeyField()));
         String sql = "DELETE FROM "
            + tableName
            + " WHERE "
            + primaryKeyName
            + " IN ("
            + deleteRequest.getPrimaryKeys().stream().map(x -> "?").collect(Collectors.joining(","))
            + ")";
         List<Serializable> params = deleteRequest.getPrimaryKeys();

         // todo sql customization - can edit sql and/or param list

         ConnectionManager connectionManager = new ConnectionManager();
         Connection connection = connectionManager.getConnection(new RDBMSBackendMetaData(deleteRequest.getBackend()));

         QueryManager.executeUpdateForRowCount(connection, sql, params);
         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);
         for(Serializable primaryKey : deleteRequest.getPrimaryKeys())
         {
            QRecord qRecord = new QRecord().withTableName(deleteRequest.getTableName()).withValue("id", primaryKey);
            // todo uh, identify any errors?
            QRecordWithStatus recordWithStatus = new QRecordWithStatus(qRecord);
            recordsWithStatus.add(recordWithStatus);
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing delete: " + e.getMessage(), e);
      }
   }

}
