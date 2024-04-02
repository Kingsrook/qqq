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


import java.time.Instant;


/*******************************************************************************
 **
 *******************************************************************************/
public class SimpleStateKey<T> extends AbstractStateKey
{
   private final T key;



   /*******************************************************************************
    ** Constructor.
    **
    *******************************************************************************/
   public SimpleStateKey(T key)
   {
      this.key = key;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return (String.valueOf(this.key));
   }



   /*******************************************************************************
    ** Make the key give a unique string to identify itself.
    *
    *******************************************************************************/
   @Override
   public String getUniqueIdentifier()
   {
      return (String.valueOf(this.key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean equals(Object o)
   {
      if(this == o)
      {
         return true;
      }
      if(o == null || getClass() != o.getClass())
      {
         return false;
      }
      SimpleStateKey<?> that = (SimpleStateKey<?>) o;
      return key.equals(that.key);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return key.hashCode();
   }



   /*******************************************************************************
    ** Getter for startTime
    *******************************************************************************/
   public Instant getStartTime()
   {
      //////////////////////////////////////////
      // For now these will never get cleaned //
      //////////////////////////////////////////
      return (Instant.now());
   }
}
