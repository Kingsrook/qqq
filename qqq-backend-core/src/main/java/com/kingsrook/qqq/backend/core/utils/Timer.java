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


import java.time.Duration;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import org.apache.logging.log4j.Level;


/*******************************************************************************
 ** Object to help track performance/runtime of codes.
 *******************************************************************************/
public class Timer
{
   private static final QLogger LOG = QLogger.getLogger(Timer.class);

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
      mark(message, false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void mark(String message, boolean prettyPrint)
   {
      long now = System.currentTimeMillis();

      if(!prettyPrint)
      {
         LOG.log(level, String.format("%s: Last [%5d] Total [%5d] %s", name, (now - last), (now - start), message));
      }
      else
      {

         Duration lastDuration  = Duration.ofMillis(now - last);
         Duration totalDuration = Duration.ofMillis(now - start);

         LOG.log(level, String.format(
            "%s: Last [%d hours, %d minutes, %d seconds, %d milliseconds] Total [%d hours, %d minutes, %d seconds, %d milliseconds] %s",
            name, lastDuration.toHours(), lastDuration.toMinutesPart(), lastDuration.toSecondsPart(), lastDuration.toMillisPart(),
            totalDuration.toHours(), totalDuration.toMinutesPart(), totalDuration.toSecondsPart(), totalDuration.toMillisPart(),
            message));
      }

      last = now;
   }
}
