/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.scheduler;


import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*******************************************************************************
 ** class to give a human-friendly descriptive string from a cron expression.
 ** (written in half by my friend Mr. Chatty G)
 *******************************************************************************/
public class CronDescriber
{
   private static final Map<String, String> DAY_OF_WEEK_MAP = new HashMap<>();
   private static final Map<String, String> MONTH_MAP       = new HashMap<>();

   static
   {
      DAY_OF_WEEK_MAP.put("1", "Sunday");
      DAY_OF_WEEK_MAP.put("2", "Monday");
      DAY_OF_WEEK_MAP.put("3", "Tuesday");
      DAY_OF_WEEK_MAP.put("4", "Wednesday");
      DAY_OF_WEEK_MAP.put("5", "Thursday");
      DAY_OF_WEEK_MAP.put("6", "Friday");
      DAY_OF_WEEK_MAP.put("7", "Saturday");

      ////////////////////////////////
      // Quartz also allows SUN-SAT //
      ////////////////////////////////
      DAY_OF_WEEK_MAP.put("SUN", "Sunday");
      DAY_OF_WEEK_MAP.put("MON", "Monday");
      DAY_OF_WEEK_MAP.put("TUE", "Tuesday");
      DAY_OF_WEEK_MAP.put("WED", "Wednesday");
      DAY_OF_WEEK_MAP.put("THU", "Thursday");
      DAY_OF_WEEK_MAP.put("FRI", "Friday");
      DAY_OF_WEEK_MAP.put("SAT", "Saturday");

      MONTH_MAP.put("1", "January");
      MONTH_MAP.put("2", "February");
      MONTH_MAP.put("3", "March");
      MONTH_MAP.put("4", "April");
      MONTH_MAP.put("5", "May");
      MONTH_MAP.put("6", "June");
      MONTH_MAP.put("7", "July");
      MONTH_MAP.put("8", "August");
      MONTH_MAP.put("9", "September");
      MONTH_MAP.put("10", "October");
      MONTH_MAP.put("11", "November");
      MONTH_MAP.put("12", "December");

      ////////////////////////////////
      // Quartz also allows JAN-DEC //
      ////////////////////////////////
      MONTH_MAP.put("JAN", "January");
      MONTH_MAP.put("FEB", "February");
      MONTH_MAP.put("MAR", "March");
      MONTH_MAP.put("APR", "April");
      MONTH_MAP.put("MAY", "May");
      MONTH_MAP.put("JUN", "June");
      MONTH_MAP.put("JUL", "July");
      MONTH_MAP.put("AUG", "August");
      MONTH_MAP.put("SEP", "September");
      MONTH_MAP.put("OCT", "October");
      MONTH_MAP.put("NOV", "November");
      MONTH_MAP.put("DEC", "December");
   }

