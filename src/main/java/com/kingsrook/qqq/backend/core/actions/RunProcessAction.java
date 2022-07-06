/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.backend.core.actions;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
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

      QProcessMetaData process = runProcessRequest.getInstance().getProcess(runProcessRequest.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runProcessRequest.getProcessName() + "] is not defined in this instance.");
      }

      RunProcessResult runProcessResult = new RunProcessResult();

      UUIDStateKey         stateKey           = new UUIDStateKey();
      RunBackendStepResult lastFunctionResult = null;

      // todo - custom routing?
      List<QStepMetaData> functionList = process.getStepList();
      for(QStepMetaData function : functionList)
      {
         RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(runProcessRequest.getInstance());

         if(lastFunctionResult == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            // for the first request, load state from the run process request to prime the run function request. //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            primeFunction(runProcessRequest, runBackendStepRequest);
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // for functions after the first one, load from state management to prime the request //
            ////////////////////////////////////////////////////////////////////////////////////////
            loadState(stateKey, runBackendStepRequest);
         }

         runBackendStepRequest.setProcessName(process.getName());
         runBackendStepRequest.setStepName(function.getName());
         runBackendStepRequest.setSession(runProcessRequest.getSession());
         runBackendStepRequest.setCallback(runProcessRequest.getCallback());
         lastFunctionResult = new RunBackendStepAction().execute(runBackendStepRequest);
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
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   private StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return InMemoryStateProvider.getInstance();

      // todo - by using JSON serialization internally, this makes stupidly large payloads and crashes things.
      // return TempFileStateProvider.getInstance();
   }



   /*******************************************************************************
    ** Store the process state from a function result to the state provider
    **
    *******************************************************************************/
   private void storeState(UUIDStateKey stateKey, RunBackendStepResult runBackendStepResult)
   {
      getStateProvider().put(stateKey, runBackendStepResult.getProcessState());
   }



   /*******************************************************************************
    ** Copy data (the state) down from the run-process request, down into the run-
    ** function request.
    *******************************************************************************/
   private void primeFunction(RunProcessRequest runProcessRequest, RunBackendStepRequest runBackendStepRequest)
   {
      runBackendStepRequest.seedFromRunProcessRequest(runProcessRequest);
   }



   /*******************************************************************************
    ** Load the process state into a function request from the state provider
    **
    *******************************************************************************/
   private void loadState(UUIDStateKey stateKey, RunBackendStepRequest runBackendStepRequest) throws QException
   {
      Optional<ProcessState> processState = getStateProvider().get(ProcessState.class, stateKey);
      runBackendStepRequest.seedFromProcessState(processState
         .orElseThrow(() -> new QException("Could not find process state in state provider.")));
   }

}
