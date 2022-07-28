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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Object to connect a producer of records with a consumer.
 ** Best for those to be on different threads, to avoid deadlock.
 *******************************************************************************/
public class RecordPipe
{
   private ArrayBlockingQueue<QRecord> queue = new ArrayBlockingQueue<>(10_000);


   /*******************************************************************************
    ** Add a record to the pipe
    ** Returns true iff the record fit in the pipe; false if the pipe is currently full.
    *******************************************************************************/
   public boolean addRecord(QRecord record)
   {
      return (queue.offer(record));
   }



   /*******************************************************************************
    ** Add a list of records to the pipe
    *******************************************************************************/
   public void addRecords(List<QRecord> records)
   {
      queue.addAll(records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> consumeAvailableRecords()
   {
      List<QRecord> rs = new ArrayList<>();

      while(true)
      {
         QRecord record = queue.poll();
         if(record == null)
         {
            break;
         }
         rs.add(record);
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int countAvailableRecords()
   {
      return (queue.size());
   }

}