   /***************************************************************************
    **
    ***************************************************************************/
   public static String getDescription(String cronExpression) throws ParseException
   {
      String[] parts = cronExpression.split("\\s+");
      if(parts.length < 6 || parts.length > 7)
      {
         throw new ParseException("Invalid cron expression: " + cronExpression, 0);
      }

      String seconds    = parts[0];
      String minutes    = parts[1];
      String hours      = parts[2];
      String dayOfMonth = parts[3];
      String month      = parts[4];
      String dayOfWeek  = parts[5];
      String year       = parts.length == 7 ? parts[6] : "*";

      StringBuilder description = new StringBuilder();

      description.append("At ");
      description.append(describeTime(seconds, minutes, hours));
      description.append(", on ");
      description.append(describeDayOfMonth(dayOfMonth));
      description.append(" of ");
      description.append(describeMonth(month));
      description.append(", ");
      description.append(describeDayOfWeek(dayOfWeek));
      if(!year.equals("*"))
      {
         description.append(", in ").append(year);
      }
      description.append(".");

      return description.toString();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String describeTime(String seconds, String minutes, String hours)
   {
      return String.format("%s, %s, %s", describePart(seconds, "second"), describePart(minutes, "minute"), describePart(hours, "hour"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String describeDayOfMonth(String dayOfMonth)
   {
      if(dayOfMonth.equals("?"))
      {
         return "every day";
      }
      else if(dayOfMonth.equals("L"))
      {
         return "the last day";
      }
      else if(dayOfMonth.contains("W"))
      {
         return "the nearest weekday to day " + dayOfMonth.replace("W", "");
      }
      else
      {
         return (describePart(dayOfMonth, "day"));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String describeMonth(String month)
   {
      if(month.equals("*"))
      {
         return "every month";
      }
      else
      {
         String[]      months = month.split(",");
         StringBuilder result = new StringBuilder();
         for(String m : months)
         {
            result.append(MONTH_MAP.getOrDefault(m, m)).append(", ");
         }
         return result.substring(0, result.length() - 2);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String describeDayOfWeek(String dayOfWeek)
   {
      if(dayOfWeek.equals("?"))
      {
         return "every day of the week";
      }
      else if(dayOfWeek.equals("L"))
      {
         return "the last day of the week";
      }
      else if(dayOfWeek.contains("#"))
      {
         String[] parts = dayOfWeek.split("#");
         return String.format("the %s %s of the month", ordinal(parts[1]), DAY_OF_WEEK_MAP.getOrDefault(parts[0], parts[0]));
      }
      else if(dayOfWeek.contains("-"))
      {
         String[] parts = dayOfWeek.split("-");
         return String.format("from %s to %s", DAY_OF_WEEK_MAP.getOrDefault(parts[0], parts[0]), DAY_OF_WEEK_MAP.getOrDefault(parts[1], parts[1]));
      }
      else
      {
         String[]      days   = dayOfWeek.split(",");
         StringBuilder result = new StringBuilder();
         for(String d : days)
         {
            result.append(DAY_OF_WEEK_MAP.getOrDefault(d, d)).append(", ");
         }
         return result.substring(0, result.length() - 2);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String describePart(String part, String label)
   {
      if(part.equals("*"))
      {
         return "every " + label;
      }
      else if(part.contains("/"))
      {
         String[] parts = part.split("/");
         if(parts[0].equals("*"))
         {
            parts[0] = "0";
         }
         return String.format("every %s " + label + "s starting at %s", parts[1], parts[0]);
      }
      else if(part.contains(","))
      {
         if(label.equals("hour"))
         {
            String[]     parts = part.split(",");
            List<String> partList = Arrays.stream(parts).map(p -> hourToAmPm(p)).toList();
            return String.join(", ", partList);
         }
         else
         {
            if(label.equals("day"))
            {
               return "days " + part.replace(",", ", ");
            }
            else
            {
               return part.replace(",", ", ") + " " + label + "s";
            }
         }
      }
      else if(part.contains("-"))
      {
         String[] parts = part.split("-");
         if(label.equals("day"))
         {
            return String.format("%ss from %s to %s", label, parts[0], parts[1]);
         }
         else if(label.equals("hour"))
         {
            return String.format("from %s to %s", hourToAmPm(parts[0]), hourToAmPm(parts[1]));
         }
         else
         {
            return String.format("from %s to %s %s", parts[0], parts[1], label + "s");
         }
      }
      else
      {
         if(label.equals("day"))
         {
            return label + " " + part;
         }
         if(label.equals("hour"))
         {
            return hourToAmPm(part);
         }
         else
         {
            return part + " " + label + "s";
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String hourToAmPm(String part)
   {
      try
      {
         int hour = Integer.parseInt(part);
         return switch(hour)
         {
            case 0 -> "midnight";
            case 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 -> hour + " AM";
            case 12 -> "noon";
            case 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 -> (hour - 12) + " PM";
            default -> hour + " hours";
         };
      }
      catch(Exception e)
      {
         return part + " hours";
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static String ordinal(String number)
   {
      int n = Integer.parseInt(number);
      if(n >= 11 && n <= 13)
      {
         return n + "th";
      }

      return switch(n % 10)
      {
         case 1 -> n + "st";
         case 2 -> n + "nd";
         case 3 -> n + "rd";
         default -> n + "th";
      };
   }
}
