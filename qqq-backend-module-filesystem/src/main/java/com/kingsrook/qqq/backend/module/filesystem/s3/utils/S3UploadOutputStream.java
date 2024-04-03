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

package com.kingsrook.qqq.backend.module.filesystem.s3.utils;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadResult;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** OutputStream implementation that knows how to stream data into a new S3 file.
 **
 ** This will be done using a multipart-upload if the contents are > 5MB.
 *******************************************************************************/
public class S3UploadOutputStream extends OutputStream
{
   private static final QLogger LOG = QLogger.getLogger(S3UploadOutputStream.class);

   private final AmazonS3 amazonS3;
   private final String   bucketName;
   private final String   key;

   private byte[] buffer = new byte[5 * 1024 * 1024];
   private int    offset = 0;

   private InitiateMultipartUploadResult initiateMultipartUploadResult = null;
   private List<UploadPartResult>        uploadPartResultList          = null;

   private boolean isClosed = false;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public S3UploadOutputStream(AmazonS3 amazonS3, String bucketName, String key)
   {
      this.amazonS3 = amazonS3;
      this.bucketName = bucketName;
      this.key = key;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void write(int b) throws IOException
   {
      buffer[offset] = (byte) b;
      offset++;

      uploadIfNeeded();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void uploadIfNeeded()
   {
      if(offset == buffer.length)
      {
         //////////////////////////////////////////
         // start or continue a multipart upload //
         //////////////////////////////////////////
         if(initiateMultipartUploadResult == null)
         {
            LOG.info("Initiating a multipart upload", logPair("key", key));
            initiateMultipartUploadResult = amazonS3.initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key));
            uploadPartResultList = new ArrayList<>();
         }

         LOG.info("Uploading a part", logPair("key", key), logPair("partNumber", uploadPartResultList.size() + 1));
         UploadPartRequest uploadPartRequest = new UploadPartRequest()
            .withUploadId(initiateMultipartUploadResult.getUploadId())
            .withPartNumber(uploadPartResultList.size() + 1)
            .withInputStream(new ByteArrayInputStream(buffer))
            .withBucketName(bucketName)
            .withKey(key)
            .withPartSize(buffer.length);

         uploadPartResultList.add(amazonS3.uploadPart(uploadPartRequest));

         //////////////////
         // reset buffer //
         //////////////////
         offset = 0;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void write(byte[] b, int off, int len) throws IOException
   {
      int bytesToWrite = len;

      while(bytesToWrite > buffer.length - offset)
      {
         int size = buffer.length - offset;
         System.arraycopy(b, off, buffer, offset, size);
         offset = buffer.length;
         uploadIfNeeded();
         off += size;
         bytesToWrite -= size;
      }

      int size = len - off;
      System.arraycopy(b, off, buffer, offset, size);
      offset += size;
      uploadIfNeeded();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void close() throws IOException
   {
      if(isClosed)
      {
         LOG.debug("Redundant call to close an already-closed S3UploadOutputStream.  Returning with noop.", logPair("key", key));
         return;
      }

      if(initiateMultipartUploadResult != null)
      {
         if(offset > 0)
         {
            //////////////////////////////////////////////////
            // if there's a final part to upload, do it now //
            //////////////////////////////////////////////////
            LOG.info("Uploading a part", logPair("key", key), logPair("isFinalPart", true), logPair("partNumber", uploadPartResultList.size() + 1));
            UploadPartRequest uploadPartRequest = new UploadPartRequest()
               .withUploadId(initiateMultipartUploadResult.getUploadId())
               .withPartNumber(uploadPartResultList.size() + 1)
               .withInputStream(new ByteArrayInputStream(buffer, 0, offset))
               .withBucketName(bucketName)
               .withKey(key)
               .withPartSize(offset);
            uploadPartResultList.add(amazonS3.uploadPart(uploadPartRequest));
         }

         CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest()
            .withUploadId(initiateMultipartUploadResult.getUploadId())
            .withPartETags(uploadPartResultList)
            .withBucketName(bucketName)
            .withKey(key);
         CompleteMultipartUploadResult completeMultipartUploadResult = amazonS3.completeMultipartUpload(completeMultipartUploadRequest);
      }
      else
      {
         LOG.info("Putting object (non-multipart)", logPair("key", key), logPair("length", offset));
         ObjectMetadata objectMetadata = new ObjectMetadata();
         objectMetadata.setContentLength(offset);
         PutObjectResult putObjectResult = amazonS3.putObject(bucketName, key, new ByteArrayInputStream(buffer, 0, offset), objectMetadata);
      }

      isClosed = true;
   }

}
