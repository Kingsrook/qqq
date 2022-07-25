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
import java.util.Objects;
import java.util.UUID;


/*******************************************************************************
 **
 *******************************************************************************/
public class UUIDAndTypeStateKey extends AbstractStateKey implements Serializable
{
   private final UUID      uuid;
   private final StateType stateType;



   /*******************************************************************************
    ** Default constructor - assigns a random UUID.
    **
    *******************************************************************************/
   public UUIDAndTypeStateKey(StateType stateType)
   {
      this(UUID.randomUUID(), stateType);
   }



   /*******************************************************************************
    ** Constructor where user can supply the UUID.
    **
    *******************************************************************************/
   public UUIDAndTypeStateKey(UUID uuid, StateType stateType)
   {
      this.uuid = uuid;
      this.stateType = stateType;
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
    ** Getter for stateType
    **
    *******************************************************************************/
   public StateType getStateType()
   {
      return stateType;
   }



   /*******************************************************************************
    ** Make the key give a unique string to identify itself.
    *
    *******************************************************************************/
   @Override
   public String getUniqueIdentifier()
   {
      return (uuid.toString());
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
      UUIDAndTypeStateKey that = (UUIDAndTypeStateKey) o;
      return Objects.equals(uuid, that.uuid) && stateType == that.stateType;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int hashCode()
   {
      return Objects.hash(uuid, stateType);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "{uuid=" + uuid + ", stateType=" + stateType + '}';
   }
}
