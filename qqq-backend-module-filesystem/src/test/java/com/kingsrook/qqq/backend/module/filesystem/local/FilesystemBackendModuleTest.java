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

package com.kingsrook.qqq.backend.module.filesystem.local;


import java.io.File;
import java.io.IOException;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.AbstractFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemActionTest;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for FilesystemBackendModule
 *******************************************************************************/
public class FilesystemBackendModuleTest
{
   private final String PATH_THAT_WONT_EXIST = "some/path/that/wont/exist";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws IOException
   {
      new FilesystemActionTest().primeFilesystem();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   public void afterEach() throws Exception
   {
      new FilesystemActionTest().cleanFilesystem();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testListFiles() throws QException
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_BLOB_LOCAL_FS);

      AbstractFilesystemAction abstractFilesystemAction = new AbstractFilesystemAction();
      QBackendMetaData         backend                  = qInstance.getBackendForTable(table.getName());

      //////////////////////////////////////////////////////////
      // with no filter given, all (3) files should come back //
      //////////////////////////////////////////////////////////
      List<File> files = abstractFilesystemAction.listFiles(table, backend);
      assertEquals(3, files.size());

      /////////////////////////////////////////
      // filter for a file name that's found //
      /////////////////////////////////////////
      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-2.txt")));
      assertEquals(1, files.size());
      assertEquals("BLOB-2.txt", files.get(0).getName());

      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-1.txt")));
      assertEquals(1, files.size());
      assertEquals("BLOB-1.txt", files.get(0).getName());

      ///////////////////////////////////
      // filter for 2 names that exist //
      ///////////////////////////////////
      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.IN, "BLOB-1.txt", "BLOB-2.txt")));
      assertEquals(2, files.size());

      /////////////////////////////////////////////
      // filter for a file name that isn't found //
      /////////////////////////////////////////////
      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "NOT-FOUND.txt")));
      assertEquals(0, files.size());

      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.IN, "BLOB-2.txt", "NOT-FOUND.txt")));
      assertEquals(1, files.size());

      ////////////////////////////////////////////////////
      // 2 criteria, and'ed, and can't match, so find 0 //
      ////////////////////////////////////////////////////
      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(
         new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-1.txt"),
         new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-2.txt")));
      assertEquals(0, files.size());

      //////////////////////////////////////////////////
      // 2 criteria, or'ed, and both match, so find 2 //
      //////////////////////////////////////////////////
      files = abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(
         new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-1.txt"),
         new QFilterCriteria("fileName", QCriteriaOperator.EQUALS, "BLOB-2.txt"))
         .withBooleanOperator(QQueryFilter.BooleanOperator.OR));
      assertEquals(2, files.size());

      //////////////////////////////////////
      // ensure unsupported filters throw //
      //////////////////////////////////////
      assertThatThrownBy(() -> abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("foo", QCriteriaOperator.GREATER_THAN, 42))))
         .hasMessageContaining("Unable to query filesystem table by field");
      assertThatThrownBy(() -> abstractFilesystemAction.listFiles(table, backend, new QQueryFilter(new QFilterCriteria("fileName", QCriteriaOperator.IS_BLANK))))
         .hasMessageContaining("Unable to query filename field using operator");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteFile() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);

      /////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files - then delete one, then re-list, and assert that we have one fewer //
      /////////////////////////////////////////////////////////////////////////////////////////////
      List<File> filesBeforeDelete = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));

      FilesystemBackendModule filesystemBackendModule = new FilesystemBackendModule();
      filesystemBackendModule.getActionBase().deleteFile(qInstance, table, filesBeforeDelete.get(0).getAbsolutePath());

      List<File> filesAfterDelete = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));
      Assertions.assertEquals(filesBeforeDelete.size() - 1, filesAfterDelete.size(),
         "Should be one fewer file listed after deleting one.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteFileDoesNotExist() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files - then try to delete a fake path, then re-list, and assert that we have the same count //
      // note, our contract is that as long as the file doesn't exist after calling delete (e.g., if it wasn't there //
      // to begin with, then we're okay, and don't expect an exception                                               //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<File> filesBeforeDelete = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));

      FilesystemBackendModule filesystemBackendModule = new FilesystemBackendModule();
      filesystemBackendModule.getActionBase().deleteFile(qInstance, table, PATH_THAT_WONT_EXIST);

      List<File> filesAfterDelete = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));
      Assertions.assertEquals(filesBeforeDelete.size(), filesAfterDelete.size(),
         "Should be same number of files after deleting bogus path");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMoveFile() throws Exception
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);
      String         basePath  = ((FilesystemBackendMetaData) qInstance.getBackendForTable(table.getName())).getBasePath();
      String         subPath   = basePath + File.separator + "subdir";

      ////////////////////////////////////////////////////////////////////////////////////////////////////
      // first list the files (non-recursively) - then move one into a sub-folder, then re-list, and    //
      // assert that we have one fewer then list again including sub-folders, and see the changed count //
      ////////////////////////////////////////////////////////////////////////////////////////////////////
      List<File> filesBeforeMove = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));

      FilesystemBackendModule filesystemBackendModule = new FilesystemBackendModule();
      String                  originalFilePath        = filesBeforeMove.get(0).getAbsolutePath();
      String                  movedFilePath           = originalFilePath.replace(basePath, subPath);

      filesystemBackendModule.getActionBase().moveFile(qInstance, table, originalFilePath, movedFilePath);

      List<File> filesAfterMove = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));
      Assertions.assertEquals(filesBeforeMove.size() - 1, filesAfterMove.size(), "Should be one fewer file in the listing after moving one.");

      //////////////////////////////////////////////////////////////////////////
      // move the file back and assert that the count goes back to the before //
      //////////////////////////////////////////////////////////////////////////
      filesystemBackendModule.getActionBase().moveFile(qInstance, table, movedFilePath, originalFilePath);

      List<File> filesAfterMoveBack = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));
      Assertions.assertEquals(filesBeforeMove.size(), filesAfterMoveBack.size(), "Should be original number of files after moving back");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMoveFileDoesNotExit() throws Exception
   {
      QInstance      qInstance        = TestUtils.defineInstance();
      QTableMetaData table            = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);
      String         basePath         = ((FilesystemBackendMetaData) qInstance.getBackendForTable(table.getName())).getBasePath();
      String         subPath          = basePath + File.separator + "subdir";
      List<File>     filesBeforeMove  = new AbstractFilesystemAction().listFiles(table, qInstance.getBackendForTable(table.getName()));
      String         originalFilePath = filesBeforeMove.get(0).getAbsolutePath();
      String         movedFilePath    = originalFilePath.replace(basePath, subPath);

      Assertions.assertThrows(FilesystemException.class, () ->
      {
         FilesystemBackendModule filesystemBackendModule = new FilesystemBackendModule();
         filesystemBackendModule.getActionBase().moveFile(qInstance, table, PATH_THAT_WONT_EXIST, movedFilePath);
      });

   }

}