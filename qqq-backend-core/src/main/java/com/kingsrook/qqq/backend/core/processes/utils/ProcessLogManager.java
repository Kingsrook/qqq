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

package com.kingsrook.qqq.backend.core.processes.utils;


import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.processlogs.ProcessLog;
import com.kingsrook.qqq.backend.core.model.processlogs.ProcessLogRecordInt;
import com.kingsrook.qqq.backend.core.model.processlogs.ProcessLogStep;
import com.kingsrook.qqq.backend.core.model.processlogs.ProcessLogSummary;
import com.kingsrook.qqq.backend.core.model.processlogs.ProcessLogValue;
import com.kingsrook.qqq.backend.core.model.processlogs.QQQProcessAccessor;
import com.kingsrook.qqq.backend.core.model.session.QQQUserAccessor;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableAccessor;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** class responsible for building processLog and related records
 *******************************************************************************/
public class ProcessLogManager
{
   private static final QLogger LOG = QLogger.getLogger(ProcessLogManager.class);

   private Integer processLogId;

   private List<ProcessLogStep> processLogSteps = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public void beginProcessLog(RunBackendStepInput runBackendStepInput)
   {
      try
      {
         String  processName = runBackendStepInput.getProcessName();
         Integer processId   = QQQProcessAccessor.getProcessId(processName);
         Integer userId      = QQQUserAccessor.getCurrentUserId();

         InsertOutput insertOutput = new InsertAction().execute(new InsertInput(ProcessLog.TABLE_NAME).withRecordEntity(new ProcessLog()
            .withQqqProcessId(processId)
            .withQqqUserId(userId)
            .withStartTime(Instant.now())
         ));

         QRecord insertedRecord = insertOutput.getRecords().get(0);
         if(CollectionUtils.nullSafeHasContents(insertedRecord.getErrors()))
         {
            LOG.warn("Error storing process log records", logPair("errors", insertedRecord.getErrors()), logPair("processName", runBackendStepInput.getValue("processName")));
         }
         else
         {
            processLogId = insertedRecord.getValueInteger("id");
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error storing process log records", e, logPair("processName", runBackendStepInput.getValue("processName")));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void finishProcessLog(RunBackendStepInput runBackendStepInput, ArrayList<ProcessSummaryLineInterface> processSummaryLines, Optional<Exception> exception)
   {
      try
      {
         String  sourceTableName = runBackendStepInput.getValueString(StreamedETLProcess.FIELD_SOURCE_TABLE);
         Integer tableId         = QQQTableAccessor.getTableId(sourceTableName);

         if(this.processLogId == null)
         {
            LOG.info("Call to finishProcessLog, but without a processLogId.");
         }

         Set<String> valueNamesToIgnore = new HashSet<>();
         valueNamesToIgnore.add("basepullReadyToUpdateTimestamp");
         valueNamesToIgnore.add("basepullTimestampField");
         valueNamesToIgnore.add("extract");
         valueNamesToIgnore.add("fetchHeavyFields");
         valueNamesToIgnore.add("load");
         valueNamesToIgnore.add("previewMessage");
         valueNamesToIgnore.add("processResults");
         valueNamesToIgnore.add("supportsFullValidation");
         valueNamesToIgnore.add("transactionLevel");
         valueNamesToIgnore.add("transform");

         ////////////////////////////////////////////////////////////////
         // build processLogValue records out of values in the process //
         ////////////////////////////////////////////////////////////////
         List<QRecord> processLogValues = new ArrayList<>();
         for(Map.Entry<String, Serializable> entry : runBackendStepInput.getValues().entrySet())
         {
            if(valueNamesToIgnore.contains(entry.getKey()))
            {
               continue;
            }

            processLogValues.add(new ProcessLogValue()
               .withName(entry.getKey())
               .withValue(ValueUtils.getValueAsString(entry.getValue()))
               .toQRecord());
         }

         /////////////////////////////////////////////////////////////////////
         // look at the process summary lines - build processLogSummary and //
         // processLogRecordInt records from them                           //
         /////////////////////////////////////////////////////////////////////
         List<QRecord> processLogRecordInts = new ArrayList<>();
         List<QRecord> processLogSummaries  = new ArrayList<>();
         for(ProcessSummaryLineInterface psl : CollectionUtils.nonNullList(processSummaryLines))
         {
            String message = null;
            if(psl instanceof ProcessSummaryLine processSummaryLine)
            {
               message = String.format("%,d", processSummaryLine.getCount()) + " " + processSummaryLine.getMessage();

               /////////////////////////////////////////////////////////////////////////////
               // for ProcessSummaryLine type, grab its primary keys to build record-ints //
               /////////////////////////////////////////////////////////////////////////////
               for(Serializable primaryKey : CollectionUtils.nonNullList(processSummaryLine.getPrimaryKeys()))
               {
                  processLogRecordInts.add(new ProcessLogRecordInt()
                     .withMessage(processSummaryLine.getMessage())
                     .withStatus(processSummaryLine.getStatus().toString())
                     .withRecordId(ValueUtils.getValueAsString(primaryKey))
                     .withQqqTableId(tableId)
                     .toQRecord());
               }
            }
            else
            {
               LOG.warn("need other types supported here - or method in the interface (yeah, that!)");
            }

            processLogSummaries.add(new ProcessLogSummary()
               .withStatus(psl.getStatus().toString())
               .withMessage(message)
               .toQRecord());
         }

         exception.ifPresent(e -> processLogValues.add(new ProcessLogValue()
            .withName("topLevelException")
            .withValue(e.getMessage())
            .toQRecord()));

         new UpdateAction().execute(new UpdateInput(ProcessLog.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", processLogId)
            .withValue("endTime", Instant.now())
            .withAssociatedRecords("processLogValues", processLogValues)
            .withAssociatedRecords("processLogRecordInts", processLogRecordInts)
            .withAssociatedRecords("processLogSummaries", processLogSummaries)
            .withAssociatedRecords("processLogSteps", processLogSteps.stream().map(pls -> pls.toQRecord()).toList())
         ));

      }
      catch(Exception e)
      {
         LOG.warn("Error storing process log records", e, logPair("processName", runBackendStepInput.getValue("processName")));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addStep(String name, Instant start, Instant end, Integer recordCount)
   {
      processLogSteps.add(new ProcessLogStep()
         .withName(name)
         .withStartTime(start)
         .withEndTime(end)
         .withRunTimeMillis(start == null || end == null ? null : (int) (end.toEpochMilli() - start.toEpochMilli()))
         .withRecordCount(recordCount)
      );
   }

   /*******************************************************************************
    ** not sure if we want this - right now, getting it from summaries, but ... ?
    *******************************************************************************/
   /*
   public void recordProcessRecords(List<QRecord> qRecords)
   {
      try
      {
         List<QRecord> processLogRecordInts = new ArrayList<>();
         for(QRecord qRecord : qRecords)
         {
            processLogRecordInts.add(new ProcessLogRecordInt()
               .withProcessLogId(processLogId)
               .withRecordId()
               .toQRecord()
            );
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error storing processLogRecord records", e);
      }
   }
   */

}
