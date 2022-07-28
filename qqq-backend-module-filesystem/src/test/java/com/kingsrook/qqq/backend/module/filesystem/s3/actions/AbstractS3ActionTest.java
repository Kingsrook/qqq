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


import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for AbstractS3Action
 *******************************************************************************/
class AbstractS3ActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBuildAmazonS3ClientFromBackendMetaData()
   {
      String regionName = Regions.AP_SOUTHEAST_3.getName();
      S3BackendMetaData s3BackendMetaData = new S3BackendMetaData()
         .withAccessKey("Not a real access key")
         .withSecretKey("Also not a real key")
         .withRegion(regionName);
      AmazonS3 amazonS3 = new AbstractS3Action().buildAmazonS3ClientFromBackendMetaData(s3BackendMetaData);
      assertEquals(regionName, amazonS3.getRegionName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBuildAmazonS3ClientFromBackendMetaDataWrongType()
   {
      assertThrows(IllegalArgumentException.class, () ->
      {
         new AbstractS3Action().buildAmazonS3ClientFromBackendMetaData(new QBackendMetaData());
      });
   }
}