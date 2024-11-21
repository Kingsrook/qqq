/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for CountingHash
 *******************************************************************************/
class CountingHashTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      CountingHash<String> countingHash = new CountingHash<>();

      assertNull(countingHash.get("a"));

      countingHash.add("a");
      assertEquals(1, countingHash.get("a"));

      countingHash.add("a");
      assertEquals(2, countingHash.get("a"));

      countingHash.add("a", 2);
      assertEquals(4, countingHash.get("a"));

      countingHash.add("b", 5);
      assertEquals(5, countingHash.get("b"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAlwaysMutable()
   {
      CountingHash<String> alwaysMutable = new CountingHash<>(Map.of("A", 5));
      alwaysMutable.add("A");
      alwaysMutable.add("B");
      assertEquals(6, alwaysMutable.get("A"));
      assertEquals(1, alwaysMutable.get("B"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPut()
   {
      CountingHash<String> alwaysMutable = new CountingHash<>(Map.of("A", 5));
      alwaysMutable.put("A", 25);
      assertEquals(25, alwaysMutable.get("A"));
      alwaysMutable.put("A");
      assertEquals(26, alwaysMutable.get("A"));
   }

}
