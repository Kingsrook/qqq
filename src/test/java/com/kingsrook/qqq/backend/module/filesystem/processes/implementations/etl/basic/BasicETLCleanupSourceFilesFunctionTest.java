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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.RunFunctionAction;
import com.kingsrook.qqq.backend.core.callbacks.NoopCallback;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.basic.BasicETLProcess;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.FilesystemRecordBackendDetailFields;
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
   public void testDelete() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);

      RunFunctionResult runFunctionResult = runFunction(qInstance, filePath, Map.of(
         BasicETLCleanupSourceFilesFunction.FIELD_MOVE_OR_DELETE, "delete",
         BasicETLCleanupSourceFilesFunction.FIELD_DESTINATION_FOR_MOVES, "/tmp/trash"));

      assertNull(runFunctionResult.getError());
      assertFalse(new File(filePath).exists(), "File should have been deleted.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMove() throws Exception
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    filePath  = getRandomFilePathPersonTable(qInstance);

      String trashDir = File.separator + "tmp" + File.separator + "trash";
      RunFunctionResult runFunctionResult = runFunction(qInstance, filePath, Map.of(
         BasicETLCleanupSourceFilesFunction.FIELD_MOVE_OR_DELETE, "move",
         BasicETLCleanupSourceFilesFunction.FIELD_DESTINATION_FOR_MOVES, trashDir));

      assertNull(runFunctionResult.getError());
      assertFalse(new File(filePath).exists(), "File should have been moved.");

      String movedPath = trashDir + File.separator + (new File(filePath).getName());
      assertTrue(new File(movedPath).exists(), "File should have been moved.");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private RunFunctionResult runFunction(QInstance qInstance, String filePath, Map<String, String> values) throws Exception
   {
      QFunctionMetaData qFunctionMetaData = new BasicETLCleanupSourceFilesFunction().defineFunctionMetaData();
      QProcessMetaData  qProcessMetaData  = new QProcessMetaData().withName("testScaffold").addFunction(qFunctionMetaData);
      qInstance.addProcess(qProcessMetaData);

      File file = new File(filePath);
      FileUtils.writeStringToFile(file, "content");

      List<QRecord> records = List.of(new QRecord().withBackendDetail(FilesystemRecordBackendDetailFields.FULL_PATH, filePath));

      RunFunctionRequest runFunctionRequest = new RunFunctionRequest(qInstance);
      runFunctionRequest.setFunctionName(qFunctionMetaData.getName());
      runFunctionRequest.setProcessName(qProcessMetaData.getName());
      runFunctionRequest.setCallback(new NoopCallback());
      runFunctionRequest.setRecords(records);
      runFunctionRequest.setSession(TestUtils.getMockSession());
      runFunctionRequest.addValue(BasicETLProcess.FIELD_SOURCE_TABLE, TestUtils.TABLE_NAME_PERSON_LOCAL_FS);
      runFunctionRequest.addValue(BasicETLProcess.FIELD_DESTINATION_TABLE, TestUtils.TABLE_NAME_PERSON_S3);

      for(Map.Entry<String, String> entry : values.entrySet())
      {
         runFunctionRequest.addValue(entry.getKey(), entry.getValue());
      }

      RunFunctionAction runFunctionAction = new RunFunctionAction();
      return (runFunctionAction.execute(runFunctionRequest));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getRandomFilePathPersonTable(QInstance qInstance)
   {
      FilesystemBackendMetaData     backend        = (FilesystemBackendMetaData) qInstance.getBackend(TestUtils.BACKEND_NAME_LOCAL_FS);
      FilesystemTableBackendDetails backendDetails = (FilesystemTableBackendDetails) qInstance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS).getBackendDetails();
      String                        tablePath      = backend.getBasePath() + File.separator + backendDetails.getPath();
      String                        filePath       = tablePath + File.separator + UUID.randomUUID();
      return filePath;
   }

}