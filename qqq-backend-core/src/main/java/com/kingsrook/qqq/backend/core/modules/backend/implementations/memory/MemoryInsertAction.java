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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.memory;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** In-memory version of insert action.
 **
 *******************************************************************************/
public class MemoryInsertAction extends AbstractMemoryAction implements InsertInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      try
      {
         QTableMetaData table = insertInput.getTable();
         Instant        now   = Instant.now();

         for(QRecord record : insertInput.getRecords())
         {
            ///////////////////////////////////////////
            // todo .. better (not hard-coded names) //
            ///////////////////////////////////////////
            setValueIfTableHasField(record, table, "createDate", now, false);
            setValueIfTableHasField(record, table, "modifyDate", now, false);
         }

         InsertOutput insertOutput = new InsertOutput();
         insertOutput.setRecords(MemoryRecordStore.getInstance().insert(insertInput, true));
         return (insertOutput);
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
   }

}
