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


import java.io.OutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportDestination;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.RenderedReport;
import com.kingsrook.qqq.backend.core.model.savedreports.RenderedReportStatus;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Process step to actually execute rendering a saved report.
 *******************************************************************************/
public class RenderSavedReportExecuteStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(RenderSavedReportExecuteStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QRecord renderedReportRecord = null;

      try
      {
         ////////////////////////////////
         // read inputs, set up params //
         ////////////////////////////////
         String       storageTableName     = runBackendStepInput.getValueString(RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_TABLE_NAME);
         ReportFormat reportFormat         = ReportFormat.fromString(runBackendStepInput.getValueString(RenderSavedReportMetaDataProducer.FIELD_NAME_REPORT_FORMAT));
         SavedReport  savedReport          = new SavedReport(runBackendStepInput.getRecords().get(0));
         String       downloadFileBaseName = getDownloadFileBaseName(runBackendStepInput, savedReport);
         String       storageReference     = LocalDate.now() + "/" + LocalTime.now().toString().replaceAll(":", "").replaceFirst("\\..*", "") + "/" + UUID.randomUUID() + "/" + downloadFileBaseName + "." + reportFormat.getExtension();
         OutputStream outputStream         = new StorageAction().createOutputStream(new StorageInput(storageTableName).withReference(storageReference));

         LOG.info("Starting to render a report", logPair("savedReportId", savedReport.getId()), logPair("tableName", savedReport.getTableName()), logPair("storageReference", storageReference));
         runBackendStepInput.getAsyncJobCallback().updateStatus("Generating Report");

         //////////////////////////////////////////////////////////////////
         // insert a rendered-report record indicating that it's running //
         //////////////////////////////////////////////////////////////////
         renderedReportRecord = new InsertAction().execute(new InsertInput(RenderedReport.TABLE_NAME).withRecordEntity(new RenderedReport()
            .withSavedReportId(savedReport.getId())
            .withStartTime(Instant.now())
            // todo .withJobUuid(runBackendStepInput.get)
            .withRenderedReportStatusId(RenderedReportStatus.RUNNING.getId())
            .withReportFormat(ReportFormatPossibleValueEnum.valueOf(reportFormat.name()).getPossibleValueId())
         )).getRecords().get(0);

         ////////////////////////////////////////////////////////////////////////////////////////////
         // convert the report record to report meta-data, which the GenerateReportAction works on //
         ////////////////////////////////////////////////////////////////////////////////////////////
         QReportMetaData reportMetaData = new SavedReportToReportMetaDataAdapter().adapt(savedReport, reportFormat);

         /////////////////////////////////////
         // setup & run the generate action //
         /////////////////////////////////////
         ReportInput reportInput = new ReportInput();
         reportInput.setAsyncJobCallback(runBackendStepInput.getAsyncJobCallback());
         reportInput.setReportMetaData(reportMetaData);
         reportInput.setReportDestination(new ReportDestination()
            .withReportFormat(reportFormat)
            .withReportOutputStream(outputStream));

         Map<String, Serializable> values = runBackendStepInput.getValues();
         reportInput.setInputValues(values);

         ReportOutput reportOutput = new GenerateReportAction().execute(reportInput);

         ///////////////////////////////////
         // update record to show success //
         ///////////////////////////////////
         new UpdateAction().execute(new UpdateInput(RenderedReport.TABLE_NAME).withRecord(new QRecord()
            .withValue("id", renderedReportRecord.getValue("id"))
            .withValue("resultPath", storageReference)
            .withValue("renderedReportStatusId", RenderedReportStatus.COMPLETE.getPossibleValueId())
            .withValue("endTime", Instant.now())
            .withValue("rowCount", reportOutput.getTotalRecordCount())
         ));

         runBackendStepOutput.addValue("downloadFileName", downloadFileBaseName + "." + reportFormat.getExtension());
         runBackendStepOutput.addValue("storageTableName", storageTableName);
         runBackendStepOutput.addValue("storageReference", storageReference);
         LOG.info("Completed rendering a report", logPair("savedReportId", savedReport.getId()), logPair("tableName", savedReport.getTableName()), logPair("storageReference", storageReference), logPair("rowCount", reportOutput.getTotalRecordCount()));
      }
      catch(Exception e)
      {
         if(renderedReportRecord != null)
         {
            new UpdateAction().execute(new UpdateInput(RenderedReport.TABLE_NAME).withRecord(new QRecord()
               .withValue("id", renderedReportRecord.getValue("id"))
               .withValue("renderedReportStatusId", RenderedReportStatus.FAILED.getPossibleValueId())
               .withValue("endTime", Instant.now())
               .withValue("errorMessage", ExceptionUtils.concatenateMessagesFromChain(e))
            ));
         }

         LOG.warn("Error rendering saved report", e);
         throw (e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getDownloadFileBaseName(RunBackendStepInput runBackendStepInput, SavedReport report)
   {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm").withZone(ZoneId.systemDefault());
      String            datePart  = formatter.format(Instant.now());

      String downloadFileBaseName = runBackendStepInput.getValueString("downloadFileBaseName");
      if(!StringUtils.hasContent(downloadFileBaseName))
      {
         downloadFileBaseName = report.getLabel();
      }

      //////////////////////////////////////////////////
      // these chars have caused issues, so, disallow //
      //////////////////////////////////////////////////
      downloadFileBaseName = downloadFileBaseName.replaceAll("/", "-").replaceAll(",", "_");

      return (downloadFileBaseName + " - " + datePart);
   }
}
