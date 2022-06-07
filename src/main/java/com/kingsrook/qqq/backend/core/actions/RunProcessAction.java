/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   private StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      // return InMemoryStateProvider.getInstance();
      return TempFileStateProvider.getInstance();
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
    ** Load the process state into a function request from the state provider
    **
    *******************************************************************************/
   private void loadState(UUIDStateKey stateKey, RunFunctionRequest runFunctionRequest)
   {
      ProcessState processState = getStateProvider().get(ProcessState.class, stateKey);
      runFunctionRequest.seedFromProcessState(processState);
   }

}
