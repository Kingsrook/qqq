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

package com.kingsrook.qqq.backend.core.logging;


import org.apache.logging.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** A log message, which can be "collected" by the QCollectingLogger.
 *******************************************************************************/
public class CollectedLogMessage
{
   private Level     level;
   private String    message;
   private Throwable exception;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public CollectedLogMessage()
   {
   }



   /*******************************************************************************
    ** Getter for message
    *******************************************************************************/
   public String getMessage()
   {
      return (this.message);
   }



   /*******************************************************************************
    ** Setter for message
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    ** Fluent setter for message
    *******************************************************************************/
   public CollectedLogMessage withMessage(String message)
   {
      this.message = message;
      return (this);
   }



   /*******************************************************************************
    ** Getter for exception
    *******************************************************************************/
   public Throwable getException()
   {
      return (this.exception);
   }



   /*******************************************************************************
    ** Setter for exception
    *******************************************************************************/
   public void setException(Throwable exception)
   {
      this.exception = exception;
   }



   /*******************************************************************************
    ** Fluent setter for exception
    *******************************************************************************/
   public CollectedLogMessage withException(Throwable exception)
   {
      this.exception = exception;
      return (this);
   }



   /*******************************************************************************
    ** Getter for level
    **
    *******************************************************************************/
   public Level getLevel()
   {
      return level;
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
   public CollectedLogMessage withLevel(Level level)
   {
      this.level = level;
      return (this);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public JSONObject getMessageAsJSONObject() throws JSONException
   {
      return (new JSONObject(getMessage()));
   }
}
