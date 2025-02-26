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
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for CronDescriber 
 *******************************************************************************/
class CronDescriberTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws ParseException
   {
      assertEquals("At every second, every minute, every hour, on every day of every month, every day of the week.", CronDescriber.getDescription("* * * * * ?"));
      assertEquals("At 0 seconds, every minute, every hour, on every day of every month, every day of the week.", CronDescriber.getDescription("0 * * * * ?"));
      assertEquals("At 0 seconds, 0 minutes, every hour, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 * * * ?"));
      assertEquals("At 0 seconds, 0, 30 minutes, every hour, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0,30 * * * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 0 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, 1 AM, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 1 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, 11 AM, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 11 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, noon, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 12 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, 1 PM, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 13 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, 11 PM, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 23 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on day 10 of every month, every day of the week.", CronDescriber.getDescription("0 0 0 10 * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on days 10, 20 of every month, every day of the week.", CronDescriber.getDescription("0 0 0 10,20 * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on days from 10 to 15 of every month, every day of the week.", CronDescriber.getDescription("0 0 0 10-15 * ?"));
      assertEquals("At from 10 to 15 seconds, 0 minutes, midnight, on every day of every month, every day of the week.", CronDescriber.getDescription("10-15 0 0 * * ?"));
      assertEquals("At 30 seconds, 30 minutes, from 8 AM to 4 PM, on every day of every month, every day of the week.", CronDescriber.getDescription("30 30 8-16 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on every 3 days starting at 0 of every month, every day of the week.", CronDescriber.getDescription("0 0 0 */3 * ?"));
      assertEquals("At every 5 seconds starting at 0, 0 minutes, midnight, on every day of every month, every day of the week.", CronDescriber.getDescription("0/5 0 0 * * ?"));
      assertEquals("At 0 seconds, every 30 minutes starting at 3, midnight, on every day of every month, every day of the week.", CronDescriber.getDescription("0 3/30 0 * * ?"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on every day of every month, Monday, Wednesday, Friday.", CronDescriber.getDescription("0 0 0 * * MON,WED,FRI"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on every day of every month, from Monday to Friday.", CronDescriber.getDescription("0 0 0 * * MON-FRI"));
      assertEquals("At 0 seconds, 0 minutes, midnight, on every day of every month, Sunday, Saturday.", CronDescriber.getDescription("0 0 0 * * 1,7"));
      assertEquals("At 0 seconds, 0 minutes, 2 AM, 6 AM, noon, 4 PM, 8 PM, on every day of every month, every day of the week.", CronDescriber.getDescription("0 0 2,6,12,16,20 * * ?"));
      assertEquals("??", CronDescriber.getDescription("0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010"));
   }

}