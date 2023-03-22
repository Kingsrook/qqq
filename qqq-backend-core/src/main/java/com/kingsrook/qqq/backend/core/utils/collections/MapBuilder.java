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


import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/*******************************************************************************
 ** Map.of is "great", but annoying because it makes unmodifiable maps, and it
 ** NPE's on nulls...  So, replace it with this, which returns HashMaps, which
 ** "don't suck"
 *******************************************************************************/
public class MapBuilder<K, V>
{
   private Map<K, V> map;



   /*******************************************************************************
    **
    *******************************************************************************/
   private MapBuilder(Map<K, V> map)
   {
      this.map = map;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> MapBuilder<K, V> of(Supplier<Map<K, V>> mapSupplier)
   {
      return (new MapBuilder<>(mapSupplier.get()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MapBuilder<K, V> with(K key, V value)
   {
      map.put(key, value);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<K, V> build()
   {
      return (this.map);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3,
      K k4, V v4
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      rs.put(k4, v4);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3,
      K k4, V v4,
      K k5, V v5
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      rs.put(k4, v4);
      rs.put(k5, v5);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3,
      K k4, V v4,
      K k5, V v5,
      K k6, V v6
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      rs.put(k4, v4);
      rs.put(k5, v5);
      rs.put(k6, v6);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3,
      K k4, V v4,
      K k5, V v5,
      K k6, V v6,
      K k7, V v7
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      rs.put(k4, v4);
      rs.put(k5, v5);
      rs.put(k6, v6);
      rs.put(k7, v7);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <K, V> Map<K, V> of(
      K k1, V v1,
      K k2, V v2,
      K k3, V v3,
      K k4, V v4,
      K k5, V v5,
      K k6, V v6,
      K k7, V v7,
      K k8, V v8
   )
   {
      Map<K, V> rs = new HashMap<>();
      rs.put(k1, v1);
      rs.put(k2, v2);
      rs.put(k3, v3);
      rs.put(k4, v4);
      rs.put(k5, v5);
      rs.put(k6, v6);
      rs.put(k7, v7);
      rs.put(k8, v8);
      return (rs);
   }

}
