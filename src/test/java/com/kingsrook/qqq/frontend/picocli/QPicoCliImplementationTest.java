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

package com.kingsrook.qqq.frontend.picocli;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for the QPicoCliImplementation.
 **
 *******************************************************************************/
class QPicoCliImplementationTest
{
   private static final boolean VERBOSE = true;
   private static final String CLI_NAME = "cli-unit-test";



   /*******************************************************************************
    ** Fully rebuild the test-database before each test runs, for completely known state.
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase();
   }



   /*******************************************************************************
    ** test that w/ no arguments you just get usage.
    **
    *******************************************************************************/
   @Test
   public void test_noArgs()
   {
      TestOutput testOutput = testCli();
      assertTestOutputContains(testOutput, "Usage: " + CLI_NAME);
   }



   /*******************************************************************************
    ** test that --help gives you usage.
    **
    *******************************************************************************/
   @Test
   public void test_help()
   {
      TestOutput testOutput = testCli("--help");
      assertTestOutputContains(testOutput, "Usage: " + CLI_NAME);
      assertTestOutputContains(testOutput, "Commands:.*person");
   }



   /*******************************************************************************
    ** test the --verion argument
    **
    *******************************************************************************/
   @Test
   public void test_version()
   {
      TestOutput testOutput = testCli("--version");
      assertTestOutputContains(testOutput, CLI_NAME + " v1.0");
   }



   /*******************************************************************************
    ** Test that an unrecognized opttion gives an error
    **
    *******************************************************************************/
   @Test
   public void test_badOption()
   {
      String badOption = "--asdf";
      TestOutput testOutput = testCli(badOption);
      assertTestErrorContains(testOutput, "Unknown option: '" + badOption + "'");
      assertTestErrorContains(testOutput, "Usage: " + CLI_NAME);
   }



   /*******************************************************************************
    ** test the top-level --meta-data option
    **
    *******************************************************************************/
   @Test
   public void test_metaData()
   {
      TestOutput testOutput = testCli("--meta-data");
      JSONObject metaData = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(metaData);
      assertEquals(1, metaData.keySet().size(), "Number of top-level keys");
      assertTrue(metaData.has("tables"));
      JSONObject tables = metaData.getJSONObject("tables");
      JSONObject personTable = tables.getJSONObject("person");
      assertEquals("person", personTable.getString("name"));
      assertEquals("Person", personTable.getString("label"));
   }



   /*******************************************************************************
    ** test giving a table-name, gives usage for that table
    **
    *******************************************************************************/
   @Test
   public void test_table()
   {
      TestOutput testOutput = testCli("person");
      assertTestOutputContains(testOutput, "Usage: " + CLI_NAME + " person \\[COMMAND\\]");
      assertTestOutputContains(testOutput, "Commands:.*query.*process");

      ///////////////////////////////////////////////////////
      // make sure that if there are no processes for the  //
      // table, that the processes sub-command isn't given //
      ///////////////////////////////////////////////////////
      QInstance qInstanceWithoutProcesses = TestUtils.defineInstance();
      qInstanceWithoutProcesses.setProcesses(new HashMap<>());
      testOutput = testCli(qInstanceWithoutProcesses, "person");
      assertTestOutputDoesNotContain(testOutput, "process");
   }



   /*******************************************************************************
    ** test unknown command under table, prints error and usage.
    **
    *******************************************************************************/
   @Test
   public void test_tableUnknownCommand()
   {
      String badCommand = "qwuijibo";
      TestOutput testOutput = testCli("person", badCommand);
      assertTestErrorContains(testOutput, "Unmatched argument at index 1: '" + badCommand + "'");
      assertTestErrorContains(testOutput, "Usage: " + CLI_NAME + " person \\[COMMAND\\]");
   }



