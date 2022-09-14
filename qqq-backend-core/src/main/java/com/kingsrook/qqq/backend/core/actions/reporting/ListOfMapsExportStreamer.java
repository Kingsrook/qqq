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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QReportingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Report streamer implementation that just builds up a STATIC list of lists of strings.
 ** Meant only for use in unit tests at this time...  would need refactored for
 ** multi-thread/multi-use if wanted for real usage.
 *******************************************************************************/
public class ListOfMapsExportStreamer implements ExportStreamerInterface
{
   private static final Logger LOG = LogManager.getLogger(ListOfMapsExportStreamer.class);

   private ExportInput          exportInput;
   private List<QFieldMetaData> fields;

   private static List<Map<String, String>> list    = new ArrayList<>();
   private static List<String>              headers = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public ListOfMapsExportStreamer()
   {
   }



   /*******************************************************************************
    ** Getter for list
    **
    *******************************************************************************/
   public static List<Map<String, String>> getList()
   {
      return (list);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void start(ExportInput exportInput, List<QFieldMetaData> fields) throws QReportingException
   {
      this.exportInput = exportInput;
      this.fields = fields;

      headers = new ArrayList<>();
      for(QFieldMetaData field : fields)
      {
         headers.add(field.getLabel());
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

      for(QRecord qRecord : qRecords)
      {
         addRecord(qRecord);
      }
      return (qRecords.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void addRecord(QRecord qRecord)
   {
      Map<String, String> row = new LinkedHashMap<>();
      list.add(row);
      for(int i = 0; i < fields.size(); i++)
      {
         row.put(headers.get(i), qRecord.getValueString(fields.get(i).getName()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void addTotalsRow(QRecord record) throws QReportingException
   {
      addRecord(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void finish()
   {

   }

}
