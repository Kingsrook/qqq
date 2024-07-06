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

package com.kingsrook.qqq.backend.core.processes.locks;


import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;


/*******************************************************************************
 ** Lock thrown by ProcessLockUtils when you can't get the lock.
 *******************************************************************************/
public class UnableToObtainProcessLockException extends QUserFacingException
{
   private ProcessLock existingLock;



   /*******************************************************************************
    **
    *******************************************************************************/
   public UnableToObtainProcessLockException(String message)
   {
      super(message);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public UnableToObtainProcessLockException(String message, Throwable cause)
   {
      super(message, cause);
   }



   /*******************************************************************************
    ** Getter for existingLock
    *******************************************************************************/
   public ProcessLock getExistingLock()
   {
      return (this.existingLock);
   }



   /*******************************************************************************
    ** Setter for existingLock
    *******************************************************************************/
   public void setExistingLock(ProcessLock existingLock)
   {
      this.existingLock = existingLock;
   }



   /*******************************************************************************
    ** Fluent setter for existingLock
    *******************************************************************************/
   public UnableToObtainProcessLockException withExistingLock(ProcessLock existingLock)
   {
      this.existingLock = existingLock;
      return (this);
   }

}
