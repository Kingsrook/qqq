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


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** JSON export format implementation
 *******************************************************************************/
public class JsonExportStreamer implements ExportStreamerInterface
{
   private static final Logger LOG = LogManager.getLogger(JsonExportStreamer.class);

   private ExportInput          exportInput;
   private QTableMetaData       table;
   private List<QFieldMetaData> fields;
   private OutputStream         outputStream;

   private boolean needComma = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public JsonExportStreamer()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void start(ExportInput exportInput, List<QFieldMetaData> fields, String label) throws QReportingException
   {
      this.exportInput = exportInput;
      this.fields = fields;
      table = exportInput.getTable();
      outputStream = this.exportInput.getReportOutputStream();

      try
      {
         outputStream.write("[".getBytes(StandardCharsets.UTF_8));
      }
      catch(IOException e)
      {
         throw (new QReportingException("Error starting report output", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addRecords(List<QRecord> qRecords) throws QReportingException
   {
      LOG.info("Consuming [" + qRecords.size() + "] records from the pipe");

      for(QRecord qRecord : qRecords)
      {
         writeRecord(qRecord);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeRecord(QRecord qRecord) throws QReportingException
   {
      try
      {
         if(needComma)
         {
            outputStream.write(",".getBytes(StandardCharsets.UTF_8));
         }

         String json = JsonUtils.toJson(qRecord);
         outputStream.write(json.getBytes(StandardCharsets.UTF_8));
         outputStream.flush(); // todo - less often?
         needComma = true;
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error writing JSON report", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addTotalsRow(QRecord record) throws QReportingException
   {
      writeRecord(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void finish() throws QReportingException
   {
      try
      {
         outputStream.write("]".getBytes(StandardCharsets.UTF_8));
      }
      catch(IOException e)
      {
         throw (new QReportingException("Error ending report output", e));
      }
   }

}
