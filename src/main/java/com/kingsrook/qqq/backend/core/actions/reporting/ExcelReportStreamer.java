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
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;


/*******************************************************************************
 ** Excel report format implementation
 *******************************************************************************/
public class ExcelReportStreamer implements ReportStreamerInterface
{
   private static final Logger LOG = LogManager.getLogger(ExcelReportStreamer.class);

   private ReportInput          reportInput;
   private QTableMetaData       table;
   private List<QFieldMetaData> fields;
   private OutputStream         outputStream;

   private Workbook  workbook;
   private Worksheet worksheet;
   private int       row = 1;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ExcelReportStreamer()
   {
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

      workbook = new Workbook(outputStream, "QQQ", null);
      worksheet = workbook.newWorksheet("Sheet 1");

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
            worksheet.value(0, col, column.getLabel());
            col++;
         }

         worksheet.flush();
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error starting Excel report"));
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
            int col = 0;
            for(QFieldMetaData column : fields)
            {
               Serializable value = qRecord.getValue(column.getName());
               if(value != null)
               {
                  if(value instanceof String s)
                  {
                     worksheet.value(row, col, s);
                  }
                  else if(value instanceof Number n)
                  {
                     worksheet.value(row, col, n);
                  }
                  else if(value instanceof Boolean b)
                  {
                     worksheet.value(row, col, b);
                  }
                  else if(value instanceof Date d)
                  {
                     worksheet.value(row, col, d);
                     worksheet.style(row, col).format("yyyy-MM-dd").set();
                  }
                  else if(value instanceof LocalDate d)
                  {
                     worksheet.value(row, col, d);
                     worksheet.style(row, col).format("yyyy-MM-dd").set();
                  }
                  else if(value instanceof LocalDateTime d)
                  {
                     worksheet.value(row, col, d);
                     worksheet.style(row, col).format("yyyy-MM-dd H:mm:ss").set();
                  }
                  else if(value instanceof ZonedDateTime d)
                  {
                     worksheet.value(row, col, d);
                     worksheet.style(row, col).format("yyyy-MM-dd H:mm:ss").set();
                  }
                  else
                  {
                     worksheet.value(row, col, ValueUtils.getValueAsString(value));
                  }
               }
               col++;
            }

            row++;
            worksheet.flush(); // todo?  not at all?  or just sometimes?
         }
      }
      catch(Exception e)
      {
         try
         {
            workbook.finish();
            outputStream.close();
         }
         finally
         {
            throw (new QReportingException("Error generating Excel report", e));
         }
      }

      return (qRecords.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void finish() throws QReportingException
   {
      try
      {
         if(workbook != null)
         {
            workbook.finish();
         }
      }
      catch(Exception e)
      {
         throw (new QReportingException("Error finishing Excel report", e));
      }
   }

}