   /*******************************************************************************
    ** test requesting table meta-data
    **
    *******************************************************************************/
   @Test
   public void test_tableMetaData()
   {
      TestOutput testOutput = testCli("person", "meta-data");
      JSONObject metaData = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(metaData);
      assertEquals(1, metaData.keySet().size(), "Number of top-level keys");
      JSONObject table = metaData.getJSONObject("table");
      assertEquals(4, table.keySet().size(), "Number of mid-level keys");
      assertEquals("person", table.getString("name"));
      assertEquals("Person", table.getString("label"));
      assertEquals("id", table.getString("primaryKeyField"));
      JSONObject fields = table.getJSONObject("fields");
      JSONObject field0 = fields.getJSONObject("id");
      assertEquals("id", field0.getString("name"));
      assertEquals("INTEGER", field0.getString("type"));
   }



   /*******************************************************************************
    ** test running a query on a table
    **
    *******************************************************************************/
   @Test
   public void test_tableQuery()
   {
      TestOutput testOutput = testCli("person", "query", "--skip=1", "--limit=2", "--criteria", "id NOT_EQUALS 3");
      JSONObject queryResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(queryResult);
      JSONArray records = queryResult.getJSONArray("records");
      assertEquals(2, records.length());
      // query for id != 3, and skipping 1, expect to get back rows 2 & 4
      assertEquals(2, records.getJSONObject(0).getJSONObject("values").getInt("id"));
      assertEquals(4, records.getJSONObject(1).getJSONObject("values").getInt("id"));
   }



   /*******************************************************************************
    ** test running an insert w/o specifying any fields, prints usage
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertNoFieldsPrintsUsage()
   {
      TestOutput testOutput = testCli("person", "insert");
      assertTestOutputContains(testOutput, "Usage: " + CLI_NAME + " person insert");
   }



   /*******************************************************************************
    ** test running an insert w/ fields as arguments
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertFieldArguments()
   {
      TestOutput testOutput = testCli("person", "insert",
         "--field-firstName=Lucy",
         "--field-lastName=Lu");
      JSONObject insertResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(insertResult);
      assertEquals(1, insertResult.getJSONArray("records").length());
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
   }



   /*******************************************************************************
    ** test running an insert w/ a mapping and json as an argument
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertJsonObjectArgumentWithMapping()
   {
      String mapping = """
         --mapping={"firstName":"first","lastName":"ln"}
         """;

      String jsonBody = """
         --jsonBody={"first":"Chester","ln":"Cheese"}
         """;

      TestOutput testOutput = testCli("person", "insert", mapping, jsonBody);
      JSONObject insertResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(insertResult);
      assertEquals(1, insertResult.getJSONArray("records").length());
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
      assertEquals("Chester", insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getString("firstName"));
      assertEquals("Cheese", insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getString("lastName"));
   }



   /*******************************************************************************
    ** test running an insert w/ a mapping and json as a multi-record file
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertJsonArrayFileWithMapping() throws IOException
   {
      String mapping = """
         --mapping={"firstName":"first","lastName":"ln"}
         """;

      String jsonContents = """
         [{"first":"Charlie","ln":"Bear"},{"first":"Coco","ln":"Bean"}]
         """;

      File file = new File("/tmp/" + UUID.randomUUID() + ".json");
      file.deleteOnExit();
      FileUtils.writeStringToFile(file, jsonContents);

      TestOutput testOutput = testCli("person", "insert", mapping, "--jsonFile=" + file.getAbsolutePath());
      JSONObject insertResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(insertResult);
      JSONArray records = insertResult.getJSONArray("records");
      assertEquals(2, records.length());
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
      assertEquals(7, insertResult.getJSONArray("records").getJSONObject(1).getJSONObject("values").getInt("id"));
      assertEquals("Charlie", records.getJSONObject(0).getJSONObject("values").getString("firstName"));
      assertEquals("Bear", records.getJSONObject(0).getJSONObject("values").getString("lastName"));
      assertEquals("Coco", records.getJSONObject(1).getJSONObject("values").getString("firstName"));
      assertEquals("Bean", records.getJSONObject(1).getJSONObject("values").getString("lastName"));
   }



   /*******************************************************************************
    ** test running an insert w/ an index-based mapping and csv file
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertCsvFileWithIndexMapping() throws IOException
   {
      String mapping = """
         --mapping={"firstName":1,"lastName":3}
         """;

      String csvContents = """
         "Louis","P","Willikers",1024,
         "Nestle","G","Crunch",1701,
                  
         """;

      File file = new File("/tmp/" + UUID.randomUUID() + ".csv");
      file.deleteOnExit();
      FileUtils.writeStringToFile(file, csvContents);

      TestOutput testOutput = testCli("person", "insert", mapping, "--csvFile=" + file.getAbsolutePath());
      JSONObject insertResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(insertResult);
      JSONArray records = insertResult.getJSONArray("records");
      assertEquals(2, records.length());
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
      assertEquals(7, insertResult.getJSONArray("records").getJSONObject(1).getJSONObject("values").getInt("id"));
      assertEquals("Louis", records.getJSONObject(0).getJSONObject("values").getString("firstName"));
      assertEquals("Willikers", records.getJSONObject(0).getJSONObject("values").getString("lastName"));
      assertEquals("Nestle", records.getJSONObject(1).getJSONObject("values").getString("firstName"));
      assertEquals("Crunch", records.getJSONObject(1).getJSONObject("values").getString("lastName"));
   }



   /*******************************************************************************
    ** test running an update w/o specifying any fields, prints usage
    **
    *******************************************************************************/
   @Test
   public void test_tableUpdateNoFieldsPrintsUsage()
   {
      TestOutput testOutput = testCli("person", "update");
      assertTestOutputContains(testOutput, "Usage: " + CLI_NAME + " person update");
   }



