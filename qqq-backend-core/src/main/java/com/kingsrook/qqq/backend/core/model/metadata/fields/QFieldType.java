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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 ** Possible data types for Q-fields.
 **
 *******************************************************************************/
public enum QFieldType
{
   STRING,
   INTEGER,
   DECIMAL,
   DATE,
   // TIME,
   DATE_TIME,
   TEXT,
   HTML,
   PASSWORD,
   BLOB;
   ///////////////////////////////////////////////////////////////////////
   // keep these values in sync with QFieldType.ts in qqq-frontend-core //
   ///////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    ** Get a field type enum constant for a java class.
    *******************************************************************************/
   public static QFieldType fromClass(Class<?> c) throws QException
   {
      if(c.equals(String.class))
      {
         return (STRING);
      }
      if(c.equals(Integer.class) || c.equals(int.class))
      {
         return (INTEGER);
      }
      if(c.equals(BigDecimal.class))
      {
         return (DECIMAL);
      }

      throw (new QException("Unrecognized class [" + c + "]"));
   }
}
