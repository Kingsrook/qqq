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
import java.time.LocalDateTime;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemActionTest;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
   public void filesystemBaseAfterEach() throws Exception
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
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

      FilesystemBackendMetaData backend  = (FilesystemBackendMetaData) QContext.getQInstance().getBackend(TestUtils.BACKEND_NAME_LOCAL_FS);
      String                    basePath = backend.getBasePath();
      System.out.println(basePath);

      ///////////////////////////////////////////
      // make sure 2 archive files got created //
      ///////////////////////////////////////////
      LocalDateTime now   = LocalDateTime.now();
      File[]        files = new File(basePath + "/archive/archive-of/personImporterFiles/" + now.getYear() + "/" + now.getMonth()).listFiles();
      assertNotNull(files);
      assertEquals(2, files.length);
   }

   // todo - test json

   // todo - test no files found

   // todo - confirm delete happens?

   // todo - updates?

}