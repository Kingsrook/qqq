/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for CollectionUtils
 **
 *******************************************************************************/
class CollectionUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeIsEmpty_Map()
   {
      assertTrue(CollectionUtils.nullSafeIsEmpty(Collections.emptyMap()));
      assertTrue(CollectionUtils.nullSafeIsEmpty(new HashMap<>()));
      assertTrue(CollectionUtils.nullSafeIsEmpty((Map<?, ?>) null));
      Map<String, Integer> myMap = new HashMap<>();
      assertTrue(CollectionUtils.nullSafeIsEmpty(myMap));
      myMap.put("A", 1);
      assertFalse(CollectionUtils.nullSafeIsEmpty(myMap));
      myMap.clear();
      assertTrue(CollectionUtils.nullSafeIsEmpty(myMap));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeIsEmpty_Collection()
   {
      assertTrue(CollectionUtils.nullSafeIsEmpty(Collections.emptyList()));
      assertTrue(CollectionUtils.nullSafeIsEmpty(new ArrayList<>()));
      assertTrue(CollectionUtils.nullSafeIsEmpty((List<?>) null));
      List<String> myList = new ArrayList<>();
      assertTrue(CollectionUtils.nullSafeIsEmpty(myList));
      myList.add("A");
      assertFalse(CollectionUtils.nullSafeIsEmpty(myList));
      myList.clear();
      assertTrue(CollectionUtils.nullSafeIsEmpty(myList));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeHasContents_Map()
   {
      assertFalse(CollectionUtils.nullSafeHasContents(Collections.emptyMap()));
      assertFalse(CollectionUtils.nullSafeHasContents(new HashMap<>()));
      assertFalse(CollectionUtils.nullSafeHasContents((Map<?, ?>) null));
      Map<String, Integer> myMap = new HashMap<>();
      assertFalse(CollectionUtils.nullSafeHasContents(myMap));
      myMap.put("A", 1);
      assertTrue(CollectionUtils.nullSafeHasContents(myMap));
      myMap.clear();
      assertFalse(CollectionUtils.nullSafeHasContents(myMap));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeHasContents_Collection()
   {
      assertFalse(CollectionUtils.nullSafeHasContents(Collections.emptyList()));
      assertFalse(CollectionUtils.nullSafeHasContents(new ArrayList<>()));
      assertFalse(CollectionUtils.nullSafeHasContents((List<?>) null));
      List<String> myList = new ArrayList<>();
      assertFalse(CollectionUtils.nullSafeHasContents(myList));
      myList.add("A");
      assertTrue(CollectionUtils.nullSafeHasContents(myList));
      myList.clear();
      assertFalse(CollectionUtils.nullSafeHasContents(myList));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeSize_Map()
   {
      assertEquals(0, CollectionUtils.nullSafeSize(Collections.emptyMap()));
      assertEquals(0, CollectionUtils.nullSafeSize(new HashMap<>()));
      assertEquals(0, CollectionUtils.nullSafeSize((Map<?, ?>) null));
      Map<String, Integer> myMap = new HashMap<>();
      assertEquals(0, CollectionUtils.nullSafeSize(myMap));
      myMap.put("A", 1);
      assertEquals(1, CollectionUtils.nullSafeSize(myMap));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_nullSafeSize_Collection()
   {
      assertEquals(0, CollectionUtils.nullSafeSize(Collections.emptyList()));
      assertEquals(0, CollectionUtils.nullSafeSize(new ArrayList<>()));
      assertEquals(0, CollectionUtils.nullSafeSize((List<?>) null));
      List<String> myList = new ArrayList<>();
      assertEquals(0, CollectionUtils.nullSafeSize(myList));
      myList.add("A");
      assertEquals(1, CollectionUtils.nullSafeSize(myList));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("ConstantConditions")
   @Test
   void test_addAllToMap()
   {
      Map<String, Integer> to = new HashMap<>();
      Map<String, Integer> from = new HashMap<>();

      assertThrows(NullPointerException.class, () -> CollectionUtils.addAllToMap(null, null));
      assertThrows(NullPointerException.class, () -> CollectionUtils.addAllToMap(to, null));

      // this case does not currently throw - capture that fact here in case we change it.
      CollectionUtils.addAllToMap(null, from);

      CollectionUtils.addAllToMap(to, from);
      assertEquals(0, to.size());

      from.put("A", 1);
      CollectionUtils.addAllToMap(to, from);
      assertEquals(1, to.size());
      assertEquals(1, to.get("A"));

      from.put("B", 2);
      CollectionUtils.addAllToMap(to, from);
      assertEquals(2, to.size());
      assertEquals(1, to.get("A"));
      assertEquals(2, to.get("B"));

      from.put("B", 3);
      CollectionUtils.addAllToMap(to, from);
      assertEquals(2, to.size());
      assertEquals(1, to.get("A"));
      assertEquals(3, to.get("B"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_listToMap()
   {
      assertNull(CollectionUtils.listToMap(null, null));

      List<String> myList = new ArrayList<>();
      myList.add("Apple");
      myList.add("Banana");
      assertThrows(NullPointerException.class, () -> CollectionUtils.listToMap(myList, null));

      Map<String, String> myMap = CollectionUtils.listToMap(myList, first());
      assertEquals(2, myMap.size());
      assertEquals("Apple", myMap.get("A"));
      assertEquals("Banana", myMap.get("B"));

      // confirm what a clobbered key does
      myList.add("Airplane");
      myMap = CollectionUtils.listToMap(myList, first());
      assertEquals(2, myMap.size());
      assertEquals("Airplane", myMap.get("A"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_listToMap_valueFunction()
   {
      assertNull(CollectionUtils.listToMap(null, null, null));

      List<String> myList = new ArrayList<>();
      myList.add("Apple");
      myList.add("Banana");
      assertThrows(NullPointerException.class, () -> CollectionUtils.listToMap(myList, null, null));

      Map<String, String> myMap = CollectionUtils.listToMap(myList, first(), rest());
      assertEquals(2, myMap.size());
      assertEquals("pple", myMap.get("A"));
      assertEquals("anana", myMap.get("B"));

      // confirm what a clobbered key does
      myList.add("Airplane");
      myMap = CollectionUtils.listToMap(myList, first(), rest());
      assertEquals(2, myMap.size());
      assertEquals("irplane", myMap.get("A"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_listToListingHash()
   {
      assertNull(CollectionUtils.listToListingHash(null, null));

      List<String> myList = new ArrayList<>();
      myList.add("Apple");
      myList.add("Banana");
      assertThrows(NullPointerException.class, () -> CollectionUtils.listToListingHash(myList, null));

      ListingHash<String, String> myListingHash = CollectionUtils.listToListingHash(myList, first());
      assertEquals(2, myListingHash.size());
      assertEquals(1, myListingHash.get("A").size());
      assertEquals("Apple", myListingHash.get("A").get(0));
      assertEquals(1, myListingHash.get("B").size());
      assertEquals("Banana", myListingHash.get("B").get(0));

      myList.add("Airplane");
      myListingHash = CollectionUtils.listToListingHash(myList, first());
      assertEquals(2, myListingHash.size());
      assertEquals(2, myListingHash.get("A").size());
      assertEquals("Apple", myListingHash.get("A").get(0));
      assertEquals("Airplane", myListingHash.get("A").get(1));
      assertEquals(1, myListingHash.get("B").size());
      assertEquals("Banana", myListingHash.get("B").get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_listToListingHash_valueFunction()
   {
      assertNull(CollectionUtils.listToListingHash(null, null, null));

      List<String> myList = new ArrayList<>();
      myList.add("Apple");
      myList.add("Banana");
      assertThrows(NullPointerException.class, () -> CollectionUtils.listToListingHash(myList, null, null));

      ListingHash<String, String> myListingHash = CollectionUtils.listToListingHash(myList, first(), rest());
      assertEquals(2, myListingHash.size());
      assertEquals(1, myListingHash.get("A").size());
      assertEquals("pple", myListingHash.get("A").get(0));
      assertEquals(1, myListingHash.get("B").size());
      assertEquals("anana", myListingHash.get("B").get(0));

      myList.add("Airplane");
      myListingHash = CollectionUtils.listToListingHash(myList, first(), rest());
      assertEquals(2, myListingHash.size());
      assertEquals(2, myListingHash.get("A").size());
      assertEquals("pple", myListingHash.get("A").get(0));
      assertEquals("irplane", myListingHash.get("A").get(1));
      assertEquals(1, myListingHash.get("B").size());
      assertEquals("anana", myListingHash.get("B").get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_listTo2LevelMap()
   {
      assertNull(CollectionUtils.listTo2LevelMap(null, null, null));

      List<String> myList = new ArrayList<>();
      myList.add("Apple");
      myList.add("Banana");
      myList.add("Airplane");
      assertThrows(NullPointerException.class, () -> CollectionUtils.listTo2LevelMap(myList, null, null));
      assertThrows(NullPointerException.class, () -> CollectionUtils.listTo2LevelMap(myList, first(), null));
      assertThrows(NullPointerException.class, () -> CollectionUtils.listTo2LevelMap(myList, null, second()));

      Map<String, Map<String, String>> myMap = CollectionUtils.listTo2LevelMap(myList, first(), second());
      assertEquals(2, myMap.size());
      assertEquals(2, myMap.get("A").size());
      assertEquals("Apple", myMap.get("A").get("p"));
      assertEquals("Airplane", myMap.get("A").get("i"));
      assertEquals(1, myMap.get("B").size());
      assertEquals("Banana", myMap.get("B").get("a"));

      // demonstrate clobbering behavior
      myList.add("Ape");
      myMap = CollectionUtils.listTo2LevelMap(myList, first(), second());
      assertEquals(2, myMap.get("A").size());
      assertEquals("Ape", myMap.get("A").get("p"));
   }



   /*******************************************************************************
    ** helper method to get rest of string (unsafely)
    *******************************************************************************/
   private Function<String, String> rest()
   {
      return s -> s.substring(1);
   }



   /*******************************************************************************
    ** helper method to get first char of string (unsafely)
    *******************************************************************************/
   private Function<String, String> first()
   {
      return s -> s.substring(0, 1);
   }

   /*******************************************************************************
    ** helper method to get second char of string (unsafely)
    *******************************************************************************/
   private Function<String, String> second()
   {
      return s -> s.substring(1, 2);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_getPages()
   {
      List<List<Integer>> pages = CollectionUtils.getPages(null, 5);
      assertEquals(0, pages.size());

      pages = CollectionUtils.getPages(Collections.emptyList(), 5);
      assertEquals(0, pages.size());

      pages = CollectionUtils.getPages(List.of(1, 2, 3), 5);
      assertEquals(1, pages.size());
      assertEquals(3, pages.get(0).size());

      pages = CollectionUtils.getPages(List.of(1, 2, 3, 4, 5), 5);
      assertEquals(1, pages.size());
      assertEquals(5, pages.get(0).size());

      pages = CollectionUtils.getPages(List.of(1, 2, 3, 4, 5, 6, 7), 5);
      assertEquals(2, pages.size());
      assertEquals(5, pages.get(0).size());
      assertEquals(2, pages.get(1).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test_getQuestionMarks()
   {
      assertEquals("", CollectionUtils.getQuestionMarks(null));
      assertEquals("", CollectionUtils.getQuestionMarks(Collections.emptyList()));
      assertEquals("?", CollectionUtils.getQuestionMarks(List.of(1)));
      assertEquals("?,?,?", CollectionUtils.getQuestionMarks(List.of(1, 2, 3)));
   }
}
