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


import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModule;


/*******************************************************************************
 ** (local) Filesystem backend meta data.
 *******************************************************************************/
public class S3BackendMetaData extends AbstractFilesystemBackendMetaData
{
   private String bucketName;



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

}
