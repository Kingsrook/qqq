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

package com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations;


import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunctionParameter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunctionType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunctionTypeIdentifier;


/***************************************************************************
 * Follows ISO-8601 Standard, with Monday=1, Sunday=7.
 *
 * <p>Note that this matches Java DayOfWeek (but differs from Calendar and Date,
 * as well as javascript)</p>
 ***************************************************************************/
public class WeekdayOfDateTimeFunction implements FieldFunctionType
{
   public static final FieldFunctionTypeIdentifier IDENTIFIER = () -> "WeekdayOfDateTime";

   public static final String PARAM_SORT_SUNDAY_FIRST   = "sortSundayFirst";
   public static final String PARAM_TIME_ZONE_ID        = "timeZoneId";
   public static final String PARAM_USE_SESSION_ZONE_ID = "useSessionZoneId";



   /***************************************************************************
    * Returns this function's identifier (WeekdayOfDateTime).
    ***************************************************************************/
   public FieldFunctionTypeIdentifier getIdentifier()
   {
      return (IDENTIFIER);
   }



   /***************************************************************************
    * Returns the set of DATE_TIME field types that this function may be applied to.
    ***************************************************************************/
   @Override
   public Set<QFieldType> getAllowedFieldTypes()
   {
      return Set.of(QFieldType.DATE_TIME);
   }



   /***************************************************************************
    * Returns the parameter definitions: timeZoneId (optional override), sortSundayFirst (locale-aware default), and useSessionZoneId (default true).
    ***************************************************************************/
   @Override
   public List<FieldFunctionParameter> getParameters()
   {
      DayOfWeek firstDay = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

      return List.of(
         new FieldFunctionParameter().withName(PARAM_TIME_ZONE_ID).withType(QFieldType.STRING).withIsRequired(false),
         new FieldFunctionParameter().withName(PARAM_SORT_SUNDAY_FIRST).withType(QFieldType.BOOLEAN).withIsRequired(false).withDefaultValue(DayOfWeek.SUNDAY.equals(firstDay)),
         new FieldFunctionParameter().withName(PARAM_USE_SESSION_ZONE_ID).withType(QFieldType.BOOLEAN).withIsRequired(false).withDefaultValue(true)
      );
   }



   /***************************************************************************
    * Returns INTEGER — the ISO-8601 day-of-week number (Monday=1 … Sunday=7) in the resolved time zone.
    ***************************************************************************/
   @Override
   public QFieldType getReturnType()
   {
      return QFieldType.INTEGER;
   }



   /***************************************************************************
    * Converts the datetime field value to the specified time zone and returns
    * the ISO-8601 day-of-week value, or null if the value is null.
    ***************************************************************************/
   @Override
   public Integer apply(FieldFunction fieldFunction, QRecord record) throws QValueException
   {
      Instant sourceValue = record.getValueInstant(fieldFunction.getFieldName());
      if(sourceValue == null)
      {
         return (null);
      }

      String timeZoneId = fieldFunction.getArgumentValueOrDefault(String.class, PARAM_TIME_ZONE_ID);
      ZonedDateTime zonedDateTime = sourceValue.atZone(ZoneId.of(timeZoneId));

      return zonedDateTime.getDayOfWeek().getValue();
   }



   /***************************************************************************
    * Returns the day-of-week value adjusted for sort ordering. When
    * sortSundayFirst is true, Sunday (value 7) is mapped to 0 via modulo so
    * it sorts before Monday.
    ***************************************************************************/
   @Override
   public Serializable applyForSorting(FieldFunction fieldFunction, QRecord record) throws QValueException, QException
   {
      Integer baseValue = apply(fieldFunction, record);
      if(baseValue == null)
      {
         return (null);
      }

      if(fieldFunction.getArgumentValueOrDefault(Boolean.class, PARAM_SORT_SUNDAY_FIRST))
      {
         return baseValue % 7;
      }
      else
      {
         return baseValue;
      }
   }

}
