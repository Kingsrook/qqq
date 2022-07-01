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
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
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

      UUIDStateKey      stateKey           = new UUIDStateKey();
      RunFunctionResult lastFunctionResult = null;

      // todo - custom routing?
      List<QFunctionMetaData> functionList = process.getFunctionList();
      for(QFunctionMetaData function : functionList)
      {
         RunFunctionRequest runFunctionRequest = new RunFunctionRequest(runProcessRequest.getInstance());

         if(lastFunctionResult == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            // for the first request, load state from the run process request to prime the run function request. //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            primeFunction(runProcessRequest, runFunctionRequest);
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // for functions after the first one, load from state management to prime the request //
            ////////////////////////////////////////////////////////////////////////////////////////
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
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   private StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return InMemoryStateProvider.getInstance();

      // TODO - by using JSON serialization internally, this makes stupidly large payloads and crashes things.
      // return TempFileStateProvider.getInstance();
   }



   /*******************************************************************************
    ** Store the process state from a function result to the state provider
    **
    *******************************************************************************/
   private void storeState(UUIDStateKey stateKey, RunFunctionResult runFunctionResult)
   {
      getStateProvider().put(stateKey, runFunctionResult.getProcessState());
   }



   /*******************************************************************************
    ** Copy data (the state) down from the run-process request, down into the run-
    ** function request.
    *******************************************************************************/
   private void primeFunction(RunProcessRequest runProcessRequest, RunFunctionRequest runFunctionRequest)
   {
      runFunctionRequest.seedFromRunProcessRequest(runProcessRequest);
   }



   /*******************************************************************************
    ** Load the process state into a function request from the state provider
    **
    *******************************************************************************/
   private void loadState(UUIDStateKey stateKey, RunFunctionRequest runFunctionRequest) throws QException
   {
      Optional<ProcessState> processState = getStateProvider().get(ProcessState.class, stateKey);
      runFunctionRequest.seedFromProcessState(processState
         .orElseThrow(() -> new QException("Could not find process state in state provider.")));
   }

}
