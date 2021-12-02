package com.kingsrook.qqq.backend.core.modules.mock;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.InsertResult;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;


/*******************************************************************************
 ** Mocked up version of insert action.
 **
 *******************************************************************************/
public class MockInsertAction implements InsertInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertResult execute(InsertRequest insertRequest) throws QException
   {
      try
      {
         InsertResult rs = new InsertResult();

         rs.setRecords(insertRequest.getRecords().stream().map(qRecord ->
         {
            return new QRecordWithStatus(qRecord);
         }).toList());

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }

}
