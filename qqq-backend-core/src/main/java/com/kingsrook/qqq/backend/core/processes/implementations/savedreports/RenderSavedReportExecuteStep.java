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


import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportDestination;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class RenderSavedReportExecuteStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         SavedReport savedReport = new SavedReport(runBackendStepInput.getRecords().get(0));
         File tmpFile = File.createTempFile("SavedReport" + savedReport.getId(), ".xlsx", new File("/tmp/"));

         runBackendStepInput.getAsyncJobCallback().updateStatus("Generating Report");

         QReportMetaData reportMetaData = new SavedReportToReportMetaDataAdapter().adapt(savedReport);

         try(FileOutputStream reportOutputStream = new FileOutputStream(tmpFile))
         {
            ReportInput reportInput = new ReportInput();
            reportInput.setReportMetaData(reportMetaData);
            reportInput.setReportDestination(new ReportDestination()
               .withReportFormat(ReportFormat.XLSX) // todo - variable
               .withReportOutputStream(reportOutputStream));

            Map<String, Serializable> values = runBackendStepInput.getValues();
            reportInput.setInputValues(values);

            new GenerateReportAction().execute(reportInput);
         }

         String downloadFileBaseName = getDownloadFileBaseName(runBackendStepInput, savedReport);
         runBackendStepOutput.addValue("downloadFileName", downloadFileBaseName + ".xlsx");
         runBackendStepOutput.addValue("serverFilePath", tmpFile.getCanonicalPath());
      }
      catch(Exception e)
      {
         // todo - render error screen?
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

      downloadFileBaseName = downloadFileBaseName.replaceAll("/", "-");

      return (downloadFileBaseName + " - " + datePart);
   }
}
