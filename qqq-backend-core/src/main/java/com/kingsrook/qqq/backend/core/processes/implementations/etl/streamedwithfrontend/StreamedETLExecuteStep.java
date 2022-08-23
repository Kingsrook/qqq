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
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.async.AsyncRecordPipeLoop;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;


/*******************************************************************************
 ** Backend step to do the execute portion of a streamed ETL job.
 **
 ** Works within a transaction (per the backend module of the destination table).
 *******************************************************************************/
public class StreamedETLExecuteStep extends BaseStreamedETLStep implements BackendStep
{
   private int currentRowCount = 1;



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
         ///////////////////////////////////////////////////////
         // set up the extract, transform, and load functions //
         ///////////////////////////////////////////////////////
         RecordPipe          recordPipe  = new RecordPipe();
         AbstractExtractStep extractStep = getExtractStep(runBackendStepInput);
         extractStep.setRecordPipe(recordPipe);

         AbstractTransformStep transformStep = getTransformStep(runBackendStepInput);
         AbstractLoadStep      loadStep      = getLoadStep(runBackendStepInput);

         transaction = loadStep.openTransaction(runBackendStepInput);
         loadStep.setTransaction(transaction);

         List<QRecord> loadedRecordList = new ArrayList<>();
         int recordCount = new AsyncRecordPipeLoop().run("StreamedETL>Execute>ExtractStep", null, recordPipe, (status) ->
            {
               extractStep.run(runBackendStepInput, runBackendStepOutput);
               return (runBackendStepOutput);
            },
            () -> (consumeRecordsFromPipe(recordPipe, transformStep, loadStep, runBackendStepInput, runBackendStepOutput, loadedRecordList))
         );

         runBackendStepOutput.addValue(StreamedETLProcess.FIELD_RECORD_COUNT, recordCount);
         runBackendStepOutput.setRecords(loadedRecordList);

         /////////////////////
         // commit the work //
         /////////////////////
         if(transaction.isPresent())
         {
            transaction.get().commit();
         }
      }
      catch(Exception e)
      {
         ////////////////////////////////////////////////////////////////////////////////
         // rollback the work, then re-throw the error for up-stream to catch & report //
         ////////////////////////////////////////////////////////////////////////////////
         if(transaction.isPresent())
         {
            transaction.get().rollback();
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
      Integer totalRows = runBackendStepInput.getValueInteger(StreamedETLProcess.FIELD_RECORD_COUNT);
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
      transformStep.setOutputRecordPage(new ArrayList<>());
      transformStep.run(runBackendStepInput, runBackendStepOutput);

      ////////////////////////////////////////////////
      // pass the records through the load function //
      ////////////////////////////////////////////////
      loadStep.setInputRecordPage(transformStep.getOutputRecordPage());
      loadStep.setOutputRecordPage(new ArrayList<>());
      loadStep.run(runBackendStepInput, runBackendStepOutput);

      ///////////////////////////////////////////////////////
      // copy a small number of records to the output list //
      ///////////////////////////////////////////////////////
      int i = 0;
      while(loadedRecordList.size() < PROCESS_OUTPUT_RECORD_LIST_LIMIT && i < loadStep.getOutputRecordPage().size())
      {
         loadedRecordList.add(loadStep.getOutputRecordPage().get(i++));
      }

      currentRowCount += qRecords.size();
      return (qRecords.size());
   }

}
