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
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ValueUtils
 *******************************************************************************/
class ValueUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsString() throws QValueException
   {
      assertNull(ValueUtils.getValueAsString(null));
      assertEquals("", ValueUtils.getValueAsString(""));
      assertEquals("  ", ValueUtils.getValueAsString("  "));
      assertEquals("A", ValueUtils.getValueAsString("A"));
      assertEquals("1", ValueUtils.getValueAsString("1"));
      assertEquals("1", ValueUtils.getValueAsString(1));
      assertEquals("1", ValueUtils.getValueAsString(1));
      assertEquals("1.10", ValueUtils.getValueAsString(new BigDecimal("1.10")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsBoolean() throws QValueException
   {
      assertNull(ValueUtils.getValueAsBoolean(null));
      assertTrue(ValueUtils.getValueAsBoolean("true"));
      assertTrue(ValueUtils.getValueAsBoolean("True"));
      assertTrue(ValueUtils.getValueAsBoolean("TRUE"));
      assertFalse(ValueUtils.getValueAsBoolean("false"));
      assertFalse(ValueUtils.getValueAsBoolean("yes"));
      assertFalse(ValueUtils.getValueAsBoolean("t"));
      assertFalse(ValueUtils.getValueAsBoolean(new Object()));
      assertFalse(ValueUtils.getValueAsBoolean(1));
      assertTrue(ValueUtils.getValueAsBoolean(new Object()
      {
         @Override
         public String toString()
         {
            return ("true");
         }
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsInteger() throws QValueException
   {
      assertNull(ValueUtils.getValueAsInteger(null));
      assertNull(ValueUtils.getValueAsInteger(""));
      assertNull(ValueUtils.getValueAsInteger(" "));
      assertEquals(1, ValueUtils.getValueAsInteger(1));
      assertEquals(1, ValueUtils.getValueAsInteger("1"));
      assertEquals(1_000, ValueUtils.getValueAsInteger("1,000"));
      assertEquals(1_000_000, ValueUtils.getValueAsInteger("1,000,000"));
      assertEquals(1, ValueUtils.getValueAsInteger(new BigDecimal(1)));
      assertEquals(1, ValueUtils.getValueAsInteger(new BigDecimal("1.00")));
      assertEquals(-1, ValueUtils.getValueAsInteger("-1.00"));
      assertEquals(1_000, ValueUtils.getValueAsInteger("1,000.00"));
      assertEquals(1_000, ValueUtils.getValueAsInteger(1000L));
      assertEquals(1, ValueUtils.getValueAsInteger(1.0F));
      assertEquals(1, ValueUtils.getValueAsInteger(1.0D));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger("a,b"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger(new Object()));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger(1_000_000_000_000L));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger(1.1F));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInteger(1.1D));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsBigDecimal() throws QValueException
   {
      assertNull(ValueUtils.getValueAsBigDecimal(null));
      assertNull(ValueUtils.getValueAsBigDecimal(""));
      assertNull(ValueUtils.getValueAsBigDecimal(" "));
      assertEquals(new BigDecimal("1"), ValueUtils.getValueAsBigDecimal(1));
      assertEquals(new BigDecimal("1"), ValueUtils.getValueAsBigDecimal("1"));
      assertEquals(new BigDecimal("1000"), ValueUtils.getValueAsBigDecimal("1,000"));
      assertEquals(new BigDecimal("1000000"), ValueUtils.getValueAsBigDecimal("1,000,000"));
      assertEquals(new BigDecimal("1"), ValueUtils.getValueAsBigDecimal(new BigDecimal(1)));
      assertEquals(new BigDecimal("1.00"), ValueUtils.getValueAsBigDecimal(new BigDecimal("1.00")));
      assertEquals(new BigDecimal("-1.00"), ValueUtils.getValueAsBigDecimal("-1.00"));
      assertEquals(new BigDecimal("1000.00"), ValueUtils.getValueAsBigDecimal("1,000.00"));
      assertEquals(new BigDecimal("1000"), ValueUtils.getValueAsBigDecimal(1000L));
      assertEquals(new BigDecimal("1"), ValueUtils.getValueAsBigDecimal(1.0F));
      assertEquals(new BigDecimal("1"), ValueUtils.getValueAsBigDecimal(1.0D));
      assertEquals(new BigDecimal("1000000000000"), ValueUtils.getValueAsBigDecimal(1_000_000_000_000L));
      //noinspection ConstantConditions
      assertEquals(0, new BigDecimal("1.1").compareTo(ValueUtils.getValueAsBigDecimal(1.1F).round(MathContext.DECIMAL32)));
      //noinspection ConstantConditions
      assertEquals(0, new BigDecimal("1.1").compareTo(ValueUtils.getValueAsBigDecimal(1.1D).round(MathContext.DECIMAL64)));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal("a,b"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal(new Object()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   @Test
   void testGetValueAsLocalDate() throws QValueException
   {
      LocalDate expected = LocalDate.of(1980, Month.MAY, 31);

      assertNull(ValueUtils.getValueAsLocalDate(null));
      assertNull(ValueUtils.getValueAsLocalDate(""));
      assertNull(ValueUtils.getValueAsLocalDate(" "));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(LocalDate.of(1980, 5, 31)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.sql.Date(80, 4, 31)));
      //noinspection MagicConstant
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.util.Date(80, 4, 31)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.util.Date(80, Calendar.MAY, 31)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.util.Date(80, Calendar.MAY, 31, 12, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.util.Date(80, Calendar.MAY, 31, 4, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new java.util.Date(80, Calendar.MAY, 31, 22, 0)));
      //noinspection MagicConstant
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new GregorianCalendar(1980, 4, 31)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(new GregorianCalendar(1980, Calendar.MAY, 31)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(LocalDateTime.of(1980, 5, 31, 12, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(LocalDateTime.of(1980, 5, 31, 4, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(LocalDateTime.of(1980, 5, 31, 22, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate(LocalDateTime.of(1980, Month.MAY, 31, 12, 0)));
      assertEquals(expected, ValueUtils.getValueAsLocalDate("1980-05-31"));
      assertEquals(expected, ValueUtils.getValueAsLocalDate("05/31/1980"));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsLocalDate("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsLocalDate("a,b"));
      assertThat(assertThrows(QValueException.class, () -> ValueUtils.getValueAsLocalDate("1980/05/31")).getMessage()).contains("parse");
      assertThat(assertThrows(QValueException.class, () -> ValueUtils.getValueAsLocalDate(new Object())).getMessage()).contains("Unsupported class");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   @Test
   void testGetValueAsInstant() throws QValueException
   {
      Instant expected = Instant.parse("1980-05-31T12:30:00Z");

      assertNull(ValueUtils.getValueAsInstant(null));
      assertNull(ValueUtils.getValueAsInstant(""));
      assertNull(ValueUtils.getValueAsInstant(" "));
      assertEquals(expected, ValueUtils.getValueAsInstant(expected));
      assertEquals(expected, ValueUtils.getValueAsInstant("1980-05-31T12:30:00Z"));

      ////////////////////////////
      // todo - time zone logic //
      ////////////////////////////
      // //noinspection MagicConstant
      // assertEquals(expected, ValueUtils.getValueAsInstant(new java.util.Date(80, 4, 31, 7, 30)));

      // //noinspection MagicConstant
      // assertEquals(expected, ValueUtils.getValueAsInstant(new GregorianCalendar(1980, 4, 31)));
      // assertEquals(expected, ValueUtils.getValueAsInstant(new GregorianCalendar(1980, Calendar.MAY, 31)));
      // // assertEquals(expected, ValueUtils.getValueAsInstant(InstantTime.of(1980, 5, 31, 12, 0)));
      // // assertEquals(expected, ValueUtils.getValueAsInstant(InstantTime.of(1980, 5, 31, 4, 0)));
      // // assertEquals(expected, ValueUtils.getValueAsInstant(InstantTime.of(1980, 5, 31, 22, 0)));
      // // assertEquals(expected, ValueUtils.getValueAsInstant(InstantTime.of(1980, Month.MAY, 31, 12, 0)));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant(new java.sql.Date(80, 4, 31)));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("a,b"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("1980/05/31"));
      assertThat(assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant(new Object())).getMessage()).contains("Unsupported class");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsLocalTime() throws QValueException
   {
      assertNull(ValueUtils.getValueAsInstant(null));
      assertNull(ValueUtils.getValueAsInstant(""));
      assertNull(ValueUtils.getValueAsInstant(" "));
      assertEquals(LocalTime.of(10, 42), ValueUtils.getValueAsLocalTime(LocalTime.of(10, 42)));
      assertEquals(LocalTime.of(10, 42, 59), ValueUtils.getValueAsLocalTime(LocalTime.of(10, 42, 59)));
      assertEquals(LocalTime.of(10, 42), ValueUtils.getValueAsLocalTime("10:42"));
      assertEquals(LocalTime.of(10, 42, 59), ValueUtils.getValueAsLocalTime("10:42:59"));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("a,b"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant("1980/05/31"));
      assertThat(assertThrows(QValueException.class, () -> ValueUtils.getValueAsInstant(new Object())).getMessage()).contains("Unsupported class");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetValueAsType()
   {
      assertEquals(1, ValueUtils.getValueAsType(Integer.class, "1"));
      assertEquals("1", ValueUtils.getValueAsType(String.class, 1));
      assertEquals(BigDecimal.ONE, ValueUtils.getValueAsType(BigDecimal.class, 1));
      assertEquals(true, ValueUtils.getValueAsType(Boolean.class, "true"));
      assertArrayEquals("a".getBytes(StandardCharsets.UTF_8), ValueUtils.getValueAsType(byte[].class, "a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsType(Serializable.class, 1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetFirstNonNull()
   {
      assertEquals(0, ValueUtils.getFirstNonNull(0));
      assertEquals(1, ValueUtils.getFirstNonNull(1, 2));
      assertEquals(2, ValueUtils.getFirstNonNull(null, 2));
      assertEquals(3, ValueUtils.getFirstNonNull(null, null, 3));

      assertNull(ValueUtils.getFirstNonNull());
      assertNull(ValueUtils.getFirstNonNull(new Object[] { }));
      assertNull(ValueUtils.getFirstNonNull(null));
      assertNull(ValueUtils.getFirstNonNull(null, null));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetSessionOrInstanceZoneId()
   {
      assertEquals(ZoneId.of("UTC"), ValueUtils.getSessionOrInstanceZoneId());

      QContext.getQInstance().setDefaultTimeZoneId("America/Chicago");
      assertEquals(ZoneId.of("America/Chicago"), ValueUtils.getSessionOrInstanceZoneId());

      QContext.getQSession().setValue(QSession.VALUE_KEY_USER_TIMEZONE_OFFSET_MINUTES, "-300");
      assertEquals(ZoneId.of("UTC-05:00"), ValueUtils.getSessionOrInstanceZoneId());
   }

}