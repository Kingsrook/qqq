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

package com.kingsrook.qqq.backend.module.filesystem.sftp.actions;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import com.kingsrook.qqq.backend.core.actions.interfaces.QStorageInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.metadata.SFTPBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.sftp.utils.SFTPOutputStream;
import org.apache.sshd.sftp.client.SftpClient;


/*******************************************************************************
 ** (mass, streamed) storage action for sftp module
 *******************************************************************************/
public class SFTPStorageAction extends AbstractSFTPAction implements QStorageInterface
{

   /*******************************************************************************
    ** create an output stream in the storage backend - that can be written to,
    ** for the purpose of inserting or writing a file into storage.
    *******************************************************************************/
   @Override
   public OutputStream createOutputStream(StorageInput storageInput) throws QException
   {
      try
      {
         SFTPBackendMetaData backend = (SFTPBackendMetaData) storageInput.getBackend();
         preAction(backend);

         SftpClient sftpClient = getSftpClient(backend);

         SFTPOutputStream sftpOutputStream = new SFTPOutputStream(sftpClient, getFullPath(storageInput));
         return (sftpOutputStream);
      }
      catch(Exception e)
      {
         throw (new QException("Exception creating sftp output stream for file", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFullPath(StorageInput storageInput) throws QException
   {
      QTableMetaData   table    = storageInput.getTable();
      QBackendMetaData backend  = storageInput.getBackend();
      String           fullPath = stripDuplicatedSlashes(getFullBasePath(table, backend) + File.separator + storageInput.getReference());

      /////////////////////////////////////////////////////////////
      // s3 seems to do better w/o leading slashes, so, strip... //
      /////////////////////////////////////////////////////////////
      if(fullPath.startsWith("/"))
      {
         fullPath = fullPath.substring(1);
      }

      return fullPath;
   }



   /*******************************************************************************
    ** create an input stream in the storage backend - that can be read from,
    ** for the purpose of getting or reading a file from storage.
    *******************************************************************************/
   @Override
   public InputStream getInputStream(StorageInput storageInput) throws QException
   {
      try
      {
         SFTPBackendMetaData backend = (SFTPBackendMetaData) storageInput.getBackend();
         preAction(backend);

         SftpClient  sftpClient  = getSftpClient(backend);
         InputStream inputStream = sftpClient.read(getFullPath(storageInput));

         return (inputStream);
      }
      catch(Exception e)
      {
         throw (new QException("Exception getting sftp input stream for file.", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getDownloadURL(StorageInput storageInput) throws QException
   {
      try
      {
         throw new QRuntimeException("Not implemented");
         //S3BackendMetaData backend = (S3BackendMetaData) storageInput.getBackend();
         //preAction(backend);
         //
         //AmazonS3 amazonS3 = getS3Utils().getAmazonS3();
         //String   fullPath = getFullPath(storageInput);
         //return (amazonS3.getUrl(backend.getBucketName(), fullPath).toString());
      }
      catch(Exception e)
      {
         throw (new QException("Exception getting the sftp download URL.", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void makePublic(StorageInput storageInput) throws QException
   {
      try
      {
         throw new QRuntimeException("Not implemented");
      }
      catch(Exception e)
      {
         throw (new QException("Exception making sftp file publicly available.", e));
      }
   }

}
