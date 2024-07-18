/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.savedreports;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.RenderedReport;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility for verifying that the RenderReports process works for all report
 ** records stored in the saved reports table.
 **
 ** Meant for use within a unit test, or maybe as part of an instance's boot-up/
 ** validation.
 *******************************************************************************/
public class SavedReportsTableFullVerifier
{
   private static final QLogger LOG = QLogger.getLogger(SavedReportsTableFullVerifier.class);

   private boolean removeRenderedReports = true;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void verify(List<QRecord> savedReportRecordList, String storageTableName) throws QException
   {
      Map<Integer, Exception> caughtExceptions = new LinkedHashMap<>();
      for(QRecord report : savedReportRecordList)
      {
         runReport(report, caughtExceptions, storageTableName);
      }

      //////////////////////////////////
      // log out an exceptions caught //
      //////////////////////////////////
      if(!caughtExceptions.isEmpty())
      {
         for(Map.Entry<Integer, Exception> entry : caughtExceptions.entrySet())
         {
            LOG.info("Caught an exception verifying saved reports", entry.getValue(), logPair("savdReportId", entry.getKey()));
         }
         throw (new QException("Saved Reports Verification failed with " + caughtExceptions.size() + " exception" + StringUtils.plural(caughtExceptions.size())));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runReport(QRecord savedReport, Map<Integer, Exception> caughtExceptions, String storageTableName)
   {
      try
      {
         ///////////////////////
         // render the report //
         ///////////////////////
         RunBackendStepInput  input  = new RunBackendStepInput();
         RunBackendStepOutput output = new RunBackendStepOutput();

         input.addValue(RenderSavedReportMetaDataProducer.FIELD_NAME_REPORT_FORMAT, ReportFormat.XLSX.name());
         input.addValue(RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_TABLE_NAME, storageTableName);
         input.setRecords(List.of(savedReport));

         new RenderSavedReportExecuteStep().run(input, output);
         Exception exception = output.getException();
         if(exception != null)
         {
            throw (exception);
         }

         //////////////////////////////////////////
         // clean up the report, if so requested //
         //////////////////////////////////////////
         if(removeRenderedReports)
         {
            new DeleteAction().execute(new DeleteInput(RenderedReport.TABLE_NAME).withPrimaryKey(output.getValue("renderedReportId")));
         }
      }
      catch(Exception e)
      {
         caughtExceptions.put(savedReport.getValueInteger("id"), e);
      }
   }



   /*******************************************************************************
    ** Getter for removeRenderedReports
    *******************************************************************************/
   public boolean getRemoveRenderedReports()
   {
      return (this.removeRenderedReports);
   }



   /*******************************************************************************
    ** Setter for removeRenderedReports
    *******************************************************************************/
   public void setRemoveRenderedReports(boolean removeRenderedReports)
   {
      this.removeRenderedReports = removeRenderedReports;
   }



   /*******************************************************************************
    ** Fluent setter for removeRenderedReports
    *******************************************************************************/
   public SavedReportsTableFullVerifier withRemoveRenderedReports(boolean removeRenderedReports)
   {
      this.removeRenderedReports = removeRenderedReports;
      return (this);
   }

}
