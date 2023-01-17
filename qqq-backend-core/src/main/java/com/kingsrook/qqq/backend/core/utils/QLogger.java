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

package com.kingsrook.qqq.backend.core.utils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.LogUtils;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility class for logging
 **
 *******************************************************************************/
public class QLogger
{
   private static Map<String, QLogger> loggerMap           = Collections.synchronizedMap(new HashMap<>());
   private static boolean              logSessionIdEnabled = true;

   private Logger logger;

   static
   {
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
      logger.log(level, messageToJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, String message, Throwable t)
   {
      logger.log(level, messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, Throwable t)
   {
      logger.log(level, t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message)
   {
      logger.trace(messageToJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Object... values)
   {
      logger.trace(messageToJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Throwable t)
   {
      logger.trace(messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(Throwable t)
   {
      logger.trace(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message)
   {
      logger.debug(messageToJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Object... values)
   {
      logger.debug(messageToJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Throwable t)
   {
      logger.debug(messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(Throwable t)
   {
      logger.debug(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message)
   {
      logger.info(messageToJsonString(message));
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
   public void info(String message, Object... values)
   {
      logger.info(messageToJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, Throwable t)
   {
      logger.info(messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(Throwable t)
   {
      logger.info(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message)
   {
      logger.warn(messageToJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Object... values)
   {
      logger.warn(messageToJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Throwable t)
   {
      logger.warn(messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(Throwable t)
   {
      logger.warn(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message)
   {
      logger.error(messageToJsonString(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Object... values)
   {
      logger.error(messageToJsonString(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Throwable t)
   {
      logger.error(messageToJsonString(message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(Throwable t)
   {
      logger.error(t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String messageToJsonString(String message)
   {
      List<LogPair> logPairList = new ArrayList<>();
      logPairList.add(logPair("message", message));
      addSessionLogPair(logPairList);

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
            sessionLogPair = logPair("session", "unknown");
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
