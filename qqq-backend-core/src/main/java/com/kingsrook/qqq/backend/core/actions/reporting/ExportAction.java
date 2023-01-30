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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobManager;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobState;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobStatus;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Action to generate an export from a table
 **
 ** At this time (future may change?), this action starts a new thread to run
 ** the query in the backend module.  As records are produced by the query,
 ** they are put into a RecordPipe, which the ReportStreamer pulls from, to write
 ** to the report output stream.  This action will block until the query job
 ** is complete, and the final records have been consumed from the pipe, at which
 ** time the report outputStream can be closed.
 **
 *******************************************************************************/
public class ExportAction
{
   private static final QLogger LOG = QLogger.getLogger(ExportAction.class);

   private boolean preExecuteRan       = false;
   private Integer countFromPreExecute = null;

   private static final int TIMEOUT_AFTER_NO_RECORDS_MS = 10 * 60 * 1000;
   private static final int MAX_SLEEP_MS                = 1000;
   private static final int INIT_SLEEP_MS               = 10;



   /*******************************************************************************
    ** Validation logic, that will run before the action is executed -- ideally, if
    ** a caller is going to run the execution in a thread, they'd call this method
    ** first, in their thread, to catch any validation errors before they start
    ** the thread (which they may abandon).
    *******************************************************************************/
   public void preExecute(ExportInput exportInput) throws QException
   {
      ActionHelper.validateSession(exportInput);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  backendModule            = qBackendModuleDispatcher.getQBackendModule(exportInput.getBackend());

      ///////////////////////////////////
      // verify field names (if given) //
      ///////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(exportInput.getFieldNames()))
      {
         QTableMetaData table         = exportInput.getTable();
         List<String>   badFieldNames = new ArrayList<>();
         for(String fieldName : exportInput.getFieldNames())
         {
            try
            {
               table.getField(fieldName);
            }
            catch(IllegalArgumentException iae)
            {
               badFieldNames.add(fieldName);
            }
         }

         if(!badFieldNames.isEmpty())
         {
            throw (new QUserFacingException(badFieldNames.size() == 1
               ? ("Field name " + badFieldNames.get(0) + " was not found on the " + table.getLabel() + " table.")
               : ("Fields names " + StringUtils.joinWithCommasAndAnd(badFieldNames) + " were not found on the " + table.getLabel() + " table.")));
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check if this report format has a max-rows limit -- if so, do a count to verify we're under the limit //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      ReportFormat reportFormat = exportInput.getReportFormat();
      verifyCountUnderMax(exportInput, backendModule, reportFormat);

      preExecuteRan = true;
   }



   /*******************************************************************************
    ** Run the report.
    *******************************************************************************/
   public ExportOutput execute(ExportInput exportInput) throws QException
   {
      if(!preExecuteRan)
      {
         /////////////////////////////////////
         // ensure that pre-execute has ran //
         /////////////////////////////////////
         preExecute(exportInput);
      }

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  backendModule            = qBackendModuleDispatcher.getQBackendModule(exportInput.getBackend());

      //////////////////////////
      // set up a query input //
      //////////////////////////
      QueryAction queryAction = new QueryAction();
      QueryInput  queryInput  = new QueryInput();
      queryInput.setTableName(exportInput.getTableName());
      queryInput.setFilter(exportInput.getQueryFilter());
      queryInput.setLimit(exportInput.getLimit());
      queryInput.setShouldTranslatePossibleValues(true);

      /////////////////////////////////////////////////////////////////
      // tell this query that it needs to put its output into a pipe //
      /////////////////////////////////////////////////////////////////
      RecordPipe recordPipe = new BufferedRecordPipe(500);
      queryInput.setRecordPipe(recordPipe);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up a report streamer, which will read rows from the pipe, and write formatted report rows to the output stream //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ReportFormat            reportFormat   = exportInput.getReportFormat();
      ExportStreamerInterface reportStreamer = reportFormat.newReportStreamer();
      List<QFieldMetaData>    fields         = getFields(exportInput);
      reportStreamer.start(exportInput, fields, "Sheet 1");

      //////////////////////////////////////////
      // run the query action as an async job //
      //////////////////////////////////////////
      AsyncJobManager asyncJobManager = new AsyncJobManager();
      String          queryJobUUID    = asyncJobManager.startJob("ReportAction>QueryAction", (status) -> (queryAction.execute(queryInput)));
      LOG.info("Started query job [" + queryJobUUID + "] for report");

      AsyncJobState  queryJobState  = AsyncJobState.RUNNING;
      AsyncJobStatus asyncJobStatus = null;

      long recordCount           = 0;
      int  nextSleepMillis       = INIT_SLEEP_MS;
      long lastReceivedRecordsAt = System.currentTimeMillis();
      long reportStartTime       = System.currentTimeMillis();

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
               throw (new QReportingException("Query action appears to have stopped producing records (last record received " + timeSinceLastReceivedRecord + " ms ago)."));
            }
         }
         else
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if the pipe has records, consume them.  reset the sleep timer so if we sleep again it'll be short. //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            lastReceivedRecordsAt = System.currentTimeMillis();
            nextSleepMillis = INIT_SLEEP_MS;

            List<QRecord> records = recordPipe.consumeAvailableRecords();
            processRecords(reportStreamer, fields, records);
            recordCount += records.size();

            LOG.info(countFromPreExecute != null
               ? String.format("Processed %,d of %,d records so far", recordCount, countFromPreExecute)
               : String.format("Processed %,d records so far", recordCount));
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

      LOG.info("Query job [" + queryJobUUID + "] for report completed with status: " + asyncJobStatus);

      ///////////////////////////////////////////////////
      // send the final records to the report streamer //
      ///////////////////////////////////////////////////
      List<QRecord> records = recordPipe.consumeAvailableRecords();
      processRecords(reportStreamer, fields, records);
      recordCount += records.size();

      long reportEndTime = System.currentTimeMillis();
      LOG.info((countFromPreExecute != null
         ? String.format("Processed %,d of %,d records", recordCount, countFromPreExecute)
         : String.format("Processed %,d records", recordCount))
         + String.format(" at end of report in %,d ms (%.2f records/second).", (reportEndTime - reportStartTime), 1000d * (recordCount / (.001d + (reportEndTime - reportStartTime)))));

      //////////////////////////////////////////////////////////////////
      // Critical:  we must close the stream here as our final action //
      //////////////////////////////////////////////////////////////////
      reportStreamer.finish();

      try
      {
         exportInput.getReportOutputStream().close();
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error completing report", e));
      }

      ExportOutput exportOutput = new ExportOutput();
      exportOutput.setRecordCount(recordCount);

      return (exportOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void processRecords(ExportStreamerInterface reportStreamer, List<QFieldMetaData> fields, List<QRecord> records) throws QReportingException
   {
      for(QFieldMetaData field : fields)
      {
         if(field.getName().endsWith(":possibleValueLabel"))
         {
            String effectiveFieldName = field.getName().replace(":possibleValueLabel", "");
            for(QRecord record : records)
            {
               String displayValue = record.getDisplayValue(effectiveFieldName);
               record.setValue(field.getName(), displayValue);
            }
         }
      }

      reportStreamer.addRecords(records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QFieldMetaData> getFields(ExportInput exportInput)
   {
      List<QFieldMetaData> fieldList;
      QTableMetaData       table = exportInput.getTable();
      if(exportInput.getFieldNames() != null)
      {
         fieldList = exportInput.getFieldNames().stream().map(table::getField).toList();
      }
      else
      {
         fieldList = new ArrayList<>(table.getFields().values());
      }

      //////////////////////////////////////////
      // add fields for possible value labels //
      //////////////////////////////////////////
      List<QFieldMetaData> returnList = new ArrayList<>();
      for(QFieldMetaData field : fieldList)
      {
         returnList.add(field);
         if(StringUtils.hasContent(field.getPossibleValueSourceName()))
         {
            returnList.add(new QFieldMetaData(field.getName() + ":possibleValueLabel", QFieldType.STRING).withLabel(field.getLabel() + " Name"));
         }
      }

      return (returnList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void verifyCountUnderMax(ExportInput exportInput, QBackendModuleInterface backendModule, ReportFormat reportFormat) throws QException
   {
      if(reportFormat.getMaxCols() != null)
      {
         List<QFieldMetaData> fields = getFields(exportInput);
         if(fields.size() > reportFormat.getMaxCols())
         {
            throw (new QUserFacingException("The requested report would include more columns ("
               + String.format("%,d", fields.size()) + ") than the maximum allowed ("
               + String.format("%,d", reportFormat.getMaxCols()) + ") for the selected file format (" + reportFormat + ")."));
         }
      }

      if(reportFormat.getMaxRows() != null)
      {
         if(exportInput.getLimit() == null || exportInput.getLimit() > reportFormat.getMaxRows())
         {
            CountInterface countInterface = backendModule.getCountInterface();
            CountInput     countInput     = new CountInput();
            countInput.setTableName(exportInput.getTableName());
            countInput.setFilter(exportInput.getQueryFilter());
            CountOutput countOutput = countInterface.execute(countInput);
            countFromPreExecute = countOutput.getCount();
            if(countFromPreExecute > reportFormat.getMaxRows())
            {
               throw (new QUserFacingException("The requested report would include more rows ("
                  + String.format("%,d", countFromPreExecute) + ") than the maximum allowed ("
                  + String.format("%,d", reportFormat.getMaxRows()) + ") for the selected file format (" + reportFormat + ")."));
            }
         }
      }
   }

}
