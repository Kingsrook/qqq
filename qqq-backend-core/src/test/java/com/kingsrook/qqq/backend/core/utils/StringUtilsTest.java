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


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for StringUtils
 **
 *******************************************************************************/
class StringUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_hasContent()
   {
      assertFalse(StringUtils.hasContent(""));
      assertFalse(StringUtils.hasContent("   "));
      assertFalse(StringUtils.hasContent(" \n  "));
      assertFalse(StringUtils.hasContent(null));
      assertTrue(StringUtils.hasContent("a"));
      assertTrue(StringUtils.hasContent(" a "));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nvlString()
   {
      assertEquals("foo", StringUtils.nvl("foo", "bar"));
      assertEquals("bar", StringUtils.nvl(null, "bar"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nvlObject()
   {
      assertEquals("1701", StringUtils.nvl(1701, "bar"));
      assertEquals("bar", StringUtils.nvl((Integer) null, "bar"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_safeAppend()
   {
      assertEquals("Foo", StringUtils.safeAppend("Foo", null));
      assertEquals("Foo", StringUtils.safeAppend(null, "Foo"));
      assertEquals("FooBar", StringUtils.safeAppend("Foo", "Bar"));
      assertEquals("", StringUtils.safeAppend(null, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_allCapsToMixedCase()
   {
      assertNull(StringUtils.allCapsToMixedCase(null));
      assertEquals("Foo", StringUtils.allCapsToMixedCase("FOO"));
      assertEquals("Foo Bar", StringUtils.allCapsToMixedCase("FOO BAR"));
      assertEquals("Foo bar", StringUtils.allCapsToMixedCase("FOO bar"));
      assertEquals("Foo bar", StringUtils.allCapsToMixedCase("FOo bar"));
      assertEquals("Foo Bar", StringUtils.allCapsToMixedCase("FOo BAr"));
      assertEquals("foo bar", StringUtils.allCapsToMixedCase("foo bar"));
      assertEquals("Foo Bar", StringUtils.allCapsToMixedCase("FOO_BAR"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_safeTruncate2()
   {
      assertNull(StringUtils.safeTruncate(null, 5));
      assertEquals("123", StringUtils.safeTruncate("123", 5));
      assertEquals("12345", StringUtils.safeTruncate("1234567", 5));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_safeTruncate3()
   {
      assertNull(StringUtils.safeTruncate(null, 5, "..."));
      assertEquals("123", StringUtils.safeTruncate("123", 5, "..."));
      assertEquals("12345", StringUtils.safeTruncate("12345", 5, "..."));
      assertEquals("12...", StringUtils.safeTruncate("123456", 5, "..."));
      assertEquals("12...", StringUtils.safeTruncate("1234567890", 5, "..."));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_safeTrim()
   {
      assertNull(StringUtils.safeTrim(null));
      assertEquals("foo", StringUtils.safeTrim("foo "));
      assertEquals("foo", StringUtils.safeTrim(" foo "));
      assertEquals("foo", StringUtils.safeTrim(" foo"));
      assertEquals("foo", StringUtils.safeTrim("    foo   \n   "));
      assertEquals("foo", StringUtils.safeTrim("\nfoo  \r  \n   "));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_join()
   {
      assertNull(StringUtils.join(",", null));
      assertEquals("1", StringUtils.join(",", List.of(1)));
      assertEquals("", StringUtils.join(",", List.of()));
      assertEquals("1,2,3", StringUtils.join(",", List.of(1, 2, 3)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_joinWithCommasAndAnd()
   {
      assertNull(StringUtils.joinWithCommasAndAnd(null));
      assertEquals("", StringUtils.joinWithCommasAndAnd(List.of()));
      assertEquals("A", StringUtils.joinWithCommasAndAnd(List.of("A")));
      assertEquals("A and B", StringUtils.joinWithCommasAndAnd(List.of("A", "B")));
      assertEquals("A, B, and C", StringUtils.joinWithCommasAndAnd(List.of("A", "B", "C")));
      assertEquals("A, B, C, and D", StringUtils.joinWithCommasAndAnd(List.of("A", "B", "C", "D")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_ltrim()
   {
      assertEquals("", StringUtils.ltrim(null));
      assertEquals("123", StringUtils.ltrim("123"));
      assertEquals("123", StringUtils.ltrim(" 123"));
      assertEquals("123", StringUtils.ltrim("    123"));
      assertEquals("123", StringUtils.ltrim("  \n\n  123"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_rtrim()
   {
      assertEquals("", StringUtils.rtrim(null));
      assertEquals("123", StringUtils.rtrim("123"));
      assertEquals("123", StringUtils.rtrim("123 "));
      assertEquals("123", StringUtils.rtrim("123    "));
      assertEquals("123", StringUtils.rtrim("123  \n\n  "));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_pluralCollection()
   {
      assertEquals("s", StringUtils.plural(List.of()));
      assertEquals("", StringUtils.plural(List.of(1)));
      assertEquals("s", StringUtils.plural(List.of(1, 2)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_pluralInteger()
   {
      assertEquals("s", StringUtils.plural(0));
      assertEquals("", StringUtils.plural(1));
      assertEquals("s", StringUtils.plural(2));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_pluralCollectionStringString()
   {
      assertEquals("es", StringUtils.plural(List.of(), "", "es"));
      assertEquals("", StringUtils.plural(List.of(1), "", "es"));
      assertEquals("es", StringUtils.plural(List.of(1, 2), "", "es"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_pluralIntegerStringString()
   {
      assertEquals("es", StringUtils.plural(0, "", "es"));
      assertEquals("", StringUtils.plural(1, "", "es"));
      assertEquals("es", StringUtils.plural(2, "", "es"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLcFirst()
   {
      assertNull(StringUtils.lcFirst(null));
      assertEquals("", StringUtils.lcFirst(""));
      assertEquals(" ", StringUtils.lcFirst(" "));
      assertEquals("a", StringUtils.lcFirst("A"));
      assertEquals("1", StringUtils.lcFirst("1"));
      assertEquals("a", StringUtils.lcFirst("a"));
      assertEquals("aB", StringUtils.lcFirst("AB"));
      assertEquals("aBc", StringUtils.lcFirst("ABc"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUcFirst()
   {
      assertNull(StringUtils.ucFirst(null));
      assertEquals("", StringUtils.ucFirst(""));
      assertEquals(" ", StringUtils.ucFirst(" "));
      assertEquals("A", StringUtils.ucFirst("A"));
      assertEquals("1", StringUtils.ucFirst("1"));
      assertEquals("A", StringUtils.ucFirst("a"));
      assertEquals("Ab", StringUtils.ucFirst("ab"));
      assertEquals("Abc", StringUtils.ucFirst("abc"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPluralFormat()
   {
      assertEquals("Apple", StringUtils.pluralFormat(1, "Apple{,s}"));
      assertEquals("Apples", StringUtils.pluralFormat(0, "Apple{,s}"));
      assertEquals("Apples", StringUtils.pluralFormat(2, "Apple{,s}"));

      assertEquals("Apple and Orange", StringUtils.pluralFormat(1, "Apple{,s} and Orange{,s}"));
      assertEquals("Apples and Oranges", StringUtils.pluralFormat(2, "Apple{,s} and Orange{,s}"));

      assertEquals("Apple was eaten", StringUtils.pluralFormat(1, "Apple{,s} {was,were} eaten"));
      assertEquals("Apples were eaten", StringUtils.pluralFormat(2, "Apple{,s} {was,were} eaten"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEmptyToNull()
   {
      assertNull(StringUtils.emptyToNull(null));
      assertNull(StringUtils.emptyToNull(""));
      assertNull(StringUtils.emptyToNull(" "));
      assertNull(StringUtils.emptyToNull("  "));
      assertEquals("a", StringUtils.emptyToNull("a"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAppendIncrementingSuffix()
   {
      assertEquals("test (1)", StringUtils.appendIncrementingSuffix("test"));
      assertEquals("test (2)", StringUtils.appendIncrementingSuffix("test (1)"));
      assertEquals("test (a) (1)", StringUtils.appendIncrementingSuffix("test (a)"));
      assertEquals("test (a32) (1)", StringUtils.appendIncrementingSuffix("test (a32)"));
      assertEquals("test ((2)) (1)", StringUtils.appendIncrementingSuffix("test ((2))"));
      assertEquals("test ((2)) (101)", StringUtils.appendIncrementingSuffix("test ((2)) (100)"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSafeEqualsIgnoreCase()
   {
      assertTrue(StringUtils.safeEqualsIgnoreCase(null, null));
      assertFalse(StringUtils.safeEqualsIgnoreCase("a", null));
      assertFalse(StringUtils.safeEqualsIgnoreCase(null, "a"));
      assertTrue(StringUtils.safeEqualsIgnoreCase("a", "a"));
      assertTrue(StringUtils.safeEqualsIgnoreCase("A", "a"));
      assertFalse(StringUtils.safeEqualsIgnoreCase("a", "b"));
      assertTrue(StringUtils.safeEqualsIgnoreCase("timothy d. chamberlain", "TIMOThy d. chaMberlain"));
      assertTrue(StringUtils.safeEqualsIgnoreCase("timothy d. chamberlain", "timothy d. chamberlain"));
   }

}
