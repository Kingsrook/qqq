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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeFunction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for Memoization 
 *******************************************************************************/
class MemoizationTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      Memoization<String, Integer> memoization = new Memoization<>();
      memoization.setMaxSize(3);
      memoization.setTimeout(Duration.ofMillis(100));

      assertThat(memoization.getResult("one")).isEmpty();
      memoization.storeResult("one", 1);
      assertThat(memoization.getResult("one")).isPresent().get().isEqualTo(1);

      ////////////////////////////////////////////////////
      // store 3 more results - this should force 1 out //
      ////////////////////////////////////////////////////
      memoization.storeResult("two", 2);
      memoization.storeResult("three", 3);
      memoization.storeResult("four", 4);
      assertThat(memoization.getResult("one")).isEmpty();

      //////////////////////////////////
      // make sure others are present //
      //////////////////////////////////
      assertThat(memoization.getResult("two")).isPresent().get().isEqualTo(2);
      assertThat(memoization.getResult("three")).isPresent().get().isEqualTo(3);
      assertThat(memoization.getResult("four")).isPresent().get().isEqualTo(4);

      /////////////////////////////////////////////////////////////
      // wait more than the timeout, then make sure all are gone //
      /////////////////////////////////////////////////////////////
      SleepUtils.sleep(150, TimeUnit.MILLISECONDS);
      assertThat(memoization.getResult("two")).isEmpty();
      assertThat(memoization.getResult("three")).isEmpty();
      assertThat(memoization.getResult("four")).isEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCanStoreNull()
   {
      Memoization<String, Integer> memoization = new Memoization<>();
      memoization.storeResult("null", null);

      ///////////////////////////////////////////////////////////////////////////////////////////
      // note - we can't tell a stored null apart from a non-stored value by calling getResult //
      ///////////////////////////////////////////////////////////////////////////////////////////
      Optional<Integer> optionalNull = memoization.getResult("null");
      assertNotNull(optionalNull);
      assertTrue(optionalNull.isEmpty());

      ////////////////////////////////////////////
      // instead, we must use getMemoizedResult //
      ////////////////////////////////////////////
      Optional<MemoizedResult<Integer>> optionalMemoizedResult = memoization.getMemoizedResult("null");
      assertNotNull(optionalMemoizedResult);
      assertTrue(optionalMemoizedResult.isPresent());
      assertNull(optionalMemoizedResult.get().getResult());

      /////////////////////////////////////////////////////////////////
      // make sure getMemoizedResult returns empty for an un-set key //
      /////////////////////////////////////////////////////////////////
      optionalMemoizedResult = memoization.getMemoizedResult("never-stored");
      assertNotNull(optionalMemoizedResult);
      assertTrue(optionalMemoizedResult.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLookupFunction()
   {
      AtomicInteger lookupFunctionCallCounter = new AtomicInteger(0);

      Memoization<String, Integer> memoization = new Memoization<>();

      UnsafeFunction<String, Integer, Exception> lookupFunction = numberString ->
      {
         lookupFunctionCallCounter.getAndIncrement();

         if(numberString.equals("null"))
         {
            return (null);
         }

         return Integer.parseInt(numberString);
      };

      //////////////////////////////////////////////////////////////////////////////////////////
      // get "1" twice - should return 1 each time, and call the lookup function exactly once //
      //////////////////////////////////////////////////////////////////////////////////////////
      assertThat(memoization.getResult("1", lookupFunction)).isPresent().contains(1);
      assertEquals(1, lookupFunctionCallCounter.get());

      assertThat(memoization.getResult("1", lookupFunction)).isPresent().contains(1);
      assertEquals(1, lookupFunctionCallCounter.get());

      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now get "null" twice - should return null each time, and call the lookup function exactly once more //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertThat(memoization.getResult("null", lookupFunction)).isEmpty();
      assertEquals(2, lookupFunctionCallCounter.get());

      assertThat(memoization.getResult("null", lookupFunction)).isEmpty();
      assertEquals(2, lookupFunctionCallCounter.get());

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now make a call that throws twice - again, should return null each time, and only do one more loookup call //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertThat(memoization.getResult(null, lookupFunction)).isEmpty();
      assertEquals(3, lookupFunctionCallCounter.get());

      assertThat(memoization.getResult(null, lookupFunction)).isEmpty();
      assertEquals(3, lookupFunctionCallCounter.get());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("Slow, so not for CI - but good to demonstrate thread-safety during dev")
   void testMultiThread() throws InterruptedException, ExecutionException
   {
      Memoization<String, Integer> memoization     = new Memoization<>();
      ExecutorService              executorService = Executors.newFixedThreadPool(20);

      List<Future<?>> futures = new ArrayList<>();

      for(int i = 0; i < 20; i++)
      {
         int finalI = i;
         futures.add(executorService.submit(() ->
         {
            System.out.println("Start " + finalI);
            for(int n = 0; n < 1_000_000; n++)
            {
               memoization.storeResult(String.valueOf(n), n);
               memoization.getResult(String.valueOf(n));

               if(n % 100_000 == 0)
               {
                  System.out.format("Thread %d at %,d\n", finalI, +n);
               }
            }
            System.out.println("End " + finalI);
         }));
      }

      while(!futures.isEmpty())
      {
         Iterator<Future<?>> iterator = futures.iterator();
         while(iterator.hasNext())
         {
            Future<?> next = iterator.next();
            if(next.isDone())
            {
               Object o = next.get();
               iterator.remove();
            }
         }
      }

      System.out.println("All Done");
   }

}