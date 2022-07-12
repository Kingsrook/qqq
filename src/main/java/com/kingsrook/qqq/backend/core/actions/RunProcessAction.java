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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Action handler for running q-processes (which are a sequence of q-functions).
 *
 *******************************************************************************/
public class RunProcessAction
{
   private static final Logger LOG = LogManager.getLogger(RunProcessAction.class);



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

      //////////////////////////////////////////////////////////
      // generate a UUID for the process, if one wasn't given //
      //////////////////////////////////////////////////////////
      if(runProcessRequest.getProcessUUID() == null)
      {
         runProcessRequest.setProcessUUID(UUID.randomUUID().toString());
      }
      runProcessResult.setProcessUUID(runProcessRequest.getProcessUUID());

      UUIDAndTypeStateKey stateKey     = new UUIDAndTypeStateKey(UUID.fromString(runProcessRequest.getProcessUUID()), StateType.PROCESS_STATUS);
      ProcessState        processState = primeProcessState(runProcessRequest, stateKey);

      // todo - custom routing
      List<QStepMetaData> stepList = getAvailableStepList(process, runProcessRequest);
      try
      {
         STEP_LOOP:
         for(QStepMetaData step : stepList)
         {
            if(step instanceof QFrontendStepMetaData)
            {
               ////////////////////////////////////////////////////////////////
               // Handle what to do with frontend steps, per request setting //
               ////////////////////////////////////////////////////////////////
               switch(runProcessRequest.getFrontendStepBehavior())
               {
                  case BREAK ->
                  {
                     LOG.info("Breaking process [" + process.getName() + "] at frontend step (as requested by caller): " + step.getName());
                     processState.setNextStepName(step.getName());
                     break STEP_LOOP;
                  }
                  case SKIP ->
                  {
                     LOG.info("Skipping frontend step [" + step.getName() + "] in process [" + process.getName() + "] (as requested by caller)");

                     //////////////////////////////////////////////////////////////////////
                     // much less error prone in case this code changes in the future... //
                     //////////////////////////////////////////////////////////////////////
                     // noinspection UnnecessaryContinue
                     continue;
                  }
                  case FAIL ->
                  {
                     LOG.info("Throwing error for frontend step [" + step.getName() + "] in process [" + process.getName() + "] (as requested by caller)");
                     throw (new QException("Failing process at step " + step.getName() + " (as requested, to fail on frontend steps)"));
                  }
                  default -> throw new IllegalStateException("Unexpected value: " + runProcessRequest.getFrontendStepBehavior());
               }
            }
            else if(step instanceof QBackendStepMetaData backendStepMetaData)
            {
               ///////////////////////
               // Run backend steps //
               ///////////////////////
               runBackendStep(runProcessRequest, process, runProcessResult, stateKey, backendStepMetaData, processState);
            }
            else
            {
               //////////////////////////////////////////////////
               // in case we have a different step type, throw //
               //////////////////////////////////////////////////
               throw (new QException("Unsure how to run a step of type: " + step.getClass().getName()));
            }
         }
      }
      catch(QException qe)
      {
         ////////////////////////////////////////////////////////////
         // upon exception (e.g., one thrown by a step), throw it. //
         ////////////////////////////////////////////////////////////
         throw (qe);
      }
      catch(Exception e)
      {
         ////////////////////////////////////////////////////////////
         // upon exception (e.g., one thrown by a step), throw it. //
         ////////////////////////////////////////////////////////////
         throw (new QException("Error running process", e));
      }
      finally
      {
         //////////////////////////////////////////////////////
         // always put the final state in the process result //
         //////////////////////////////////////////////////////
         runProcessResult.setProcessState(processState);
      }

