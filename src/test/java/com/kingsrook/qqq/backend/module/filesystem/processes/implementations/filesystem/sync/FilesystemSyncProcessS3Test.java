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


import java.util.List;
import java.util.stream.Collectors;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.kingsrook.qqq.backend.core.actions.RunBackendStepAction;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModule;
import com.kingsrook.qqq.backend.module.filesystem.s3.S3BackendModuleSubclassForTest;
import com.kingsrook.qqq.backend.module.filesystem.s3.actions.AbstractS3Action;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3TableBackendDetails;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for FilesystemSyncProcess using S3 backend
 *******************************************************************************/
class FilesystemSyncProcessS3Test extends BaseS3Test
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws Exception
   {
      QBackendModuleDispatcher.registerBackendModule(new S3BackendModuleSubclassForTest());
      QInstance qInstance = TestUtils.defineInstance();

      String sourceBucket     = "source-bucket";
      String archiveBucket    = "archive-bucket";
      String processingBucket = "processing-bucket";

      getAmazonS3().createBucket(sourceBucket);
      getAmazonS3().createBucket(archiveBucket);
      getAmazonS3().createBucket(processingBucket);

      S3BackendMetaData sourceBackend     = defineBackend(qInstance, "source", sourceBucket);
      S3BackendMetaData archiveBackend    = defineBackend(qInstance, "archive", archiveBucket);
      S3BackendMetaData processingBackend = defineBackend(qInstance, "processing", processingBucket);

      QTableMetaData sourceTable     = defineTable(qInstance, "source", sourceBackend, "source", "*/l3/*.csv");
      QTableMetaData archiveTable    = defineTable(qInstance, "archive", archiveBackend, "archive", "*/l3/*.csv");
      QTableMetaData processingTable = defineTable(qInstance, "processing", processingBackend, "processing", "**/*.csv");

      QProcessMetaData     process = new FilesystemSyncProcess().defineProcessMetaData();
      QBackendStepMetaData step    = process.getBackendStep(FilesystemSyncFunction.FUNCTION_NAME);
      qInstance.addProcess(process);

      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_SOURCE_TABLE).setDefaultValue(sourceTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_ARCHIVE_TABLE).setDefaultValue(archiveTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_PROCESSING_TABLE).setDefaultValue(processingTable.getName());

      ///////////////////////////
      // write some test files //
      ///////////////////////////
      writeTestFile(sourceBackend, sourceTable, "foo/l3/1.csv", "x");
      writeTestFile(sourceBackend, sourceTable, "bar/l3/2.csv", "x");
      writeTestFile(archiveBackend, archiveTable, "foo/l3/1.csv", "x");

      printTableListing(sourceBackend, sourceTable);
      printTableListing(archiveBackend, archiveTable);
      printTableListing(processingBackend, processingTable);

      //////////////////
      // run the step //
      //////////////////
      RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(qInstance);
      runBackendStepRequest.setStepName(step.getName());
      runBackendStepRequest.setProcessName(process.getName());
      runBackendStepRequest.setSession(TestUtils.getMockSession());

      RunBackendStepAction runFunctionAction    = new RunBackendStepAction();
      RunBackendStepResult runBackendStepResult = runFunctionAction.execute(runBackendStepRequest);
      System.out.println(runBackendStepResult);

      printTableListing(sourceBackend, sourceTable);
      printTableListing(archiveBackend, archiveTable);
      printTableListing(processingBackend, processingTable);

      assertTableListing(archiveBackend, archiveTable, "root/archive/foo/l3/1.csv", "root/archive/bar/l3/2.csv");
      assertTableListing(processingBackend, processingTable, "root/processing/bar/l3/2.csv");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testArchiveAndProcessingInSameBucket() throws Exception
   {
      QBackendModuleDispatcher.registerBackendModule(new S3BackendModuleSubclassForTest());
      QInstance qInstance = TestUtils.defineInstance();

      String vendorBucket = "vendor-bucket";
      String localBucket  = "local-bucket";

      getAmazonS3().createBucket(vendorBucket);
      getAmazonS3().createBucket(localBucket);

      S3BackendMetaData vendorBackend = defineBackend(qInstance, "source", vendorBucket);
      S3BackendMetaData localBackend  = defineBackend(qInstance, "archive", localBucket);

      QTableMetaData sourceTable     = defineTable(qInstance, "source", vendorBackend, "source", "*/l3/*.csv");
      QTableMetaData archiveTable    = defineTable(qInstance, "archive", localBackend, "archive", "*/l3/*.csv");
      QTableMetaData processingTable = defineTable(qInstance, "processing", localBackend, "processing", "**/*.csv");

      QProcessMetaData     process  = new FilesystemSyncProcess().defineProcessMetaData();
      QBackendStepMetaData step = process.getBackendStep(FilesystemSyncFunction.FUNCTION_NAME);
      qInstance.addProcess(process);

      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_SOURCE_TABLE).setDefaultValue(sourceTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_ARCHIVE_TABLE).setDefaultValue(archiveTable.getName());
      step.getInputMetaData().getFieldThrowing(FilesystemSyncProcess.FIELD_PROCESSING_TABLE).setDefaultValue(processingTable.getName());

      ///////////////////////////
      // write some test files //
      ///////////////////////////
      writeTestFile(vendorBackend, sourceTable, "foo/l3/1.csv", "x");
      writeTestFile(vendorBackend, sourceTable, "bar/l3/2.csv", "x");
      writeTestFile(localBackend, archiveTable, "foo/l3/1.csv", "x");

      printTableListing(vendorBackend, sourceTable);
      printTableListing(localBackend, archiveTable);
      printTableListing(localBackend, processingTable);

      //////////////////
      // run the step //
      //////////////////
      RunBackendStepRequest runBackendStepRequest = new RunBackendStepRequest(qInstance);
      runBackendStepRequest.setStepName(step.getName());
      runBackendStepRequest.setProcessName(process.getName());
      runBackendStepRequest.setSession(TestUtils.getMockSession());

      RunBackendStepAction runFunctionAction    = new RunBackendStepAction();
      RunBackendStepResult runBackendStepResult = runFunctionAction.execute(runBackendStepRequest);
      System.out.println(runBackendStepResult);

      printTableListing(vendorBackend, sourceTable);
      printTableListing(localBackend, archiveTable);
      printTableListing(localBackend, processingTable);

      assertTableListing(localBackend, archiveTable, "root/archive/foo/l3/1.csv", "root/archive/bar/l3/2.csv");
      assertTableListing(localBackend, processingTable, "root/processing/bar/l3/2.csv");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertTableListing(S3BackendMetaData backend, QTableMetaData table, String... paths) throws QModuleDispatchException
   {
      S3BackendModule  module     = (S3BackendModule) new QBackendModuleDispatcher().getQBackendModule(backend);
      AbstractS3Action actionBase = (AbstractS3Action) module.getActionBase();

      List<S3ObjectSummary> s3ObjectSummaries = actionBase.listFiles(table, backend);
      assertEquals(paths.length, s3ObjectSummaries.size(), "Expected number of files in table: " + table.getName());
      for(String path : paths)
      {
         assertTrue(s3ObjectSummaries.stream().anyMatch(s3o -> s3o.getKey().equals(path)),
            "Path [" + path + "] should be in the listing, but was not.  Full listing is: " +
               s3ObjectSummaries.stream().map(S3ObjectSummary::getKey).collect(Collectors.joining(",")));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void printTableListing(S3BackendMetaData backend, QTableMetaData table) throws QModuleDispatchException
   {
      S3BackendModule  module     = (S3BackendModule) new QBackendModuleDispatcher().getQBackendModule(backend);
      AbstractS3Action actionBase = (AbstractS3Action) module.getActionBase();

      System.out.println("Files in: " + table.getName());
      actionBase.listFiles(table, backend).forEach(o -> System.out.println(o.getKey()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeTestFile(S3BackendMetaData backend, QTableMetaData table, String name, String content) throws Exception
   {
      S3BackendModule  module     = (S3BackendModule) new QBackendModuleDispatcher().getQBackendModule(backend);
      AbstractS3Action actionBase = (AbstractS3Action) module.getActionBase();
      String           fullPath   = actionBase.getFullBasePath(table, backend);

      actionBase.writeFile(backend, fullPath + "/" + name, content.getBytes());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private S3BackendMetaData defineBackend(QInstance qInstance, String which, String bucketName)
   {
      QBackendMetaData backendMetaData = new S3BackendMetaData()
         .withBucketName(bucketName)
         .withBasePath("root")
         .withName("backend-" + which);
      qInstance.addBackend(backendMetaData);
      return (S3BackendMetaData) backendMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QTableMetaData defineTable(QInstance qInstance, String which, QBackendMetaData backend, String path, String glob)
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withName("table-" + which)
         .withBackendName(backend.getName())
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withBackendDetails(new S3TableBackendDetails()
            .withBasePath(path)
            .withGlob(glob));
      qInstance.addTable(qTableMetaData);
      return (qTableMetaData);
   }

}