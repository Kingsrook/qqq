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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Backend step to do a full validation of a streamed ETL job
 *******************************************************************************/
public class StreamedETLValidateStep extends BaseStreamedETLStep implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(StreamedETLValidateStep.class);

   private int currentRowCount = 1;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @SuppressWarnings("checkstyle:indentation")
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      /////////////////////////////////////////////////////////////////////
      // check if we are supported in this process - if not, return noop //
      /////////////////////////////////////////////////////////////////////
      boolean supportsFullValidation = runBackendStepInput.getValue_boolean(StreamedETLWithFrontendProcess.FIELD_SUPPORTS_FULL_VALIDATION);
      if(!supportsFullValidation)
      {
         LOG.info("Process does not support validation, so skipping validation step");
         return;
      }

      ////////////////////////////////////////////////////////////////////////////////
      // check if we've been requested to run in this process - if not, return noop //
      ////////////////////////////////////////////////////////////////////////////////
      boolean doFullValidation = runBackendStepInput.getValue_boolean(StreamedETLWithFrontendProcess.FIELD_DO_FULL_VALIDATION);
      if(!doFullValidation)
      {
         LOG.info("Not requested to do full validation, so skipping validation step");
         return;
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if we're proceeding with full validation, move the review step to be after validation in the step list //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ArrayList<String> stepList = new ArrayList<>(runBackendStepOutput.getProcessState().getStepList());
      System.out.println("Step list pre: " + stepList);
      stepList.removeIf(s -> s.equals(StreamedETLWithFrontendProcess.STEP_NAME_REVIEW));
      stepList.add(stepList.indexOf(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE) + 1, StreamedETLWithFrontendProcess.STEP_NAME_REVIEW);
      runBackendStepOutput.getProcessState().setStepList(stepList);
      System.out.println("Step list post: " + stepList);

      //////////////////////////////////////////////////////////
      // basically repeat the preview step, but with no limit //
      //////////////////////////////////////////////////////////
      RecordPipe          recordPipe  = new RecordPipe();
      AbstractExtractStep extractStep = getExtractStep(runBackendStepInput);
      extractStep.setLimit(null);
      extractStep.setRecordPipe(recordPipe);

      AbstractTransformStep transformStep = getTransformStep(runBackendStepInput);
      if(!(transformStep instanceof ProcessSummaryProviderInterface processSummaryProvider))
      {
         throw (new QException("Transform Step " + transformStep.getClass().getName() + " does not implement ProcessSummaryProviderInterface."));
      }

      List<QRecord> previewRecordList = new ArrayList<>();
      int recordCount = new AsyncRecordPipeLoop().run("StreamedETL>Preview>ValidateStep", PROCESS_OUTPUT_RECORD_LIST_LIMIT, recordPipe, (status) ->
         {
            extractStep.run(runBackendStepInput, runBackendStepOutput);
            return (runBackendStepOutput);
         },
         () -> (consumeRecordsFromPipe(recordPipe, transformStep, runBackendStepInput, runBackendStepOutput, previewRecordList))
      );

      runBackendStepOutput.setRecords(previewRecordList);
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT, recordCount);

      //////////////////////////////////////////////////////
      // get the process summary from the validation step //
      //////////////////////////////////////////////////////
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY, processSummaryProvider.getProcessSummary(false));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, AbstractTransformStep transformStep, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<QRecord> previewRecordList) throws QException
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

      /////////////////////////////////////////////////////
      // pass the records through the transform function //
      /////////////////////////////////////////////////////
      transformStep.setInputRecordPage(qRecords);
      transformStep.run(runBackendStepInput, runBackendStepOutput);

      ///////////////////////////////////////////////////////
      // copy a small number of records to the output list //
      ///////////////////////////////////////////////////////
      int i = 0;
      while(previewRecordList.size() < PROCESS_OUTPUT_RECORD_LIST_LIMIT && i < transformStep.getOutputRecordPage().size())
      {
         previewRecordList.add(transformStep.getOutputRecordPage().get(i++));
      }

      currentRowCount += qRecords.size();
      return (qRecords.size());
   }

}
