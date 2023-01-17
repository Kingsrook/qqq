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
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.jsonLog;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.core.logging.LogUtils
 *******************************************************************************/
class LogUtilsTest extends BaseTest
{
   private static final QLogger LOG = QLogger.getLogger(LogUtilsTest.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      ////////////////
      // null cases //
      ////////////////
      assertEquals("{}", jsonLog());
      assertEquals("{}", jsonLog((LogPair) null));
      assertEquals("{}", jsonLog((LogPair[]) null));
      assertEquals("""
         {"null":null}""", jsonLog(logPair(null, (LogPair) null)));
      assertEquals("""
         {"null":null}""", jsonLog(logPair(null, (LogPair[]) null)));

      //////////////
      // escaping //
      //////////////
      assertEquals("""
         {"f.o.o":"b\\"a\\"r"}""", jsonLog(logPair("f\"o\"o", "b\"a\"r")));

      //////////////////
      // normal stuff //
      //////////////////
      assertEquals("""
         {"foo":"bar"}""", jsonLog(logPair("foo", "bar")));

      assertEquals("""
         {"bar":1}""", jsonLog(logPair("bar", 1)));

      assertEquals("""
         {"baz":3.50}""", jsonLog(logPair("baz", new BigDecimal("3.50"))));

      ////////////////
      // many pairs //
      ////////////////
      assertEquals("""
         {"foo":"bar","bar":1,"baz":3.50}""", jsonLog(logPair("foo", "bar"), logPair("bar", 1), logPair("baz", new BigDecimal("3.50"))));

      //////////////////
      // nested pairs //
      //////////////////
      assertEquals("""
         {"foo":{"bar":1,"baz":2}}""", jsonLog(logPair("foo", logPair("bar", 1), logPair("baz", 2))));

      assertEquals("""
         {
            "foo":
            {
               "bar":1,
               "baz":2
            }
         }""".replaceAll("\\s", ""), jsonLog(logPair("foo", logPair("bar", 1), logPair("baz", 2))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLog2()
   {
      LOG.info(jsonLog(logPair("message", "Doing a thing"), logPair("trackingNo", "1Z123123123"), logPair("Order", logPair("id", 89101324), logPair("client", "ACME"))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLogging()
   {
      LOG.info(jsonLog(logPair("message", "Doing a thing"), logPair("trackingNo", "1Z123123123"), logPair("Order", logPair("id", 89101324), logPair("client", "ACME"))));
   }

}