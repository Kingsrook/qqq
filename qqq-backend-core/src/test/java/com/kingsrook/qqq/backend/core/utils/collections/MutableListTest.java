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


import java.util.LinkedList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.core.utils.collections.MutableList
 *******************************************************************************/
class MutableListTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      List<Integer> list = new MutableList<>(List.of(1));
      list.add(2);
      list.clear();

      list = new MutableList<>(List.of(3));
      list.add(0, 4);
      list.remove(0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNullInput()
   {
      List<Integer> list = new MutableList<>(null);
      list.add(1);
      assertEquals(1, list.size());

      MutableList<Integer> mutableList = new MutableList<>(null, LinkedList::new);
      mutableList.add(1);
      assertEquals(1, mutableList.size());
      assertEquals(LinkedList.class, mutableList.getUnderlyingList().getClass());
   }

}