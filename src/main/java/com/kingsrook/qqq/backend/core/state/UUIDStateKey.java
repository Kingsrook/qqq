/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
