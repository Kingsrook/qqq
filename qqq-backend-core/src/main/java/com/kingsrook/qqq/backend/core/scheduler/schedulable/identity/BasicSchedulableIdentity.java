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

package com.kingsrook.qqq.backend.core.scheduler.schedulable.identity;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Basic implementation of interface for identifying schedulable things
 *******************************************************************************/
public class BasicSchedulableIdentity implements SchedulableIdentity
{
   private String identity;
   private String description;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public BasicSchedulableIdentity(String identity, String description)
   {
      if(!StringUtils.hasContent(identity))
      {
         throw (new IllegalArgumentException("Identity may not be null or empty."));
      }

      this.identity = identity;
      this.description = description;
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

      BasicSchedulableIdentity that = (BasicSchedulableIdentity) o;
      return Objects.equals(identity, that.identity);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(identity);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getIdentity()
   {
      return identity;
   }



   /*******************************************************************************
    ** Getter for description
    **
    *******************************************************************************/
   @Override
   public String getDescription()
   {
      return description;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return getIdentity();
   }

}
