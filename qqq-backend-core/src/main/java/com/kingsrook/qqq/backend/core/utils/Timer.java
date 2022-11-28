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


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Object to help track performance/runtime of codes.
 *******************************************************************************/
public class Timer
{
   private static final Logger LOG = LogManager.getLogger(Timer.class);

   private String name;
   private long   start;
   private long   last;
   private Level  level = Level.DEBUG;



   /*******************************************************************************
    **
    *******************************************************************************/
   public Timer(String name)
   {
      this.name = name;
      start = System.currentTimeMillis();
      last = start;
   }



   /*******************************************************************************
    ** Setter for level
    **
    *******************************************************************************/
   public void setLevel(Level level)
   {
      this.level = level;
   }



   /*******************************************************************************
    ** Fluent setter for level
    **
    *******************************************************************************/
   public Timer withLevel(Level level)
   {
      this.level = level;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void mark(String message)
   {
      long now = System.currentTimeMillis();
      LOG.log(level, String.format("%s: Last [%5d] Total [%5d] %s", name, (now - last), (now - start), message));
      last = now;
   }
}
