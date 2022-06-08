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
