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


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for ListBuilder
 *******************************************************************************/
class ListBuilderTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSameAsListOf()
   {
      assertEquals(List.of(1), ListBuilder.of(1));
      assertEquals(List.of(1, 2), ListBuilder.of(1, 2));
      assertEquals(List.of(1, 2, 3), ListBuilder.of(1, 2, 3));
      assertEquals(List.of(1, 2, 3, 4), ListBuilder.of(1, 2, 3, 4));
      assertEquals(List.of(1, 2, 3, 4, 5), ListBuilder.of(1, 2, 3, 4, 5));
      assertEquals(List.of(1, 2, 3, 4, 5, 6), ListBuilder.of(1, 2, 3, 4, 5, 6));
      assertEquals(List.of(1, 2, 3, 4, 5, 6, 7), ListBuilder.of(1, 2, 3, 4, 5, 6, 7));
      assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), ListBuilder.of(1, 2, 3, 4, 5, 6, 7, 8));
      assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), ListBuilder.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
      assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), ListBuilder.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBetterThanListMapOf()
   {
      ///////////////////////////////
      // assert this doesn't throw //
      ///////////////////////////////
      List<Integer> list = ListBuilder.of(1, null, 3);

      ///////////////////////////////////////
      // this too, doesn't freaking throw. //
      ///////////////////////////////////////
      list.add(4);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBuilderMode()
   {
      List<String> builtList = new ListBuilder<String>().with("A").with("B").build();
      assertEquals(List.of("A", "B"), builtList);
      assertEquals(ArrayList.class, builtList.getClass());

      List<String> builtLinkedList = new ListBuilder<String>(new LinkedList<>()).with("A").with("B").build();
      assertEquals(List.of("A", "B"), builtLinkedList);
      assertEquals(LinkedList.class, builtLinkedList.getClass());
   }

}