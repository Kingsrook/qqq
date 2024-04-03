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

package com.kingsrook.qqq.backend.core.state;


import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Singleton class that provides a (non-persistent!!) in-memory state provider.
 *******************************************************************************/
public class InMemoryStateProvider implements StateProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(InMemoryStateProvider.class);

   private static InMemoryStateProvider instance;

   private final Map<AbstractStateKey, Object> map;

   private int jobPeriodSeconds = 60 * 15;
   private int jobInitialDelay  = 60 * 60 * 4;



   /*******************************************************************************
    ** Private constructor for singleton.
    *******************************************************************************/
   private InMemoryStateProvider()
   {
      this.map = new HashMap<>();

      ///////////////////////////////////////////////////////////
      // Start a single thread executor to handle the cleaning //
      ///////////////////////////////////////////////////////////
      ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.scheduleAtFixedRate(new InMemoryStateProvider.InMemoryStateProviderCleanJob(), jobInitialDelay, jobPeriodSeconds, TimeUnit.SECONDS);
   }



   /*******************************************************************************
    ** Runnable that gets scheduled to periodically clean the InMemoryStateProvider
    *******************************************************************************/
   private static class InMemoryStateProviderCleanJob implements Runnable
   {
      private static final QLogger LOG = QLogger.getLogger(InMemoryStateProvider.InMemoryStateProviderCleanJob.class);



      /*******************************************************************************
       ** run
       *******************************************************************************/
      @Override
      public void run()
      {
         try
         {
            Instant cleanTime = Instant.now().minus(4, ChronoUnit.HOURS);
            getInstance().clean(cleanTime);
         }
         catch(Exception e)
         {
            LOG.warn("Error cleaning InMemoryStateProvider entries.", e);
         }
      }
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static InMemoryStateProvider getInstance()
   {
      if(instance == null)
      {
         instance = new InMemoryStateProvider();
      }
      return instance;
   }



   /*******************************************************************************
    ** Put a block of data, under a key, into the state store.
    *******************************************************************************/
   @Override
   public <T extends Serializable> void put(AbstractStateKey key, T data)
   {
      map.put(key, data);
   }



   /*******************************************************************************
    ** Get a block of data, under a key, from the state store.
    *******************************************************************************/
   @Override
   public <T extends Serializable> Optional<T> get(Class<? extends T> type, AbstractStateKey key)
   {
      try
      {
         return Optional.ofNullable(type.cast(map.get(key)));
      }
      catch(ClassCastException cce)
      {
         throw new RuntimeException("Stored state value could not be cast to desired type", cce);
      }
   }



   /*******************************************************************************
    ** Remove a block of data, under a key, from the state store.
    *******************************************************************************/
   @Override
   public void remove(AbstractStateKey key)
   {
      map.remove(key);
   }



   /*******************************************************************************
    ** Clean entries that started before the given Instant
    *
    *******************************************************************************/
   @Override
   public void clean(Instant cleanBeforeInstant)
   {
      long    jobStartTime = System.currentTimeMillis();
      Integer beforeSize   = map.size();
      LOG.info("Starting clean for InMemoryStateProvider.", logPair("beforeSize", beforeSize));

      map.entrySet().removeIf(e -> e.getKey().getStartTime().isBefore(cleanBeforeInstant));

      Integer afterSize = map.size();
      long    endTime   = System.currentTimeMillis();
      LOG.info("Completed clean for InMemoryStateProvider.", logPair("beforeSize", beforeSize), logPair("afterSize", afterSize), logPair("amountCleaned", (beforeSize - afterSize)), logPair("runTimeMillis", (endTime - jobStartTime)));
   }

}
