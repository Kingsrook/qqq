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
import com.kingsrook.qqq.backend.core.actions.reporting.RecordPipe;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Query output that uses a RecordPipe
 *******************************************************************************/
class QueryOutputRecordPipe implements QueryOutputStorageInterface
{
   private static final QLogger LOG = QLogger.getLogger(QueryOutputRecordPipe.class);

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
      recordPipe.addRecord(record);
   }



   /*******************************************************************************
    ** add a list of records to this output
    *******************************************************************************/
   @Override
   public void addRecords(List<QRecord> records)
   {
      recordPipe.addRecords(records);
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
