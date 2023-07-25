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


import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ActionTimeoutHelper 
 *******************************************************************************/
class ActionTimeoutHelperTest extends BaseTest
{
   boolean didCancel = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTimesOut()
   {
      didCancel = false;
      ActionTimeoutHelper actionTimeoutHelper = new ActionTimeoutHelper(10, TimeUnit.MILLISECONDS, () -> doCancel());
      actionTimeoutHelper.start();
      SleepUtils.sleep(50, TimeUnit.MILLISECONDS);
      assertTrue(didCancel);
      assertTrue(actionTimeoutHelper.getDidTimeout());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetsCancelled()
   {
      didCancel = false;
      ActionTimeoutHelper actionTimeoutHelper = new ActionTimeoutHelper(100, TimeUnit.MILLISECONDS, () -> doCancel());
      actionTimeoutHelper.start();
      SleepUtils.sleep(10, TimeUnit.MILLISECONDS);
      actionTimeoutHelper.cancel();
      assertFalse(didCancel);
      SleepUtils.sleep(200, TimeUnit.MILLISECONDS);
      assertFalse(didCancel);
      assertFalse(actionTimeoutHelper.getDidTimeout());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void doCancel()
   {
      didCancel = true;
   }

}