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

package com.kingsrook.qqq.backend.module.filesystem.s3;


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
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3DeleteAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3InsertAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3QueryAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.S3UpdateAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3TableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.s3.utils.S3Utils;


/*******************************************************************************
 ** QQQ Backend module for working with AWS S3 filesystems
 *******************************************************************************/
public class S3BackendModule implements QBackendModuleInterface, FilesystemBackendModuleInterface
{
   private S3Utils s3Utils;



   /*******************************************************************************
    ** Method where a backend module must be able to provide its type (name).
    *******************************************************************************/
   @Override
   public String getBackendType()
   {
      return ("s3");
   }



   /*******************************************************************************
    ** Method to identify the class used for backend meta data for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QBackendMetaData> getBackendMetaDataClass()
   {
      return (S3BackendMetaData.class);
   }



   /*******************************************************************************
    ** Method to identify the class used for table-backend details for this module.
    *******************************************************************************/
   @Override
   public Class<? extends QTableBackendDetails> getTableBackendDetailsClass()
   {
      return (S3TableBackendDetails.class);
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
      QBackendMetaData backend    = instance.getBackend(table.getBackendName());
      String           bucketName = ((S3BackendMetaData) backend).getBucketName();

      getS3Utils().deleteObject(bucketName, fileReference);
   }




   /*******************************************************************************
    ** In contrast with the DeleteAction, which deletes RECORDS - this is a
    ** filesystem-(or s3, sftp, etc)-specific extension to delete an entire FILE
    ** e.g., for post-ETL.
    **
    ** @throws FilesystemException if the move is known to have failed
    *******************************************************************************/
   @Override
   public void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException
   {
      QBackendMetaData backend    = instance.getBackend(table.getBackendName());
      String           bucketName = ((S3BackendMetaData) backend).getBucketName();

      getS3Utils().moveObject(bucketName, source, destination);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QueryInterface getQueryInterface()
   {
      return new S3QueryAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public InsertInterface getInsertInterface()
   {
      return (new S3InsertAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public UpdateInterface getUpdateInterface()
   {
      return (new S3UpdateAction());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public DeleteInterface getDeleteInterface()
   {
      return (new S3DeleteAction());
   }



   /*******************************************************************************
    ** Setter for s3Utils
    *******************************************************************************/
   public void setS3Utils(S3Utils s3Utils)
   {
      this.s3Utils = s3Utils;
   }



   /*******************************************************************************
    ** Internal accessor for the s3Utils object - should always use this, not the field.
    *******************************************************************************/
   private S3Utils getS3Utils()
   {
      if(s3Utils == null)
      {
         s3Utils = new S3Utils();
      }

      return s3Utils;
   }

}