      return (runProcessResult);
   }



   /*******************************************************************************
    ** When we start running a process (or resuming it), get data in the RunProcessRequest
    ** either from the state provider (if they're found, for a resume).
    *******************************************************************************/
   ProcessState primeProcessState(RunProcessRequest runProcessRequest, UUIDAndTypeStateKey stateKey) throws QException
   {
      Optional<ProcessState> optionalProcessState = loadState(stateKey);
      if(optionalProcessState.isEmpty())
      {
         if(runProcessRequest.getStartAfterStep() == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////
            // this is fine - it means its our first time running in the backend.            //
            // Go ahead and store the state that we have (e.g., w/ initial records & values) //
            ///////////////////////////////////////////////////////////////////////////////////
            storeState(stateKey, runProcessRequest.getProcessState());
            optionalProcessState = Optional.of(runProcessRequest.getProcessState());
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // if this isn't the first step, but there's no state, then that's a problem, so fail //
            ////////////////////////////////////////////////////////////////////////////////////////
            throw (new QException("Could not find state for process [" + runProcessRequest.getProcessName() + "] [" + stateKey.getUuid() + "] in state provider."));
         }
      }
      else
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         // capture any values that the caller may have supplied in the request, before restoring from state //
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         Map<String, Serializable> valuesFromCaller = runProcessRequest.getValues();

         ///////////////////////////////////////////////////
         // if there is a previously stored state, use it //
         ///////////////////////////////////////////////////
         runProcessRequest.seedFromProcessState(optionalProcessState.get());

         ///////////////////////////////////////////////////////////////////////////
         // if there were values from the caller, put those (back) in the request //
         ///////////////////////////////////////////////////////////////////////////
         if(valuesFromCaller != null)
         {
            for(Map.Entry<String, Serializable> entry : valuesFromCaller.entrySet())
            {
               runProcessRequest.addValue(entry.getKey(), entry.getValue());
            }
         }
      }

      ProcessState processState = optionalProcessState.get();
      processState.clearNextStepName();
      return processState;
   }



   /*******************************************************************************
    ** Run a single backend step.
    *******************************************************************************/
   private void runBackendStep(RunProcessRequest runProcessRequest, QProcessMetaData process, RunProcessResult runProcessResult, UUIDAndTypeStateKey stateKey, QBackendStepMetaData backendStep, ProcessState processState) throws Exception
   {
      RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(runProcessRequest.getInstance(), processState);
      runBackendStepRequest.setProcessName(process.getName());
      runBackendStepRequest.setStepName(backendStep.getName());
      runBackendStepRequest.setSession(runProcessRequest.getSession());
      runBackendStepRequest.setCallback(runProcessRequest.getCallback());
      runBackendStepRequest.setAsyncJobCallback(runProcessRequest.getAsyncJobCallback());
      RunBackendStepResult lastFunctionResult = new RunBackendStepAction().execute(runBackendStepRequest);
      storeState(stateKey, lastFunctionResult.getProcessState());

      if(lastFunctionResult.getException() != null)
      {
         runProcessResult.setException(lastFunctionResult.getException());
         throw (lastFunctionResult.getException());
      }
   }



   /*******************************************************************************
    ** Get the list of steps which are eligible to run.
    *******************************************************************************/
   private List<QStepMetaData> getAvailableStepList(QProcessMetaData process, RunProcessRequest runProcessRequest)
   {
      if(runProcessRequest.getStartAfterStep() == null)
      {
         /////////////////////////////////////////////////////////////////////////////
         // if the caller did not supply a 'startAfterStep', then use the full list //
         /////////////////////////////////////////////////////////////////////////////
         return (process.getStepList());
      }
      else
      {
         ////////////////////////////////////////////////////////////////////////////////
         // else, loop until the startAfterStep is found, and return the ones after it //
         ////////////////////////////////////////////////////////////////////////////////
         boolean             foundStartAfterStep = false;
         List<QStepMetaData> rs                  = new ArrayList<>();

         for(QStepMetaData step : process.getStepList())
         {
            if(foundStartAfterStep)
            {
               rs.add(step);
            }

            if(step.getName().equals(runProcessRequest.getStartAfterStep()))
            {
               foundStartAfterStep = true;
            }
         }
         return (rs);
      }
   }



   /*******************************************************************************
    ** Load an instance of the appropriate state provider
    **
    *******************************************************************************/
   public static StateProviderInterface getStateProvider()
   {
      // TODO - read this from somewhere in meta data eh?
      return InMemoryStateProvider.getInstance();

      // todo - by using JSON serialization internally, this makes stupidly large payloads and crashes things.
      // return TempFileStateProvider.getInstance();
   }



   /*******************************************************************************
    ** public method to get a process state just by UUID.
    *******************************************************************************/
   public static Optional<ProcessState> getState(String processUUID)
   {
      return (getStateProvider().get(ProcessState.class, new UUIDAndTypeStateKey(UUID.fromString(processUUID), StateType.PROCESS_STATUS)));
   }



   /*******************************************************************************
    ** Store the process state from a function result to the state provider
    **
    *******************************************************************************/
   private void storeState(UUIDAndTypeStateKey stateKey, ProcessState processState)
   {
      getStateProvider().put(stateKey, processState);
   }



   /*******************************************************************************
    ** Load the process state.
    **
    *******************************************************************************/
   private Optional<ProcessState> loadState(UUIDAndTypeStateKey stateKey)
   {
      return (getStateProvider().get(ProcessState.class, stateKey));
   }

}
