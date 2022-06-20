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

package com.kingsrook.qqq.backend.module.filesystem.s3.utils;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility methods for working with AWS S3.
 *******************************************************************************/
public class S3Utils
{
   private static final Logger LOG = LogManager.getLogger(S3Utils.class);

   private AmazonS3 s3;



   /*******************************************************************************
    ** List the objects in an S3 bucket at a given path
    *******************************************************************************/
   public List<S3ObjectSummary> listObjectsInBucketAtPath(String bucketName, String fullPath, boolean includeSubfolders)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////
      // s3 list requests find nothing if the path starts with a /, so strip away any leading slashes //
      // also strip away trailing /'s, for consistent known paths.                                    //
      // also normalize any duplicated /'s to a single /.                                             //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      fullPath = fullPath.replaceFirst("^/+", "").replaceFirst("/+$", "").replaceAll("//+", "/");

      ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
         .withBucketName(bucketName)
         .withPrefix(fullPath);

      ListObjectsV2Result   listObjectsV2Result = null;
      List<S3ObjectSummary> rs                  = new ArrayList<>();

      do
      {
         if(listObjectsV2Result != null)
         {
            listObjectsV2Request.setContinuationToken(listObjectsV2Result.getNextContinuationToken());
         }
         listObjectsV2Result = getS3().listObjectsV2(listObjectsV2Request);

         //////////////////////////////////
         // put files in the result list //
         //////////////////////////////////
         for(S3ObjectSummary objectSummary : listObjectsV2Result.getObjectSummaries())
         {
            String key = objectSummary.getKey();

            //////////////////
            // skip folders //
            //////////////////
            if(key.endsWith("/"))
            {
               LOG.debug("Skipping file [{}] because it is a folder", key);
               continue;
            }

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if we're not supposed to include subfolders, check the path on this file, and only include it if it matches the request //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(!includeSubfolders)
            {
               String prefix = key.substring(0, key.lastIndexOf("/"));
               if(!prefix.equals(fullPath))
               {
                  LOG.debug("Skipping file [{}] in a sub-folder [{}] != [{}]", key, prefix, fullPath);
                  continue;
               }
            }

            rs.add(objectSummary);
         }
      }
      while(listObjectsV2Result.isTruncated());

      return rs;
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public InputStream getObjectAsInputStream(S3ObjectSummary s3ObjectSummary)
   {
      return getS3().getObject(s3ObjectSummary.getBucketName(), s3ObjectSummary.getKey()).getObjectContent();
   }


   /*******************************************************************************
    ** Setter for AmazonS3 client object.
    *******************************************************************************/
   public void setAmazonS3(AmazonS3 s3)
   {
      this.s3 = s3;
   }



   /*******************************************************************************
    ** Getter for AmazonS3 client object.
    *******************************************************************************/
   public AmazonS3 getS3()
   {
      if(s3 == null)
      {
         s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
      }

      return s3;
   }


}
