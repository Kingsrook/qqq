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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobManager;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobState;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobStatus;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLExtractFunction;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLLoadFunction;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLTransformFunction;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Backend step to do a streamed ETL
 *******************************************************************************/
public class StreamedETLBackendStep implements BackendStep
{
   private static final Logger LOG = LogManager.getLogger(StreamedETLBackendStep.class);

   private static final int TIMEOUT_AFTER_NO_RECORDS_MS = 10 * 60 * 1000;

   private static final int MAX_SLEEP_MS  = 1000;
   private static final int INIT_SLEEP_MS = 10;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QBackendTransaction transaction = openTransaction(runBackendStepInput);

      try
      {
         RecordPipe              recordPipe              = new RecordPipe();
         BasicETLExtractFunction basicETLExtractFunction = new BasicETLExtractFunction();
         basicETLExtractFunction.setRecordPipe(recordPipe);

         //////////////////////////////////////////
         // run the query action as an async job //
         //////////////////////////////////////////
         AsyncJobManager asyncJobManager = new AsyncJobManager();
         String queryJobUUID = asyncJobManager.startJob("ReportAction>QueryAction", (status) ->
         {
            basicETLExtractFunction.run(runBackendStepInput, runBackendStepOutput);
            return (runBackendStepOutput);
         });
         LOG.info("Started query job [" + queryJobUUID + "] for report");

         AsyncJobState  queryJobState  = AsyncJobState.RUNNING;
         AsyncJobStatus asyncJobStatus = null;

         long recordCount           = 0;
         int  nextSleepMillis       = INIT_SLEEP_MS;
         long lastReceivedRecordsAt = System.currentTimeMillis();
         long jobStartTime          = System.currentTimeMillis();

         while(queryJobState.equals(AsyncJobState.RUNNING))
         {
            if(recordPipe.countAvailableRecords() == 0)
            {
               ///////////////////////////////////////////////////////////
               // if the pipe is empty, sleep to let the producer work. //
               // todo - smarter sleep?  like get notified vs. sleep?   //
               ///////////////////////////////////////////////////////////
               LOG.info("No records are available in the pipe. Sleeping [" + nextSleepMillis + "] ms to give producer a chance to work");
               SleepUtils.sleep(nextSleepMillis, TimeUnit.MILLISECONDS);
               nextSleepMillis = Math.min(nextSleepMillis * 2, MAX_SLEEP_MS);

               long timeSinceLastReceivedRecord = System.currentTimeMillis() - lastReceivedRecordsAt;
               if(timeSinceLastReceivedRecord > TIMEOUT_AFTER_NO_RECORDS_MS)
               {
                  throw (new QException("Query action appears to have stopped producing records (last record received " + timeSinceLastReceivedRecord + " ms ago)."));
               }
            }
            else
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////////////
               // if the pipe has records, consume them.  reset the sleep timer so if we sleep again it'll be short. //
               ////////////////////////////////////////////////////////////////////////////////////////////////////////
               lastReceivedRecordsAt = System.currentTimeMillis();
               nextSleepMillis = INIT_SLEEP_MS;

               recordCount += consumeRecordsFromPipe(recordPipe, runBackendStepInput, runBackendStepOutput, transaction);

               LOG.info(String.format("Processed %,d records so far", recordCount));
            }

            ////////////////////////////////////
            // refresh the query job's status //
            ////////////////////////////////////
            Optional<AsyncJobStatus> optionalAsyncJobStatus = asyncJobManager.getJobStatus(queryJobUUID);
            if(optionalAsyncJobStatus.isEmpty())
            {
               /////////////////////////////////////////////////
               // todo - ... maybe some version of try-again? //
               /////////////////////////////////////////////////
               throw (new QException("Could not get status of report query job [" + queryJobUUID + "]"));
            }
            asyncJobStatus = optionalAsyncJobStatus.get();
            queryJobState = asyncJobStatus.getState();
         }

         LOG.info("Query job [" + queryJobUUID + "] for ETL completed with status: " + asyncJobStatus);

         //////////////////////////////////////////////////////
         // send the final records to transform & load steps //
         //////////////////////////////////////////////////////
         recordCount += consumeRecordsFromPipe(recordPipe, runBackendStepInput, runBackendStepOutput, transaction);

         /////////////////////
         // commit the work //
         /////////////////////
         transaction.commit();

         long reportEndTime = System.currentTimeMillis();
         LOG.info(String.format("Processed %,d records", recordCount)
            + String.format(" at end of ETL job in %,d ms (%.2f records/second).", (reportEndTime - jobStartTime), 1000d * (recordCount / (.001d + (reportEndTime - jobStartTime)))));

         runBackendStepOutput.addValue(StreamedETLProcess.FIELD_RECORD_COUNT, recordCount);
      }
      catch(Exception e)
      {
         ////////////////////////////////////////////////////////////////////////////////
         // rollback the work, then re-throw the error for up-stream to catch & report //
         ////////////////////////////////////////////////////////////////////////////////
         transaction.rollback();
         throw (e);
      }
      finally
      {
         ////////////////////////////////////////////////////////////
         // always close our transactions (e.g., jdbc connections) //
         ////////////////////////////////////////////////////////////
         transaction.close();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QBackendTransaction openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      InsertInput insertInput = new InsertInput(runBackendStepInput.getInstance());

      insertInput.setSession(runBackendStepInput.getSession());
      insertInput.setTableName(runBackendStepInput.getValueString(BasicETLProcess.FIELD_DESTINATION_TABLE));

      return new InsertAction().openTransaction(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, QBackendTransaction transaction) throws QException
   {
      List<QRecord> qRecords = recordPipe.consumeAvailableRecords();

      preTransform(qRecords, runBackendStepInput, runBackendStepOutput);

      runBackendStepInput.setRecords(qRecords);
      new BasicETLTransformFunction().run(runBackendStepInput, runBackendStepOutput);

      postTransform(qRecords, runBackendStepInput, runBackendStepOutput);

      runBackendStepInput.setRecords(runBackendStepOutput.getRecords());
      BasicETLLoadFunction basicETLLoadFunction = new BasicETLLoadFunction();
      basicETLLoadFunction.setTransaction(transaction);
      basicETLLoadFunction.run(runBackendStepInput, runBackendStepOutput);

      return (qRecords.size());
   }



   /*******************************************************************************
    ** Customization point for subclasses of this step.
    *******************************************************************************/
   protected void preTransform(List<QRecord> qRecords, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }



   /*******************************************************************************
    ** Customization point for subclasses of this step.
    *******************************************************************************/
   protected void postTransform(List<QRecord> qRecords, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput)
   {
      ////////////////////////
      // noop in base class //
      ////////////////////////
   }

}
