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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.util.List;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Query output that uses a RecordPipe
 *******************************************************************************/
class QueryOutputRecordPipe implements QueryOutputStorageInterface
{
   private static final Logger LOG = LogManager.getLogger(QueryOutputRecordPipe.class);

   private RecordPipe recordPipe;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutputRecordPipe(RecordPipe recordPipe)
   {
      this.recordPipe = recordPipe;
   }



   /*******************************************************************************
    ** add a record to this output
    *******************************************************************************/
   @Override
   public void addRecord(QRecord record)
   {
      if(!recordPipe.addRecord(record))
      {
         do
         {
            LOG.debug("Record pipe.add failed (due to full pipe).  Blocking.");
            SleepUtils.sleep(10, TimeUnit.MILLISECONDS);
         }
         while(!recordPipe.addRecord(record));
         LOG.debug("Done blocking.");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void blockIfPipeIsTooFull()
   {
      if(recordPipe.countAvailableRecords() >= 100_000)
      {
         LOG.info("Record pipe is kinda full.  Blocking for a bit");
         do
         {
            SleepUtils.sleep(10, TimeUnit.MILLISECONDS);
         }
         while(recordPipe.countAvailableRecords() >= 10_000);
         LOG.info("Done blocking.");
      }
   }



   /*******************************************************************************
    ** add a list of records to this output
    *******************************************************************************/
   @Override
   public void addRecords(List<QRecord> records)
   {
      recordPipe.addRecords(records);
      blockIfPipeIsTooFull();
   }



   /*******************************************************************************
    ** Get all stored records
    *******************************************************************************/
   @Override
   public List<QRecord> getRecords()
   {
      throw (new IllegalStateException("getRecords may not be called on a piped query output"));
   }

}