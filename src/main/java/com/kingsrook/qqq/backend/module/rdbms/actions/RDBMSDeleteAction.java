package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.DeleteResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.module.rdbms.RDBSMBackendMetaData;
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
         Connection connection = connectionManager.getConnection(new RDBSMBackendMetaData(deleteRequest.getBackend()));

         QueryManager.executeUpdateForRowCount(connection, sql, params);
         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);
         for(Serializable primaryKey : deleteRequest.getPrimaryKeys())
         {
            QRecord qRecord = new QRecord().withTableName(deleteRequest.getTableName()).withPrimaryKey(primaryKey);
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
