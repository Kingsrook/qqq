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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.audits.AuditAction;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.utils.ProcessLogManager;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Backend step to do the execute portion of a streamed ETL job.
 **
 ** Works within a transaction (per the backend module of the destination table).
 *******************************************************************************/
public class StreamedETLExecuteStep extends BaseStreamedETLStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(StreamedETLExecuteStep.class);

   private int currentRowCount = 1;

   private ProcessLogManager processLogManager = new ProcessLogManager();


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @SuppressWarnings("checkstyle:indentation")
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Optional<QBackendTransaction> transaction = Optional.empty();

      try
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus("Executing Process");
         processLogManager.beginProcessLog(runBackendStepInput);

         ///////////////////////////////////////////////////////
         // set up the extract, transform, and load functions //
         ///////////////////////////////////////////////////////
         AbstractExtractStep   extractStep   = getExtractStep(runBackendStepInput);
         AbstractTransformStep transformStep = getTransformStep(runBackendStepInput);
         AbstractLoadStep      loadStep      = getLoadStep(runBackendStepInput);

         loadStep.setTransformStep(transformStep);

         /////////////////////////////////////////////////////////////////////////////
         // let the load step override the capacity for the record pipe.            //
         // this is useful for slower load steps - so that the extract step doesn't //
         // fill the pipe, then timeout waiting for all the records to be consumed, //
         // before it can put more records in.                                      //
         /////////////////////////////////////////////////////////////////////////////
         RecordPipe recordPipe;
         Integer    overrideRecordPipeCapacity = loadStep.getOverrideRecordPipeCapacity(runBackendStepInput);
         if(overrideRecordPipeCapacity != null)
         {
            recordPipe = new RecordPipe(overrideRecordPipeCapacity);
            LOG.debug("per " + loadStep.getClass().getName() + ", we are overriding record pipe capacity to: " + overrideRecordPipeCapacity);
         }
         else
         {
            overrideRecordPipeCapacity = transformStep.getOverrideRecordPipeCapacity(runBackendStepInput);
            if(overrideRecordPipeCapacity != null)
            {
               recordPipe = new RecordPipe(overrideRecordPipeCapacity);
               LOG.debug("per " + transformStep.getClass().getName() + ", we are overriding record pipe capacity to: " + overrideRecordPipeCapacity);
            }
            else
            {
               recordPipe = new RecordPipe();
            }
         }

         extractStep.setRecordPipe(recordPipe);
         extractStep.preRun(runBackendStepInput, runBackendStepOutput);

         transformStep.preRun(runBackendStepInput, runBackendStepOutput);
         loadStep.preRun(runBackendStepInput, runBackendStepOutput);

         /////////////////////////////////////////////////////////////////////////////
         // open a transaction for the whole process, if that's the requested level //
         /////////////////////////////////////////////////////////////////////////////
         boolean doProcessLevelTransaction = StreamedETLWithFrontendProcess.TRANSACTION_LEVEL_PROCESS.equals(runBackendStepInput.getValueString(StreamedETLWithFrontendProcess.FIELD_TRANSACTION_LEVEL));
         if(doProcessLevelTransaction)
         {
            transaction = loadStep.openTransaction(runBackendStepInput);
            loadStep.setTransaction(transaction);
            transformStep.setTransaction(transaction);
         }

         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // we will pass a step to the processLogManager each time the extractor gets blocked (considering that the end of a 'page') //
         // we'll know about that via blocked & un-blocked callbacks.  we'll use an atomic reference to an instant to capture the    //
         // 'start' time of each page, as when an un-block occurs.                                                                   //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         AtomicReference<Instant> previousExtractStart             = new AtomicReference<>(Instant.now());
         AtomicInteger            previousExtractRecordsAddedCount = new AtomicInteger(0);

         ////////////////////////////////////////////////////////////////////////////////////////////////////
         // when the extractor becomes blocked, record that as the end of one instance of the extract step //
         ////////////////////////////////////////////////////////////////////////////////////////////////////
         recordPipe.setUponBlockedCallback(() ->
         {
            ///////////////////////////////////////////////////////////////////////////////////////
            // tood - something isn't coming out quite right in the number of extract records... //
            ///////////////////////////////////////////////////////////////////////////////////////
            int pageSize = recordPipe.getRecordsAddedCounter() - previousExtractRecordsAddedCount.get();
            processLogManager.addStep("extract", previousExtractStart.get(), Instant.now(), pageSize);
            previousExtractRecordsAddedCount.set(pageSize);
         });

         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // when the extractor becomes unblocked, record that timestamp as the start of the next instance/page of extract //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         recordPipe.setUponUnblockedCallback(() ->
         {
            previousExtractStart.set(Instant.now());
         });

         ////////////////////////////////////////
         // set up and run the async pipe loop //
         ////////////////////////////////////////
         List<QRecord>       loadedRecordList    = new ArrayList<>();
         AsyncRecordPipeLoop asyncRecordPipeLoop = new AsyncRecordPipeLoop();
         if(overrideRecordPipeCapacity != null && overrideRecordPipeCapacity < asyncRecordPipeLoop.getMinRecordsToConsume())
         {
            asyncRecordPipeLoop.setMinRecordsToConsume(overrideRecordPipeCapacity);
         }

         int recordCount = asyncRecordPipeLoop.run("StreamedETL>Execute>ExtractStep", null, recordPipe, (status) ->
            {
               extractStep.run(runBackendStepInput, runBackendStepOutput);

               int pageSize = recordPipe.getRecordsAddedCounter() - previousExtractRecordsAddedCount.get();
               processLogManager.addStep("extract", previousExtractStart.get(), Instant.now(), pageSize);

               return (runBackendStepOutput);
            },
            () -> (consumeRecordsFromPipe(recordPipe, transformStep, loadStep, runBackendStepInput, runBackendStepOutput, loadedRecordList))
         );

         runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT, recordCount);

         updateRecordsWithDisplayValuesAndPossibleValues(runBackendStepInput, loadedRecordList);
         runBackendStepOutput.setRecords(loadedRecordList);

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // get the process summary from the load step, if it's a summary-provider -- else, use the transform step (which is always a provider) //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         ArrayList<ProcessSummaryLineInterface> processSummaryLines = null;
         if(loadStep instanceof ProcessSummaryProviderInterface provider)
         {
            processSummaryLines = provider.doGetProcessSummary(runBackendStepOutput, true);
            runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, processSummaryLines);
         }

         if(CollectionUtils.nullSafeIsEmpty(processSummaryLines))
         {
            processSummaryLines = transformStep.doGetProcessSummary(runBackendStepOutput, true);
            runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, processSummaryLines);
         }

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // use a subclass of runBackendStepOutput that makes it clear you can't use the recordList, as it's a "preview/subset" record list //
         // this prevents bugs where you might think you have the full record list, but really don't                                        //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         BackendStepPostRunOutput postRunOutput = new BackendStepPostRunOutput(runBackendStepOutput);
         BackendStepPostRunInput  postRunInput  = new BackendStepPostRunInput(runBackendStepInput);
         transformStep.postRun(postRunInput, postRunOutput);
         loadStep.postRun(postRunInput, postRunOutput);

         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // explicitly copy values back into the runStepOutput from the post-run output                                     //
         // this might not be needed, since they (presumably) share a processState object, but just in case that changes... //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         runBackendStepOutput.setValues(postRunOutput.getValues());

         if(recordCount > 0)
         {
            LOG.info("Processed [" + recordCount + "] records.");
         }

         //////////////////////////////////////////////////////////////////////////////
         // set the flag to state that the basepull timestamp should be updated now. //
         // (upstream will check if the process was running as a basepull)           //
         //////////////////////////////////////////////////////////////////////////////
         runBackendStepOutput.addValue(RunProcessAction.BASEPULL_READY_TO_UPDATE_TIMESTAMP_FIELD, true);

         processLogManager.finishProcessLog(runBackendStepInput, processSummaryLines, Optional.empty());

         ////////////////////////////////////////////////////////
         // commit the work at the process level if applicable //
         ////////////////////////////////////////////////////////
         if(doProcessLevelTransaction && transaction.isPresent())
         {
            transaction.get().commit();
         }
      }
      catch(Exception e)
      {
         processLogManager.finishProcessLog(runBackendStepInput, null, Optional.of(e));

         ////////////////////////////////////////////////////////////////////////////////
         // rollback the work, then re-throw the error for up-stream to catch & report //
         ////////////////////////////////////////////////////////////////////////////////
         if(transaction.isPresent())
         {
            LOG.warn("Caught top-level process exception - rolling back transaction", e);
            transaction.get().rollback();
         }
         else
         {
            LOG.warn("Caught top-level process exception - would roll back transaction, but none is present", e);
         }
         throw (e);
      }
      finally
      {
         ////////////////////////////////////////////////////////////
         // always close our transactions (e.g., jdbc connections) //
         ////////////////////////////////////////////////////////////
         if(transaction.isPresent())
         {
            transaction.get().close();
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, AbstractTransformStep transformStep, AbstractLoadStep loadStep, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<QRecord> loadedRecordList) throws QException
   {
      ////////////////////////////////////////////////////////////////////
      // open a transaction for the page, if that's the requested level //
      ////////////////////////////////////////////////////////////////////
      Optional<QBackendTransaction> transaction            = Optional.empty();
      boolean                       doPageLevelTransaction = StreamedETLWithFrontendProcess.TRANSACTION_LEVEL_PAGE.equals(runBackendStepInput.getValueString(StreamedETLWithFrontendProcess.FIELD_TRANSACTION_LEVEL));
      if(doPageLevelTransaction)
      {
         transaction = loadStep.openTransaction(runBackendStepInput);
         loadStep.setTransaction(transaction);
         transformStep.setTransaction(transaction);
      }

      try
      {
         Integer totalRows = runBackendStepInput.getValueInteger(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT);
         if(totalRows != null)
         {
            runBackendStepInput.getAsyncJobCallback().updateStatus(currentRowCount, totalRows);
         }

         ///////////////////////////////////
         // get the records from the pipe //
         ///////////////////////////////////
         List<QRecord> qRecords = recordPipe.consumeAvailableRecords();

         //////////////////////////////////////////////////////////////////////////////
         // not sure if we want this - right now, getting it from summaries, but ... //
         //////////////////////////////////////////////////////////////////////////////
         // processLogManager.recordProcessRecords(qRecords);

         ///////////////////////////////////////////////////////////////////////
         // make streamed input & output objects from the run input & outputs //
         ///////////////////////////////////////////////////////////////////////
         StreamedBackendStepInput  streamedBackendStepInput  = new StreamedBackendStepInput(runBackendStepInput, qRecords);
         StreamedBackendStepOutput streamedBackendStepOutput = new StreamedBackendStepOutput(runBackendStepOutput);

         /////////////////////////////////////////////////////
         // pass the records through the transform function //
         /////////////////////////////////////////////////////
         Instant transformStart = Instant.now();
         transformStep.run(streamedBackendStepInput, streamedBackendStepOutput);
         Instant transformEnd = Instant.now();
         List<AuditInput> auditInputListFromTransform = streamedBackendStepOutput.getAuditInputList();

         ////////////////////////////////////////////////
         // pass the records through the load function //
         ////////////////////////////////////////////////
         streamedBackendStepInput = new StreamedBackendStepInput(runBackendStepInput, streamedBackendStepOutput.getRecords());
         streamedBackendStepOutput = new StreamedBackendStepOutput(runBackendStepOutput);

         Instant loadStart = Instant.now();
         loadStep.run(streamedBackendStepInput, streamedBackendStepOutput);
         Instant          loadEnd                = Instant.now();
         List<AuditInput> auditInputListFromLoad = streamedBackendStepOutput.getAuditInputList();

         /////////////////////////////////////////////////////
         // add the data for these steps to the process log //
         /////////////////////////////////////////////////////
         processLogManager.addStep("transform", transformStart, transformEnd, qRecords.size());
         processLogManager.addStep("load", loadStart, loadEnd, qRecords.size());

         ///////////////////////////////////////////////////////
         // copy a small number of records to the output list //
         ///////////////////////////////////////////////////////
         int i = 0;
         while(loadedRecordList.size() < PROCESS_OUTPUT_RECORD_LIST_LIMIT && i < streamedBackendStepOutput.getRecords().size())
         {
            loadedRecordList.add(streamedBackendStepOutput.getRecords().get(i++));
         }

         //////////////////////////////////////////////////////
         // if we have a batch of audit inputs, execute them //
         //////////////////////////////////////////////////////
         List<AuditInput> mergedAuditInputList = CollectionUtils.mergeLists(auditInputListFromTransform, auditInputListFromLoad);
         if(CollectionUtils.nullSafeHasContents(mergedAuditInputList))
         {
            AuditAction auditAction = new AuditAction();
            for(AuditInput auditInput : mergedAuditInputList)
            {
               auditAction.execute(auditInput);
            }
         }
         runBackendStepOutput.setAuditInputList(null);

         if(doPageLevelTransaction && transaction.isPresent())
         {
            transaction.get().commit();
         }

         currentRowCount += qRecords.size();
         return (qRecords.size());
      }
      catch(Exception e)
      {
         if(doPageLevelTransaction && transaction.isPresent())
         {
            LOG.warn("Caught page-level process exception - rolling back transaction", e);
            transaction.get().rollback();
         }
         throw (e);
      }
      finally
      {
         if(doPageLevelTransaction && transaction.isPresent())
         {
            transaction.get().close();
         }
      }
   }

}
