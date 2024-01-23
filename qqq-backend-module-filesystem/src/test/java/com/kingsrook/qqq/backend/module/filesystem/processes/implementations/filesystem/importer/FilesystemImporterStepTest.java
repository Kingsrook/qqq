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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.importer;


import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemActionTest;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for FilesystemImporterStep 
 *******************************************************************************/
class FilesystemImporterStepTest extends FilesystemActionTest
{

   //////////////////////////////////////////////////////////////////////////
   // note - we take advantage of the @BeforeEach and @AfterEach to set up //
   // and clean up files on disk for this test.                            //
   //////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   public void afterEach() throws Exception
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      /////////////////////////////////////////////////////
      // make sure we see 2 source files before we begin //
      /////////////////////////////////////////////////////
      FilesystemBackendMetaData backend   = (FilesystemBackendMetaData) QContext.getQInstance().getBackend(TestUtils.BACKEND_NAME_LOCAL_FS);
      String                    basePath  = backend.getBasePath();
      File                      sourceDir = new File(basePath + "/persons-csv/");
      assertEquals(2, listOrFail(sourceDir).length);

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      String importBaseName = "personImporter";
      assertEquals(2, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX)).getCount());
      assertEquals(5, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX)).getCount());

      QRecord record = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(1, record.getValue("importFileId"));
      assertEquals("John", record.getValue("firstName"));
      assertThat(record.getValue("values")).isInstanceOf(String.class);
      JSONObject values = new JSONObject(record.getValueString("values"));
      assertEquals("John", values.get("firstName"));

      ///////////////////////////////////////////
      // make sure 2 archive files got created //
      ///////////////////////////////////////////
      LocalDateTime now = LocalDateTime.now();
      assertEquals(2, listOrFail(new File(basePath + "/archive/archive-of/personImporterFiles/" + now.getYear() + "/" + now.getMonth())).length);

      ////////////////////////////////////////////
      // make sure the source files got deleted //
      ////////////////////////////////////////////
      assertEquals(0, listOrFail(sourceDir).length);
   }



   /*******************************************************************************
    ** do a listFiles, but fail properly if it returns null (so IJ won't warn all the time)
    *******************************************************************************/
   private static File[] listOrFail(File dir)
   {
      File[] files = dir.listFiles();
      if(files == null)
      {
         fail("Null result when listing directory: " + dir);
      }
      return (files);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testJSON() throws QException
   {
      ////////////////////////////////////////////////////////////////////
      // adjust the process to use the JSON file table, and JSON format //
      ////////////////////////////////////////////////////////////////////
      QProcessMetaData process = QContext.getQInstance().getProcess(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_SOURCE_TABLE)).findFirst().get().setDefaultValue(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_FILE_FORMAT)).findFirst().get().setDefaultValue("json");

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      String importBaseName = "personImporter";
      assertEquals(2, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX)).getCount());
      assertEquals(3, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX)).getCount());

      QRecord record = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(1, record.getValue("importFileId"));
      assertEquals("John", record.getValue("firstName"));
      assertThat(record.getValue("values")).isInstanceOf(String.class);
      JSONObject values = new JSONObject(record.getValueString("values"));
      assertEquals("John", values.get("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoFilesFound() throws Exception
   {
      cleanFilesystem();

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      String importBaseName = "personImporter";
      assertEquals(0, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX)).getCount());
      assertEquals(0, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX)).getCount());
   }

   // todo - updates?



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDuplicateFileNameNonUpdate() throws Exception
   {
      FilesystemBackendMetaData backend   = (FilesystemBackendMetaData) QContext.getQInstance().getBackend(TestUtils.BACKEND_NAME_LOCAL_FS);
      String                    basePath  = backend.getBasePath();
      File                      sourceDir = new File(basePath + "/persons-csv/");

      /////////////////////////////////////////////////////////////////
      // run the process once - assert how many records got inserted //
      /////////////////////////////////////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      String importBaseName = "personImporter";
      assertEquals(2, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX)).getCount());
      assertEquals(5, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX)).getCount());

      ///////////////////////////////////////////////////////
      // put the source files back - assert they are there //
      ///////////////////////////////////////////////////////
      writePersonCSVFiles(new File(basePath));
      assertEquals(2, listOrFail(sourceDir).length);

      ////////////////////////
      // re-run the process //
      ////////////////////////
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      ////////////////////////////////////////
      // make sure no new records are built //
      ////////////////////////////////////////
      assertEquals(2, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX)).getCount());
      assertEquals(5, new CountAction().execute(new CountInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX)).getCount());

      /////////////////////////////////////////////////
      // make sure no new archive files were created //
      /////////////////////////////////////////////////
      LocalDateTime now = LocalDateTime.now();
      assertEquals(2, listOrFail(new File(basePath + "/archive/archive-of/personImporterFiles/" + now.getYear() + "/" + now.getMonth())).length);

      ////////////////////////////////////////////
      // make sure the source files got deleted //
      ////////////////////////////////////////////
      assertEquals(0, listOrFail(sourceDir).length);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKey() throws QException
   {
      //////////////////////////////////////////////
      // Add a security name/value to our process //
      //////////////////////////////////////////////
      QProcessMetaData process = QContext.getQInstance().getProcess(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_NAME)).findFirst().get().setDefaultValue("customerId");
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_VALUE)).findFirst().get().setDefaultValue(47);

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      ////////////////////////////////////////////////////////////////////////////////////////////
      // assert the security field gets its value on both the importFile & importRecord records //
      ////////////////////////////////////////////////////////////////////////////////////////////
      String  importBaseName = "personImporter";
      QRecord fileRecord     = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(47, fileRecord.getValue("customerId"));

      QRecord recordRecord = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(47, recordRecord.getValue("customerId"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecuritySupplier() throws QException
   {
      //////////////////////////////////////////////
      // Add a security name/value to our process //
      //////////////////////////////////////////////
      QProcessMetaData process = QContext.getQInstance().getProcess(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_IMPORT_SECURITY_FIELD_NAME)).findFirst().get().setDefaultValue("customerId");
      process.getInputFields().stream().filter(f -> f.getName().equals(FilesystemImporterStep.FIELD_IMPORT_SECURITY_VALUE_SUPPLIER)).findFirst().get().setDefaultValue(new QCodeReference(SecuritySupplier.class));

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.LOCAL_PERSON_CSV_FILE_IMPORTER_PROCESS_NAME);
      new RunProcessAction().execute(runProcessInput);

      ////////////////////////////////////////////////////////////////////////////////////////////
      // assert the security field gets its value on both the importFile & importRecord records //
      ////////////////////////////////////////////////////////////////////////////////////////////
      String  importBaseName = "personImporter";
      QRecord fileRecord     = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_FILE_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(1701, fileRecord.getValue("customerId"));

      QRecord recordRecord = new GetAction().executeForRecord(new GetInput(importBaseName + FilesystemImporterMetaDataTemplate.IMPORT_RECORD_TABLE_SUFFIX).withPrimaryKey(1));
      assertEquals(1701, recordRecord.getValue("customerId"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SecuritySupplier implements Function<QRecord, Serializable>
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public Serializable apply(QRecord qRecord)
      {
         return (1701);
      }
   }

}