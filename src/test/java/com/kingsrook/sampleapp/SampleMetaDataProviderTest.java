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

package com.kingsrook.sampleapp;


import java.io.File;
import java.io.IOException;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.QueryAction;
import com.kingsrook.qqq.backend.core.actions.RunProcessAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemQueryAction;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class SampleMetaDataProviderTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCityFileTable() throws Exception
   {
      QTableMetaData fileTable       = SampleMetaDataProvider.defineTableCityFile();
      File           destinationFile = copyTestFileToRandomNameUnderTable(fileTable);

      try
      {
         QueryRequest queryRequest = new QueryRequest();
         queryRequest.setInstance(SampleMetaDataProvider.defineInstance());
         queryRequest.setTableName(fileTable.getName());

         QueryResult queryResult = new FilesystemQueryAction().execute(queryRequest);
         System.out.println(queryResult);
         Assertions.assertEquals(3, queryResult.getRecords().size(), "Should load all records from the file");
      }
      finally
      {
         destinationFile.delete();
      }
   }



   private File copyTestFileToRandomNameUnderTable(QTableMetaData fedExTable) throws IOException
   {
      File destinationDir = new File(SampleMetaDataProvider.defineFilesystemBackend().getBasePath() + File.separator +
         ((FilesystemTableBackendDetails) fedExTable.getBackendDetails()).getBasePath());
      destinationDir.mkdirs();
      File destinationFile = new File(destinationDir.getAbsolutePath() + File.separator + UUID.randomUUID());

      FileUtils.writeStringToFile(destinationFile, """
         id,name,state
         1,Chester,IL
         2,Red Bud,IL
         3,Sparta,IL""");

      return destinationFile;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testGreetProcess() throws Exception
   {
      QInstance         qInstance   = SampleMetaDataProvider.defineInstance();
      QTableMetaData    personTable = SampleMetaDataProvider.defineTablePerson();
      RunProcessRequest request     = new RunProcessRequest(qInstance);
      request.setSession(new QSession());
      request.setProcessName(SampleMetaDataProvider.PROCESS_NAME_GREET);

      QueryRequest queryRequest = new QueryRequest(qInstance);
      queryRequest.setTableName(personTable.getName());
      queryRequest.setSession(new QSession());
      QueryResult queryResult = new QueryAction().execute(queryRequest);

      request.setRecords(queryResult.getRecords());
      request.addValue("greetingPrefix", "Hello");
      request.addValue("greetingSuffix", "there");

      RunProcessResult result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("id")), "records should have an id, set by the process");
   }

}