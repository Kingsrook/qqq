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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.NoCodeWidgetRenderer;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.NoCodeWidgetFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.BasepullConfiguration;
import com.kingsrook.qqq.backend.core.state.InMemoryStateProvider;
import com.kingsrook.qqq.backend.core.state.StateProviderInterface;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 ** Action handler for running q-processes (which are a sequence of q-functions).
 *
 *******************************************************************************/
public class RunProcessAction
{
   private static final QLogger LOG = QLogger.getLogger(RunProcessAction.class);

   public static final String BASEPULL_THIS_RUNTIME_KEY = "basepullThisRuntimeKey";
   public static final String BASEPULL_LAST_RUNTIME_KEY = "basepullLastRuntimeKey";
   public static final String BASEPULL_TIMESTAMP_FIELD  = "basepullTimestampField";

   ////////////////////////////////////////////////////////////////////////////////////////////////
   // indicator that the timestamp field should be updated - e.g., the execute step is finished. //
   ////////////////////////////////////////////////////////////////////////////////////////////////
   public static final String BASEPULL_READY_TO_UPDATE_TIMESTAMP_FIELD = "basepullReadyToUpdateTimestamp";
   public static final String BASEPULL_DID_QUERY_USING_TIMESTAMP_FIELD = "basepullDidQueryUsingTimestamp";



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

      /////////////////////////////////////////////////////////
      // if process is 'basepull' style, keep track of 'now' //
      /////////////////////////////////////////////////////////
      BasepullConfiguration basepullConfiguration = process.getBasepullConfiguration();
      if(basepullConfiguration != null)
      {
         ///////////////////////////////////////
         // get the stored basepull timestamp //
         ///////////////////////////////////////
         persistLastRunTime(runProcessInput, process, basepullConfiguration);
      }

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

