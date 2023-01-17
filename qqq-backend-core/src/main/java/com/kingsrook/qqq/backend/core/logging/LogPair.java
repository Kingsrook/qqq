/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.logging;


import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;


/*******************************************************************************
 **
 *******************************************************************************/
public class LogPair
{
   private String key;
   private Object value;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public LogPair(String key, Object value)
   {
      this.key = key;
      this.value = value;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      String valueString = getValueString(value);

      return "\"" + Objects.requireNonNullElse(key, "null").replace('"', '.') + "\":" + valueString;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getValueString(Object value)
   {
      String valueString;
      if(value == null)
      {
         valueString = "null";
      }
      else if(value instanceof LogPair subLogPair)
      {
         valueString = '{' + subLogPair.toString() + '}';
      }
      else if(value instanceof LogPair[] subLogPairs)
      {
         String subLogPairsString = Arrays.stream(subLogPairs).map(LogPair::toString).collect(Collectors.joining(","));
         valueString = '{' + subLogPairsString + '}';
      }
      else if(value instanceof LogUtils.UnsafeSupplier us)
      {
         try
         {
            Object o = us.get();
            return getValueString(o);
         }
         catch(Exception e)
         {
            valueString = "LogValueError";
         }
      }
      else if(value instanceof Number n)
      {
         valueString = String.valueOf(n);
      }
      else
      {
         valueString = '"' + String.valueOf(value).replace("\"", "\\\"") + '"';
      }
      return valueString;
   }
}
