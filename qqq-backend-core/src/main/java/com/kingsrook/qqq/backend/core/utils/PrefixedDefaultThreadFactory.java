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


import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/*******************************************************************************
 ** ThreadFactory implementation that puts a common prefix on all threads.
 **
 ** Makes it so that, instead of having 100s of pool-x-thread-y names that are
 ** hard to tell apart, they can have a prefix:  MyService-pool-x-thread-y, vs
 ** YourThing-pool-x-thread-y.
 **
 ** You can put '-' at the end of your threadNamePrefix (constructor arg) or
 ** you can omit it, either way, we'll make it look like shown above.
 *******************************************************************************/
public class PrefixedDefaultThreadFactory implements ThreadFactory
{
   private final String        threadNamePrefix;
   private final ThreadFactory threadFactory;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public PrefixedDefaultThreadFactory(String threadNamePrefix)
   {
      if(StringUtils.hasContent(threadNamePrefix))
      {
         this.threadNamePrefix = threadNamePrefix.replaceAll("-+$", "") + "-";
      }
      else
      {
         this.threadNamePrefix = "";
      }

      threadFactory = Executors.defaultThreadFactory();
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public PrefixedDefaultThreadFactory(Class<?> callerClass)
   {
      this(callerClass != null ? callerClass.getSimpleName() : "");
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public PrefixedDefaultThreadFactory(Object caller)
   {
      this(caller != null ? caller.getClass() : null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Thread newThread(Runnable r)
   {
      Thread thread = threadFactory.newThread(r);
      thread.setName(threadNamePrefix + thread.getName());
      return (thread);
   }

}
