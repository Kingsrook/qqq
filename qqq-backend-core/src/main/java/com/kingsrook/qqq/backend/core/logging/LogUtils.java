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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeSupplier;


/*******************************************************************************
 **
 *******************************************************************************/
public class LogUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static String jsonLog(List<LogPair> logPairs)
   {
      List<LogPair> filteredList = logPairs.stream().filter(Objects::nonNull).toList();
      if(filteredList.isEmpty())
      {
         if(QLogger.processTagLogPairJson != null)
         {
            return ("{" + QLogger.processTagLogPairJson + "}");
         }
         else
         {
            return ("{}");
         }
      }

      return ('{' + filteredList.stream().map(LogPair::toString).collect(Collectors.joining(","))
         + (QLogger.processTagLogPairJson != null ? (',' + QLogger.processTagLogPairJson) : "") + '}');
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String jsonLog(LogPair... logPairs)
   {
      if(logPairs == null || logPairs.length == 0)
      {
         return ("{}");
      }

      return (jsonLog(Arrays.asList(logPairs)));
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
   public static LogPair logPair(String key, UnsafeSupplier<Object, Exception> valueSupplier)
   {
      try
      {
         return (new LogPair(key, valueSupplier.get()));
      }
      catch(Exception e)
      {
         return (new LogPair(key, "exceptionLoggingValue: " + e.getMessage()));
      }
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
   static String filterStackTrace(String stackTrace)
   {
      try
      {
         String        packagesToKeep = "com.kingsrook|com.nutrifresh"; // todo - parameterize!!
         StringBuilder rs             = new StringBuilder();
         String[]      lines          = stackTrace.split("\n");

         int    indexWithinSubStack = 0;
         int    skipsInThisPackage  = 0;
         String packageBeingSkipped = null;

         for(String line : lines)
         {
            boolean keepLine = true;

            if(line.matches("^\\s+at .*"))
            {
               keepLine = false;
               indexWithinSubStack++;
               if(line.matches("^\\s+at (" + packagesToKeep + ").*"))
               {
                  keepLine = true;
               }
               if(indexWithinSubStack == 1)
               {
                  keepLine = true;
               }
            }
            else
            {
               indexWithinSubStack = 0;

               if(skipsInThisPackage > 0)
               {
                  rs.append("\t... ").append(skipsInThisPackage).append(" in ").append(packageBeingSkipped).append("\n");
                  skipsInThisPackage = 0;
               }
            }

            if(keepLine)
            {
               rs.append(line).append("\n");
            }
            else
            {
               String thisPackage = line.replaceFirst("\\s+at ", "").replaceFirst("(\\w+\\.\\w+).*", "$1");
               if(Objects.equals(thisPackage, packageBeingSkipped))
               {
                  skipsInThisPackage++;
               }
               else
               {
                  if(skipsInThisPackage > 0)
                  {
                     rs.append("\t... ").append(skipsInThisPackage).append(" in ").append(packageBeingSkipped).append("\n");
                  }
                  skipsInThisPackage = 1;
               }
               packageBeingSkipped = thisPackage;
            }
         }

         if(rs.length() > 0)
         {
            rs.deleteCharAt(rs.length() - 1);
         }

         return (rs.toString());
      }
      catch(Exception e)
      {
         e.printStackTrace();

         ///////////////////////////////////////////////
         // upon any exception, just return the input //
         ///////////////////////////////////////////////
         return (stackTrace);
      }
   }
}
