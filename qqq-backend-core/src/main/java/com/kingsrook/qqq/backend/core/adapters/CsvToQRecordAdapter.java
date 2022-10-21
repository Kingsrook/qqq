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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
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
 ** Based on which method is called, can either take a pipe, and stream records
 ** into it - or return a list of all records from the file.  Either way, at this
 ** time, the full CSV string is read & parsed - a future optimization might read
 ** the CSV content from a stream as well.
 *******************************************************************************/
public class CsvToQRecordAdapter
{
   private RecordPipe    recordPipe = null;
   private List<QRecord> recordList = null;



   /*******************************************************************************
    ** stream records from a CSV String into a RecordPipe, for a given table, optionally
    ** using a given mapping.
    **
    *******************************************************************************/
   public void buildRecordsFromCsv(RecordPipe recordPipe, String csv, QTableMetaData table, AbstractQFieldMapping<?> mapping, Consumer<QRecord> recordCustomizer)
   {
      buildRecordsFromCsv(new InputWrapper().withRecordPipe(recordPipe).withCsv(csv).withTable(table).withMapping(mapping).withRecordCustomizer(recordCustomizer));
   }



   /*******************************************************************************
    ** convert a CSV String into a List of QRecords, for a given table, optionally
    ** using a given mapping.
    **
    *******************************************************************************/
   public List<QRecord> buildRecordsFromCsv(String csv, QTableMetaData table, AbstractQFieldMapping<?> mapping)
   {
      buildRecordsFromCsv(new InputWrapper().withCsv(csv).withTable(table).withMapping(mapping));
      return (recordList);
   }



