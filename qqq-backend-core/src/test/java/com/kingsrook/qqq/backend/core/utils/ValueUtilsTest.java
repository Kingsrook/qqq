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
import java.math.MathContext;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for ValueUtils
 *******************************************************************************/
class ValueUtilsTest
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
      assertEquals(0, new BigDecimal("1.1").compareTo(ValueUtils.getValueAsBigDecimal(1.1F).round(MathContext.DECIMAL32)));
      assertEquals(0, new BigDecimal("1.1").compareTo(ValueUtils.getValueAsBigDecimal(1.1D).round(MathContext.DECIMAL64)));

      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal("a"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal("a,b"));
      assertThrows(QValueException.class, () -> ValueUtils.getValueAsBigDecimal(new Object()));
   }

}