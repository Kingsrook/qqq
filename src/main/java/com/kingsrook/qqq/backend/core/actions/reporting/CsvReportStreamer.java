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
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** CSV report format implementation
 *******************************************************************************/
public class CsvReportStreamer implements ReportStreamerInterface
{
   private static final Logger LOG = LogManager.getLogger(CsvReportStreamer.class);

   private final QRecordToCsvAdapter qRecordToCsvAdapter;

   private ReportInput          reportInput;
   private QTableMetaData       table;
   private List<QFieldMetaData> fields;
   private OutputStream         outputStream;



   /*******************************************************************************
    **
    *******************************************************************************/
   public CsvReportStreamer()
   {
      qRecordToCsvAdapter = new QRecordToCsvAdapter();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void start(ReportInput reportInput, List<QFieldMetaData> fields) throws QReportingException
   {
      this.reportInput = reportInput;
      this.fields = fields;
      table = reportInput.getTable();
      outputStream = this.reportInput.getReportOutputStream();

      writeReportHeaderRow();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeReportHeaderRow() throws QReportingException
   {
      try
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
   public int takeRecordsFromPipe(RecordPipe recordPipe) throws QReportingException
   {
      List<QRecord> qRecords = recordPipe.consumeAvailableRecords();
      LOG.info("Consuming [" + qRecords.size() + "] records from the pipe");

      try
      {
         for(QRecord qRecord : qRecords)
         {
            String csv = qRecordToCsvAdapter.recordToCsv(table, qRecord, fields);
            outputStream.write(csv.getBytes(StandardCharsets.UTF_8));
            outputStream.flush(); // todo - less often?
         }
         return (qRecords.size());
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
   public void finish()
   {

   }

}
