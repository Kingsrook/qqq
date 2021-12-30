/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.QModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;


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
      QModuleDispatcher qModuleDispatcher = new QModuleDispatcher();

      QBackendMetaData backend = insertRequest.getBackend();

      QModuleInterface qModule = qModuleDispatcher.getQModule(backend);
      // todo pre-customization - just get to modify the request?
      InsertResult insertResult = qModule.getInsertInterface().execute(insertRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return insertResult;
   }
}
