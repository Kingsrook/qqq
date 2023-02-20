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

package com.kingsrook.qqq.backend.core.actions.async;


import java.util.UUID;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for AsyncJobCallback
 *******************************************************************************/
class AsyncJobCallbackTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      AsyncJobStatus   asyncJobStatus   = new AsyncJobStatus();
      AsyncJobCallback asyncJobCallback = new AsyncJobCallback(UUID.randomUUID(), asyncJobStatus);

      /////////////////////////////////////////////////////
      // make sure current never goes greater than total //
      /////////////////////////////////////////////////////
      asyncJobCallback.updateStatus(3, 2);
      assertEquals(2, asyncJobStatus.getTotal());
      assertEquals(2, asyncJobStatus.getCurrent());

      asyncJobCallback.updateStatus("With Message", 3, 2);
      assertEquals(2, asyncJobStatus.getTotal());
      assertEquals(2, asyncJobStatus.getCurrent());

      //////////////////////////
      // reset, then count up //
      //////////////////////////
      asyncJobCallback.updateStatus(1, 3);
      assertEquals(3, asyncJobStatus.getTotal());
      assertEquals(1, asyncJobStatus.getCurrent());

      asyncJobCallback.incrementCurrent();
      assertEquals(2, asyncJobStatus.getCurrent());

      asyncJobCallback.incrementCurrent();
      assertEquals(3, asyncJobStatus.getCurrent());

      /////////////////////////////////
      // try to go to 4 - stay at 3. //
      /////////////////////////////////
      asyncJobCallback.incrementCurrent();
      assertEquals(3, asyncJobStatus.getCurrent());

      ///////////
      // reset //
      ///////////
      asyncJobCallback.updateStatus(1, 3);
      assertEquals(3, asyncJobStatus.getTotal());
      assertEquals(1, asyncJobStatus.getCurrent());

      asyncJobCallback.incrementCurrent(4);
      assertEquals(3, asyncJobStatus.getCurrent());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateStatusOnlyUpwards()
   {
      AsyncJobStatus   asyncJobStatus   = new AsyncJobStatus();
      AsyncJobCallback asyncJobCallback = new AsyncJobCallback(UUID.randomUUID(), asyncJobStatus);

      asyncJobCallback.updateStatusOnlyUpwards(10, 100);
      assertEquals(10, asyncJobStatus.getCurrent());
      assertEquals(100, asyncJobStatus.getTotal());

      asyncJobCallback.updateStatusOnlyUpwards(5, 100);
      assertEquals(10, asyncJobStatus.getCurrent());
      assertEquals(100, asyncJobStatus.getTotal());

      asyncJobCallback.updateStatusOnlyUpwards(11, 99);
      assertEquals(10, asyncJobStatus.getCurrent());
      assertEquals(100, asyncJobStatus.getTotal());

      asyncJobCallback.updateStatusOnlyUpwards(11, 100);
      assertEquals(11, asyncJobStatus.getCurrent());
      assertEquals(100, asyncJobStatus.getTotal());

      asyncJobCallback.updateStatusOnlyUpwards(11, 101);
      assertEquals(11, asyncJobStatus.getCurrent());
      assertEquals(101, asyncJobStatus.getTotal());
   }

}