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

package com.kingsrook.qqq.backend.module.api.utils;


import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.module.api.utils.QueryStringParser
 *******************************************************************************/
class QueryStringParserTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      assertEquals(Map.of(), QueryStringParser.parseQueryStringSingleValuePerKey(null));
      assertEquals(Map.of(), QueryStringParser.parseQueryStringSingleValuePerKey(""));
      assertEquals(Map.of("foo", "bar"), QueryStringParser.parseQueryStringSingleValuePerKey("foo=bar"));
      assertEquals(Map.of("foo", "bar=baz"), QueryStringParser.parseQueryStringSingleValuePerKey("foo=bar=baz"));
      assertEquals(Map.of("foo", "bar", "baz", ""), QueryStringParser.parseQueryStringSingleValuePerKey("foo=bar&baz="));
      assertEquals(Map.of("foo", "bar", "baz", "1"), QueryStringParser.parseQueryStringSingleValuePerKey("foo=bar&baz=1"));

      assertEquals(Map.of(), QueryStringParser.parseQueryStringMultiValuePerKey(null));
      assertEquals(Map.of(), QueryStringParser.parseQueryStringMultiValuePerKey(""));
      assertEquals(Map.of("foo", List.of("bar")), QueryStringParser.parseQueryStringMultiValuePerKey("foo=bar"));
      assertEquals(Map.of("foo", List.of("bar"), "baz", List.of("")), QueryStringParser.parseQueryStringMultiValuePerKey("foo=bar&baz="));
      assertEquals(Map.of("foo", List.of("bar"), "baz", List.of("1")), QueryStringParser.parseQueryStringMultiValuePerKey("foo=bar&baz=1"));

      assertEquals(Map.of("foo", List.of("bar", "baz")), QueryStringParser.parseQueryStringMultiValuePerKey("foo=bar&foo=baz"));
      assertEquals(Map.of("foo", List.of("bar", "baz"), "bar", List.of("1")), QueryStringParser.parseQueryStringMultiValuePerKey("foo=bar&foo=baz&bar=1"));
   }

}