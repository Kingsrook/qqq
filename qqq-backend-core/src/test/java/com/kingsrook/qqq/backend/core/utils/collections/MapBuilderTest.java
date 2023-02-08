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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MapBuilder
 *******************************************************************************/
class MapBuilderTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSameAsMapOf()
   {
      assertEquals(Map.of("1", 1), MapBuilder.of("1", 1));
      assertEquals(Map.of("1", 1, "2", 2), MapBuilder.of("1", 1, "2", 2));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3), MapBuilder.of("1", 1, "2", 2, "3", 3));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3, "4", 4), MapBuilder.of("1", 1, "2", 2, "3", 3, "4", 4));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5), MapBuilder.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6), MapBuilder.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7), MapBuilder.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7));
      assertEquals(Map.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7, "8", 8), MapBuilder.of("1", 1, "2", 2, "3", 3, "4", 4, "5", 5, "6", 6, "7", 7, "8", 8));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBetterThanMapOf()
   {
      ///////////////////////////////
      // assert this doesn't throw //
      ///////////////////////////////
      Map<String, Object> map = MapBuilder.of("1", null);

      // this too, doesn't freaking throw. //
      ///////////////////////////////////////
      map.put("2", null);
   }

}