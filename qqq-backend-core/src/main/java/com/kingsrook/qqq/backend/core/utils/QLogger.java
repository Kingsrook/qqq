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


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility class for logging
 **
 *******************************************************************************/
public class QLogger
{
   private static Map<String, QLogger> loggerMap = new HashMap<>();
   private        Logger               logger;



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
      logger.log(level, wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void log(Level level, String message, Throwable t)
   {
      logger.log(level, wrapMessage(message), t);
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
      logger.trace(wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Object... values)
   {
      logger.trace(wrapMessage(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void trace(String message, Throwable t)
   {
      logger.trace(wrapMessage(message), t);
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
      logger.debug(wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Object... values)
   {
      logger.debug(wrapMessage(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void debug(String message, Throwable t)
   {
      logger.debug(wrapMessage(message), t);
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
      logger.info(wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, Object... values)
   {
      logger.info(wrapMessage(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(String message, Throwable t)
   {
      logger.info(wrapMessage(message), t);
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
      logger.warn(wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Object... values)
   {
      logger.warn(wrapMessage(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(String message, Throwable t)
   {
      logger.warn(wrapMessage(message), t);
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
      logger.error(wrapMessage(message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Object... values)
   {
      logger.error(wrapMessage(message), values);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(String message, Throwable t)
   {
      logger.error(wrapMessage(message), t);
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
   private String wrapMessage(String message)
   {
      String propertyName  = "qqq.logger.logSessionId.disabled";
      String propertyValue = System.getProperty(propertyName, "");
      if(propertyValue.equals("true"))
      {
         return (message);
      }

      QSession session       = QContext.getQSession();
      String   sessionString = (session != null) ? session.getUuid() : "Not provided";
      return ("Session [" + sessionString + "] | " + message);
   }
}
