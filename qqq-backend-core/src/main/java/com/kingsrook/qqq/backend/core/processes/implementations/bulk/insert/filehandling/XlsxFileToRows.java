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
import java.util.stream.Stream;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;


/*******************************************************************************
 **
 *******************************************************************************/
public class XlsxFileToRows extends AbstractIteratorBasedFileToRows<org.dhatim.fastexcel.reader.Row> implements FileToRowsInterface
{
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
         workbook = new ReadableWorkbook(inputStream);
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
         values[i] = readerRow.getCell(i).getText();
      }

      return new BulkLoadFileRow(values);
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
