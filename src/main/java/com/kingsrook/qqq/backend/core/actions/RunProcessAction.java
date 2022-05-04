/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;
import com.kingsrook.qqq.backend.core.state.UUIDStateKey;


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

      UUIDStateKey stateKey = new UUIDStateKey();
      RunFunctionResult lastFunctionResult = null;

      // todo - custom routing?
      List<QFunctionMetaData> functionList = process.getFunctionList();
      for(QFunctionMetaData function : functionList)
      {
         RunFunctionRequest runFunctionRequest = new RunFunctionRequest(runProcessRequest.getInstance());

         if(lastFunctionResult != null)
         {
            loadState(stateKey, runFunctionRequest);
         }

         runFunctionRequest.setProcessName(process.getName());
         runFunctionRequest.setFunctionName(function.getName());
         runFunctionRequest.setSession(runProcessRequest.getSession());
         runFunctionRequest.setCallback(runProcessRequest.getCallback());
         lastFunctionResult = new RunFunctionAction().execute(runFunctionRequest);
         if(lastFunctionResult.getError() != null)
         {
            runProcessResult.setError(lastFunctionResult.getError());
            break;
         }

         storeState(stateKey, lastFunctionResult);
      }

      if(lastFunctionResult != null)
      {
         runProcessResult.seedFromLastFunctionResult(lastFunctionResult);
      }

      return (runProcessResult);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      // return InMemoryStateProvider.getInstance();
      return TempFileStateProvider.getInstance();
   }



   private void storeState(UUIDStateKey stateKey, RunFunctionResult runFunctionResult)
   {
      getStateProvider().put(stateKey, runFunctionResult.getProcessState());
   }



   private void loadState(UUIDStateKey stateKey, RunFunctionRequest runFunctionRequest)
   {
      ProcessState processState = getStateProvider().get(ProcessState.class, stateKey);
      runFunctionRequest.seedFromProcessState(processState);
   }

}
