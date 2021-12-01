package com.kingsrook.qqq.frontend.picocli;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class QPicoCliImplementationTest
{
   private static final boolean VERBOSE = true;
   private static final String CLI_NAME = "cli-unit-test";



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_noArgs()
   {
      TestOutput testOutput = testCli();
      assertTrue(testOutput.getOutput().contains("Usage: " + CLI_NAME));
   }



   /*******************************************************************************
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
    **
    *******************************************************************************/
   @Test
   public void test_version()
   {
      TestOutput testOutput = testCli("--version");
      assertTrue(testOutput.getOutput().contains(CLI_NAME + " v1.0"));
   }



   /*******************************************************************************
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