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

package com.kingsrook.qqq.backend.core.actions.interfaces;


import java.util.HashSet;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Interface for the Get action.
 **
 *******************************************************************************/
public interface GetInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   GetOutput execute(GetInput getInput) throws QException;

   /*******************************************************************************
    **
    *******************************************************************************/
   default void validateInput(GetInput getInput) throws QException
   {
      if(getInput.getPrimaryKey() != null & getInput.getUniqueKey() != null)
      {
         throw new QException("A GetInput may not contain both a primary key [" + getInput.getPrimaryKey() + "] and unique key [" + getInput.getUniqueKey() + "]");
      }

      if(getInput.getUniqueKey() != null)
      {
         QTableMetaData table      = getInput.getTable();
         boolean        foundMatch = false;
         for(UniqueKey uniqueKey : CollectionUtils.nonNullList(table.getUniqueKeys()))
         {
            if(new HashSet<>(uniqueKey.getFieldNames()).equals(getInput.getUniqueKey().keySet()))
            {
               foundMatch = true;
               break;
            }
         }

         if(!foundMatch)
         {
            throw new QException("Table [" + table.getName() + "] does not have a unique key defined on fields: " + getInput.getUniqueKey().keySet().stream().sorted().toList());
         }
      }
   }
}
