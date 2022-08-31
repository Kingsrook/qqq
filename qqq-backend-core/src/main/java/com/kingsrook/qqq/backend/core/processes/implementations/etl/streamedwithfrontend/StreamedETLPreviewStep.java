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
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Backend step to do a preview of a full streamed ETL job
 *******************************************************************************/
public class StreamedETLPreviewStep extends BaseStreamedETLStep implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(StreamedETLPreviewStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @SuppressWarnings("checkstyle:indentation")
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Integer limit = PROCESS_OUTPUT_RECORD_LIST_LIMIT; // todo - use a field instead of hard-coded here?

      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if the do-full-validation flag has already been set, then do the validation step instead of this one //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      boolean supportsFullValidation = runBackendStepInput.getValue_boolean(StreamedETLWithFrontendProcess.FIELD_SUPPORTS_FULL_VALIDATION);
      boolean doFullValidation       = runBackendStepInput.getValue_boolean(StreamedETLWithFrontendProcess.FIELD_DO_FULL_VALIDATION);
      if(supportsFullValidation && doFullValidation)
      {
         skipToValidateStep(runBackendStepOutput);
         return;
      }

      ///////////////////////////////////////////
      // request a count from the extract step //
      ///////////////////////////////////////////
      AbstractExtractStep extractStep = getExtractStep(runBackendStepInput);
      Integer             recordCount = extractStep.doCount(runBackendStepInput);
      runBackendStepOutput.addValue(StreamedETLProcess.FIELD_RECORD_COUNT, recordCount);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if the count is less than the normal limit here, and this process supports validation, then go straight to the validation step //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - maybe some future version we do this - maybe based on a user-preference
      // if(supportsFullValidation && recordCount <= limit)
      // {
      //    skipToValidateStep(runBackendStepOutput);
      //    return;
      // }

      ////////////////////////////////////////////////////////
      // proceed with a doing a limited extract & transform //
      ////////////////////////////////////////////////////////
      RecordPipe recordPipe = new RecordPipe();
      extractStep.setLimit(limit);
      extractStep.setRecordPipe(recordPipe);

      AbstractTransformStep transformStep = getTransformStep(runBackendStepInput);
      transformStep.preRun(runBackendStepInput, runBackendStepOutput);

      List<QRecord> previewRecordList = new ArrayList<>();
      new AsyncRecordPipeLoop().run("StreamedETL>Preview>ExtractStep", PROCESS_OUTPUT_RECORD_LIST_LIMIT, recordPipe, (status) ->
         {
            extractStep.run(runBackendStepInput, runBackendStepOutput);
            return (runBackendStepOutput);
         },
         () -> (consumeRecordsFromPipe(recordPipe, transformStep, runBackendStepInput, runBackendStepOutput, previewRecordList))
      );

      updateRecordsWithDisplayValuesAndPossibleValues(runBackendStepInput, previewRecordList);
      runBackendStepOutput.setRecords(previewRecordList);

      transformStep.postRun(runBackendStepInput, runBackendStepOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void skipToValidateStep(RunBackendStepOutput runBackendStepOutput)
   {
      LOG.info("Skipping to validation step");
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_DO_FULL_VALIDATION, true);
      ArrayList<String> stepList = new ArrayList<>(runBackendStepOutput.getProcessState().getStepList());
      System.out.println("Step list pre: " + stepList);
      stepList.removeIf(s -> s.equals(StreamedETLWithFrontendProcess.STEP_NAME_REVIEW));
      stepList.add(stepList.indexOf(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE) + 1, StreamedETLWithFrontendProcess.STEP_NAME_REVIEW);
      runBackendStepOutput.getProcessState().setStepList(stepList);
      System.out.println("Step list post: " + stepList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, AbstractTransformStep transformStep, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<QRecord> transformedRecordList) throws QException
   {
      ///////////////////////////////////
      // get the records from the pipe //
      ///////////////////////////////////
      List<QRecord> qRecords = recordPipe.consumeAvailableRecords();

      /////////////////////////////////////////////////////
      // pass the records through the transform function //
      /////////////////////////////////////////////////////
      transformStep.setInputRecordPage(qRecords);
      transformStep.run(runBackendStepInput, runBackendStepOutput);

      ////////////////////////////////////////////////////
      // add the transformed records to the output list //
      ////////////////////////////////////////////////////
      transformedRecordList.addAll(transformStep.getOutputRecordPage());

      return (qRecords.size());
   }

}