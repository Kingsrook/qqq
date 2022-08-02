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

package com.kingsrook.qqq.backend.core.utils;


import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;


/*******************************************************************************
 ** Hash that provides "listing" capability -- keys map to lists of values that
 ** are automatically/easily added to
 **
 *******************************************************************************/
public class ListingHash<K, V> implements Map<K, List<V>>, Serializable
{
   public static final long serialVersionUID = 0L;

   private Map<K, List<V>> hashMap = null;



   /*******************************************************************************
    ** Default constructor
    **
    *******************************************************************************/
   public ListingHash()
   {
      this.hashMap = new HashMap<>();
   }



   /*******************************************************************************
    ** Constructor where you can supply a source map (e.g., if you want a specific
    ** Map type (like LinkedHashMap), or with pre-values
    **
    *******************************************************************************/
   public ListingHash(Map<K, List<V>> sourceMap)
   {
      this.hashMap = sourceMap;
   }



   /*******************************************************************************
    ** Add a value to the entry/list for this key
    **
    *******************************************************************************/
   public List<V> add(K key, V value)
   {
      List<V> list = getOrCreateListForKey(key);
      list.add(value);
      return (list);
   }



   /*******************************************************************************
    ** Add all elements of the collection of v's to this listing hash, using keys
    ** generated by passing each v to the supplied keyFunction (which return's K's)
    **
    *******************************************************************************/
   public void addAll(Collection<V> vs, Function<V, K> keyFunction)
   {
      if(vs == null || keyFunction == null)
      {
         return;
      }

      for(V v : vs)
      {
         add(keyFunction.apply(v), v);
      }
   }



   /*******************************************************************************
    ** Add multiple values to the entry/list for this key
    **
    *******************************************************************************/
   public List<V> addAll(K key, Collection<V> values)
   {
      List<V> list = getOrCreateListForKey(key);
      list.addAll(values);
      return (list);
   }



   /*******************************************************************************
    ** Add all object from another ListingHash ('that') to this one.  Note, does it
    ** at the level of the whole key=list (e.g., if a key was already in this, it'll
    ** be replaced with the list from 'that'.
    **
    *******************************************************************************/
   public void addAll(ListingHash<K, V> that)
   {
      if(that == null)
      {
         return;
      }

      for(K key : that.keySet())
      {
         addAll(key, that.get(key));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<V> getOrCreateListForKey(K key)
   {
      List<V> list;

      if(!this.hashMap.containsKey(key))
      {
         /////////////////////////////////
         // create list, place into map //
         /////////////////////////////////
         list = new LinkedList<V>();
         this.hashMap.put(key, list);
      }
      else
      {
         list = this.hashMap.get(key);
      }
      return list;
   }

   /////////////////////////////////////////////////////////////////////////
   // wrappings of methods taken from the internal HashMap of this object //
   /////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public void clear()
   {
      this.hashMap.clear();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean containsKey(Object key)
   {
      return (this.hashMap.containsKey(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean containsValue(Object value)
   {
      return (this.hashMap.containsValue(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<Entry<K, List<V>>> entrySet()
   {
      return (this.hashMap.entrySet());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean equals(Object o)
   {
      return (this.hashMap.equals(o));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<V> get(Object key)
   {
      return (this.hashMap.get(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int hashCode()
   {
      return (this.hashMap.hashCode());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean isEmpty()
   {
      return (this.hashMap.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Set<K> keySet()
   {
      return (this.hashMap.keySet());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<V> put(K key, List<V> value)
   {
      return (this.hashMap.put(key, value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void putAll(Map<? extends K, ? extends List<V>> t)
   {
      this.hashMap.putAll(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<V> remove(Object key)
   {
      return (this.hashMap.remove(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int size()
   {
      return (this.hashMap.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Collection<List<V>> values()
   {
      return (this.hashMap.values());
   }
}
