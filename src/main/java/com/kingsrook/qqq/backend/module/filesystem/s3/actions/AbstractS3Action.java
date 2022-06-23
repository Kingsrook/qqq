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

package com.kingsrook.qqq.backend.module.filesystem.s3.actions;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.utils.S3Utils;


/*******************************************************************************
 ** Base class for all S3 filesystem actions
 *******************************************************************************/
public class AbstractS3Action extends AbstractBaseFilesystemAction<S3ObjectSummary>
{
   private S3Utils s3Utils;



   /*******************************************************************************
    ** Set the S3Utils object.
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



   /*******************************************************************************
    ** List the files for a table.
    *******************************************************************************/
   @Override
   public List<S3ObjectSummary> listFiles(QTableMetaData table, QBackendMetaData backendBase)
   {
      S3BackendMetaData s3BackendMetaData = getBackendMetaData(S3BackendMetaData.class, backendBase);

      String fullPath   = getFullPath(table, backendBase);
      String bucketName = s3BackendMetaData.getBucketName();

      ////////////////////////////////////////////////////////////////////
      // todo - read metadata to decide if we should include subfolders //
      // todo - look at metadata to configure the s3 client here?       //
      ////////////////////////////////////////////////////////////////////
      boolean includeSubfolders = false;
      return getS3Utils().listObjectsInBucketAtPath(bucketName, fullPath, includeSubfolders);
   }



   /*******************************************************************************
    ** Read the contents of a file.
    *******************************************************************************/
   @Override
   public InputStream readFile(S3ObjectSummary s3ObjectSummary) throws IOException
   {
      return (getS3Utils().getObjectAsInputStream(s3ObjectSummary));
   }



   /*******************************************************************************
    ** Add backend details to records about the file that they are in.
    *******************************************************************************/
   @Override
   protected void addBackendDetailsToRecords(List<QRecord> recordsInFile, S3ObjectSummary s3ObjectSummary)
   {
      recordsInFile.forEach(record ->
      {
         record.withBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, s3ObjectSummary.getKey());
      });
   }

}
