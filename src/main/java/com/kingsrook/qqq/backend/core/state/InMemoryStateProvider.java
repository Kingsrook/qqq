/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.state;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/*******************************************************************************
 ** Singleton class that provides a (non-persistent!!) in-memory state provider.
 *******************************************************************************/
public class InMemoryStateProvider implements StateProviderInterface
{
   private static InMemoryStateProvider instance;

   private final Map<AbstractStateKey, Object> map;



   /*******************************************************************************
    ** Private constructor for singleton.
    *******************************************************************************/
   private InMemoryStateProvider()
   {
      this.map = new HashMap<>();
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static InMemoryStateProvider getInstance()
   {
      if(instance == null)
      {
         instance = new InMemoryStateProvider();
      }
      return instance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public <T extends Serializable> void put(AbstractStateKey key, T data)
   {
      map.put(key, data);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public <T extends Serializable> T get(Class<? extends T> type, AbstractStateKey key)
   {
      try
      {
         return type.cast(map.get(key));
      }
      catch(ClassCastException cce)
      {
         throw new RuntimeException("Stored state value could not be cast to desired type", cce);
      }
   }

}
