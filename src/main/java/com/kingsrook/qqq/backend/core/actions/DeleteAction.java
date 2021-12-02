package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.DeleteResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.QModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;


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
      QModuleDispatcher qModuleDispatcher = new QModuleDispatcher();

      QBackendMetaData backend = deleteRequest.getBackend();

      QModuleInterface qModule = qModuleDispatcher.getQModule(backend);
      // todo pre-customization - just get to modify the request?
      DeleteResult deleteResult = qModule.getDeleteInterface().execute(deleteRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return deleteResult;
   }
}
