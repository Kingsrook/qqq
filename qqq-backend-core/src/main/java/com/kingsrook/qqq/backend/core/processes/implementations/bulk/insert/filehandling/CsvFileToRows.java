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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class CsvFileToRows extends AbstractIteratorBasedFileToRows<CSVRecord> implements FileToRowsInterface
{
   private CSVParser csvParser;



   /***************************************************************************
    **
    ***************************************************************************/
   public static CsvFileToRows forString(String csv) throws QException
   {
      CsvFileToRows csvFileToRows = new CsvFileToRows();

      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csv.getBytes());
      csvFileToRows.init(byteArrayInputStream);

      return (csvFileToRows);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void init(InputStream inputStream) throws QException
   {
      try
      {
         csvParser = new CSVParser(new InputStreamReader(inputStream), CSVFormat.DEFAULT
            .withIgnoreSurroundingSpaces()
         );
         setIterator(csvParser.iterator());
      }
      catch(IOException e)
      {
         throw new QException("Error opening CSV Parser", e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public BulkLoadFileRow makeRow(CSVRecord csvRecord)
   {
      Serializable[] values = new Serializable[csvRecord.size()];
      int            i      = 0;
      for(String s : csvRecord)
      {
         values[i++] = s;
      }

      return (new BulkLoadFileRow(values));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void close() throws Exception
   {
      if(csvParser != null)
      {
         csvParser.close();
      }
   }

}
