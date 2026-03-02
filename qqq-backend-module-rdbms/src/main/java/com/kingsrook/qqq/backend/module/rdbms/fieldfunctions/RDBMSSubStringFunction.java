/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.fieldfunctions;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.SubStringFunction;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;


/*******************************************************************************
 * RDBMS adapter for the {@link SubStringFunction} that generates SQL
 * {@code SUBSTRING(col FROM ? [FOR ?])} expressions.
 *
 * <p>The fromIndex bind parameter is always supplied; the length bind parameter
 * is included only when the {@code length} argument is provided.</p>
 *******************************************************************************/
public class RDBMSSubStringFunction implements RDBMSFieldFunctionAdapterInterface
{

   /***************************************************************************
    * Returns a SUBSTRING SQL expression, using {@code FROM ?} form without a
    * length, or {@code FROM ? FOR ?} form when a length argument is present.
    ***************************************************************************/
   @Override
   public String wrapColumnName(String escapedColumnName, FieldFunction fieldFunction)
   {
      Integer length = fieldFunction.getArgumentValueOrDefault(Integer.class, SubStringFunction.LENGTH_PARAM);
      if(length == null)
      {
         return "SUBSTRING(" + escapedColumnName + " FROM ?)";
      }
      else
      {
         return "SUBSTRING(" + escapedColumnName + " FROM ? FOR ?)";
      }
   }



   /***************************************************************************
    * Returns the SQL bind parameters for the SUBSTRING expression: [fromIndex]
    * or [fromIndex, length] depending on whether length was specified.
    ***************************************************************************/
   @Override
   public List<Serializable> getParams(FieldFunction fieldFunction)
   {
      Integer fromIndex = fieldFunction.getArgumentValueOrDefault(Integer.class, SubStringFunction.FROM_INDEX_PARAM);
      Integer length    = fieldFunction.getArgumentValueOrDefault(Integer.class, SubStringFunction.LENGTH_PARAM);

      if(length == null)
      {
         return ListBuilder.of(fromIndex);
      }
      else
      {
         return ListBuilder.of(fromIndex, length);
      }
   }

}
