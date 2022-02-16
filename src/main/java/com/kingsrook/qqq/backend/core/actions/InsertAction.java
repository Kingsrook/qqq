/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


/*******************************************************************************
 ** Action to insert one or more records.
 **
 *******************************************************************************/
public class InsertAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertResult execute(InsertRequest insertRequest) throws QException
   {
      ActionHelper.validateSession(insertRequest);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface qModule = qBackendModuleDispatcher.getQModule(insertRequest.getBackend());
      // todo pre-customization - just get to modify the request?
      InsertResult insertResult = qModule.getInsertInterface().execute(insertRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return insertResult;
   }
}
