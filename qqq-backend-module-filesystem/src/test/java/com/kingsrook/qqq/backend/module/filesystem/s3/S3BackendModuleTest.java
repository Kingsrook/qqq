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

package com.kingsrook.qqq.backend.module.filesystem.s3;


import java.util.List;
import java.util.UUID;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.AbstractS3Action;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;


/*******************************************************************************
 ** Unit test for S3BackendModule
 *******************************************************************************/
@DisabledOnOs(OS.LINUX)
public class S3BackendModuleTest extends BaseS3Test
{
   private final String PATH_THAT_WONT_EXIST = "some/path/that/wont/exist";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteFile() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_S3);

      /////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files - then delete one, then re-list, and assert that we have one fewer //
      /////////////////////////////////////////////////////////////////////////////////////////////
      List<S3ObjectSummary> s3ObjectSummariesBeforeDelete = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");

      S3BackendModule  s3BackendModule = new S3BackendModule();
      AbstractS3Action actionBase      = (AbstractS3Action) s3BackendModule.getActionBase();
      actionBase.setS3Utils(getS3Utils());
      actionBase.deleteFile(qInstance, table, s3ObjectSummariesBeforeDelete.get(0).getKey());

      List<S3ObjectSummary> s3ObjectSummariesAfterDelete = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");
      Assertions.assertEquals(s3ObjectSummariesBeforeDelete.size() - 1, s3ObjectSummariesAfterDelete.size(),
         "Should be one fewer file listed after deleting one.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteFileDoesNotExist() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_S3);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files - then try to delete a fake path, then re-list, and assert that we have the same count //
      // note, we'd like to detect the non-delete, but there's no such info back from aws it appears?                //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<S3ObjectSummary> s3ObjectSummariesBeforeDelete = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");

      S3BackendModule  s3BackendModule = new S3BackendModule();
      AbstractS3Action actionBase      = (AbstractS3Action) s3BackendModule.getActionBase();
      actionBase.setS3Utils(getS3Utils());
      actionBase.deleteFile(qInstance, table, PATH_THAT_WONT_EXIST);

      List<S3ObjectSummary> s3ObjectSummariesAfterDelete = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");
      Assertions.assertEquals(s3ObjectSummariesBeforeDelete.size(), s3ObjectSummariesAfterDelete.size(),
         "Should be same number of files after deleting bogus path");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMoveFile() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_S3);
      String         subPath   = TEST_FOLDER + "/" + SUB_FOLDER;

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files (non-recursively) - then move one into a sub-folder, then re-list, and    //
      // assert that we have one fewer then list again including sub-folders, and see the changed count //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      List<S3ObjectSummary> s3ObjectSummariesBeforeMove            = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");
      List<S3ObjectSummary> s3ObjectSummariesInSubFolderBeforeMove = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, subPath, "");
      List<S3ObjectSummary> s3ObjectSummariesRecursiveBeforeMove   = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/**");

      S3BackendModule  s3BackendModule = new S3BackendModule();
      AbstractS3Action actionBase      = (AbstractS3Action) s3BackendModule.getActionBase();
      actionBase.setS3Utils(getS3Utils());
      String key = s3ObjectSummariesBeforeMove.get(0).getKey();
      actionBase.moveFile(qInstance, table, key, key.replaceFirst(TEST_FOLDER, subPath));

      List<S3ObjectSummary> s3ObjectSummariesAfterMove            = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "");
      List<S3ObjectSummary> s3ObjectSummariesRecursiveAfterMove   = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, TEST_FOLDER, "/**");
      List<S3ObjectSummary> s3ObjectSummariesInSubFolderAfterMove = getS3Utils().listObjectsInBucketMatchingGlob(BUCKET_NAME, subPath, "");

      Assertions.assertEquals(s3ObjectSummariesBeforeMove.size() - 1, s3ObjectSummariesAfterMove.size(),
         "Should be one fewer file in the non-recursive listing after moving one.");
      Assertions.assertEquals(s3ObjectSummariesRecursiveBeforeMove.size(), s3ObjectSummariesRecursiveAfterMove.size(),
         "Should be same number of files in the recursive listing before and after the move");
      Assertions.assertEquals(s3ObjectSummariesInSubFolderBeforeMove.size() + 1, s3ObjectSummariesInSubFolderAfterMove.size(),
         "Should be one move file in the sub-folder listing after moving one.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMoveFileDoesNotExit() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_S3);
      String         subPath   = TEST_FOLDER + "/" + SUB_FOLDER;

      S3BackendModule  s3BackendModule = new S3BackendModule();
      AbstractS3Action actionBase      = (AbstractS3Action) s3BackendModule.getActionBase();
      actionBase.setS3Utils(getS3Utils());

      Assertions.assertThrows(FilesystemException.class, () ->
         actionBase.moveFile(qInstance, table, PATH_THAT_WONT_EXIST, subPath + "/" + UUID.randomUUID())
      );
   }

}