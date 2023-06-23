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

package com.kingsrook.qqq.backend.core.exceptions;


import org.apache.logging.log4j.Level;


/*******************************************************************************
 * Base class for checked exceptions thrown in qqq.
 *
 *******************************************************************************/
public class QException extends Exception
{
   private boolean hasLoggedWarning;
   private boolean hasLoggedError;



   /*******************************************************************************
    ** Constructor of message
    **
    *******************************************************************************/
   public QException(Throwable t)
   {
      super(t.getMessage(), t);
   }



   /*******************************************************************************
    ** Constructor of message
    **
    *******************************************************************************/
   public QException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    ** Constructor of message & cause
    **
    *******************************************************************************/
   public QException(String message, Throwable cause)
   {
      super(message, cause);
   }



   /*******************************************************************************
    ** Getter for hasLoggedWarning
    *******************************************************************************/
   public boolean getHasLoggedWarning()
   {
      return (this.hasLoggedWarning);
   }



   /*******************************************************************************
    ** Setter for hasLoggedWarning
    *******************************************************************************/
   public void setHasLoggedWarning(boolean hasLoggedWarning)
   {
      this.hasLoggedWarning = hasLoggedWarning;
   }



   /*******************************************************************************
    ** Fluent setter for hasLoggedWarning
    *******************************************************************************/
   public QException withHasLoggedWarning(boolean hasLoggedWarning)
   {
      this.hasLoggedWarning = hasLoggedWarning;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hasLoggedError
    *******************************************************************************/
   public boolean getHasLoggedError()
   {
      return (this.hasLoggedError);
   }



   /*******************************************************************************
    ** Setter for hasLoggedError
    *******************************************************************************/
   public void setHasLoggedError(boolean hasLoggedError)
   {
      this.hasLoggedError = hasLoggedError;
   }



   /*******************************************************************************
    ** Fluent setter for hasLoggedError
    *******************************************************************************/
   public QException withHasLoggedError(boolean hasLoggedError)
   {
      this.hasLoggedError = hasLoggedError;
      return (this);
   }



   /*******************************************************************************
    ** helper function for getting if level logged
    *******************************************************************************/
   public boolean hasLoggedLevel(Level level)
   {
      if(Level.WARN.equals(level))
      {
         return (hasLoggedWarning);
      }
      if(Level.ERROR.equals(level))
      {
         return (hasLoggedError);
      }
      return (false);
   }



   /*******************************************************************************
    ** helper function for setting if level logged
    *******************************************************************************/
   public void setHasLoggedLevel(Level level)
   {
      if(Level.WARN.equals(level))
      {
         setHasLoggedWarning(true);
      }
      if(Level.ERROR.equals(level))
      {
         setHasLoggedError(true);
      }
   }

}
