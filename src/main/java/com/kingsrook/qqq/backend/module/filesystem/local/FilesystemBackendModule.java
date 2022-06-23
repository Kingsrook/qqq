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

package com.kingsrook.qqq.backend.module.filesystem.local;


import java.io.File;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableBackendDetails;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.modules.interfaces.UpdateInterface;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemBackendModuleInterface;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemDeleteAction;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemInsertAction;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemQueryAction;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemUpdateAction;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** QQQ Backend module for working with (local) Filesystems.
 *******************************************************************************/
public class FilesystemBackendModule implements QBackendModuleInterface, FilesystemBackendModuleInterface
{
   private static final Logger LOG = LogManager.getLogger(FilesystemBackendModule.class);


   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   @Override
   public String getBackendType()
   {
      return ("filesystem");
   }



   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (FilesystemBackendMetaData.class);
   }



   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return FilesystemTableBackendDetails.class;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return new FilesystemQueryAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new FilesystemInsertAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateInterface getUpdateInterface()
   {
      return (new FilesystemUpdateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new FilesystemDeleteAction());
   }



   /*******************************************************************************
    ** In contrast with the DeleteAction, which deletes RECORDS - this is a
    ** filesystem-(or s3, sftp, etc)-specific extension to delete an entire FILE
    ** e.g., for post-ETL.
    **
    ** @throws FilesystemException if the delete is known to have failed, and the file is thought to still exit
    *******************************************************************************/
   @Override
   public void deleteFile(QInstance instance, QTableMetaData table, String fileReference) throws FilesystemException
   {
      File file = new File(fileReference);
      if(!file.exists())
      {
         //////////////////////////////////////////////////////////////////////////////////////////////
         // if the file doesn't exist, just exit with noop.  don't throw an error - that should only //
         // happen if the "contract" of the method is broken, and the file still exists              //
         //////////////////////////////////////////////////////////////////////////////////////////////
         LOG.debug("Not deleting file [{}], because it does not exist.", file);
         return;
      }

      if(!file.delete())
      {
         throw (new FilesystemException("Failed to delete file: " + fileReference));
      }
   }



   /*******************************************************************************
    ** Move a file from a source path, to a destination path.
    **
    ** @throws FilesystemException if the delete is known to have failed
    *******************************************************************************/
   @Override
   public void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException
   {
      File sourceFile        = new File(source);
      File destinationFile   = new File(destination);
      File destinationParent = destinationFile.getParentFile();

      if(!sourceFile.exists())
      {
         throw (new FilesystemException("Cannot move file " + source + ", as it does not exist."));
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // if the destination folder doesn't exist, try to make it - and fail if that fails //
      //////////////////////////////////////////////////////////////////////////////////////
      if(!destinationParent.exists())
      {
         LOG.debug("Making destination directory {} for move", destinationParent.getAbsolutePath());
         if(!destinationParent.mkdirs())
         {
            throw (new FilesystemException("Failed to make destination directory " + destinationParent.getAbsolutePath() + " to move " + source + " into."));
         }
      }

      if(!sourceFile.renameTo(destinationFile))
      {
         throw (new FilesystemException("Failed to move (rename) file " + source + " to " + destination));
      }
   }

}
