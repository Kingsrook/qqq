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
import java.nio.charset.StandardCharsets;
import com.kingsrook.qqq.backend.core.actions.processes.RunBackendStepAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.BaseTest;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.Cardinality;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.RecordFormat;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for FilesystemSyncProcess
 *******************************************************************************/
class FilesystemSyncProcessTest extends BaseTest
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
      reInitInstanceInContext(qInstance);

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
      RunBackendStepInput runBackendStepInput = new RunBackendStepInput();
      runBackendStepInput.setStepName(step.getName());
      runBackendStepInput.setProcessName(process.getName());

      RunBackendStepAction runFunctionAction    = new RunBackendStepAction();
      RunBackendStepOutput runBackendStepOutput = runFunctionAction.execute(runBackendStepInput);
      // System.out.println(runBackendStepResult);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeTestFile(String basePath, QTableMetaData table, String name, String content) throws IOException
   {
      String path = ((FilesystemTableBackendDetails) table.getBackendDetails()).getBasePath();
      File   file = new File(basePath + "/" + path + "/" + name);
      FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
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
            .withCardinality(Cardinality.MANY)
            .withRecordFormat(RecordFormat.CSV)
            .withBasePath(subPath));
   }

}