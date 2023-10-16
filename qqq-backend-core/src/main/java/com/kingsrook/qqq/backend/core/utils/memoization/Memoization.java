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

package com.kingsrook.qqq.backend.core.utils.memoization;


import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.logging.QLogger;


/*******************************************************************************
 ** Basic memoization functionality - with result timeouts (only when doing a get -
 ** there's no cleanup thread), and max-size.
 *******************************************************************************/
public class Memoization<K, V>
{
   private static final QLogger LOG = QLogger.getLogger(Memoization.class);

   private final Map<K, MemoizedResult<V>> map = Collections.synchronizedMap(new LinkedHashMap<>());

   private Duration timeout = Duration.ofSeconds(600);
   private Integer  maxSize = 1000;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public Memoization()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Optional<V> getResult(K key)
   {
      MemoizedResult<V> result = map.get(key);
      if(result != null)
      {
         if(result.getTime().isAfter(Instant.now().minus(timeout)))
         {
            return (Optional.of(result.getResult()));
         }
      }

      return (Optional.empty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void storeResult(K key, V value)
   {
      map.put(key, new MemoizedResult<>(value));

      //////////////////////////////////////
      // make sure map didn't get too big //
      // do this thread safely, please    //
      //////////////////////////////////////
      try
      {
         if(map.size() > maxSize)
         {
            synchronized(map)
            {
               Iterator<Map.Entry<K, MemoizedResult<V>>> iterator = null;
               while(map.size() > maxSize)
               {
                  if(iterator == null)
                  {
                     iterator = map.entrySet().iterator();
                  }

                  if(iterator.hasNext())
                  {
                     iterator.next();
                     iterator.remove();
                  }
                  else
                  {
                     break;
                  }
               }
            }
         }
      }
      catch(Exception e)
      {
         LOG.error("Error managing size of a Memoization", e);
      }
   }



   /*******************************************************************************
    ** Setter for timeoutSeconds
    **
    *******************************************************************************/
   public void setTimeout(Duration timeout)
   {
      this.timeout = timeout;
   }



   /*******************************************************************************
    ** Setter for maxSize
    **
    *******************************************************************************/
   public void setMaxSize(Integer maxSize)
   {
      this.maxSize = maxSize;
   }



   /*******************************************************************************
    ** package-private - for tests to look at the map.
    **
    *******************************************************************************/
   Map<K, MemoizedResult<V>> getMap()
   {
      return map;
   }
}
