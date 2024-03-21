/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.scheduler.quartz.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.NoopTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;


/*******************************************************************************
 **
 *******************************************************************************/
public class ResumeQuartzJobsProcess extends AbstractLoadStep implements MetaDataProducerInterface<QProcessMetaData>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      String tableName = "quartzJobDetails";

      return StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(getClass().getSimpleName())
         .withLabel("Resume Quartz Jobs")
         .withPreviewMessage("This is a preview of the jobs that will be resumed.")
         .withTableName(tableName)
         .withSourceTable(tableName)
         .withDestinationTable(tableName)
         .withExtractStepClass(ExtractViaQueryStep.class)
         .withTransformStepClass(NoopTransformStep.class)
         .withLoadStepClass(getClass())
         .withIcon(new QIcon("play_circle_outline"))
         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("id", QFieldType.LONG),
            new QFieldMetaData("jobName", QFieldType.STRING),
            new QFieldMetaData("jobGroup", QFieldType.STRING)))
         .getProcessMetaData();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         QuartzScheduler instance = QuartzScheduler.getInstance();
         for(QRecord record : runBackendStepInput.getRecords())
         {
            instance.resumeJob(record.getValueString("jobName"), record.getValueString("jobGroup"));
         }
      }
      catch(Exception e)
      {
         throw (new QException("Error resuming jobs", e));
      }
   }

}
