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

package com.kingsrook.qqq.backend.module.api.exceptions;


import com.kingsrook.qqq.backend.core.exceptions.QException;


/*******************************************************************************
 ** Exception thrown when an API HTTP request failed due to a bad status code.
 ** This exception includes the status code as a field
 *******************************************************************************/
public class QBadHttpResponseStatusException extends QException
{
   private int statusCode;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBadHttpResponseStatusException(String message, int statusCode)
   {
      super(message);
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Getter for statusCode
    *******************************************************************************/
   public int getStatusCode()
   {
      return (this.statusCode);
   }



   /*******************************************************************************
    ** Setter for statusCode
    *******************************************************************************/
   public void setStatusCode(int statusCode)
   {
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Fluent setter for statusCode
    *******************************************************************************/
   public QBadHttpResponseStatusException withStatusCode(int statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }

}
