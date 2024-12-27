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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import org.dhatim.fastexcel.reader.Cell;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.ReadingOptions;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;


/*******************************************************************************
 **
 *******************************************************************************/
public class XlsxFileToRows extends AbstractIteratorBasedFileToRows<org.dhatim.fastexcel.reader.Row> implements FileToRowsInterface
{
   private static final QLogger LOG = QLogger.getLogger(XlsxFileToRows.class);

   private static final Pattern DAY_PATTERN = Pattern.compile(".*\\b(d|dd)\\b.*");

   private ReadableWorkbook                        workbook;
   private Stream<org.dhatim.fastexcel.reader.Row> rows;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void init(InputStream inputStream) throws QException
   {
      try
      {
         workbook = new ReadableWorkbook(inputStream, new ReadingOptions(true, true));
         Sheet sheet = workbook.getFirstSheet();

         rows = sheet.openStream();
         setIterator(rows.iterator());
      }
      catch(IOException e)
      {
         throw new QException("Error opening XLSX Parser", e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public BulkLoadFileRow makeRow(org.dhatim.fastexcel.reader.Row readerRow)
   {
      Serializable[] values = new Serializable[readerRow.getCellCount()];

      for(int i = 0; i < readerRow.getCellCount(); i++)
      {
         values[i] = processCell(readerRow, i);
      }

      return new BulkLoadFileRow(values, getRowNo());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Serializable processCell(Row readerRow, int columnIndex)
   {
      Cell cell = readerRow.getCell(columnIndex);
      if(cell == null)
      {
         return (null);
      }

      String dataFormatString = cell.getDataFormatString();
      switch(cell.getType())
      {
         case NUMBER ->
         {
            /////////////////////////////////////////////////////////////////////////////////////
            // dates, date-times, integers, and decimals are all identified as type = "number" //
            // so go through this process to try to identify what user means it as             //
            /////////////////////////////////////////////////////////////////////////////////////
            if(isDateTimeFormat(dataFormatString))
            {
               ////////////////////////////////////////////////////////////////////////////////////////
               // first - if it has a date-time looking format string, then treat it as a date-time. //
               ////////////////////////////////////////////////////////////////////////////////////////
               return (cell.asDate());
            }
            else if(isDateFormat(dataFormatString))
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////
               // second, if it has a date looking format string (which is a sub-set of date-time), then treat as date. //
               ///////////////////////////////////////////////////////////////////////////////////////////////////////////
               return (cell.asDate().toLocalDate());
            }
            else
            {
               ////////////////////////////////////////////////////////////////////////////////////////
               // now assume it's a number - but in case this optional is empty (why?) return a null //
               ////////////////////////////////////////////////////////////////////////////////////////
               Optional<BigDecimal> bigDecimal = readerRow.getCellAsNumber(columnIndex);
               if(bigDecimal.isEmpty())
               {
                  return (null);
               }

               try
               {
                  ////////////////////////////////////////////////////////////
                  // now if the bigDecimal is an exact integer, return that //
                  ////////////////////////////////////////////////////////////
                  Integer i = bigDecimal.get().intValueExact();
                  return (i);
               }
               catch(ArithmeticException e)
               {
                  /////////////////////////////////
                  // else, end up with a decimal //
                  /////////////////////////////////
                  return (bigDecimal.get());
               }
            }
         }
         case STRING ->
         {
            return cell.asString();
         }
         case BOOLEAN ->
         {
            return cell.asBoolean();
         }
         case EMPTY, ERROR, FORMULA ->
         {
            LOG.debug("cell type: " + cell.getType() + " had value string: " + cell.asString());
            return (null);
         }
         default ->
         {
            return (null);
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static boolean isDateTimeFormat(String dataFormatString)
   {
      if(dataFormatString == null)
      {
         return (false);
      }

      if(hasDay(dataFormatString) && hasHour(dataFormatString))
      {
         return (true);
      }

      return false;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static boolean hasHour(String dataFormatString)
   {
      return dataFormatString.contains("h");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static boolean hasDay(String dataFormatString)
   {
      return DAY_PATTERN.matcher(dataFormatString).matches();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static boolean isDateFormat(String dataFormatString)
   {
      if(dataFormatString == null)
      {
         return (false);
      }

      if(hasDay(dataFormatString))
      {
         return (true);
      }

      return false;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void close() throws Exception
   {
      if(workbook != null)
      {
         workbook.close();
      }

      if(rows != null)
      {
         rows.close();
      }
   }

}
