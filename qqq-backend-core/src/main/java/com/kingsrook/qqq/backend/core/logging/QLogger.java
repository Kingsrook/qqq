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

package com.kingsrook.qqq.backend.core.logging;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Wrapper for
 **
 *******************************************************************************/
public class QLogger
{
   private static Map<String, QLogger> loggerMap = Collections.synchronizedMap(new HashMap<>());

   private static boolean logSessionIdEnabled = true;

   private Logger logger;

   static
   {
      //////////////////////////////////////////////////////////////////////////////////////////////
      // read the property to see if sessionIds in log messages is enabled, just once, statically //
      //////////////////////////////////////////////////////////////////////////////////////////////
      String propertyName  = "qqq.logger.logSessionId.disabled";
      String propertyValue = System.getProperty(propertyName, "");
      if(propertyValue.equals("true"))
      {
         logSessionIdEnabled = false;
      }
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   public QLogger(Logger logger)
   {
      this.logger = logger;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QLogger getLogger(Class<?> c)
   {
      return (loggerMap.computeIfAbsent(c.getName(), x -> new QLogger(LogManager.getLogger(c))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, String message)
   {
      logger.log(level, makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, String message, Throwable t)
   {
      logger.log(level, makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, Throwable t)
   {
      logger.log(level, makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message)
   {
      logger.trace(makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, LogPair... logPairs)
   {
      logger.trace(makeJsonString(message, null, logPairs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Object... values)
   {
      logger.trace(makeJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Throwable t)
   {
      logger.trace(makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(Throwable t)
   {
      logger.trace(makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message)
   {
      logger.debug(makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, LogPair... logPairs)
   {
      logger.debug(makeJsonString(message, null, logPairs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Object... values)
   {
      logger.debug(makeJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Throwable t)
   {
      logger.debug(makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(Throwable t)
   {
      logger.debug(makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message)
   {
      logger.info(makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(LogPair... logPairs)
   {
      logger.info(LogUtils.jsonLog(addSessionLogPair(Arrays.asList(logPairs))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(List<LogPair> logPairList)
   {
      logger.info(LogUtils.jsonLog(addSessionLogPair(logPairList)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, LogPair... logPairs)
   {
      logger.info(makeJsonString(message, null, logPairs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, Object... values)
   {
      logger.info(makeJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, Throwable t)
   {
      logger.info(makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(Throwable t)
   {
      logger.info(makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message)
   {
      logger.warn(makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, LogPair... logPairs)
   {
      logger.warn(makeJsonString(message, null, logPairs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Object... values)
   {
      logger.warn(makeJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Throwable t)
   {
      logger.warn(makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(Throwable t)
   {
      logger.warn(makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message)
   {
      logger.error(makeJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, LogPair... logPairs)
   {
      logger.error(makeJsonString(message, null, logPairs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Object... values)
   {
      logger.error(makeJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Throwable t)
   {
      logger.error(makeJsonString(message, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(Throwable t)
   {
      logger.error(makeJsonString(null, t));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeJsonString(String message)
   {
      return (makeJsonString(message, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeJsonString(String message, Throwable t)
   {
      return (makeJsonString(message, t, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeJsonString(String message, Throwable t, LogPair[] logPairs)
   {
      List<LogPair> logPairList = new ArrayList<>();
      if(logPairs != null)
      {
         logPairList.addAll(Arrays.stream(logPairs).toList());
      }

      if(StringUtils.hasContent(message))
      {
         logPairList.add(0, logPair("message", message));
      }

      addSessionLogPair(logPairList);

      if(t != null)
      {
         logPairList.add(logPair("stackTrace", LogUtils.filterStackTrace(ExceptionUtils.getStackTrace(t))));
      }

      return (LogUtils.jsonLog(logPairList));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<LogPair> addSessionLogPair(List<LogPair> logPairList)
   {
      if(logSessionIdEnabled)
      {
         QSession session = QContext.getQSession();
         LogPair  sessionLogPair;

         if(session == null)
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // note - being careful here to make the same json structure whether session is known or unknown //
            // (e.g., not a string in one case and an object in another case) - to help loggly.              //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            sessionLogPair = logPair("session", logPair("id", "unknown"));
         }
         else
         {
            String user = "unknown";
            if(session.getUser() != null)
            {
               user = session.getUser().getIdReference();
            }
            sessionLogPair = logPair("session", logPair("id", session.getUuid()), logPair("user", user));
         }

         try
         {
            logPairList.add(sessionLogPair);
         }
         catch(Exception e)
         {
            //////////////////////////////////////
            // deal with not-modifiable list... //
            //////////////////////////////////////
            logPairList = new ArrayList<>(logPairList);
            logPairList.add(sessionLogPair);
         }
      }
      return (logPairList);
   }
}
