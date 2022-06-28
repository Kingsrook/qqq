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

package com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.QSecretReader;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModule;


/*******************************************************************************
 ** S3 backend meta data.
 *******************************************************************************/
public class S3BackendMetaData extends AbstractFilesystemBackendMetaData
{
   private String bucketName;
   private String accessKey;
   private String secretKey;
   private String region;



   /*******************************************************************************
    ** Default Constructor.
    *******************************************************************************/
   public S3BackendMetaData()
   {
      super();
      setBackendType(S3BackendModule.class);
   }



   /*******************************************************************************
    ** Getter for bucketName
    **
    *******************************************************************************/
   public String getBucketName()
   {
      return bucketName;
   }



   /*******************************************************************************
    ** Setter for bucketName
    **
    *******************************************************************************/
   public void setBucketName(String bucketName)
   {
      this.bucketName = bucketName;
   }



   /*******************************************************************************
    ** Fluent setter for bucketName
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends S3BackendMetaData> T withBucketName(String bucketName)
   {
      this.bucketName = bucketName;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for accessKey
    **
    *******************************************************************************/
   public String getAccessKey()
   {
      return accessKey;
   }



   /*******************************************************************************
    ** Setter for accessKey
    **
    *******************************************************************************/
   public void setAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
   }



   /*******************************************************************************
    ** Fluent setter for accessKey
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends S3BackendMetaData> T withAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for secretKey
    **
    *******************************************************************************/
   public String getSecretKey()
   {
      return secretKey;
   }



   /*******************************************************************************
    ** Setter for secretKey
    **
    *******************************************************************************/
   public void setSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
   }



   /*******************************************************************************
    ** Fluent setter for secretKey
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends S3BackendMetaData> T withSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
      return (T) this;
   }



   /*******************************************************************************
    ** Getter for region
    **
    *******************************************************************************/
   public String getRegion()
   {
      return region;
   }



   /*******************************************************************************
    ** Setter for region
    **
    *******************************************************************************/
   public void setRegion(String region)
   {
      this.region = region;
   }



   /*******************************************************************************
    ** Fluent setter for region
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public <T extends S3BackendMetaData> T withRegion(String region)
   {
      this.region = region;
      return (T) this;
   }



   /*******************************************************************************
    ** Called by the QInstanceEnricher - to do backend-type-specific enrichments.
    ** Original use case is:  reading secrets into fields (e.g., passwords).
    *******************************************************************************/
   @Override
   public void enrich()
   {
      super.enrich();
      QSecretReader secretReader = new QSecretReader();
      accessKey = secretReader.readSecret(accessKey);
      secretKey = secretReader.readSecret(secretKey);
   }

}