            if(step instanceof QFrontendStepMetaData frontendStep)
            {
               ////////////////////////////////////////////////////////////////
               // Handle what to do with frontend steps, per request setting //
               ////////////////////////////////////////////////////////////////
               switch(runProcessInput.getFrontendStepBehavior())
               {
                  case BREAK ->
                  {
                     LOG.trace("Breaking process [" + process.getName() + "] at frontend step (as requested by caller): " + step.getName());
                     processFrontendStepFieldDefaultValues(processState, frontendStep);
                     processFrontendComponents(processState, frontendStep);
                     processState.setNextStepName(step.getName());
                     break STEP_LOOP;
                  }
                  case SKIP ->
                  {
                     LOG.trace("Skipping frontend step [" + step.getName() + "] in process [" + process.getName() + "] (as requested by caller)");

                     //////////////////////////////////////////////////////////////////////
                     // much less error prone in case this code changes in the future... //
                     //////////////////////////////////////////////////////////////////////
                     // noinspection UnnecessaryContinue
                     continue;
                  }
                  case FAIL ->
                  {
                     LOG.trace("Throwing error for frontend step [" + step.getName() + "] in process [" + process.getName() + "] (as requested by caller)");
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

         ///////////////////////////////////////////////////////////////////////////
         // if 'basepull' style process, update the stored basepull timestamp     //
         // but only when we've been signaled to do so - i.e., only if we did our //
         // query using the timestamp field, and only after an Execute step runs. //
         ///////////////////////////////////////////////////////////////////////////
         if(basepullConfiguration != null
            && BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(runProcessInput.getValue(BASEPULL_DID_QUERY_USING_TIMESTAMP_FIELD)))
            && BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(runProcessInput.getValue(BASEPULL_READY_TO_UPDATE_TIMESTAMP_FIELD))))
         {
            storeLastRunTime(runProcessInput, process, basepullConfiguration);
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
    **
    *******************************************************************************/
   private void processFrontendComponents(ProcessState processState, QFrontendStepMetaData frontendStep) throws QException
   {
      for(QFrontendComponentMetaData component : CollectionUtils.nonNullList(frontendStep.getComponents()))
      {
         if(component instanceof NoCodeWidgetFrontendComponentMetaData noCodeWidgetComponent)
         {
            NoCodeWidgetRenderer noCodeWidgetRenderer = new NoCodeWidgetRenderer();
            Map<String, Object>  context              = noCodeWidgetRenderer.initContext(null);
            context.putAll(processState.getValues());
            String html = noCodeWidgetRenderer.renderOutputs(context, noCodeWidgetComponent.getOutputs());
            processState.getValues().put(frontendStep.getName() + ".html", html);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void processFrontendStepFieldDefaultValues(ProcessState processState, QFrontendStepMetaData step)
   {
      for(QFieldMetaData formField : CollectionUtils.mergeLists(step.getFormFields(), step.getInputFields(), step.getViewFields(), step.getOutputFields()))
      {
         if(formField.getDefaultValue() != null && processState.getValues().get(formField.getName()) == null)
         {
            processState.getValues().put(formField.getName(), formField.getDefaultValue());
         }
      }
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
      RunBackendStepInput runBackendStepInput = new RunBackendStepInput(processState);
      runBackendStepInput.setProcessName(process.getName());
      runBackendStepInput.setStepName(backendStep.getName());
      runBackendStepInput.setCallback(runProcessInput.getCallback());
      runBackendStepInput.setFrontendStepBehavior(runProcessInput.getFrontendStepBehavior());
      runBackendStepInput.setAsyncJobCallback(runProcessInput.getAsyncJobCallback());

      runBackendStepInput.setTableName(process.getTableName());
      if(!StringUtils.hasContent(runBackendStepInput.getTableName()))
      {
         ////////////////////////////////////////////////////////////////
         // help support generic (e.g., not tied-to-a-table) processes //
         ////////////////////////////////////////////////////////////////
         if(runProcessInput.getValue("tableName") != null)
         {
            runBackendStepInput.setTableName(ValueUtils.getValueAsString(runProcessInput.getValue("tableName")));
         }
      }

      ///////////////////////////////////////////////////////////////
      // if 'basepull' values are in the inputs, add to step input //
      ///////////////////////////////////////////////////////////////
      if(runProcessInput.getValues().containsKey(BASEPULL_LAST_RUNTIME_KEY))
      {
         runBackendStepInput.setBasepullLastRunTime((Instant) runProcessInput.getValues().get(BASEPULL_LAST_RUNTIME_KEY));
      }

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
            throw (new QException("Could not find a step named [" + stepName + "] in this process."));
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



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String determineBasepullKeyValue(QProcessMetaData process, BasepullConfiguration basepullConfiguration) throws QException
   {
      String basepullKeyValue = (basepullConfiguration.getKeyValue() != null) ? basepullConfiguration.getKeyValue() : process.getName();

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if backend specifies that it uses variants, look for that data in the session and append to our basepull key //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(process.getSchedule().getVariantBackend() != null)
      {
         QSession         session         = QContext.getQSession();
         QBackendMetaData backendMetaData = QContext.getQInstance().getBackend(process.getSchedule().getVariantBackend());
         if(session.getBackendVariants() == null || !session.getBackendVariants().containsKey(backendMetaData.getVariantOptionsTableTypeValue()))
         {
            throw (new QException("Could not find Backend Variant information for Backend '" + backendMetaData.getName() + "'"));
         }
         basepullKeyValue += "-" + session.getBackendVariants().get(backendMetaData.getVariantOptionsTableTypeValue());
      }

      return (basepullKeyValue);
   }



   /*******************************************************************************
    ** Insert or update the last runtime value for this basepull into the backend.
    *******************************************************************************/
   protected void storeLastRunTime(RunProcessInput runProcessInput, QProcessMetaData process, BasepullConfiguration basepullConfiguration) throws QException
   {
      String basepullTableName            = basepullConfiguration.getTableName();
      String basepullKeyFieldName         = basepullConfiguration.getKeyField();
      String basepullLastRunTimeFieldName = basepullConfiguration.getLastRunTimeFieldName();
      String basepullKeyValue             = determineBasepullKeyValue(process, basepullConfiguration);

      ///////////////////////////////////////
      // get the stored basepull timestamp //
      ///////////////////////////////////////
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(basepullTableName);
      queryInput.setFilter(new QQueryFilter().withCriteria(
         new QFilterCriteria()
            .withFieldName(basepullKeyFieldName)
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(basepullKeyValue))));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      //////////////////////////////////////////
      // get the runtime for this process run //
      //////////////////////////////////////////
      Instant newRunTime = (Instant) runProcessInput.getValues().get(BASEPULL_THIS_RUNTIME_KEY);

      /////////////////////////////////////////////////
      // update if found, otherwise insert new value //
      /////////////////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
      {
         ///////////////////////////////////////////////////////////////////////////////
         // update the basepull table with 'now' (which is before original query ran) //
         ///////////////////////////////////////////////////////////////////////////////
         QRecord basepullRecord = queryOutput.getRecords().get(0);
         basepullRecord.setValue(basepullLastRunTimeFieldName, newRunTime);

         ////////////
         // update //
         ////////////
         UpdateInput updateInput = new UpdateInput();
         updateInput.setTableName(basepullTableName);
         updateInput.setRecords(List.of(basepullRecord));
         new UpdateAction().execute(updateInput);
      }
      else
      {
         QRecord basepullRecord = new QRecord()
            .withValue(basepullKeyFieldName, basepullKeyValue)
            .withValue(basepullLastRunTimeFieldName, newRunTime);

         ////////////////////////////////
         // insert new basepull record //
         ////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(basepullTableName);
         insertInput.setRecords(List.of(basepullRecord));
         new InsertAction().execute(insertInput);
      }
   }



   /*******************************************************************************
    ** Lookup the last runtime for this basepull, and set it (plus now) in the process's
    ** values.
    *******************************************************************************/
   protected void persistLastRunTime(RunProcessInput runProcessInput, QProcessMetaData process, BasepullConfiguration basepullConfiguration) throws QException
   {
      ////////////////////////////////////////////////////////
      // if these values were already computed, don't re-do //
      ////////////////////////////////////////////////////////
      if(runProcessInput.getValue(BASEPULL_THIS_RUNTIME_KEY) != null)
      {
         return;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // store 'now', which will be used to update basepull record if process completes successfully //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      Instant now = Instant.now();
      runProcessInput.getValues().put(BASEPULL_THIS_RUNTIME_KEY, now);

      String  basepullTableName                    = basepullConfiguration.getTableName();
      String  basepullKeyFieldName                 = basepullConfiguration.getKeyField();
      String  basepullLastRunTimeFieldName         = basepullConfiguration.getLastRunTimeFieldName();
      Integer basepullHoursBackForInitialTimestamp = basepullConfiguration.getHoursBackForInitialTimestamp();
      String  basepullKeyValue                     = determineBasepullKeyValue(process, basepullConfiguration);

      ///////////////////////////////////////
      // get the stored basepull timestamp //
      ///////////////////////////////////////
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(basepullTableName);
      queryInput.setFilter(new QQueryFilter().withCriteria(
         new QFilterCriteria()
            .withFieldName(basepullKeyFieldName)
            .withOperator(QCriteriaOperator.EQUALS)
            .withValues(List.of(basepullKeyValue))));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // get the stored time, if not, default to 'now' unless a number of hours to offset was provided //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      Instant lastRunTime = now;
      if(CollectionUtils.nullSafeHasContents(queryOutput.getRecords()))
      {
         QRecord basepullRecord = queryOutput.getRecords().get(0);
         lastRunTime = ValueUtils.getValueAsInstant(basepullRecord.getValue(basepullLastRunTimeFieldName));
      }
      else if(basepullHoursBackForInitialTimestamp != null)
      {
         lastRunTime = lastRunTime.minus(basepullHoursBackForInitialTimestamp, ChronoUnit.HOURS);
      }

      runProcessInput.getValues().put(BASEPULL_LAST_RUNTIME_KEY, lastRunTime);
      runProcessInput.getValues().put(BASEPULL_TIMESTAMP_FIELD, basepullConfiguration.getTimestampField());
   }
}
