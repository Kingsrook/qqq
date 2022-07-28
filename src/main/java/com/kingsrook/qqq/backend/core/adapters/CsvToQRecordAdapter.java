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

package com.kingsrook.qqq.backend.core.adapters;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


/*******************************************************************************
 ** Adapter class to convert a CSV string into a list of QRecords.
 **
 *******************************************************************************/
public class CsvToQRecordAdapter
{

   /*******************************************************************************
    ** convert a CSV String into a List of QRecords, for a given table, optionally
    ** using a given mapping.
    **
    ** todo - meta-data validation, type handling
    *******************************************************************************/
   public List<QRecord> buildRecordsFromCsv(String csv, QTableMetaData table, AbstractQFieldMapping<?> mapping)
   {
      if(!StringUtils.hasContent(csv))
      {
         throw (new IllegalArgumentException("Empty csv value was provided."));
      }

      List<QRecord> rs = new ArrayList<>();
      try
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // if there's no mapping (e.g., table-standard field names), or key-based mapping, then first row is headers //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(mapping == null || AbstractQFieldMapping.SourceType.KEY.equals(mapping.getSourceType()))
         {
            CSVParser csvParser = new CSVParser(new StringReader(csv),
               CSVFormat.DEFAULT
                  .withFirstRecordAsHeader()
                  .withIgnoreHeaderCase()
                  .withTrim());

            List<String> headers = csvParser.getHeaderNames();
            headers = makeHeadersUnique(headers);

            List<CSVRecord> csvRecords = csvParser.getRecords();
            for(CSVRecord csvRecord : csvRecords)
            {
               //////////////////////////////////////////////////////////////////
               // put values from the CSV record into a map of header -> value //
               //////////////////////////////////////////////////////////////////
               Map<String, String> csvValues = new HashMap<>();
               for(int i=0; i<headers.size(); i++)
               {
                  csvValues.put(headers.get(i), csvRecord.get(i));
               }

               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               // now move values into the QRecord, using the mapping to get the 'header' corresponding to each QField //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               QRecord qRecord = new QRecord();
               rs.add(qRecord);
               for(QFieldMetaData field : table.getFields().values())
               {
                  String fieldSource = mapping == null ? field.getName() : String.valueOf(mapping.getFieldSource(field.getName()));
                  qRecord.setValue(field.getName(), csvValues.get(fieldSource));
               }
            }
         }
         else if(AbstractQFieldMapping.SourceType.INDEX.equals(mapping.getSourceType()))
         {
            ///////////////////////////////
            // else, index-based mapping //
            ///////////////////////////////
            CSVParser csvParser = new CSVParser(new StringReader(csv),
               CSVFormat.DEFAULT
                  .withTrim());

            List<CSVRecord> csvRecords = csvParser.getRecords();
            for(CSVRecord csvRecord : csvRecords)
            {
               /////////////////////////////////////////////////////////////////
               // put values from the CSV record into a map of index -> value //
               /////////////////////////////////////////////////////////////////
               Map<Integer, String> csvValues = new HashMap<>();
               int                  index     = 1;
               for(String value : csvRecord)
               {
                  csvValues.put(index++, value);
               }

               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               // now move values into the QRecord, using the mapping to get the 'header' corresponding to each QField //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               QRecord qRecord = new QRecord();
               rs.add(qRecord);
               for(QFieldMetaData field : table.getFields().values())
               {
                  Integer fieldIndex = (Integer) mapping.getFieldSource(field.getName());
                  qRecord.setValue(field.getName(), csvValues.get(fieldIndex));
               }
            }
         }
         else
         {
            throw (new IllegalArgumentException("Unrecognized mapping source type: " + mapping.getSourceType()));
         }
      }
      catch(IOException e)
      {
         throw (new IllegalArgumentException("Error parsing CSV: " + e.getMessage(), e));
      }

      return (rs);
   }



   /*******************************************************************************
    ** For a list of headers, if any duplicates are found, add a numeric suffix
    ** to the duplicates.
    **
    ** So this header row:  A,B,C,C,C
    ** Would become:  A,B,C,C 2,C 3
    **
    ** See unit test for more scenarios - some of which we do not handle well yet,
    ** such as "C 2, C, C 3"
    *******************************************************************************/
   protected List<String> makeHeadersUnique(List<String> headers)
   {
      Map<String, Integer> countsByHeader = new HashMap<>();
      List<String>         rs             = new ArrayList<>();

      for(String header : headers)
      {
         String headerToUse = header;
         String headerWithoutSuffix = header.replaceFirst(" \\d+$", "");

         if(countsByHeader.containsKey(headerWithoutSuffix))
         {
            int suffix = countsByHeader.get(headerWithoutSuffix) + 1;
            countsByHeader.put(headerWithoutSuffix, suffix);
            headerToUse = headerWithoutSuffix + " " + suffix;
         }
         else
         {
            countsByHeader.put(headerWithoutSuffix, 1);
         }
         rs.add(headerToUse);
      }
      return (rs);
   }

}
