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


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.actions.interfaces.QActionInterface;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Base class for all core actions in the Memory backend module.
 *******************************************************************************/
public abstract class AbstractMemoryAction implements QActionInterface
{

   /*******************************************************************************
    ** If the table has a field with the given name, then set the given value in the
    ** given record - flag added to control overwriting value.
    *******************************************************************************/
   protected void setValueIfTableHasField(QRecord record, QTableMetaData table, String fieldName, Serializable value, boolean overwriteIfSet)
   {
      try
      {
         if(table.getFields().containsKey(fieldName))
         {
            ///////////////////////////////////////////////////////////////////////
            // always set value if boolean to overwrite is true, otherwise,      //
            // only set the value if there is currently no content for the field //
            ///////////////////////////////////////////////////////////////////////
            if(overwriteIfSet || !StringUtils.hasContent(record.getValueString(fieldName)))
            {
               record.setValue(fieldName, value);
            }
         }
      }
      catch(Exception e)
      {
         /////////////////////////////////////////////////
         // this means field doesn't exist, so, ignore. //
         /////////////////////////////////////////////////
      }
   }

}

