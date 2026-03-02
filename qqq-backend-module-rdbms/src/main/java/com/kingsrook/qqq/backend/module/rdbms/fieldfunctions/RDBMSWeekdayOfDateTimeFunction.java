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
import java.time.ZoneId;
import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.WeekdayOfDateTimeFunction;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.apache.commons.lang3.BooleanUtils;


/*******************************************************************************
 * RDBMS adapter for the {@link WeekdayOfDateTimeFunction}
 * that generates MySQL-compatible {@code WEEKDAY(CONVERT_TZ(col, ?, ?)) + 1} SQL expressions.
 *
 * <p>Converts from UTC to the resolved target time zone before extracting the weekday,
 * applying ISO-8601 numbering (Monday=1 … Sunday=7) by adding 1 to MySQL's result.</p>
 *******************************************************************************/
public class RDBMSWeekdayOfDateTimeFunction implements RDBMSFieldFunctionAdapterInterface
{

   /***************************************************************************
    * Returns a {@code WEEKDAY(CONVERT_TZ(col, ?, ?)) + 1} expression; the two
    * bind parameters (UTC source zone and target zone) are supplied by
    * {@link #getParams}.
    ***************************************************************************/
   @Override
   public String wrapColumnName(String escapedColumnName, FieldFunction fieldFunction)
   {
      ////////////////////////////////////////////////////////////////////
      // Mysql WEEKDAY returns values Monday=0, Sunday=6.               //
      // QQQ attempts to standardize on ISO-8601 (Monday=1, Sunday=7).  //
      // simplest way to map this is just adding 1 to WEEKDAY's output. //
      // this works for both a SELECT clause or a WHERE clause          //
      ////////////////////////////////////////////////////////////////////
      return "WEEKDAY(CONVERT_TZ(" + escapedColumnName + ", ?, ?)) + 1";
   }



   /***************************************************************************
    * Returns the two CONVERT_TZ bind parameters: the source timezone (always
    * "UTC") and the resolved target timezone (session, instance default, or
    * explicit override).
    ***************************************************************************/
   @Override
   public List<Serializable> getParams(FieldFunction fieldFunction)
   {
      Boolean useSessionZoneId = fieldFunction.getArgumentValueOrDefault(Boolean.class, WeekdayOfDateTimeFunction.PARAM_USE_SESSION_ZONE_ID);
      String  timeZoneId;
      if(useSessionZoneId)
      {
         timeZoneId = ObjectUtils.tryElse(() -> ValueUtils.getSessionOrInstanceZoneId().toString(), ZoneId.systemDefault().getId());
      }
      else
      {
         timeZoneId = QContext.getQInstance().getDefaultTimeZoneId();
      }

      String timeZoneIdArg = fieldFunction.getArgumentValueOrDefault(String.class, WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID);
      if(StringUtils.hasContent(timeZoneIdArg))
      {
         timeZoneId = ValueUtils.getValueAsString(timeZoneIdArg);
      }

      return List.of("UTC", timeZoneId);
   }



   /***************************************************************************
    * Returns the ORDER BY expression; when sortSundayFirst is true, applies
    * {@code % 7} so Sunday (value 7) sorts as 0 before Monday.
    ***************************************************************************/
   @Override
   public String wrapColumnNameForOrderBy(String escapedColumnName, FieldFunction fieldFunction)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////
      // to sort Sunday first, do a % 7, which puts Sunday(7) = 0, and leaves Monday(1) = 1, etc. //
      //////////////////////////////////////////////////////////////////////////////////////////////
      Boolean sundayFirst = fieldFunction.getArgumentValueOrDefault(Boolean.class, WeekdayOfDateTimeFunction.PARAM_SORT_SUNDAY_FIRST);
      return "(WEEKDAY(CONVERT_TZ(" + escapedColumnName + ", ?, ?)) + 1)" + (BooleanUtils.isTrue(sundayFirst) ? " % 7" : "");
   }
}
