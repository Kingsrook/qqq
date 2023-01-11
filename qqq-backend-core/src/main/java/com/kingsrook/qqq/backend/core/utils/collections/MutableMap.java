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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.utils.lambdas.VoidVoidMethod;


/*******************************************************************************
 ** Object to wrap a Map, so that in case a caller provided an immutable Map,
 ** you can safely perform mutating operations on it (in which case, it'll get
 ** replaced by an actual mutable Map).
 *******************************************************************************/
public class MutableMap<K, V> implements Map<K, V>
{
   private Map<K, V>                  sourceMap;
   private Class<? extends Map<K, V>> mutableTypeIfNeeded;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public MutableMap(Map<K, V> sourceMap)
   {
      this(sourceMap, (Class) HashMap.class);
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public MutableMap(Map<K, V> sourceMap, Class<? extends Map<K, V>> mutableTypeIfNeeded)
   {
      this.sourceMap = sourceMap;
      this.mutableTypeIfNeeded = mutableTypeIfNeeded;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void replaceSourceMapWithMutableCopy()
   {
      try
      {
         Map<K, V> replacementMap = mutableTypeIfNeeded.getConstructor().newInstance();
         replacementMap.putAll(sourceMap);
         sourceMap = replacementMap;
      }
      catch(Exception e)
      {
         throw (new IllegalStateException("The mutable type provided for this MutableMap [" + mutableTypeIfNeeded.getName() + "] could not be instantiated."));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private <T> T doMutableOperationForValue(Supplier<T> supplier)
   {
      try
      {
         return (supplier.get());
      }
      catch(UnsupportedOperationException uoe)
      {
         replaceSourceMapWithMutableCopy();
         return (supplier.get());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doMutableOperationForVoid(VoidVoidMethod method)
   {
      try
      {
         method.run();
      }
      catch(UnsupportedOperationException uoe)
      {
         replaceSourceMapWithMutableCopy();
         method.run();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int size()
   {
      return (sourceMap.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isEmpty()
   {
      return (sourceMap.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean containsKey(Object key)
   {
      return (sourceMap.containsKey(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean containsValue(Object value)
   {
      return (sourceMap.containsValue(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public V get(Object key)
   {
      return (sourceMap.get(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public V put(K key, V value)
   {
      return (doMutableOperationForValue(() -> sourceMap.put(key, value)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public V remove(Object key)
   {
      return (doMutableOperationForValue(() -> sourceMap.remove(key)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void putAll(Map<? extends K, ? extends V> m)
   {
      doMutableOperationForVoid(() -> sourceMap.putAll(m));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void clear()
   {
      doMutableOperationForVoid(() -> sourceMap.clear());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Set<K> keySet()
   {
      return (sourceMap.keySet());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Collection<V> values()
   {
      return (sourceMap.values());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Set<Entry<K, V>> entrySet()
   {
      return (sourceMap.entrySet());
   }
}
