/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.tracing;


/*******************************************************************************
 ** Basic class that can be passed in to ProcessTracerInterface.handleMessage.
 ** This class just provides for a string message.  We anticipate subclasses
 ** that may have more specific data, that specific tracer implementations may
 ** be aware of.  
 *******************************************************************************/
public class ProcessTracerMessage
{
   private String message;


   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessTracerMessage()
   {
   }


   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessTracerMessage(String message)
   {
      this.message = message;
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
   public ProcessTracerMessage withMessage(String message)
   {
      this.message = message;
      return (this);
   }

}
