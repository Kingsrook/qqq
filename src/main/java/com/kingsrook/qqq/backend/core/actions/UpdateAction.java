/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


/*******************************************************************************
 ** Action to update one or more records.
 **
 *******************************************************************************/
public class UpdateAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateResult execute(UpdateRequest updateRequest) throws QException
   {
      ActionHelper.validateSession(updateRequest);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface qModule = qBackendModuleDispatcher.getQModule(updateRequest.getBackend());
      // todo pre-customization - just get to modify the request?
      UpdateResult updateResult = qModule.getUpdateInterface().execute(updateRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return updateResult;
   }
}
