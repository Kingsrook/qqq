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


import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.RunBackendStepAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BasicETLCleanupSourceFilesFunction
 *******************************************************************************/
public class BasicETLCleanupSourceFilesFunctionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDelete1Record1File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);
      testDelete(qInstance, List.of(filePath));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDelete2Records1File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);
      testDelete(qInstance, List.of(filePath, filePath));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDelete2Record2File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath1 = getRandomFilePathPersonTable(qInstance);
      String    filePath2 = getRandomFilePathPersonTable(qInstance);
      testDelete(qInstance, List.of(filePath1, filePath2));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMove1Record1File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);
      testMove(qInstance, List.of(filePath));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMove2Records1File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);
      testMove(qInstance, List.of(filePath, filePath));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMove2Record2File() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath1 = getRandomFilePathPersonTable(qInstance);
      String    filePath2 = getRandomFilePathPersonTable(qInstance);
      testMove(qInstance, List.of(filePath1, filePath2));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void testDelete(QInstance qInstance, List<String> filePaths) throws Exception
   {
      RunBackendStepResult runBackendStepResult = runFunction(qInstance, filePaths, Map.of(
         BasicETLCleanupSourceFilesFunction.FIELD_MOVE_OR_DELETE, BasicETLCleanupSourceFilesFunction.VALUE_DELETE,
         // todo - even though this field isn't needed, since we gave a value of "delete"
         //  the RunFunctionAction considers any missing input to be an error...
         BasicETLCleanupSourceFilesFunction.FIELD_DESTINATION_FOR_MOVES, ""));

      assertNull(runBackendStepResult.getError());
      for(String filePath : filePaths)
      {
         assertFalse(new File(filePath).exists(), "File should have been deleted.");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void testMove(QInstance qInstance, List<String> filePaths) throws Exception
   {
      String trashDir = File.separator + "tmp" + File.separator + "trash";
      RunBackendStepResult runBackendStepResult = runFunction(qInstance, filePaths, Map.of(
         BasicETLCleanupSourceFilesFunction.FIELD_MOVE_OR_DELETE, BasicETLCleanupSourceFilesFunction.VALUE_MOVE,
         BasicETLCleanupSourceFilesFunction.FIELD_DESTINATION_FOR_MOVES, trashDir));

      assertNull(runBackendStepResult.getError());

      for(String filePath : filePaths)
      {
         assertFalse(new File(filePath).exists(), "File should have been moved.");

         String movedPath = trashDir + File.separator + (new File(filePath).getName());
         assertTrue(new File(movedPath).exists(), "File should have been moved.");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunBackendStepResult runFunction(QInstance qInstance, List<String> filePaths, Map<String, String> values) throws Exception
   {
      QBackendStepMetaData backendStepMetaData = new BasicETLCleanupSourceFilesFunction().defineStepMetaData();
      QProcessMetaData     qProcessMetaData  = new QProcessMetaData().withName("testScaffold").addStep(backendStepMetaData);
      qInstance.addProcess(qProcessMetaData);

      HashSet<String> filePathsSet = new HashSet<>(filePaths);
      for(String filePath : filePathsSet)
      {
         File file = new File(filePath);
         FileUtils.writeStringToFile(file, "content");
      }

      // List<QRecord> records = filePaths.stream()
      //    .map(filePath -> new QRecord().withBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, filePath)).toList();

      RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(qInstance);
      runBackendStepRequest.setStepName(backendStepMetaData.getName());
      runBackendStepRequest.setProcessName(qProcessMetaData.getName());
      // runFunctionRequest.setRecords(records);
      runBackendStepRequest.setSession(TestUtils.getMockSession());
      runBackendStepRequest.addValue(BasicETLProcess.FIELD_SOURCE_TABLE, TestUtils.TABLE_NAME_PERSON_LOCAL_FS);
      runBackendStepRequest.addValue(BasicETLProcess.FIELD_DESTINATION_TABLE, TestUtils.TABLE_NAME_PERSON_S3);
      runBackendStepRequest.addValue(BasicETLCollectSourceFileNamesFunction.FIELD_SOURCE_FILE_PATHS, StringUtils.join(",", filePathsSet));

      for(Map.Entry<String, String> entry : values.entrySet())
      {
         runBackendStepRequest.addValue(entry.getKey(), entry.getValue());
      }

      RunBackendStepAction runFunctionAction = new RunBackendStepAction();
      return (runFunctionAction.execute(runBackendStepRequest));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getRandomFilePathPersonTable(QInstance qInstance)
   {
      FilesystemBackendMetaData     backend        = (FilesystemBackendMetaData) qInstance.getBackend(TestUtils.BACKEND_NAME_LOCAL_FS);
      FilesystemTableBackendDetails backendDetails = (FilesystemTableBackendDetails) qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS).getBackendDetails();
      String                        tablePath      = backend.getBasePath() + File.separator + backendDetails.getBasePath();
      String                        filePath       = tablePath + File.separator + UUID.randomUUID();
      return filePath;
   }

}