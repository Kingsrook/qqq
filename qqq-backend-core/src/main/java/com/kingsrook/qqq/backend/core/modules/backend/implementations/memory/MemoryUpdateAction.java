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
import com.kingsrook.qqq.backend.core.actions.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** In-memory version of update action.
 **
 *******************************************************************************/
public class MemoryUpdateAction extends AbstractMemoryAction implements UpdateInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateOutput execute(UpdateInput updateInput) throws QException
   {
      try
      {
         QTableMetaData table = updateInput.getTable();
         Instant        now   = Instant.now();

         for(QRecord record : updateInput.getRecords())
         {
            ///////////////////////////////////////////
            // todo .. better (not hard-coded names) //
            ///////////////////////////////////////////
            setValueIfTableHasField(record, table, "modifyDate", now, false);
         }

         UpdateOutput updateOutput = new UpdateOutput();
         updateOutput.setRecords(MemoryRecordStore.getInstance().update(updateInput, true));
         return (updateOutput);
      }
      catch(Exception e)
      {
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }

}
