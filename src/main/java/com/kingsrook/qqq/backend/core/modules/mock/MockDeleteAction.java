package com.kingsrook.qqq.backend.core.modules.mock;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.DeleteResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockDeleteAction implements DeleteInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteResult execute(DeleteRequest deleteRequest) throws QException
   {
      try
      {
         DeleteResult rs = new DeleteResult();

         rs.setRecords(deleteRequest.getPrimaryKeys().stream().map(primaryKey ->
         {
            QRecord qRecord = new QRecord().withTableName(deleteRequest.getTableName()).withPrimaryKey(primaryKey);
            return new QRecordWithStatus(qRecord);
         }).toList());

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing delete: " + e.getMessage(), e);
      }
   }

}
