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

package com.kingsrook.qqq.backend.core.model.statusmessages;


import java.io.Serializable;


/*******************************************************************************
 ** Abstract Base class for status messages (errors or warnings) that can be
 ** attached to QRecords.
 **
 ** They look like exceptions, but they aren't throwable, and they are meant
 ** to just be put in a record's error or warning list.  Those lists were originally
 ** just Strings, but we wanted to have some type information communicated with
 ** them, e.g., for marking an error as caused by bad-data (e.g., from a user, e.g.,
 ** for an HTTP 400) vs. a server-side error, etc.
 *******************************************************************************/
public abstract class QStatusMessage implements Serializable
{
   private String message;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QStatusMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    ** Getter for message
    **
    *******************************************************************************/
   public String getMessage()
   {
      return message;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (message);
   }
}
