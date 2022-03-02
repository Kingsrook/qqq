/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.mock;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.modules.interfaces.UpdateInterface;


/*******************************************************************************
 ** Mocked up version of update action.
 **
 *******************************************************************************/
public class MockUpdateAction implements UpdateInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateResult execute(UpdateRequest updateRequest) throws QException
   {
      try
      {
         UpdateResult rs = new UpdateResult();

         rs.setRecords(updateRequest.getRecords().stream().map(qRecord ->
         {
            return new QRecordWithStatus(qRecord);
         }).toList());

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }

}
