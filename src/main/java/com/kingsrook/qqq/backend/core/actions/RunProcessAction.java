/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Action handler for running q-processes (which are a sequence of q-functions).
 *
 *******************************************************************************/
public class RunProcessAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessResult execute(RunProcessRequest runProcessRequest) throws QException
   {
      ActionHelper.validateSession(runProcessRequest);

      ///////////////////////////////////////////////////////
      // todo - shouldn't meta-data validation catch this? //
      ///////////////////////////////////////////////////////
      QProcessMetaData process = runProcessRequest.getInstance().getProcess(runProcessRequest.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runProcessRequest.getProcessName() + "] is not defined in this instance.");
      }

      RunProcessResult runProcessResult = new RunProcessResult();

      // todo - custom routing?
      List<QFunctionMetaData> functionList = process.getFunctionList();
      for(QFunctionMetaData function : functionList)
      {
         RunFunctionRequest runFunctionRequest = new RunFunctionRequest(runProcessRequest.getInstance());
         runFunctionRequest.setProcessName(process.getName());
         runFunctionRequest.setFunctionName(function.getName());
         // todo - how does this work again? runFunctionRequest.setCallback(?);
         RunFunctionResult functionResult = new RunFunctionAction().execute(runFunctionRequest);
         if(functionResult.getError() != null)
         {
            runProcessResult.setError(functionResult.getError());
            break;
         }
      }

      return (runProcessResult);
   }

}
