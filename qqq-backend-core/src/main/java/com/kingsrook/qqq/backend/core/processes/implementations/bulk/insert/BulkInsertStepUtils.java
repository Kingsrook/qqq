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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;


/*******************************************************************************
 **
 *******************************************************************************/
public class BulkInsertStepUtils
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static StorageInput getStorageInputForTheFile(RunBackendStepInput input) throws QException
   {
      @SuppressWarnings("unchecked")
      ArrayList<StorageInput> storageInputs = (ArrayList<StorageInput>) input.getValue("theFile");
      if(storageInputs == null)
      {
         throw (new QException("StorageInputs for theFile were not found in process state"));
      }

      if(storageInputs.isEmpty())
      {
         throw (new QException("StorageInputs for theFile was an empty list"));
      }

      StorageInput storageInput = storageInputs.get(0);
      return (storageInput);
   }

}
