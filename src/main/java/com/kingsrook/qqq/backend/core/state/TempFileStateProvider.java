/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.state;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.apache.commons.io.FileUtils;


/*******************************************************************************
 ** Singleton class that provides a (non-persistent!!) in-memory state provider.
 *******************************************************************************/
public class TempFileStateProvider implements StateProviderInterface
{
   private static TempFileStateProvider instance;



   /*******************************************************************************
    ** Private constructor for singleton.
    *******************************************************************************/
   private TempFileStateProvider()
   {
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static TempFileStateProvider getInstance()
   {
      if(instance == null)
      {
         instance = new TempFileStateProvider();
      }
      return instance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public <T extends Serializable> void put(AbstractStateKey key, T data)
   {
      try
      {
         String json = JsonUtils.toJson(data);
         FileUtils.writeStringToFile(new File("/tmp/" + key.toString()), json);
      }
      catch(IOException e)
      {
         // todo better
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public <T extends Serializable> T get(Class<? extends T> type, AbstractStateKey key)
   {
      try
      {
         String json = FileUtils.readFileToString(new File("/tmp/" + key.toString()));
         return JsonUtils.toObject(json, type);
      }
      catch(ClassCastException cce)
      {
         throw new RuntimeException("Stored state value could not be cast to desired type", cce);
      }
      catch(IOException ie)
      {
         throw new RuntimeException("Error loading state from file", ie);
      }
   }

}
