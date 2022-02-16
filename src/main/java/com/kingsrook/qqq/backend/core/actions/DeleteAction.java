/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


/*******************************************************************************
 ** Action to delete 1 or more records.
 **
 *******************************************************************************/
public class DeleteAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteResult execute(DeleteRequest deleteRequest) throws QException
   {
      ActionHelper.validateSession(deleteRequest);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface qModule = qBackendModuleDispatcher.getQModule(deleteRequest.getBackend());
      // todo pre-customization - just get to modify the request?
      DeleteResult deleteResult = qModule.getDeleteInterface().execute(deleteRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return deleteResult;
   }
}
