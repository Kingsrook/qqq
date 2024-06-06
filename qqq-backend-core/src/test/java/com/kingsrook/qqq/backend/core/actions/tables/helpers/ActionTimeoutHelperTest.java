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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ActionTimeoutHelper 
 *******************************************************************************/
class ActionTimeoutHelperTest extends BaseTest
{
   private static AtomicInteger cancelCount;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
   {
      cancelCount = new AtomicInteger(0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTimesOut()
   {
      ActionTimeoutHelper actionTimeoutHelper = new ActionTimeoutHelper(10, TimeUnit.MILLISECONDS, () -> doCancel());
      actionTimeoutHelper.start();
      SleepUtils.sleep(50, TimeUnit.MILLISECONDS);
      assertEquals(1, cancelCount.get());
      assertTrue(actionTimeoutHelper.getDidTimeout());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetsCancelled()
   {
      ActionTimeoutHelper actionTimeoutHelper = new ActionTimeoutHelper(100, TimeUnit.MILLISECONDS, () -> doCancel());
      actionTimeoutHelper.start();
      SleepUtils.sleep(10, TimeUnit.MILLISECONDS);
      actionTimeoutHelper.cancel();
      assertEquals(0, cancelCount.get());
      SleepUtils.sleep(200, TimeUnit.MILLISECONDS);
      assertEquals(0, cancelCount.get());
      assertFalse(actionTimeoutHelper.getDidTimeout());
   }



   /*******************************************************************************
    ** goal here is - confirm that we can have more threads running at same time
    ** than we have threads allocated to the ActionTimeoutHelper's thread pool,
    ** and they should all still get cancelled.
    *******************************************************************************/
   @Test
   void testManyThreads() throws InterruptedException, ExecutionException
   {
      int N = 50;

      ExecutorService executorService = Executors.newCachedThreadPool();
      List<Future<?>> futureList      = new ArrayList<>();

      for(int i = 0; i < N; i++)
      {
         System.out.println("Submitting: " + i);
         futureList.add(executorService.submit(() ->
         {
            ActionTimeoutHelper actionTimeoutHelper = new ActionTimeoutHelper(10, TimeUnit.MILLISECONDS, () -> doCancel());
            actionTimeoutHelper.start();
            SleepUtils.sleep(1, TimeUnit.SECONDS);
         }));
      }

      for(Future<?> future : futureList)
      {
         future.get();
      }

      assertEquals(N, cancelCount.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doCancel()
   {
      cancelCount.getAndIncrement();
   }

}