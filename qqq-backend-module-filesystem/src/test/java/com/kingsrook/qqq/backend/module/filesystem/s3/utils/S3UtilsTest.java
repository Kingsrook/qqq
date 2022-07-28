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
import java.util.List;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 **
 *******************************************************************************/
@DisabledOnOs(OS.LINUX)
public class S3UtilsTest extends BaseS3Test
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testListObjectsInBucketAtPath()
   {
      S3Utils s3Utils = getS3Utils();
      assertEquals(3, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/").size(), "Expected # of s3 objects without subfolders");
      assertEquals(2, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/*.csv").size(), "Expected # of csv s3 objects without subfolders");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/*.txt").size(), "Expected # of txt s3 objects without subfolders");
      assertEquals(0, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/*.pdf").size(), "Expected # of pdf s3 objects without subfolders");
      assertEquals(4, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/**").size(), "Expected # of s3 objects with subfolders");
      assertEquals(3, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "/" + TEST_FOLDER, "/").size(), "With leading slash");
      assertEquals(3, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "/" + TEST_FOLDER, "").size(), "Without trailing slash");
      assertEquals(3, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "//" + TEST_FOLDER, "//").size(), "With multiple leading and trailing slashes");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER + "/" + SUB_FOLDER, "").size(), "Just in the subfolder non-recursive");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER + "/" + SUB_FOLDER, "/**").size(), "Just in the subfolder recursive");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER + "//" + SUB_FOLDER, "/**").size(), "Just in the subfolder recursive, multi /");
      assertEquals(0, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "not-a-real-path/", "").size(), "In a non-existing folder");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "/", "").size(), "In the root folder, specified as /");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "//", "").size(), "In the root folder, specified as multiple /s");
      assertEquals(1, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "", "").size(), "In the root folder, specified as empty-string");
      assertEquals(5, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "/", "**").size(), "In the root folder, specified as /, and recursively");
      assertEquals(5, s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "", "**").size(), "In the root folder, specified as empty-string, and recursively");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGetObjectAsInputStream() throws IOException
   {
      S3Utils               s3Utils           = getS3Utils();
      List<S3ObjectSummary> s3ObjectSummaries = s3Utils.listObjectsInBucketMatchingGlob(BUCKET_NAME, "test-files", "");
      S3ObjectSummary       s3ObjectSummary   = s3ObjectSummaries.stream().filter(o -> o.getKey().contains("1.csv")).findAny().get();
      InputStream           inputStream       = s3Utils.getObjectAsInputStream(s3ObjectSummary);
      String                csvFromS3         = IOUtils.toString(inputStream);

      assertEquals(getCSVData1(), csvFromS3, "File from S3 should match expected content");
   }

}