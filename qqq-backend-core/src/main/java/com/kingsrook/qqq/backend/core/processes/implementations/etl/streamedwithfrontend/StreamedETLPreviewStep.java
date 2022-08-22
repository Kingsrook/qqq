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


/*******************************************************************************
 ** Backend step to do a preview of a full streamed ETL job
 *******************************************************************************/
public class StreamedETLPreviewStep extends BaseStreamedETLStep implements BackendStep
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   @SuppressWarnings("checkstyle:indentation")
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      RecordPipe              recordPipe      = new RecordPipe();
      AbstractExtractFunction extractFunction = getExtractFunction(runBackendStepInput);
      extractFunction.setLimit(IN_MEMORY_RECORD_LIMIT); // todo - process field?
      extractFunction.setRecordPipe(recordPipe);

      AbstractTransformFunction transformFunction = getTransformFunction(runBackendStepInput);

      List<QRecord> transformedRecordList = new ArrayList<>();
      new AsyncRecordPipeLoop().run("StreamedETL>Preview>ExtractFunction", IN_MEMORY_RECORD_LIMIT, recordPipe, (status) ->
         {
            extractFunction.run(runBackendStepInput, runBackendStepOutput);
            return (runBackendStepOutput);
         },
         () -> (consumeRecordsFromPipe(recordPipe, transformFunction, runBackendStepInput, runBackendStepOutput, transformedRecordList))
      );

      runBackendStepOutput.setRecords(transformedRecordList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private int consumeRecordsFromPipe(RecordPipe recordPipe, AbstractTransformFunction transformFunction, RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput, List<QRecord> transformedRecordList) throws QException
   {
      ///////////////////////////////////
      // get the records from the pipe //
      ///////////////////////////////////
      List<QRecord> qRecords = recordPipe.consumeAvailableRecords();

      /////////////////////////////////////////////////////
      // pass the records through the transform function //
      /////////////////////////////////////////////////////
      transformFunction.setInputRecordPage(qRecords);
      transformFunction.run(runBackendStepInput, runBackendStepOutput);

      ////////////////////////////////////////////////////
      // add the transformed records to the output list //
      ////////////////////////////////////////////////////
      transformedRecordList.addAll(transformFunction.getOutputRecordPage());

      return (qRecords.size());
   }

}
