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


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;


/*******************************************************************************
 ** Utilities work values - e.g., type-cast-like operations
 *******************************************************************************/
public class ValueUtils
{

   /*******************************************************************************
    ** Type-safely make a String from any Object.
    *******************************************************************************/
   public static String getValueAsString(Object value)
   {
      if(value == null)
      {
         return (null);
      }
      else if(value instanceof String s)
      {
         return (s);
      }
      else
      {
         return (String.valueOf(value));
      }
   }



   /*******************************************************************************
    ** Returns null for null input;
    ** Returns the input object for Boolean-typed inputs.
    ** Then, follows Boolean.parseBoolean, returning true iff value is a case-insensitive
    ** match for "true", for String.valueOf the input
    *******************************************************************************/
   public static Boolean getValueAsBoolean(Object value)
   {
      if(value == null)
      {
         return (null);
      }
      else if(value instanceof Boolean b)
      {
         return (b);
      }
      else if(value instanceof String s)
      {
         return (Boolean.parseBoolean(s));
      }
      else
      {
         return (Boolean.parseBoolean(String.valueOf(value)));
      }
   }



   /*******************************************************************************
    ** Type-safely make an Integer from any Object.
    ** null and empty-string inputs return null.
    ** We try to strip away commas and decimals (as long as they are exactly equal to the int value)
    ** We may throw if the input can't be converted to an integer.
    *******************************************************************************/
   public static Integer getValueAsInteger(Object value) throws QValueException
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof Integer i)
         {
            return (i);
         }
         else if(value instanceof Long l)
         {
            return Math.toIntExact(l);
         }
         else if(value instanceof Float f)
         {
            if(f.intValue() != f)
            {
               throw (new QValueException(f + " does not have an exact integer representation."));
            }
            return (f.intValue());
         }
         else if(value instanceof Double d)
         {
            if(d.intValue() != d)
            {
               throw (new QValueException(d + " does not have an exact integer representation."));
            }
            return (d.intValue());
         }
         else if(value instanceof BigDecimal bd)
         {
            return bd.intValueExact();
         }
         else if(value instanceof String s)
         {
            if(!StringUtils.hasContent(s))
            {
               return (null);
            }

            try
            {
               return (Integer.parseInt(s));
            }
            catch(NumberFormatException nfe)
            {
               if(s.contains(","))
               {
                  String sWithoutCommas = s.replaceAll(",", "");
                  try
                  {
                     return (getValueAsInteger(sWithoutCommas));
                  }
                  catch(Exception ignore)
                  {
                     throw (nfe);
                  }
               }
               if(s.matches(".*\\.\\d+$"))
               {
                  String sWithoutDecimal = s.replaceAll("\\.\\d+$", "");
                  try
                  {
                     return (getValueAsInteger(sWithoutDecimal));
                  }
                  catch(Exception ignore)
                  {
                     throw (nfe);
                  }
               }
               throw (nfe);
            }
         }
         else
         {
            throw (new IllegalArgumentException("Unsupported class " + value.getClass().getName() + " for converting to Integer."));
         }
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to an Integer.", e));
      }
   }



   /*******************************************************************************
    ** Type-safely make a BigDecimal from any Object.
    ** null and empty-string inputs return null.
    ** We may throw if the input can't be converted to a BigDecimal
    *******************************************************************************/
   public static BigDecimal getValueAsBigDecimal(Object value) throws QValueException
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof BigDecimal bd)
         {
            return (bd);
         }
         else if(value instanceof Integer i)
         {
            return new BigDecimal(i);
         }
         else if(value instanceof Long l)
         {
            return new BigDecimal(l);
         }
         else if(value instanceof Float f)
         {
            return new BigDecimal(f);
         }
         else if(value instanceof Double d)
         {
            return new BigDecimal(d);
         }
         else if(value instanceof String s)
         {
            if(!StringUtils.hasContent(s))
            {
               return (null);
            }

            try
            {
               return (new BigDecimal(s));
            }
            catch(NumberFormatException nfe)
            {
               if(s.contains(","))
               {
                  String sWithoutCommas = s.replaceAll(",", "");
                  try
                  {
                     return (getValueAsBigDecimal(sWithoutCommas));
                  }
                  catch(Exception ignore)
                  {
                     throw (nfe);
                  }
               }
               throw (nfe);
            }
         }
         else
         {
            throw (new IllegalArgumentException("Unsupported class " + value.getClass().getName() + " for converting to BigDecimal."));
         }
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to an BigDecimal.", e));
      }
   }

}
