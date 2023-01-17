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

package com.kingsrook.qqq.backend.javalin;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class QJavalinAccessLogger
{
   private static final QLogger LOG = QLogger.getLogger(QJavalinAccessLogger.class);

   private static ThreadLocal<Long>   requestStartTime  = new ThreadLocal<>();
   private static ThreadLocal<String> requestActionName = new ThreadLocal<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   static void logStart(String actionName, LogPair... logPairs)
   {
      requestStartTime.set(System.currentTimeMillis());
      requestActionName.set(actionName);
      List<LogPair> pairList = new ArrayList<>(Arrays.asList(logPairs));
      pairList.add(0, logPair("access", "start"));
      pairList.add(1, logPair("action", actionName));
      LOG.info(pairList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void logEndSuccess(LogPair... logPairs)
   {
      List<LogPair> pairList = new ArrayList<>(Arrays.asList(logPairs));
      pairList.add(0, logPair("access", "end-ok"));
      accessLogEnd(pairList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void logEndFail(Throwable t, LogPair... logPairs)
   {
      List<LogPair> pairList = new ArrayList<>(Arrays.asList(logPairs));
      pairList.add(0, logPair("access", "end-fail"));
      pairList.add(1, logPair("exceptionType", t.getClass().getSimpleName()));
      pairList.add(2, logPair("exceptionMessage", t.getMessage()));
      accessLogEnd(pairList);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void accessLogEnd(List<LogPair> pairList)
   {
      String actionName = requestActionName.get();
      requestActionName.remove();
      if(StringUtils.hasContent(actionName))
      {
         pairList.add(1, logPair("action", actionName));
      }

      Long startTime = requestStartTime.get();
      requestStartTime.remove();
      if(startTime != null)
      {
         long endTime = System.currentTimeMillis();
         pairList.add(logPair("millis", (endTime - startTime)));
      }
      LOG.info(pairList);
   }

}
