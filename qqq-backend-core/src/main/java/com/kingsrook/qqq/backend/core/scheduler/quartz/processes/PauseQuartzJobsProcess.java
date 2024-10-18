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
import java.util.function.BiFunction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.NoopTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;


/*******************************************************************************
 **
 *******************************************************************************/
public class PauseQuartzJobsProcess extends AbstractLoadStep implements MetaDataProducerInterface<MetaDataProducerMultiOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance qInstance) throws QException
   {
      BiFunction<String, String, QProcessMetaData> processMaker = (String tableName, String label) ->
         StreamedETLWithFrontendProcess.processMetaDataBuilder()
            .withName(getClass().getSimpleName())
            .withLabel(label)
            .withPreviewMessage("This is a preview of the jobs that will be paused.")
            .withTableName(tableName)
            .withSourceTable(tableName)
            .withDestinationTable(tableName)
            .withExtractStepClass(ExtractViaQueryStep.class)
            .withTransformStepClass(NoopTransformStep.class)
            .withLoadStepClass(getClass())
            .withIcon(new QIcon("pause_circle_outline"))
            .withReviewStepRecordFields(List.of(
               new QFieldMetaData("id", QFieldType.LONG),
               new QFieldMetaData("jobName", QFieldType.STRING),
               new QFieldMetaData("jobGroup", QFieldType.STRING),
               new QFieldMetaData("description", QFieldType.STRING)))
            .getProcessMetaData()
            .withPermissionRules(new QPermissionRules().withPermissionBaseName(getClass().getSimpleName()));

      MetaDataProducerMultiOutput output = new MetaDataProducerMultiOutput();
      output.add(processMaker.apply("quartzJobDetails", "Pause Quartz Jobs"));
      output.add(processMaker.apply("quartzTriggers", "Pause Quartz Triggers").withName(getClass().getSimpleName() + "ForTriggers"));
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         QuartzScheduler instance = QuartzScheduler.getInstance();
         for(QRecord record : runBackendStepInput.getRecords())
         {
            instance.pauseJob(record.getValueString("jobName"), record.getValueString("jobGroup"));
         }
      }
      catch(Exception e)
      {
         throw (new QException("Error pausing jobs", e));
      }
   }

}
