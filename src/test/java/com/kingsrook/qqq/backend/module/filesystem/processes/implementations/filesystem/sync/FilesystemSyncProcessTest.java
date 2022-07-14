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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.sync;


import java.io.File;
import java.io.IOException;
import com.kingsrook.qqq.backend.core.actions.RunBackendStepAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for FilesystemSyncProcess
 *******************************************************************************/
class FilesystemSyncProcessTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws Exception
   {
      TestUtils.cleanInstanceFiles();

      QTableMetaData       sourceTable     = defineTable("source");
      QTableMetaData       archiveTable    = defineTable("archive");
      QTableMetaData       processingTable = defineTable("processing");
      QProcessMetaData     process         = new FilesystemSyncProcess().defineProcessMetaData();
      QBackendStepMetaData step            = (QBackendStepMetaData) process.getStep(FilesystemSyncStep.STEP_NAME);

      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_SOURCE_TABLE).setDefaultValue(sourceTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_ARCHIVE_TABLE).setDefaultValue(archiveTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_PROCESSING_TABLE).setDefaultValue(processingTable.getName());
      // step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_MAX_FILES_TO_ARCHIVE).setDefaultValue(1);

      QInstance qInstance = TestUtils.defineInstance();
      qInstance.addTable(sourceTable);
      qInstance.addTable(archiveTable);
      qInstance.addTable(processingTable);
      qInstance.addProcess(process);

      ///////////////////////////
      // write some test files //
      ///////////////////////////
      String basePath = ((FilesystemBackendMetaData) qInstance.getBackend(TestUtils.BACKEND_NAME_LOCAL_FS)).getBasePath();
      writeTestFile(basePath, sourceTable, "1.txt", "x");
      writeTestFile(basePath, sourceTable, "2.txt", "x");
      // writeTestFile(basePath, sourceTable, "3.txt", "x");
      writeTestFile(basePath, archiveTable, "2.txt", "x");

      //////////////////////
      // run the step //
      //////////////////////
      RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(qInstance);
      runBackendStepRequest.setStepName(step.getName());
      runBackendStepRequest.setProcessName(process.getName());
      runBackendStepRequest.setSession(TestUtils.getMockSession());

      RunBackendStepAction runFunctionAction    = new RunBackendStepAction();
      RunBackendStepResult runBackendStepResult = runFunctionAction.execute(runBackendStepRequest);
      // System.out.println(runBackendStepResult);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeTestFile(String basePath, QTableMetaData table, String name, String content) throws IOException
   {
      String path = ((FilesystemTableBackendDetails) table.getBackendDetails()).getBasePath();
      File   file = new File(basePath + "/" + path + "/" + name);
      FileUtils.writeStringToFile(file, content);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineTable(String subPath)
   {
      return new QTableMetaData()
         .withName("table-" + subPath)
         .withBackendName(TestUtils.BACKEND_NAME_LOCAL_FS)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withBackendDetails(new FilesystemTableBackendDetails()
            .withBasePath(subPath));
   }

}