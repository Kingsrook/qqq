package com.kingsrook.qqq.frontend.picocli;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
      assertTrue(testOutput.getOutput().contains("Usage: " + CLI_NAME));
   }



   /*******************************************************************************
    ** test that --help gives you usage.
    **
    *******************************************************************************/
   @Test
   public void test_help()
   {
      TestOutput testOutput = testCli("--help");
      assertTrue(testOutput.getOutput().contains("Usage: " + CLI_NAME));
      assertTrue(testOutput.getOutput().matches("(?s).*Commands:.*person.*"));
   }



   /*******************************************************************************
    ** test the --verion argument
    **
    *******************************************************************************/
   @Test
   public void test_version()
   {
      TestOutput testOutput = testCli("--version");
      assertTrue(testOutput.getOutput().contains(CLI_NAME + " v1.0"));
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
      assertTrue(testOutput.getError().contains("Unknown option: '" + badOption + "'"));
      assertTrue(testOutput.getError().contains("Usage: " + CLI_NAME));
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
      assertTrue(testOutput.getOutput().contains("Usage: " + CLI_NAME + " person [COMMAND]"));
      assertTrue(testOutput.getOutput().matches("(?s).*Commands:.*query.*"));
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
      assertTrue(testOutput.getError().contains("Unmatched argument at index 1: '" + badCommand + "'"));
      assertTrue(testOutput.getError().contains("Usage: " + CLI_NAME + " person [COMMAND]"));
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
      assertEquals(2, queryResult.getJSONArray("records").length());
      // query for id != 3, and skipping 1, expect to get back rows 2 & 4
      assertEquals(2, queryResult.getJSONArray("records").getJSONObject(0).getInt("primaryKey"));
      assertEquals(4, queryResult.getJSONArray("records").getJSONObject(1).getInt("primaryKey"));
   }



   /*******************************************************************************
    ** test running an insert w/o specifying any fields, prints usage
    **
    *******************************************************************************/
   @Test
   public void test_tableInsertNoFieldsPrintsUsage()
   {
      TestOutput testOutput = testCli("person", "insert");
      assertTrue(testOutput.getOutput().contains("Usage: " + CLI_NAME + " person insert"));
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
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getInt("primaryKey"));
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
      assertEquals(6, insertResult.getJSONArray("records").getJSONObject(0).getInt("primaryKey"));
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
      assertEquals(6, records.getJSONObject(0).getInt("primaryKey"));
      assertEquals(7, records.getJSONObject(1).getInt("primaryKey"));
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
      assertEquals(6, records.getJSONObject(0).getInt("primaryKey"));
      assertEquals(7, records.getJSONObject(1).getInt("primaryKey"));
      assertEquals("Louis", records.getJSONObject(0).getJSONObject("values").getString("firstName"));
      assertEquals("Willikers", records.getJSONObject(0).getJSONObject("values").getString("lastName"));
      assertEquals("Nestle", records.getJSONObject(1).getJSONObject("values").getString("firstName"));
      assertEquals("Crunch", records.getJSONObject(1).getJSONObject("values").getString("lastName"));
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
      assertEquals(2, deleteResult.getJSONArray("records").length());
      assertEquals(2, deleteResult.getJSONArray("records").getJSONObject(0).getInt("primaryKey"));
      assertEquals(4, deleteResult.getJSONArray("records").getJSONObject(1).getInt("primaryKey"));
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
    **
    *******************************************************************************/
   private TestOutput testCli(String... args)
   {
      QInstance qInstance = TestUtils.defineInstance();
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
