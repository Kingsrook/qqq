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


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.FieldFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit tests for {@link WeekdayOfDateTimeFunction}
 *******************************************************************************/
class WeekdayOfDateTimeFunctionTest extends BaseTest
{
   private final WeekdayOfDateTimeFunction function = new WeekdayOfDateTimeFunction();



   /***************************************************************************
    ** Basic test with a known timestamp in UTC
    ***************************************************************************/
   @Test
   void testBasic() throws Exception
   {
      /////////////////////////////////////////
      // 2026-02-23 12:00:00 UTC is a Monday //
      /////////////////////////////////////////
      Instant mondayNoonUtc = LocalDateTime.of(2026, 2, 23, 12, 0, 0)
         .toInstant(ZoneOffset.UTC);

      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "UTC"));

      assertEquals(1, function.apply(ff, new QRecord().withValue("timestamp", mondayNoonUtc)));
   }



   /***************************************************************************
    ** Test that timezone affects the weekday result.
    ** 23:00 UTC on Monday = 01:00 Tuesday in Europe/Athens (UTC+2)
    ***************************************************************************/
   @Test
   void testTimezoneBoundary() throws Exception
   {
      Instant mondayLateUtc = LocalDateTime.of(2026, 2, 23, 23, 0, 0)
         .toInstant(ZoneOffset.UTC);

      ////////////////////////////////
      // In UTC, this is Monday (1) //
      ////////////////////////////////
      FieldFunction ffUtc = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "UTC"));

      assertEquals(1, function.apply(ffUtc, new QRecord().withValue("timestamp", mondayLateUtc)));

      ///////////////////////////////////////////////////
      // In Europe/Athens (UTC+2), this is Tuesday (2) //
      ///////////////////////////////////////////////////
      FieldFunction ffAthens = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "Europe/Athens"));

      assertEquals(2, function.apply(ffAthens, new QRecord().withValue("timestamp", mondayLateUtc)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testNullSource() throws Exception
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "UTC"));

      assertNull(function.apply(ff, new QRecord().withValue("timestamp", null)));
   }



   /***************************************************************************
    ** With sundayFirst=true, applyForSorting should map Sunday(7) -> 0
    ***************************************************************************/
   @Test
   void testApplyForSortingSundayFirst() throws Exception
   {
      //////////////////////////////////////
      // 2026-03-01 12:00 UTC is a Sunday //
      //////////////////////////////////////
      Instant sundayNoonUtc = LocalDateTime.of(2026, 3, 1, 12, 0, 0)
         .toInstant(ZoneOffset.UTC);

      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(
            WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "UTC",
            WeekdayOfDateTimeFunction.PARAM_SORT_SUNDAY_FIRST, true
         ));

      assertEquals(0, function.applyForSorting(ff, new QRecord().withValue("timestamp", sundayNoonUtc)));

      /////////////////////////////////////////////////
      // Monday should sort as 1                      //
      /////////////////////////////////////////////////
      Instant mondayNoonUtc = LocalDateTime.of(2026, 2, 23, 12, 0, 0)
         .toInstant(ZoneOffset.UTC);

      assertEquals(1, function.applyForSorting(ff, new QRecord().withValue("timestamp", mondayNoonUtc)));
   }



   /***************************************************************************
    ** With sundayFirst=false, applyForSorting should return raw value
    ***************************************************************************/
   @Test
   void testApplyForSortingMondayFirst() throws Exception
   {
      Instant sundayNoonUtc = LocalDateTime.of(2026, 3, 1, 12, 0, 0)
         .toInstant(ZoneOffset.UTC);

      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(WeekdayOfDateTimeFunction.IDENTIFIER)
         .withFieldName("timestamp")
         .withArguments(Map.of(
            WeekdayOfDateTimeFunction.PARAM_TIME_ZONE_ID, "UTC",
            WeekdayOfDateTimeFunction.PARAM_SORT_SUNDAY_FIRST, false
         ));

      assertEquals(7, function.applyForSorting(ff, new QRecord().withValue("timestamp", sundayNoonUtc)));
   }

}
