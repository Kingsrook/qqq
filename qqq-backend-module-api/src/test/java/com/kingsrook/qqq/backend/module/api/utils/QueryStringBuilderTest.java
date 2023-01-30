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

package com.kingsrook.qqq.backend.module.api.utils;


import com.kingsrook.qqq.backend.module.api.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QueryStringBuilder
 *******************************************************************************/
class QueryStringBuilderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEmpty()
   {
      assertEquals("", new QueryStringBuilder().toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleWithoutAnd()
   {
      QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
      queryStringBuilder.addPair("foo", 1);
      assertEquals("?foo=1", queryStringBuilder.toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleWithAnd()
   {
      QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
      queryStringBuilder.addPair("foo", 1);
      queryStringBuilder.addPair("bar=2");
      assertEquals("?foo=1&bar=2", queryStringBuilder.toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFluent()
   {
      assertEquals("?foo=1&bar=2", new QueryStringBuilder()
         .withPair("foo", 1)
         .withPair("bar=2")
         .toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEncoding()
   {
      QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
      queryStringBuilder.addPair("percent", "99%"); // % should get encoded to %25
      queryStringBuilder.addPair("and=this%26that"); // %26 should stay as-is -- not be re-encoded.
      assertEquals("?percent=99%25&and=this%26that", queryStringBuilder.toQueryString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPairWithoutValue()
   {
      QueryStringBuilder queryStringBuilder = new QueryStringBuilder();
      queryStringBuilder.addPair("name1");
      queryStringBuilder.addPair("name2=");
      assertEquals("?name1=&name2=", queryStringBuilder.toString());
   }

}