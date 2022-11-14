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


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.session.QSession;
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
   public void debug(QSession session, String message)
   {
      logger.debug(wrapMessage(session, message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void info(QSession session, String message)
   {
      logger.info(wrapMessage(session, message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(QSession session, String message)
   {
      logger.warn(wrapMessage(session, message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void warn(QSession session, String message, Throwable t)
   {
      logger.warn(wrapMessage(session, message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(QSession session, String message)
   {
      logger.error(wrapMessage(session, message));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void error(QSession session, String message, Throwable t)
   {
      logger.error(wrapMessage(session, message), t);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String wrapMessage(QSession session, String message)
   {
      String propertyName  = "qqq.logger.logSessionId.disabled";
      String propertyValue = System.getProperty(propertyName, "");
      if(propertyValue.equals("true"))
      {
         return (message);
      }

      String sessionString = (session != null) ? session.getUuid() : "Not provided";
      return ("Session [" + sessionString + "] | " + message);
   }
}
