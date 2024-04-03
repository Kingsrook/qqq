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

package com.kingsrook.qqq.backend.module.filesystem.local.actions;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.kingsrook.qqq.backend.core.actions.interfaces.QStorageInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.jetbrains.annotations.NotNull;


/*******************************************************************************
 ** (mass, streamed) storage action for filesystem module
 *******************************************************************************/
public class FilesystemStorageAction extends AbstractFilesystemAction implements QStorageInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public OutputStream createOutputStream(StorageInput storageInput) throws QException
   {
      try
      {
         String fullPath = getFullPath(storageInput);
         File file = new File(fullPath);
         if(!file.getParentFile().exists())
         {
            if(!file.getParentFile().mkdirs())
            {
               throw (new QException("Could not make directory required for storing file: " + fullPath));
            }
         }

         return (new FileOutputStream(fullPath));
      }
      catch(IOException e)
      {
         throw (new QException("IOException creating output stream for file", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @NotNull
   private String getFullPath(StorageInput storageInput)
   {
      QTableMetaData   table    = storageInput.getTable();
      QBackendMetaData backend  = storageInput.getBackend();
      String           fullPath = stripDuplicatedSlashes(getFullBasePath(table, backend) + File.separator + storageInput.getReference());
      return fullPath;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InputStream getInputStream(StorageInput storageInput) throws QException
   {
      try
      {
         return (new FileInputStream(getFullPath(storageInput)));
      }
      catch(IOException e)
      {
         throw (new QException("IOException getting input stream for file", e));
      }
   }

}