   /*******************************************************************************
    ** convert a CSV String into a List of QRecords, for a given table, optionally
    ** using a given mapping.
    **
    ** todo - meta-data validation, type handling
    *******************************************************************************/
   public void buildRecordsFromCsv(InputWrapper inputWrapper)
   {
      String                   csv              = inputWrapper.getCsv();
      AbstractQFieldMapping<?> mapping          = inputWrapper.getMapping();
      Consumer<QRecord>        recordCustomizer = inputWrapper.getRecordCustomizer();
      QTableMetaData           table            = inputWrapper.getTable();
      Integer                  limit            = inputWrapper.getLimit();

      if(!StringUtils.hasContent(csv))
      {
         throw (new IllegalArgumentException("Empty csv value was provided."));
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if caller supplied a record pipe, use it -- but if it's null, then create a recordList to populate. //
      // see addRecord method for usage.                                                                     //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      this.recordPipe = inputWrapper.getRecordPipe();
      if(this.recordPipe == null)
      {
         this.recordList = new ArrayList<>();
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // once, from a DOS csv file (that had come from Excel), we had a "﻿" character (FEFF, Byte-order marker) at the start of a //
      // CSV, which caused our first header to not match...  So, let us strip away any FEFF or FFFE's at the start of CSV strings.     //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(csv.length() > 1 && (csv.charAt(0) == 0xfeff || csv.charAt(0) == 0xfffe))
      {
         csv = csv.substring(1);
      }

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

            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            int                 recordCount = 0;
            while(csvIterator.hasNext())
            {
               CSVRecord csvRecord = csvIterator.next();

               //////////////////////////////////////////////////////////////////
               // put values from the CSV record into a map of header -> value //
               //////////////////////////////////////////////////////////////////
               Map<String, String> csvValues = new HashMap<>();
               for(int i = 0; i < headers.size() && i < csvRecord.size(); i++)
               {
                  String header = adjustHeaderCase(headers.get(i), inputWrapper);
                  csvValues.put(header, csvRecord.get(i));
               }

               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               // now move values into the QRecord, using the mapping to get the 'header' corresponding to each QField //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////
               QRecord qRecord = new QRecord();
               for(QFieldMetaData field : table.getFields().values())
               {
                  String fieldSource = mapping == null ? field.getName() : String.valueOf(mapping.getFieldSource(field.getName()));
                  fieldSource = adjustHeaderCase(fieldSource, inputWrapper);
                  qRecord.setValue(field.getName(), csvValues.get(fieldSource));
               }

               runRecordCustomizer(recordCustomizer, qRecord);
               addRecord(qRecord);

               recordCount++;
               if(limit != null && recordCount > limit)
               {
                  break;
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

            Iterator<CSVRecord> csvIterator = csvParser.iterator();
            int                 recordCount = 0;
            while(csvIterator.hasNext())
            {
               CSVRecord csvRecord = csvIterator.next();

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
               for(QFieldMetaData field : table.getFields().values())
               {
                  Integer fieldIndex = (Integer) mapping.getFieldSource(field.getName());
                  qRecord.setValue(field.getName(), csvValues.get(fieldIndex));
               }

               runRecordCustomizer(recordCustomizer, qRecord);
               addRecord(qRecord);

               recordCount++;
               if(limit != null && recordCount > limit)
               {
                  break;
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
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String adjustHeaderCase(String s, InputWrapper inputWrapper)
   {
      if(inputWrapper.caseSensitiveHeaders)
      {
         return (s);
      }
      return (s.toLowerCase());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runRecordCustomizer(Consumer<QRecord> recordCustomizer, QRecord qRecord)
   {
      if(recordCustomizer != null)
      {
         recordCustomizer.accept(qRecord);
      }
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
         String headerToUse         = header;
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



   /*******************************************************************************
    ** Add a record - either to the pipe, or list, whichever we're building.
    *******************************************************************************/
   private void addRecord(QRecord record)
   {
      if(recordPipe != null)
      {
         recordPipe.addRecord(record);
      }

      if(recordList != null)
      {
         recordList.add(record);
      }
   }



   /*******************************************************************************
    ** Getter for recordList - note - only is valid if you don't supply a pipe in
    ** the input.  If you do supply a pipe, then you get an exception if you call here!
    **
    *******************************************************************************/
   public List<QRecord> getRecordList()
   {
      if(recordPipe != null)
      {
         throw (new IllegalStateException("getRecordList called on a CSVToQRecordAdapter that ran with a recordPipe."));
      }

      return recordList;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class InputWrapper
   {
      private RecordPipe               recordPipe;
      private String                   csv;
      private QTableMetaData           table;
      private AbstractQFieldMapping<?> mapping;
      private Consumer<QRecord>        recordCustomizer;
      private Integer                  limit;

      private boolean caseSensitiveHeaders = false;



      /*******************************************************************************
       ** Getter for recordPipe
       **
       *******************************************************************************/
      public RecordPipe getRecordPipe()
      {
         return recordPipe;
      }



      /*******************************************************************************
       ** Setter for recordPipe
       **
       *******************************************************************************/
      public void setRecordPipe(RecordPipe recordPipe)
      {
         this.recordPipe = recordPipe;
      }



      /*******************************************************************************
       ** Fluent setter for recordPipe
       **
       *******************************************************************************/
      public InputWrapper withRecordPipe(RecordPipe recordPipe)
      {
         this.recordPipe = recordPipe;
         return (this);
      }



      /*******************************************************************************
       ** Getter for csv
       **
       *******************************************************************************/
      public String getCsv()
      {
         return csv;
      }



      /*******************************************************************************
       ** Setter for csv
       **
       *******************************************************************************/
      public void setCsv(String csv)
      {
         this.csv = csv;
      }



      /*******************************************************************************
       ** Fluent setter for csv
       **
       *******************************************************************************/
      public InputWrapper withCsv(String csv)
      {
         this.csv = csv;
         return (this);
      }



      /*******************************************************************************
       ** Getter for table
       **
       *******************************************************************************/
      public QTableMetaData getTable()
      {
         return table;
      }



      /*******************************************************************************
       ** Setter for table
       **
       *******************************************************************************/
      public void setTable(QTableMetaData table)
      {
         this.table = table;
      }



      /*******************************************************************************
       ** Fluent setter for table
       **
       *******************************************************************************/
      public InputWrapper withTable(QTableMetaData table)
      {
         this.table = table;
         return (this);
      }



      /*******************************************************************************
       ** Getter for mapping
       **
       *******************************************************************************/
      public AbstractQFieldMapping<?> getMapping()
      {
         return mapping;
      }



      /*******************************************************************************
       ** Setter for mapping
       **
       *******************************************************************************/
      public void setMapping(AbstractQFieldMapping<?> mapping)
      {
         this.mapping = mapping;
      }



      /*******************************************************************************
       ** Fluent setter for mapping
       **
       *******************************************************************************/
      public InputWrapper withMapping(AbstractQFieldMapping<?> mapping)
      {
         this.mapping = mapping;
         return (this);
      }



      /*******************************************************************************
       ** Getter for recordCustomizer
       **
       *******************************************************************************/
      public Consumer<QRecord> getRecordCustomizer()
      {
         return recordCustomizer;
      }



      /*******************************************************************************
       ** Setter for recordCustomizer
       **
       *******************************************************************************/
      public void setRecordCustomizer(Consumer<QRecord> recordCustomizer)
      {
         this.recordCustomizer = recordCustomizer;
      }



      /*******************************************************************************
       ** Fluent setter for recordCustomizer
       **
       *******************************************************************************/
      public InputWrapper withRecordCustomizer(Consumer<QRecord> recordCustomizer)
      {
         this.recordCustomizer = recordCustomizer;
         return (this);
      }



      /*******************************************************************************
       ** Getter for limit
       **
       *******************************************************************************/
      public Integer getLimit()
      {
         return limit;
      }



      /*******************************************************************************
       ** Setter for limit
       **
       *******************************************************************************/
      public void setLimit(Integer limit)
      {
         this.limit = limit;
      }



      /*******************************************************************************
       ** Fluent setter for limit
       **
       *******************************************************************************/
      public InputWrapper withLimit(Integer limit)
      {
         this.limit = limit;
         return (this);
      }



      /*******************************************************************************
       ** Getter for caseSensitiveHeaders
       **
       *******************************************************************************/
      public boolean getCaseSensitiveHeaders()
      {
         return caseSensitiveHeaders;
      }



      /*******************************************************************************
       ** Setter for caseSensitiveHeaders
       **
       *******************************************************************************/
      public void setCaseSensitiveHeaders(boolean caseSensitiveHeaders)
      {
         this.caseSensitiveHeaders = caseSensitiveHeaders;
      }



      /*******************************************************************************
       ** Fluent setter for caseSensitiveHeaders
       **
       *******************************************************************************/
      public InputWrapper withCaseSensitiveHeaders(boolean caseSensitiveHeaders)
      {
         this.caseSensitiveHeaders = caseSensitiveHeaders;
         return (this);
      }

   }

}
