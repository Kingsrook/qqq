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
 ** Backend step to do a execute a streamed ETL job
 *******************************************************************************/
public class StreamedETLExecuteStep extends BaseStreamedETLStep implements BackendStep
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @SuppressWarnings("checkstyle:indentation")
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QBackendTransaction transaction = null;

      try
      {
         ///////////////////////////////////////////////////////
         // set up the extract, transform, and load functions //
         ///////////////////////////////////////////////////////
         RecordPipe              recordPipe      = new RecordPipe();
         AbstractExtractFunction extractFunction = getExtractFunction(runBackendStepInput);
         extractFunction.setRecordPipe(recordPipe);

         AbstractTransformFunction transformFunction = getTransformFunction(runBackendStepInput);
         AbstractLoadFunction      loadFunction      = getLoadFunction(runBackendStepInput);

         transaction = loadFunction.openTransaction(runBackendStepInput);

         List<QRecord> loadedRecordList = new ArrayList<>();
         int recordCount = new AsyncRecordPipeLoop().run("StreamedETL>Execute>ExtractFunction", null, recordPipe, (status) ->
            {
               extractFunction.run(runBackendStepInput, runBackendStepOutput);
               return (runBackendStepOutput);
            },
            () -> (consumeRecordsFromPipe(recordPipe, transformFunction, loadFunction, runBackendStepInput, runBackendStepOutput, loadedRecordList))
         );

         runBackendStepOutput.addValue(StreamedETLProcess.FIELD_RECORD_COUNT, recordCount);
         runBackendStepOutput.setRecords(loadedRecordList);

         /////////////////////
         // commit the work //
         /////////////////////
         if(transaction != null)
         {
            transaction.commit();
         }
      }
      catch(Exception e)
      {
         ////////////////////////////////////////////////////////////////////////////////
         // rollback the work, then re-throw the error for up-stream to catch & report //
         ////////////////////////////////////////////////////////////////////////////////
         if(transaction != null)
         {
            transaction.rollback();
         }
         throw (e);
      }
      finally
      {
         ////////////////////////////////////////////////////////////
         // always close our transactions (e.g., jdbc connections) //
         ////////////////////////////////////////////////////////////
         if(transaction != null)
         {
            transaction.close();
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, AbstractTransformFunction transformFunction, AbstractLoadFunction loadFunction, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<QRecord> loadedRecordList) throws QException
   {
      ///////////////////////////////////
      // get the records from the pipe //
      ///////////////////////////////////
      List<QRecord> qRecords = recordPipe.consumeAvailableRecords();

      /////////////////////////////////////////////////////
      // pass the records through the transform function //
      /////////////////////////////////////////////////////
      transformFunction.setInputRecordPage(qRecords);
      transformFunction.setOutputRecordPage(new ArrayList<>());
      transformFunction.run(runBackendStepInput, runBackendStepOutput);

      ////////////////////////////////////////////////
      // pass the records through the load function //
      ////////////////////////////////////////////////
      loadFunction.setInputRecordPage(transformFunction.getOutputRecordPage());
      loadFunction.setOutputRecordPage(new ArrayList<>());
      loadFunction.run(runBackendStepInput, runBackendStepOutput);

      ///////////////////////////////////////////////////////
      // copy a small number of records to the output list //
      ///////////////////////////////////////////////////////
      int i = 0;
      while(loadedRecordList.size() < IN_MEMORY_RECORD_LIMIT && i < loadFunction.getOutputRecordPage().size())
      {
         loadedRecordList.add(loadFunction.getOutputRecordPage().get(i++));
      }

      return (qRecords.size());
   }

}
