/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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


import java.util.Objects;
import java.util.UUID;


/*******************************************************************************
 **
 *******************************************************************************/
public class UUIDStateKey extends AbstractStateKey
{
   private final UUID uuid;



   /*******************************************************************************
    ** Default constructor - assigns a random UUID.
    **
    *******************************************************************************/
   public UUIDStateKey()
   {
      uuid = UUID.randomUUID();
   }



   /*******************************************************************************
    ** Constructor that lets you supply a UUID.
    **
    *******************************************************************************/
   public UUIDStateKey(UUID uuid)
   {
      this.uuid = uuid;
   }



   /*******************************************************************************
    ** Getter for uuid
    **
    *******************************************************************************/
   public UUID getUuid()
   {
      return uuid;
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

      UUIDStateKey that = (UUIDStateKey) o;
      return Objects.equals(uuid, that.uuid);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(uuid);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return uuid.toString();
   }
}
