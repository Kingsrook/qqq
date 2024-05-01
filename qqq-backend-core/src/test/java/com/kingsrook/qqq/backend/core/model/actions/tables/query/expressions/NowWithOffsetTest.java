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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions;


import java.time.temporal.ChronoUnit;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for NowWithOffset
 *******************************************************************************/
class NowWithOffsetTest extends BaseTest
{
   private static final long DAY_IN_MILLIS = 24 * 60 * 60 * 1000;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      long now = System.currentTimeMillis();

      long oneWeekAgoMillis = NowWithOffset.minus(1, ChronoUnit.WEEKS).evaluate().toEpochMilli();
      assertThat(oneWeekAgoMillis).isCloseTo(now - (7 * DAY_IN_MILLIS), Offset.offset(10_000L));

      long oneWeekFromNowMillis = NowWithOffset.plus(2, ChronoUnit.WEEKS).evaluate().toEpochMilli();
      assertThat(oneWeekFromNowMillis).isCloseTo(now + (14 * DAY_IN_MILLIS), Offset.offset(10_000L));

      long oneMonthAgoMillis = NowWithOffset.minus(1, ChronoUnit.MONTHS).evaluate().toEpochMilli();
      assertThat(oneMonthAgoMillis).isCloseTo(now - (30 * DAY_IN_MILLIS), Offset.offset(10_000L + 2 * DAY_IN_MILLIS));

      long oneMonthFromNowMillis = NowWithOffset.plus(2, ChronoUnit.MONTHS).evaluate().toEpochMilli();
      assertThat(oneMonthFromNowMillis).isCloseTo(now + (60 * DAY_IN_MILLIS), Offset.offset(10_000L + 3 * DAY_IN_MILLIS));

      long oneYearAgoMillis = NowWithOffset.minus(1, ChronoUnit.YEARS).evaluate().toEpochMilli();
      assertThat(oneYearAgoMillis).isCloseTo(now - (365 * DAY_IN_MILLIS), Offset.offset(10_000L + 2 * DAY_IN_MILLIS));

      long oneYearFromNowMillis = NowWithOffset.plus(2, ChronoUnit.YEARS).evaluate().toEpochMilli();
      assertThat(oneYearFromNowMillis).isCloseTo(now + (730 * DAY_IN_MILLIS), Offset.offset(10_000L + 3 * DAY_IN_MILLIS));
   }

}
