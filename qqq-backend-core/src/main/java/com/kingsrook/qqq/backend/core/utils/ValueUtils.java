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


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;


/*******************************************************************************
 ** Utilities work values - e.g., type-cast-like operations
 *******************************************************************************/
public class ValueUtils
{
   private static final DateTimeFormatter dateTimeFormatter_yyyyMMddWithDashes = DateTimeFormatter.ofPattern("yyyy-MM-dd");
   private static final DateTimeFormatter dateTimeFormatter_MdyyyyWithSlashes  = DateTimeFormatter.ofPattern("M/d/yyyy");
   private static final DateTimeFormatter dateTimeFormatter_yyyyMMdd           = DateTimeFormatter.ofPattern("yyyyMMdd");



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
         else if(value instanceof BigInteger b)
         {
            return (b.intValue());
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
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to Integer."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to an Integer.", e));
      }
   }



   /*******************************************************************************
    ** Type-safely make a LocalDateTime from any Object.
    ** null and empty-string inputs return null.
    ** We may throw if the input can't be converted to a LocalDateTime
    *******************************************************************************/
   public static LocalDateTime getValueAsLocalDateTime(Object value) throws QValueException
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof LocalDateTime ldt)
         {
            return (ldt);
         }
         else if(value instanceof java.sql.Timestamp ts)
         {
            return ts.toLocalDateTime();
         }
         else if(value instanceof Calendar c)
         {
            TimeZone tz  = c.getTimeZone();
            ZoneId   zid = (tz == null) ? ZoneId.systemDefault() : tz.toZoneId();
            return LocalDateTime.ofInstant(c.toInstant(), zid);
         }
         else if(value instanceof String s)
         {
            return LocalDateTime.parse(s);
         }
         else
         {
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to LocalDateTime."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to a LocalDateTime.", e));
      }
   }



   /*******************************************************************************
    ** Type-safely make a LocalDate from any Object.
    ** null and empty-string inputs return null.
    ** We may throw if the input can't be converted to a LocalDate
    *******************************************************************************/
   public static LocalDate getValueAsLocalDate(Object value) throws QValueException
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof LocalDate ld)
         {
            return (ld);
         }
         else if(value instanceof java.sql.Date d)
         {
            return d.toLocalDate();
         }
         else if(value instanceof java.util.Date d)
         {
            return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
         }
         else if(value instanceof Calendar c)
         {
            TimeZone tz  = c.getTimeZone();
            ZoneId   zid = (tz == null) ? ZoneId.systemDefault() : tz.toZoneId();
            return LocalDateTime.ofInstant(c.toInstant(), zid).toLocalDate();
         }
         else if(value instanceof Instant i)
         {
            return LocalDate.ofInstant(i, ZoneId.systemDefault()); // todo - where should the zone come from?
         }
         else if(value instanceof LocalDateTime ldt)
         {
            return ldt.toLocalDate();
         }
         else if(value instanceof String s)
         {
            if(!StringUtils.hasContent(s))
            {
               return (null);
            }

            return tryLocalDateParsers(s);
         }
         else
         {
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to LocalDate."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to a LocalDate.", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static LocalDate tryLocalDateParsers(String s)
   {
      DateTimeParseException lastException = null;
      for(DateTimeFormatter dateTimeFormatter : List.of(dateTimeFormatter_yyyyMMddWithDashes, dateTimeFormatter_MdyyyyWithSlashes, dateTimeFormatter_yyyyMMdd))
      {
         try
         {
            return LocalDate.parse(s, dateTimeFormatter);
         }
         catch(DateTimeParseException dtpe)
         {
            lastException = dtpe;
         }
      }
      throw (new QValueException("Could not parse value [" + s + "] to a local date", lastException));
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
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to BigDecimal."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to a BigDecimal.", e));
      }
   }



   /*******************************************************************************
    ** Type-safely make an Instant from any Object.
    ** null and empty-string inputs return null.
    ** We may throw if the input can't be converted to a Instant
    *******************************************************************************/
   public static Instant getValueAsInstant(Object value)
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof Instant i)
         {
            return (i);
         }
         else if(value instanceof java.sql.Date d)
         {
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // note - in the jdk, this method throws UnsupportedOperationException (because of the lack of time in sql Dates) //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            return d.toInstant();
         }
         else if(value instanceof java.util.Date d)
         {
            return d.toInstant();
         }
         else if(value instanceof Calendar c)
         {
            return (c.toInstant());
         }
         else if(value instanceof LocalDateTime ldt)
         {
            ZoneId zoneId = ZoneId.systemDefault();
            return ldt.toInstant(zoneId.getRules().getOffset(ldt));
         }
         else if(value instanceof String s)
         {
            if(!StringUtils.hasContent(s))
            {
               return (null);
            }

            try
            {
               return Instant.parse(s);
            }
            catch(DateTimeParseException e)
            {
               return tryAlternativeInstantParsing(s, e);
            }
         }
         else
         {
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to Instant."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to a Instant.", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Instant tryAlternativeInstantParsing(String s, DateTimeParseException e)
   {
      if(s.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}$"))
      {
         //////////////////////////
         // todo ... time zone?? //
         //////////////////////////
         return Instant.parse(s + ":00Z");
      }
      else
      {
         throw (e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static LocalTime getValueAsLocalTime(Serializable value)
   {
      try
      {
         if(value == null)
         {
            return (null);
         }
         else if(value instanceof LocalTime lt)
         {
            return (lt);
         }
         else if(value instanceof String s)
         {
            if(!StringUtils.hasContent(s))
            {
               return (null);
            }

            return LocalTime.parse(s);
         }
         else
         {
            throw (new QValueException("Unsupported class " + value.getClass().getName() + " for converting to LocalTime."));
         }
      }
      catch(QValueException qve)
      {
         throw (qve);
      }
      catch(Exception e)
      {
         throw (new QValueException("Value [" + value + "] could not be converted to a LocalTime.", e));
      }
   }
}
