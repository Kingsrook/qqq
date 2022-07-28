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


import java.util.concurrent.TimeUnit;


/*******************************************************************************
 ** Utility methods to help with sleeping!
 *******************************************************************************/
public class SleepUtils
{

   /*******************************************************************************
    ** Sleep for as close as we can to the specified amount of time (ignoring
    ** InterruptedException - continuing to sleep more).
    *******************************************************************************/
   public static void sleep(long duration, TimeUnit timeUnit)
   {
      long millis = timeUnit.toMillis(duration);
      long start  = System.currentTimeMillis();
      long end    = start + millis;

      while(System.currentTimeMillis() < end)
      {
         try
         {
            long millisToSleep = end - System.currentTimeMillis();
            Thread.sleep(millisToSleep);
         }
         catch(InterruptedException e)
         {
            // sleep more.
         }
      }
   }

}