   /*******************************************************************************
    ** test running an update w/ fields as arguments
    **
    *******************************************************************************/
   @Test
   public void test_tableUpdateFieldArguments() throws Exception
   {
      assertRowValueById("person", "first_name", "Garret", 5);
      TestOutput testOutput = testCli("person", "update",
         "--primaryKey=5",
         "--field-firstName=Lucy",
         "--field-lastName=Lu");
      JSONObject updateResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(updateResult);
      assertEquals(1, updateResult.getJSONArray("records").length());
      assertEquals(5, updateResult.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
      assertRowValueById("person", "first_name", "Lucy", 5);
   }



   private void assertRowValueById(String tableName, String columnName, String value, Integer id) throws Exception
   {
      TestUtils.runTestSql("SELECT " + columnName + " FROM " + tableName + " WHERE id=" + id, (rs -> {
         if(rs.next())
         {
            assertEquals(value, rs.getString(1));
         }
         else
         {
            fail("Row not found");
         }
      }));
   }



   /*******************************************************************************
    ** test running a delete against a table
    **
    *******************************************************************************/
   @Test
   public void test_tableDelete() throws Exception
   {
      TestOutput testOutput = testCli("person", "delete", "--primaryKey", "2,4");
      JSONObject deleteResult = JsonUtils.toJSONObject(testOutput.getOutput());
      assertNotNull(deleteResult);
      JSONArray records = deleteResult.getJSONArray("records");
      assertEquals(2, records.length());
      assertEquals(2, records.getJSONObject(0).getJSONObject("values").getInt("id"));
      assertEquals(4, records.getJSONObject(1).getJSONObject("values").getInt("id"));
      TestUtils.runTestSql("SELECT id FROM person", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertTrue(rs.getInt(1) == 1 || rs.getInt(1) == 3 || rs.getInt(1) == 5);
         }
         assertEquals(3, rowsFound);
      }));
   }



   /*******************************************************************************
    ** test requesting the list of processes for a table
    **
    *******************************************************************************/
   @Test
   public void test_tableProcess() throws Exception
   {
      TestOutput testOutput = testCli("person", "process");

      ////////////////////////////////////////////////
      // should list the processes under this table //
      ////////////////////////////////////////////////
      assertTestOutputContains(testOutput, "Commands.*greet");
   }



   /*******************************************************************************
    ** test trying to run a process, but giving an invalid name.
    **
    *******************************************************************************/
   @Test
   public void test_tableProcessUnknownName() throws Exception
   {
      String badProcessName = "not-a-process";
      TestOutput testOutput = testCli("person", "process", badProcessName);
      assertTestErrorContains(testOutput, "Unmatched argument at index 2: '" + badProcessName + "'");
      assertTestErrorContains(testOutput, "Usage: " + CLI_NAME + " person process \\[COMMAND\\]");
   }



   /*******************************************************************************
    ** test running a process on a table
    **
    *******************************************************************************/
   @Test
   @Disabled // not yet done.
   public void test_tableProcessGreet() throws Exception
   {
      TestOutput testOutput = testCli("person", "process", "greet");

      fail("Assertion not written...");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertTestOutputContains(TestOutput testOutput, String expectedRegexSubstring)
   {
      if(!testOutput.getOutput().matches("(?s).*" + expectedRegexSubstring + ".*"))
      {
         fail("Expected output to contain this regex pattern:\n" + expectedRegexSubstring
            + "\nBut it did not.  The full output was:\n" + testOutput.getOutput());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertTestOutputDoesNotContain(TestOutput testOutput, String expectedRegexSubstring)
   {
      if(testOutput.getOutput().matches("(?s).*" + expectedRegexSubstring + ".*"))
      {
         fail("Expected output to not contain this regex pattern:\n" + expectedRegexSubstring
            + "\nBut it did.  The full output was:\n" + testOutput.getOutput());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertTestErrorContains(TestOutput testOutput, String expectedRegexSubstring)
   {
      if(!testOutput.getError().matches("(?s).*" + expectedRegexSubstring + ".*"))
      {
         fail("Expected error-output to contain this regex pattern:\n" + expectedRegexSubstring
            + "\nBut it did not.  The full error-output was:\n" + testOutput.getOutput());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private TestOutput testCli(String... args)
   {
      QInstance qInstance = TestUtils.defineInstance();
      return testCli(qInstance, args);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private TestOutput testCli(QInstance qInstance, String... args)
   {
      QPicoCliImplementation qPicoCliImplementation = new QPicoCliImplementation(qInstance);

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

      if(VERBOSE)
      {
         System.out.println("> " + CLI_NAME + (args == null ? "" : " " + StringUtils.join(" ", Arrays.stream(args).toList())));
      }

      qPicoCliImplementation.runCli(CLI_NAME, args, new PrintStream(outputStream, true), new PrintStream(errorStream, true));

      String output = outputStream.toString(StandardCharsets.UTF_8);
      String error = errorStream.toString(StandardCharsets.UTF_8);

      if(VERBOSE)
      {
         System.out.println(output);
         System.err.println(error);
      }

      TestOutput testOutput = new TestOutput(output, error);
      return (testOutput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class TestOutput
   {
      private String output;
      private String[] outputLines;
      private String error;
      private String[] errorLines;



      /*******************************************************************************
       **
       *******************************************************************************/
      public TestOutput(String output, String error)
      {
         this.output = output;
         this.error = error;

         this.outputLines = output.split("\n");
         this.errorLines = error.split("\n");
      }



      /*******************************************************************************
       ** Getter for output
       **
       *******************************************************************************/
      public String getOutput()
      {
         return output;
      }



      /*******************************************************************************
       ** Setter for output
       **
       *******************************************************************************/
      public void setOutput(String output)
      {
         this.output = output;
      }



      /*******************************************************************************
       ** Getter for outputLines
       **
       *******************************************************************************/
      public String[] getOutputLines()
      {
         return outputLines;
      }



      /*******************************************************************************
       ** Setter for outputLines
       **
       *******************************************************************************/
      public void setOutputLines(String[] outputLines)
      {
         this.outputLines = outputLines;
      }



      /*******************************************************************************
       ** Getter for error
       **
       *******************************************************************************/
      public String getError()
      {
         return error;
      }



      /*******************************************************************************
       ** Setter for error
       **
       *******************************************************************************/
      public void setError(String error)
      {
         this.error = error;
      }



      /*******************************************************************************
       ** Getter for errorLines
       **
       *******************************************************************************/
      public String[] getErrorLines()
      {
         return errorLines;
      }



      /*******************************************************************************
       ** Setter for errorLines
       **
       *******************************************************************************/
      public void setErrorLines(String[] errorLines)
      {
         this.errorLines = errorLines;
      }
   }
}
