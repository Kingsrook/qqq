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
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.backend.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Action to generate a report.
 **
 ** At this time (future may change?), this action starts a new thread to run
 ** the query in the backend module.  As records are produced by the query,
 ** they are put into a RecordPipe, which the ReportStreamer pulls from, to write
 ** to the report output stream.  This action will block until the query job
 ** is complete, and the final records have been consumed from the pipe, at which
 ** time the report outputStream can be closed.
 **
 *******************************************************************************/
public class ReportAction
{
   private static final Logger LOG = LogManager.getLogger(ReportAction.class);

   private boolean preExecuteRan = false;
   private Integer countFromPreExecute = null;



   /*******************************************************************************
    ** Validation logic, that will run before the action is executed -- ideally, if
    ** a caller is going to run the execution in a thread, they'd call this method
    ** first, in their thread, to catch any validation errors before they start
    ** the thread (which they may abandon).
    *******************************************************************************/
   public void preExecute(ReportInput reportInput) throws QException
   {
      ActionHelper.validateSession(reportInput);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  backendModule            = qBackendModuleDispatcher.getQBackendModule(reportInput.getBackend());

      ///////////////////////////////////
      // verify field names (if given) //
      ///////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(reportInput.getFieldNames()))
      {
         QTableMetaData table         = reportInput.getTable();
         List<String>   badFieldNames = new ArrayList<>();
         for(String fieldName : reportInput.getFieldNames())
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
      ReportFormat reportFormat = reportInput.getReportFormat();
      verifyCountUnderMax(reportInput, backendModule, reportFormat);

      preExecuteRan = true;
   }



   /*******************************************************************************
    ** Run the report.
    *******************************************************************************/
   public ReportOutput execute(ReportInput reportInput) throws QException
   {
      if(!preExecuteRan)
      {
         /////////////////////////////////////
         // ensure that pre-execute has ran //
         /////////////////////////////////////
         preExecute(reportInput);
      }

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface  backendModule            = qBackendModuleDispatcher.getQBackendModule(reportInput.getBackend());
      QTableMetaData           table                    = reportInput.getTable();

      //////////////////////////
      // set up a query input //
      //////////////////////////
      QueryInterface queryInterface = backendModule.getQueryInterface();
      QueryInput     queryInput     = new QueryInput(reportInput.getInstance());
      queryInput.setSession(reportInput.getSession());
      queryInput.setTableName(reportInput.getTableName());
      queryInput.setFilter(reportInput.getQueryFilter());
      queryInput.setLimit(reportInput.getLimit());

      /////////////////////////////////////////////////////////////////
      // tell this query that it needs to put its output into a pipe //
      /////////////////////////////////////////////////////////////////
      RecordPipe recordPipe = new RecordPipe();
      queryInput.setRecordPipe(recordPipe);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up a report streamer, which will read rows from the pipe, and write formatted report rows to the output stream //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ReportFormat            reportFormat   = reportInput.getReportFormat();
      ReportStreamerInterface reportStreamer = reportFormat.newReportStreamer();
      reportStreamer.start(reportInput);

      //////////////////////////////////////////
      // run the query action as an async job //
      //////////////////////////////////////////
      AsyncJobManager asyncJobManager = new AsyncJobManager();
      String          jobUUID         = asyncJobManager.startJob("ReportAction>QueryAction", (status) -> (queryInterface.execute(queryInput)));
      LOG.info("Started query job [" + jobUUID + "] for report");

      AsyncJobState  asyncJobState   = AsyncJobState.RUNNING;
      AsyncJobStatus asyncJobStatus  = null;
      int            nextSleepMillis = 10;
      long           recordCount     = 0;
      while(asyncJobState.equals(AsyncJobState.RUNNING))
      {
         int recordsConsumed = reportStreamer.takeRecordsFromPipe(recordPipe);
         recordCount += recordsConsumed;

         if(countFromPreExecute != null)
         {
            LOG.info(String.format("Processed %,d of %,d records so far"), recordCount, countFromPreExecute);
         }
         else
         {
            LOG.info(String.format("Processed %,d records so far", recordCount));
         }

         if(recordsConsumed == 0)
         {
            ////////////////////////////////////////////////////////////////////////////
            // do we need to sleep to let the producer work?                          //
            // todo - smarter sleep?  like get notified vs. sleep? eventually a fail? //
            ////////////////////////////////////////////////////////////////////////////
            LOG.info("Read 0 records from pipe, sleeping to give producer a chance to work");
            SleepUtils.sleep(nextSleepMillis, TimeUnit.MILLISECONDS);
            while(recordPipe.countAvailableRecords() == 0)
            {
               nextSleepMillis = Math.min(nextSleepMillis * 2, 1000);
               LOG.info("Still no records in the pipe, so sleeping more [" + nextSleepMillis + "]ms");
               SleepUtils.sleep(nextSleepMillis, TimeUnit.MILLISECONDS);
            }
         }

         nextSleepMillis = 10;

         Optional<AsyncJobStatus> optionalAsyncJobStatus = asyncJobManager.getJobStatus(jobUUID);
         if(optionalAsyncJobStatus.isEmpty())
         {
            /////////////////////////////////////////////////
            // todo - ... maybe some version of try-again? //
            /////////////////////////////////////////////////
            throw (new QException("Could not get status of report query job [" + jobUUID + "]"));
         }
         asyncJobStatus = optionalAsyncJobStatus.get();
         asyncJobState = asyncJobStatus.getState();
      }

      LOG.info("Query job [" + jobUUID + "] for report completed with status: " + asyncJobStatus);

      ///////////////////////////////////////////////////
      // send the final records to the report streamer //
      ///////////////////////////////////////////////////
      int recordsConsumed = reportStreamer.takeRecordsFromPipe(recordPipe);
      recordCount += recordsConsumed;

      //////////////////////////////////////////////////////////////////
      // Critical:  we must close the stream here as our final action //
      //////////////////////////////////////////////////////////////////
      reportStreamer.finish();

      try
      {
         reportInput.getReportOutputStream().close();
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error completing report", e));
      }

      ReportOutput reportOutput = new ReportOutput();
      reportOutput.setRecordCount(recordCount);

      return (reportOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void verifyCountUnderMax(ReportInput reportInput, QBackendModuleInterface backendModule, ReportFormat reportFormat) throws QException
   {
      if(reportFormat.getMaxRows() != null)
      {
         if(reportInput.getLimit() == null || reportInput.getLimit() > reportFormat.getMaxRows())
         {
            CountInterface countInterface = backendModule.getCountInterface();
            CountInput     countInput     = new CountInput(reportInput.getInstance());
            countInput.setSession(reportInput.getSession());
            countInput.setTableName(reportInput.getTableName());
            countInput.setFilter(reportInput.getQueryFilter());
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
