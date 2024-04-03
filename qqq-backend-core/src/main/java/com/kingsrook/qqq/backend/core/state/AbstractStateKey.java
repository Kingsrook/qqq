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

package com.kingsrook.qqq.backend.core.state;


import java.io.Serializable;
import java.time.Instant;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractStateKey implements Serializable
{
   /*******************************************************************************
    ** Make the key give a unique string to identify itself.
    *
    *******************************************************************************/
   public abstract String getUniqueIdentifier();

   /*******************************************************************************
    ** Require all state keys to implement the equals method
    *
    *******************************************************************************/
   @Override
   public abstract boolean equals(Object that);

   /*******************************************************************************
    ** Require all state keys to implement the hashCode method
    *
    *******************************************************************************/
   @Override
   public abstract int hashCode();

   /*******************************************************************************
    ** Require all state keys to implement the toString method
    *
    *******************************************************************************/
   @Override
   public abstract String toString();

   /*******************************************************************************
    ** Require all state keys to implement the getStartTime method
    *
    *******************************************************************************/
   public abstract Instant getStartTime();

}
