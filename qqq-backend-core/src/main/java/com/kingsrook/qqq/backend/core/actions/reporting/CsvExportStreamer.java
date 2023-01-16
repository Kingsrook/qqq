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


import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.kingsrook.qqq.backend.core.adapters.QRecordToCsvAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** CSV export format implementation
 *******************************************************************************/
public class CsvExportStreamer implements ExportStreamerInterface
{
   private static final QLogger LOG = QLogger.getLogger(CsvExportStreamer.class);

   private final QRecordToCsvAdapter qRecordToCsvAdapter;

   private ExportInput          exportInput;
   private QTableMetaData       table;
   private List<QFieldMetaData> fields;
   private OutputStream         outputStream;



   /*******************************************************************************
    **
    *******************************************************************************/
   public CsvExportStreamer()
   {
      qRecordToCsvAdapter = new QRecordToCsvAdapter();
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

      writeTitleAndHeader();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeTitleAndHeader() throws QReportingException
   {
      try
      {
         if(StringUtils.hasContent(exportInput.getTitleRow()))
         {
            outputStream.write((exportInput.getTitleRow() + "\n").getBytes(StandardCharsets.UTF_8));
         }

         if(exportInput.getIncludeHeaderRow())
         {
            int col = 0;
            for(QFieldMetaData column : fields)
            {
               if(col++ > 0)
               {
                  outputStream.write(',');
               }
               outputStream.write(('"' + column.getLabel() + '"').getBytes(StandardCharsets.UTF_8));
            }
            outputStream.write('\n');
         }

         outputStream.flush();
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error starting CSV report"));
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
         String csv = qRecordToCsvAdapter.recordToCsv(table, qRecord, fields);
         outputStream.write(csv.getBytes(StandardCharsets.UTF_8));
         outputStream.flush(); // todo - less often?
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error writing CSV report", e));
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
   public void finish()
   {

   }

}
