/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.utils;


import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/*******************************************************************************
 ** Utility class for working with Collections.
 **
 *******************************************************************************/
public class CollectionUtils
{
   /*******************************************************************************
    ** true if c is null or it's empty
    **
    *******************************************************************************/
   public static boolean nullSafeIsEmpty(Collection c)
   {
      if(c == null || c.isEmpty())
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    ** true if c is null or it's empty
    **
    *******************************************************************************/
   public static boolean nullSafeIsEmpty(Map c)
   {
      if(c == null || c.isEmpty())
      {
         return (true);
      }

      return (false);
   }



   /*******************************************************************************
    ** true if c is NOT null and it's not empty
    **
    *******************************************************************************/
   public static boolean nullSafeHasContents(Collection c)
   {
      return (!nullSafeIsEmpty(c));
   }



   /*******************************************************************************
    ** true if c is NOT null and it's not empty
    **
    *******************************************************************************/
   public static boolean nullSafeHasContents(Map c)
   {
      return (!nullSafeIsEmpty(c));
   }



   /*******************************************************************************
    ** 0 if c is empty, otherwise, its size.
    **
    *******************************************************************************/
   public static int nullSafeSize(Collection c)
   {
      if(c == null)
      {
         return (0);
      }

      return (c.size());
   }



   /*******************************************************************************
    ** 0 if c is empty, otherwise, its size.
    **
    *******************************************************************************/
   public static int nullSafeSize(Map c)
   {
      if(c == null)
      {
         return (0);
      }

      return (c.size());
   }



   /*******************************************************************************
    ** add all values from one map to another.
    **
    *******************************************************************************/
   public static <K, V> void addAllToMap(Map<K, V> addingTo, Map<K, V> addingFrom)
   {
      for(K key : addingFrom.keySet())
      {
         addingTo.put(key, addingFrom.get(key));
      }
   }



   /*******************************************************************************
    ** Build a map from a list, supplying a function that extracts keys from the
    ** objects in the list (the objects in the list become the values in the map).
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> listToMap(List<V> values, Function<V, ? extends K> keyFunction)
   {
      if(values == null)
      {
         return (null);
      }

      Map<K, V> rs = new HashMap<>();
      for(V value : values)
      {
         rs.put(keyFunction.apply(value), value);
      }

      return (rs);
   }



   /*******************************************************************************
    ** Build a map from a list, supplying a function that extracts keys from the
    ** objects in the list, and another function that extracts the values from the
    ** objects in the list.
    **
    *******************************************************************************/
   public static <E, K, V> Map<K, V> listToMap(List<E> elements, Function<E, ? extends K> keyFunction, Function<E, ? extends V> valueFunction)
   {
      if(elements == null)
      {
         return (null);
      }

      Map<K, V> rs = new HashMap<>();
      for(E element : elements)
      {
         rs.put(keyFunction.apply(element), valueFunction.apply(element));
      }

      return (rs);
   }



   /*******************************************************************************
    ** Build a listingHash from a list, supplying a function that extracts keys from the
    ** objects in the list (the objects in the list become the values in the map).
    **
    *******************************************************************************/
   public static <K, V> ListingHash<K, V> listToListingHash(List<V> values, Function<V, ? extends K> keyFunction)
   {
      if(values == null)
      {
         return (null);
      }

      ListingHash<K, V> rs = new ListingHash<>();
      for(V value : values)
      {
         rs.add(keyFunction.apply(value), value);
      }

      return (rs);
   }



   /*******************************************************************************
    * <p>Take a list of objects, and build a listing hash, using 2 lambdas to control
    * how keys in the listing hash are created (from the objects), and how values
    * are created.</p>
    *
    * <p>For example, given a list of Work records, if we want a Listing hash with
    * workStatusId as keys, and workId a values, we would call:</p>
    *
    * <code>listToListingHash(workList, Work::getWorkStatusId, Work::getId)</code>
    *
    * @param elements list of objects to be mapped
    * @param keyFunction function to map an object from elements list into keys
    *                    for the listing hash
    * @param valueFunction function to map an object from elements list into values
    *                       for the listing hash
    *
    * @return ListingHash that might look like:
    *     1 -> [ 73, 75, 68]
    *     2 -> [ 74 ]
    *     4 -> [ 76, 77, 79, 80, 81]
    *
    * end
    *******************************************************************************/
   public static <E, K, V> ListingHash<K, V> listToListingHash(List<E> elements, Function<E, ? extends K> keyFunction, Function<E, ? extends V> valueFunction)
   {
      if(elements == null)
      {
         return (null);
      }

      ListingHash<K, V> rs = new ListingHash<>();
      for(E element : elements)
      {
         rs.add(keyFunction.apply(element), valueFunction.apply(element));
      }

      return (rs);
   }



   /*******************************************************************************
    ** Convert a list to a 2-level Map (ie., Map of Map of Key,Value), where 2
    ** lambdas are provided for extract the two levels of keys from the objects in
    ** the list, and the values in the map are the values in the list.
    **
    *******************************************************************************/
   public static <K1, K2, V> Map<K1, Map<K2, V>> listTo2LevelMap(List<V> values, Function<V, ? extends K1> keyFunction1, Function<V, ? extends K2> keyFunction2)
   {
      if(values == null)
      {
         return (null);
      }

      Map<K1, Map<K2, V>> rs = new HashMap<>();
      for(V value : values)
      {
         K1 k1 = keyFunction1.apply(value);
         if(!rs.containsKey(k1))
         {
            rs.put(k1, new HashMap<>());
         }
         rs.get(k1).put(keyFunction2.apply(value), value);
      }

      return (rs);
   }



   /*******************************************************************************
    ** split a large collection into lists of lists, with specified pageSize
    **
    *******************************************************************************/
   public static <T> List<List<T>> getPages(Collection<T> values, int pageSize)
   {
      List<List<T>> rs = new LinkedList<>();

      if(values == null || values.isEmpty())
      {
         //////////////////////////////////////////////////////////////////
         // if there are no input values, return an empty list of lists. //
         //////////////////////////////////////////////////////////////////
         return (rs);
      }

      List<T> currentPage = new LinkedList<T>();
      rs.add(currentPage);

      for(T value : values)
      {
         if(currentPage.size() >= pageSize)
         {
            currentPage = new LinkedList<T>();
            rs.add(currentPage);
         }

         currentPage.add(value);
      }

      return (rs);
   }



   /*******************************************************************************
    ** build comma-delimited string of question marks from a collection (e.g., for
    ** an sql string)
    **
    *******************************************************************************/
   public static String getQuestionMarks(Collection<?> c)
   {
      if(CollectionUtils.nullSafeIsEmpty(c))
      {
         return ("");
      }

      StringBuilder rs = new StringBuilder();
      for(int i = 0; i < c.size(); i++)
      {
         rs.append(i > 0 ? "," : "").append("?");
      }

      return (rs.toString());
   }

}
