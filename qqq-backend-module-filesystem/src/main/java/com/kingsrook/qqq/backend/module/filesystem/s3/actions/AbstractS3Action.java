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
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.utils.S3Utils;


/*******************************************************************************
 ** Base class for all S3 filesystem actions
 *******************************************************************************/
public class AbstractS3Action extends AbstractBaseFilesystemAction<S3ObjectSummary>
{
   private static final QLogger LOG = QLogger.getLogger(AbstractS3Action.class);

   private S3Utils s3Utils;



   /*******************************************************************************
    ** Setup the s3 utils object to be used for this action.
    *******************************************************************************/
   @Override
   public void preAction(QBackendMetaData backendMetaData)
   {
      super.preAction(backendMetaData);

      if(s3Utils != null)
      {
         LOG.debug("s3Utils object is already set - not re-setting it.");
         return;
      }

      s3Utils = new S3Utils();
      s3Utils.setAmazonS3(buildAmazonS3ClientFromBackendMetaData(backendMetaData));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected AmazonS3 buildAmazonS3ClientFromBackendMetaData(QBackendMetaData backendMetaData)
   {
      S3BackendMetaData     s3BackendMetaData = getBackendMetaData(S3BackendMetaData.class, backendMetaData);
      AmazonS3ClientBuilder clientBuilder     = AmazonS3ClientBuilder.standard();

      if(StringUtils.hasContent(s3BackendMetaData.getAccessKey()) && StringUtils.hasContent(s3BackendMetaData.getSecretKey()))
      {
         BasicAWSCredentials credentials = new BasicAWSCredentials(s3BackendMetaData.getAccessKey(), s3BackendMetaData.getSecretKey());
         clientBuilder.setCredentials(new AWSStaticCredentialsProvider(credentials));
      }

      if(StringUtils.hasContent(s3BackendMetaData.getRegion()))
      {
         clientBuilder.setRegion(s3BackendMetaData.getRegion());
      }

      return (clientBuilder.build());
   }



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
      S3BackendMetaData                     s3BackendMetaData = getBackendMetaData(S3BackendMetaData.class, backendBase);
      AbstractFilesystemTableBackendDetails tableDetails      = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);

      String fullPath   = getFullBasePath(table, backendBase);
      String bucketName = s3BackendMetaData.getBucketName();
      String glob       = tableDetails.getGlob();

      ////////////////////////////////////////////////////////////////////
      // todo - look at metadata to configure the s3 client here?       //
      ////////////////////////////////////////////////////////////////////
      return getS3Utils().listObjectsInBucketMatchingGlob(bucketName, fullPath, glob);
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
    ** Write a file - to be implemented in module-specific subclasses.
    *******************************************************************************/
   @Override
   public void writeFile(QBackendMetaData backendMetaData, String path, byte[] contents) throws IOException
   {
      path = stripLeadingSlash(stripDuplicatedSlashes(path));
      String bucketName = ((S3BackendMetaData) backendMetaData).getBucketName();
      getS3Utils().writeFile(bucketName, path, contents);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String stripLeadingSlash(String path)
   {
      if(path == null)
      {
         return (null);
      }
      return (path.replaceFirst("^/+", ""));
   }



   /*******************************************************************************
    ** Get a string that represents the full path to a file.
    *******************************************************************************/
   @Override
   public String getFullPathForFile(S3ObjectSummary s3ObjectSummary)
   {
      return (s3ObjectSummary.getKey());
   }



   /*******************************************************************************
    ** e.g., with a base path of /foo/
    ** and a table path of /bar/
    ** and a file at /foo/bar/baz.txt
    ** give us just the baz.txt part.
    *******************************************************************************/
   @Override
   public String stripBackendAndTableBasePathsFromFileName(String filePath, QBackendMetaData backend, QTableMetaData table)
   {
      String tablePath    = getFullBasePath(table, backend);
      String strippedPath = filePath.replaceFirst("^/*" + stripLeadingSlash(tablePath), "");
      return (strippedPath);
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
    ** @param destination assumed to be a file path - not a directory
    ** @throws FilesystemException if the move is known to have failed
    *******************************************************************************/
   @Override
   public void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException
   {
      QBackendMetaData backend    = instance.getBackend(table.getBackendName());
      String           bucketName = ((S3BackendMetaData) backend).getBucketName();
      destination = stripLeadingSlash(stripDuplicatedSlashes(destination));

      getS3Utils().moveObject(bucketName, source, destination);
   }

}
