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

package com.kingsrook.qqq.backend.core.utils;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/*******************************************************************************
 **
 *******************************************************************************/
public class LogUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static String jsonLog(LogPair... logPairs)
   {
      if(logPairs == null || logPairs.length == 0)
      {
         return ("{}");
      }

      List<LogPair> filteredList = Arrays.stream(logPairs).filter(Objects::nonNull).toList();
      if(filteredList.isEmpty())
      {
         return ("{}");
      }

      return ('{' + filteredList.stream().map(LogPair::toString).collect(Collectors.joining(",")) + '}');
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static LogPair logPair(String key, Object value)
   {
      return (new LogPair(key, value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static LogPair logPair(String key, LogPair... values)
   {
      return (new LogPair(key, values));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class LogPair
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
         else if(value instanceof UnsafeSupplier us)
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



   /*******************************************************************************
    **
    *******************************************************************************/
   @FunctionalInterface
   public interface UnsafeSupplier
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      Object get() throws Exception;
   }

}
