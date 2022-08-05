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
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.processes.implementations.mock.MockBackendStep;
import com.kingsrook.qqq.backend.module.filesystem.local.actions.FilesystemQueryAction;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
@DisabledOnOs(OS.LINUX) // uses database; not available in CI at this time...
class SampleMetaDataProviderTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      primeTestDatabase("prime-test-database.sql");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   private void primeTestDatabase(String sqlFileName) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      try(Connection connection = connectionManager.getConnection(SampleMetaDataProvider.defineRdbmsBackend()))
      {
         InputStream primeTestDatabaseSqlStream = SampleMetaDataProviderTest.class.getResourceAsStream("/" + sqlFileName);
         Assert.assertNotNull(primeTestDatabaseSqlStream);
         List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
         lines = lines.stream().filter(line -> !line.startsWith("-- ")).toList();
         String joinedSQL = String.join("\n", lines);
         for(String sql : joinedSQL.split(";"))
         {
            QueryManager.executeUpdate(connection, sql);
         }
      }
   }



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
         QueryInput queryInput = new QueryInput();
         queryInput.setInstance(SampleMetaDataProvider.defineInstance());
         queryInput.setTableName(fileTable.getName());

         QueryOutput queryOutput = new FilesystemQueryAction().execute(queryInput);
         System.out.println(queryOutput);
         Assertions.assertEquals(3, queryOutput.getRecords().size(), "Should load all records from the file");
      }
      finally
      {
         destinationFile.delete();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
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
      QInstance       qInstance   = SampleMetaDataProvider.defineInstance();
      QTableMetaData  personTable = SampleMetaDataProvider.defineTablePerson();
      RunProcessInput request     = new RunProcessInput(qInstance);
      request.setSession(new QSession());
      request.setProcessName(SampleMetaDataProvider.PROCESS_NAME_GREET);

      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setTableName(personTable.getName());
      queryInput.setSession(new QSession());
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      request.setRecords(queryOutput.getRecords());
      request.addValue(MockBackendStep.FIELD_GREETING_PREFIX, "Hello");
      request.addValue(MockBackendStep.FIELD_GREETING_SUFFIX, "sir");

      RunProcessOutput result = new RunProcessAction().execute(request);
      assertNotNull(result);
      assertTrue(result.getRecords().stream().allMatch(r -> r.getValues().containsKey("id")), "records should have an id, set by the process");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testThrowProcess() throws Exception
   {
      QInstance       qInstance = SampleMetaDataProvider.defineInstance();
      RunProcessInput request   = new RunProcessInput(qInstance);
      request.setSession(new QSession());
      request.setProcessName(SampleMetaDataProvider.PROCESS_NAME_SIMPLE_THROW);
      request.addValue(SampleMetaDataProvider.ThrowerStep.FIELD_SLEEP_MILLIS, 10);

      assertThrows(QException.class, () ->
      {
         new RunProcessAction().execute(request);
      });
   }

}