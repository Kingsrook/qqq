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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.NoSuchFileException;
import java.time.Instant;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.apache.commons.io.FileUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** State provider that uses files in the /tmp/ directory.
 *******************************************************************************/
public class TempFileStateProvider implements StateProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(TempFileStateProvider.class);

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
    ** Put a block of data, under a key, into the state store.
    *******************************************************************************/
   @Override
   public <T extends Serializable> void put(AbstractStateKey key, T data)
   {
      try
      {
         String json = JsonUtils.toJson(data);
         FileUtils.writeStringToFile(getFile(key), json);
      }
      catch(IOException e)
      {
         LOG.error("Error putting state into file", e);
         throw (new RuntimeException("Error storing state", e));
      }
   }



   /*******************************************************************************
    ** Get a block of data, under a key, from the state store.
    *******************************************************************************/
   @Override
   public <T extends Serializable> Optional<T> get(Class<? extends T> type, AbstractStateKey key)
   {
      try
      {
         String json = FileUtils.readFileToString(getFile(key));
         return (Optional.of(JsonUtils.toObject(json, type)));
      }
      catch(FileNotFoundException | NoSuchFileException fnfe)
      {
         return (Optional.empty());
      }
      catch(IOException e)
      {
         LOG.error("Error getting state from file", e);
         throw (new RuntimeException("Error retrieving state", e));
      }
   }



   /*******************************************************************************
    ** Remove a block of data, under a key, from the state store.
    *******************************************************************************/
   @Override
   public void remove(AbstractStateKey key)
   {
      File file = getFile(key);
      if(!file.delete())
      {
         LOG.warn("Error deleting state-providing tempFile", logPair("file", file.getAbsolutePath()));
      }
   }



   /*******************************************************************************
    ** Clean entries that started before the given Instant
    *******************************************************************************/
   @Override
   public void clean(Instant startTime)
   {
      ////////////////////////////////
      // Not supported at this time //
      ////////////////////////////////
   }



   /*******************************************************************************
    ** Get the file referenced by a key
    *******************************************************************************/
   private File getFile(AbstractStateKey key)
   {
      return new File("/tmp/" + key.getUniqueIdentifier());
   }

}
