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

package com.kingsrook.qqq.backend.core.actions.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
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
   public RunProcessOutput execute(RunProcessInput runProcessInput) throws QException
   {
      ActionHelper.validateSession(runProcessInput);

      QProcessMetaData process = runProcessInput.getInstance().getProcess(runProcessInput.getProcessName());
      if(process == null)
      {
         throw new QException("Process [" + runProcessInput.getProcessName() + "] is not defined in this instance.");
      }

      RunProcessOutput runProcessOutput = new RunProcessOutput();

      //////////////////////////////////////////////////////////
      // generate a UUID for the process, if one wasn't given //
      //////////////////////////////////////////////////////////
      if(runProcessInput.getProcessUUID() == null)
      {
         runProcessInput.setProcessUUID(UUID.randomUUID().toString());
      }
      runProcessOutput.setProcessUUID(runProcessInput.getProcessUUID());

      UUIDAndTypeStateKey stateKey     = new UUIDAndTypeStateKey(UUID.fromString(runProcessInput.getProcessUUID()), StateType.PROCESS_STATUS);
      ProcessState        processState = primeProcessState(runProcessInput, stateKey, process);

      try
      {
         String lastStepName = runProcessInput.getStartAfterStep();

         STEP_LOOP:
         while(true)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            // always refresh the step list - as any step that runs can modify it (in the process state).        //
            // this is why we don't do a loop over the step list - as we'd get ConcurrentModificationExceptions. //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////
            List<QStepMetaData> stepList = getAvailableStepList(processState, process, lastStepName);
            if(stepList.isEmpty())
            {
               break;
            }

            QStepMetaData step = stepList.get(0);
            lastStepName = step.getName();

            if(step instanceof QFrontendStepMetaData)
            {
               ////////////////////////////////////////////////////////////////
               // Handle what to do with frontend steps, per request setting //
               ////////////////////////////////////////////////////////////////
               switch(runProcessInput.getFrontendStepBehavior())
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
                  default -> throw new IllegalStateException("Unexpected value: " + runProcessInput.getFrontendStepBehavior());
               }
            }
            else if(step instanceof QBackendStepMetaData backendStepMetaData)
            {
               ///////////////////////
               // Run backend steps //
               ///////////////////////
               LOG.debug("Running backend step [" + step.getName() + "] in process [" + process.getName() + "]");
               runBackendStep(runProcessInput, process, runProcessOutput, stateKey, backendStepMetaData, process, processState);
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
         runProcessOutput.setProcessState(processState);
      }

      return (runProcessOutput);
   }



   /*******************************************************************************
    ** When we start running a process (or resuming it), get data in the RunProcessRequest
    ** either from the state provider (if they're found, for a resume).
    *******************************************************************************/
   ProcessState primeProcessState(RunProcessInput runProcessInput, UUIDAndTypeStateKey stateKey, QProcessMetaData process) throws QException
   {
      Optional<ProcessState> optionalProcessState = loadState(stateKey);
      if(optionalProcessState.isEmpty())
      {
         if(runProcessInput.getStartAfterStep() == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////
            // This condition (no state in state-provider, and no start-after-step) means    //
            // that we're starting a new process!  Init the process state here, then         //
            // Go ahead and store the state that we have (e.g., w/ initial records & values) //
            ///////////////////////////////////////////////////////////////////////////////////
            ProcessState processState = runProcessInput.getProcessState();
            processState.setStepList(process.getStepList().stream().map(QStepMetaData::getName).toList());
            storeState(stateKey, processState);
            optionalProcessState = Optional.of(processState);
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////
            // if this isn't the first step, but there's no state, then that's a problem, so fail //
            ////////////////////////////////////////////////////////////////////////////////////////
            throw (new QException("Could not find state for process [" + runProcessInput.getProcessName() + "] [" + stateKey.getUuid() + "] in state provider."));
         }
      }
      else
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         // capture any values that the caller may have supplied in the request, before restoring from state //
         //////////////////////////////////////////////////////////////////////////////////////////////////////
         Map<String, Serializable> valuesFromCaller = runProcessInput.getValues();

         ///////////////////////////////////////////////////
         // if there is a previously stored state, use it //
         ///////////////////////////////////////////////////
         runProcessInput.seedFromProcessState(optionalProcessState.get());

         ///////////////////////////////////////////////////////////////////////////
         // if there were values from the caller, put those (back) in the request //
         ///////////////////////////////////////////////////////////////////////////
         if(valuesFromCaller != null)
         {
            for(Map.Entry<String, Serializable> entry : valuesFromCaller.entrySet())
            {
               runProcessInput.addValue(entry.getKey(), entry.getValue());
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
   private void runBackendStep(RunProcessInput runProcessInput, QProcessMetaData process, RunProcessOutput runProcessOutput, UUIDAndTypeStateKey stateKey, QBackendStepMetaData backendStep, QProcessMetaData qProcessMetaData, ProcessState processState) throws Exception
   {
      RunBackendStepInput runBackendStepInput = new RunBackendStepInput(runProcessInput.getInstance(), processState);
      runBackendStepInput.setProcessName(process.getName());
      runBackendStepInput.setStepName(backendStep.getName());
      runBackendStepInput.setTableName(process.getTableName());
      runBackendStepInput.setSession(runProcessInput.getSession());
      runBackendStepInput.setCallback(runProcessInput.getCallback());
      runBackendStepInput.setAsyncJobCallback(runProcessInput.getAsyncJobCallback());
      RunBackendStepOutput lastFunctionResult = new RunBackendStepAction().execute(runBackendStepInput);
      storeState(stateKey, lastFunctionResult.getProcessState());

      if(lastFunctionResult.getException() != null)
      {
         runProcessOutput.setException(lastFunctionResult.getException());
         throw (lastFunctionResult.getException());
      }
   }



   /*******************************************************************************
    ** Get the list of steps which are eligible to run.
    *******************************************************************************/
   private List<QStepMetaData> getAvailableStepList(ProcessState processState, QProcessMetaData process, String lastStep) throws QException
   {
      if(lastStep == null)
      {
         ///////////////////////////////////////////////////////////////////////
         // if the caller did not supply a 'lastStep', then use the full list //
         ///////////////////////////////////////////////////////////////////////
         return (stepNamesToSteps(process, processState.getStepList()));
      }
      else
      {
         ////////////////////////////////////////////////////////////////////////////
         // else, loop until the 'lastStep' is found, and return the ones after it //
         ////////////////////////////////////////////////////////////////////////////
         boolean      foundLastStep  = false;
         List<String> validStepNames = new ArrayList<>();

         for(String stepName : processState.getStepList())
         {
            if(foundLastStep)
            {
               validStepNames.add(stepName);
            }

            if(stepName.equals(lastStep))
            {
               foundLastStep = true;
            }
         }
         return (stepNamesToSteps(process, validStepNames));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QStepMetaData> stepNamesToSteps(QProcessMetaData process, List<String> stepNames) throws QException
   {
      List<QStepMetaData> result = new ArrayList<>();

      for(String stepName : stepNames)
      {
         QStepMetaData step = process.getStep(stepName);
         if(step == null)
         {
            throw(new QException("Could not find a step named [" + stepName + "] in this process."));
         }
         result.add(step);
      }

      return (result);
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
