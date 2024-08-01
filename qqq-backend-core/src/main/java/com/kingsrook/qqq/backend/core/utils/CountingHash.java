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


import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.backend.core.utils.collections.MutableMap;


/*******************************************************************************
 ** Hash that provides "counting" capability -- keys map to Integers that
 ** are automatically/easily summed to
 **
 *******************************************************************************/
public class CountingHash<K extends Serializable> extends AbstractMap<K, Integer> implements Serializable
{
   private Map<K, Integer> map = null;



   /*******************************************************************************
    ** Default constructor
    **
    *******************************************************************************/
   public CountingHash()
   {
      this.map = new HashMap<>();
   }



   /*******************************************************************************
    ** Constructor where you can supply a source map (e.g., if you want a specific
    ** Map type (like LinkedHashMap), or with pre-values.
    **
    ** Note - the input map will be wrapped in a MutableMap - so - it'll be mutable.
    **
    *******************************************************************************/
   public CountingHash(Map<K, Integer> sourceMap)
   {
      this.map = new MutableMap<>(sourceMap);
   }



   /*******************************************************************************
    ** Increment the value for the specified key by 1.
    **
    *******************************************************************************/
   public Integer add(K key)
   {
      Integer value = getOrCreateListForKey(key);
      Integer sum   = value + 1;
      map.put(key, sum);
      return (sum);
   }



   /*******************************************************************************
    ** Increment the value for the specified key by the supplied addend
    **
    *******************************************************************************/
   public Integer add(K key, Integer addend)
   {
      Integer value = getOrCreateListForKey(key);
      Integer sum   = value + addend;
      map.put(key, sum);
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Integer getOrCreateListForKey(K key)
   {
      Integer value;

      if(!this.map.containsKey(key))
      {
         this.map.put(key, 0);
         value = 0;
      }
      else
      {
         value = this.map.get(key);
      }
      return value;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public Set<Entry<K, Integer>> entrySet()
   {
      return this.map.entrySet();
   }

}
