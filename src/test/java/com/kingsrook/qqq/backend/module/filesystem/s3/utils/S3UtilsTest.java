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


import java.io.IOException;
import java.io.InputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
public class S3UtilsTest extends BaseS3Test
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testListObjectsInBucketAtPath()
   {
      S3Utils s3Utils = getS3Utils();
      assertEquals(2, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, TEST_FOLDER + "/", false).size(), "Expected # of s3 objects without subfolders");
      assertEquals(3, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, TEST_FOLDER + "/", true).size(), "Expected # of s3 objects with subfolders");
      assertEquals(2, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, "/" + TEST_FOLDER + "/", false).size(), "With leading slash");
      assertEquals(2, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, "/" + TEST_FOLDER, false).size(), "Without trailing slash");
      assertEquals(2, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, "//" + TEST_FOLDER + "//", false).size(), "With multiple leading and trailing slashes");
      assertEquals(1, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, TEST_FOLDER + "/" + SUB_FOLDER, false).size(), "Just in the subfolder non-recursive");
      assertEquals(1, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, TEST_FOLDER + "/" + SUB_FOLDER, true).size(), "Just in the subfolder recursive");
      assertEquals(1, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, TEST_FOLDER + "//" + SUB_FOLDER, true).size(), "Just in the subfolder recursive");
      assertEquals(0, s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, "not-a-real-path/", true).size(), "In a non-existing folder");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGetObjectAsInputStream() throws IOException
   {
      S3Utils         s3Utils         = getS3Utils();
      S3ObjectSummary s3ObjectSummary = s3Utils.listObjectsInBucketAtPath(BUCKET_NAME, "test-files", true).get(0);
      InputStream     inputStream     = s3Utils.getObjectAsInputStream(s3ObjectSummary);
      String          csvFromS3       = IOUtils.toString(inputStream);

      // todo - should check the filename somewhere, right?
      assertEquals(getCSVData1(), csvFromS3, "File from S3 should match expected content");
   }

}