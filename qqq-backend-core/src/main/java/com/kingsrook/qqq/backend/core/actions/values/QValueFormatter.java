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

package com.kingsrook.qqq.backend.core.actions.values;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility to apply display formats to values for fields
 *******************************************************************************/
public class QValueFormatter
{
   private static final Logger LOG = LogManager.getLogger(QValueFormatter.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String formatValue(QFieldMetaData field, Serializable value)
   {
      //////////////////////////////////
      // null values get null results //
      //////////////////////////////////
      if(value == null)
      {
         return (null);
      }

      ////////////////////////////////////////////////////////
      // if the field has a display format, try to apply it //
      ////////////////////////////////////////////////////////
      if(StringUtils.hasContent(field.getDisplayFormat()))
      {
         try
         {
            return (field.getDisplayFormat().formatted(value));
         }
         catch(Exception e)
         {
            LOG.warn("Error formatting value [" + value + "] for field [" + field.getName() + "] with format [" + field.getDisplayFormat() + "]: " + e.getMessage());
         }
      }

      ////////////////////////////////////////
      // by default, just get back a string //
      ////////////////////////////////////////
      return (ValueUtils.getValueAsString(value));
   }
}
