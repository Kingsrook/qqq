/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.adapters;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
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
            List<CSVRecord> csvRecords = csvParser.getRecords();
            for(CSVRecord csvRecord : csvRecords)
            {
               //////////////////////////////////////////////////////////////////
               // put values from the CSV record into a map of header -> value //
               //////////////////////////////////////////////////////////////////
               Map<String, String> csvValues = new HashMap<>();
               for(String header : headers)
               {
                  csvValues.put(header, csvRecord.get(header));
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
               int index = 1;
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

}
