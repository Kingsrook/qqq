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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.etl.basic;


import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.RunBackendStepAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BasicETLCollectSourceFileNamesFunction
 *******************************************************************************/
class BasicETLCollectSourceFileNamesStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testOneFile() throws Exception
   {
      String file   = "/tmp/test1.csv";
      String result = runTest(file);
      assertEquals(file, result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testTwoFiles() throws Exception
   {
      String file1  = "/tmp/test1.csv";
      String file2  = "/tmp/test2.csv";
      String result = runTest(file1, file2);

      //////////////////////////////////////////////////////////////////////
      // the names go into a set, so they can come out in either order... //
      //////////////////////////////////////////////////////////////////////
      assertTrue(result.equals(file1 + "," + file2) || result.equals((file2 + "," + file1)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDuplicatedFile() throws Exception
   {
      String file   = "/tmp/test1.csv";
      String result = runTest(file, file);
      assertEquals(file, result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String runTest(String... fileNames) throws Exception
   {
      QInstance            qInstance           = TestUtils.defineInstance();
      QBackendStepMetaData backendStepMetaData = new BasicETLCollectSourceFileNamesStep().defineStepMetaData();
      QProcessMetaData     qProcessMetaData    = new QProcessMetaData().withName("testScaffold").addStep(backendStepMetaData);
      qInstance.addProcess(qProcessMetaData);

      List<QRecord> records = Arrays.stream(fileNames).map(fileName ->
         new QRecord().withBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, fileName)).toList();

      RunBackendStepInput runBackendStepInput = new RunBackendStepInput(qInstance);
      runBackendStepInput.setSession(TestUtils.getMockSession());
      runBackendStepInput.setStepName(backendStepMetaData.getName());
      runBackendStepInput.setProcessName(qProcessMetaData.getName());
      runBackendStepInput.setRecords(records);

      RunBackendStepAction runBackendStepAction = new RunBackendStepAction();
      RunBackendStepOutput result               = runBackendStepAction.execute(runBackendStepInput);

      return ((String) result.getValues().get(BasicETLCollectSourceFileNamesStep.FIELD_SOURCE_FILE_PATHS));
   }
}