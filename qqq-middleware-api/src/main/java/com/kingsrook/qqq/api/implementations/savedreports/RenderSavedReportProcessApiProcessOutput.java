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

package com.kingsrook.qqq.api.implementations.savedreports;


import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import com.kingsrook.qqq.api.model.actions.HttpApiResponse;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessOutputInterface;
import com.kingsrook.qqq.api.model.openapi.Content;
import com.kingsrook.qqq.api.model.openapi.Response;
import com.kingsrook.qqq.api.model.openapi.Schema;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;


/*******************************************************************************
 ** api process output specifier for the RenderSavedReport process
 *******************************************************************************/
public class RenderSavedReportProcessApiProcessOutput implements ApiProcessOutputInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable getOutputForProcess(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException
   {
      try
      {
         ReportFormat reportFormat = ReportFormat.fromString(runProcessOutput.getValueString("reportFormat"));

         String filePath = runProcessOutput.getValueString("serverFilePath");
         File   file     = new File(filePath);
         if(reportFormat.getIsBinary())
         {
            return FileUtils.readFileToByteArray(file);
         }
         else
         {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
         }
      }
      catch(Exception e)
      {
         throw new QException("Error streaming report contents", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void customizeHttpApiResponse(HttpApiResponse httpApiResponse, RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////
      // we don't need anyone else to format our response - assume that we've done so ourselves. //
      /////////////////////////////////////////////////////////////////////////////////////////////
      httpApiResponse.setNeedsFormattedAsJson(false);

      ReportFormat reportFormat = ReportFormat.fromString(runProcessOutput.getValueString("reportFormat"));
      httpApiResponse.setContentType(reportFormat.getMimeType());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public HttpStatus.Code getSuccessStatusCode(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput)
   {
      return (HttpStatus.Code.OK);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Map<Integer, Response> getSpecResponses(String apiName)
   {
      return Map.of(HttpStatus.Code.OK.getCode(), new Response()
         .withDescription("Report contents in the requested format.")
         .withContent(Map.of(
            ReportFormat.JSON.getMimeType(), new Content()
               .withSchema(new Schema()
                  .withDescription("JSON Report contents")
                  .withExample("""
                     [
                        {"id": 1, "name": "James"},
                        {"id": 2, "name": "Jean-Luc"}
                     ]
                     """)
                  .withType("string")
                  .withFormat("text")),
            ReportFormat.CSV.getMimeType(), new Content()
               .withSchema(new Schema()
                  .withDescription("CSV Report contents")
                  .withExample("""
                     "id","name"
                     1,"James"
                     2,"Jean-Luc"
                     """)
                  .withType("string")
                  .withFormat("text")),
            ReportFormat.XLSX.getMimeType(), new Content()
               .withSchema(new Schema()
                  .withDescription("Excel Report contents")
                  .withType("string")
                  .withFormat("binary"))
         ))
      );
   }

}
