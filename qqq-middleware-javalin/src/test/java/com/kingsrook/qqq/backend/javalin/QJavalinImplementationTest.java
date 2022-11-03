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

package com.kingsrook.qqq.backend.javalin;


import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for the QJavalinImplementation
 **
 ** based on https://javalin.io/tutorials/testing - starts a javalin instance
 ** and actually makes http requests into it.
 **
 *******************************************************************************/
class QJavalinImplementationTest extends QJavalinTestBase
{


   /*******************************************************************************
    ** test the top-level meta-data endpoint
    **
    *******************************************************************************/
   @Test
   public void test_metaData()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("tables"));
      JSONObject tables = jsonObject.getJSONObject("tables");
      assertEquals(6, tables.length()); // person + 5 script tables
      JSONObject personTable = tables.getJSONObject("person");
      assertTrue(personTable.has("name"));
      assertEquals("person", personTable.getString("name"));
      assertTrue(personTable.has("label"));
      assertEquals("Person", personTable.getString("label"));
      assertFalse(personTable.getBoolean("isHidden"));

      JSONObject processes = jsonObject.getJSONObject("processes");
      assertTrue(processes.getJSONObject("simpleSleep").getBoolean("isHidden"));
   }



   /*******************************************************************************
    ** test the table-level meta-data endpoint
    **
    *******************************************************************************/
   @Test
   public void test_tableMetaData()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/table/person").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      JSONObject table = jsonObject.getJSONObject("table");
      assertEquals("person", table.getString("name"));
      assertEquals("Person", table.getString("label"));
      assertEquals("id", table.getString("primaryKeyField"));
      JSONObject fields = table.getJSONObject("fields");
      JSONObject field0 = fields.getJSONObject("id");
      assertEquals("id", field0.getString("name"));
      assertEquals("INTEGER", field0.getString("type"));
   }



   /*******************************************************************************
    ** test the table-level meta-data endpoint for a non-real name
    **
    *******************************************************************************/
   @Test
   public void test_tableMetaData_notFound()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/table/notAnActualTable").asString();

      assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      String error = jsonObject.getString("error");
      assertTrue(error.contains("not found"));
   }



   /*******************************************************************************
    ** test the process-level meta-data endpoint
    **
    *******************************************************************************/
   @Test
   public void test_processMetaData()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/process/greetInteractive").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      JSONObject process = jsonObject.getJSONObject("process");
      assertEquals("greetInteractive", process.getString("name"));
      assertEquals("Greet Interactive", process.getString("label"));
      assertEquals("person", process.getString("tableName"));

      JSONArray  frontendSteps = process.getJSONArray("frontendSteps");
      JSONObject setupStep     = frontendSteps.getJSONObject(0);
      assertEquals("Setup", setupStep.getString("label"));
      JSONArray setupFields = setupStep.getJSONArray("formFields");
      assertEquals(2, setupFields.length());
      assertTrue(setupFields.toList().stream().anyMatch(field -> "greetingPrefix".equals(((Map<?, ?>) field).get("name"))));
   }



   /*******************************************************************************
    ** test the process-level meta-data endpoint for a non-real name
    **
    *******************************************************************************/
   @Test
   public void test_processMetaData_notFound()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/process/notAnActualProcess").asString();

      assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      String error = jsonObject.getString("error");
      assertTrue(error.contains("not found"));
   }



   /*******************************************************************************
    ** test a table get (single record)
    **
    *******************************************************************************/
   @Test
   public void test_dataGet()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/1").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("values"));
      assertEquals("person", jsonObject.getString("tableName"));
      JSONObject values = jsonObject.getJSONObject("values");
      assertTrue(values.has("firstName"));
      assertTrue(values.has("id"));
   }



   /*******************************************************************************
    ** test a table get (single record) for an id that isn't found
    **
    *******************************************************************************/
   @Test
   public void test_dataGetNotFound()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/98765").asString();
      assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      String error = jsonObject.getString("error");
      assertEquals("Could not find Person with Id of 98765", error);
   }



   /*******************************************************************************
    ** test a table get (single record) for an id that isn't the expected type
    **
    *******************************************************************************/
   @Test
   public void test_dataGetWrongIdType()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/not-an-integer").asString();
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
   }



   /*******************************************************************************
    ** test a table count
    **
    *******************************************************************************/
   @Test
   public void test_dataCount()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/count").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("count"));
      int count = jsonObject.getInt("count");
      assertEquals(5, count);
   }



   /*******************************************************************************
    ** test a table count with a filter.
    **
    *******************************************************************************/
   @Test
   public void test_dataCountWithFilter()
   {
      String               filterJson = getFirstNameEqualsTimFilterJSON();
      HttpResponse<String> response   = Unirest.get(BASE_URL + "/data/person/count?filter=" + URLEncoder.encode(filterJson, StandardCharsets.UTF_8)).asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("count"));
      int count = jsonObject.getInt("count");
      assertEquals(1, count);
   }



   /*******************************************************************************
    ** test a table count POST.
    **
    *******************************************************************************/
   @Test
   public void test_dataCountPOST()
   {
      String filterJson = getFirstNameEqualsTimFilterJSON();
      HttpResponse<String> response = Unirest.post(BASE_URL + "/data/person/count")
         .field("filter", filterJson)
         .asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("count"));
      int count = jsonObject.getInt("count");
      assertEquals(1, count);
   }



   /*******************************************************************************
    ** test a table query
    **
    *******************************************************************************/
   @Test
   public void test_dataQuery()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person").asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("records"));
      JSONArray records = jsonObject.getJSONArray("records");
      assertEquals(5, records.length());
      JSONObject record0 = records.getJSONObject(0);
      assertTrue(record0.has("values"));
      assertEquals("person", record0.getString("tableName"));
      JSONObject values0 = record0.getJSONObject("values");
      assertTrue(values0.has("firstName"));
      assertTrue(values0.has("id"));
   }



   /*******************************************************************************
    ** test a table query using an actual filter.
    **
    *******************************************************************************/
   @Test
   public void test_dataQueryWithFilter()
   {
      String               filterJson = getFirstNameEqualsTimFilterJSON();
      HttpResponse<String> response   = Unirest.get(BASE_URL + "/data/person?filter=" + URLEncoder.encode(filterJson, StandardCharsets.UTF_8)).asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("records"));
      JSONArray records = jsonObject.getJSONArray("records");
      assertEquals(1, records.length());
      JSONObject record0 = records.getJSONObject(0);
      JSONObject values0 = record0.getJSONObject("values");
      assertEquals("Tim", values0.getString("firstName"));
   }



   /*******************************************************************************
    ** test a table query using an actual filter via POST.
    **
    *******************************************************************************/
   @Test
   public void test_dataQueryWithFilterPOST()
   {
      String filterJson = getFirstNameEqualsTimFilterJSON();
      HttpResponse<String> response = Unirest.post(BASE_URL + "/data/person/query")
         .field("filter", filterJson)
         .asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("records"));
      JSONArray records = jsonObject.getJSONArray("records");
      assertEquals(1, records.length());
      JSONObject record0 = records.getJSONObject(0);
      JSONObject values0 = record0.getJSONObject("values");
      assertEquals("Tim", values0.getString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFirstNameEqualsTimFilterJSON()
   {
      return """
         {"criteria":[{"fieldName":"firstName","operator":"EQUALS","values":["Tim"]}]}""";
   }



   /*******************************************************************************
    ** test an insert
    **
    *******************************************************************************/
   @Test
   public void test_dataInsert()
   {
      Map<String, Serializable> body = new HashMap<>();
      body.put("firstName", "Bobby");
      body.put("lastName", "Hull");
      body.put("email", "bobby@hull.com");

      HttpResponse<String> response = Unirest.post(BASE_URL + "/data/person")
         .header("Content-Type", "application/json")
         .body(body)
         .asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("records"));
      JSONArray records = jsonObject.getJSONArray("records");
      assertEquals(1, records.length());
      JSONObject record0 = records.getJSONObject(0);
      assertTrue(record0.has("values"));
      assertEquals("person", record0.getString("tableName"));
      JSONObject values0 = record0.getJSONObject("values");
      assertTrue(values0.has("firstName"));
      assertEquals("Bobby", values0.getString("firstName"));
      assertTrue(values0.has("id"));
      assertEquals(6, values0.getInt("id"));
   }



   /*******************************************************************************
    ** test an update
    **
    *******************************************************************************/
   @Test
   public void test_dataUpdate()
   {
      Map<String, Serializable> body = new HashMap<>();
      body.put("firstName", "Free");
      body.put("birthDate", "");

      HttpResponse<String> response = Unirest.patch(BASE_URL + "/data/person/4")
         .header("Content-Type", "application/json")
         .body(body)
         .asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("records"));
      JSONArray records = jsonObject.getJSONArray("records");
      assertEquals(1, records.length());
      JSONObject record0 = records.getJSONObject(0);
      assertTrue(record0.has("values"));
      assertEquals("person", record0.getString("tableName"));
      JSONObject values0 = record0.getJSONObject("values");
      assertEquals(4, values0.getInt("id"));
      assertEquals("Free", values0.getString("firstName"));

      ///////////////////////////////////////////////////////////////////
      // re-GET the record, and validate that birthDate was nulled out //
      ///////////////////////////////////////////////////////////////////
      response = Unirest.get(BASE_URL + "/data/person/4").asString();
      assertEquals(200, response.getStatus());
      jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertTrue(jsonObject.has("values"));
      JSONObject values = jsonObject.getJSONObject("values");
      assertFalse(values.has("birthDate"));
   }



   /*******************************************************************************
    ** test a delete
    **
    *******************************************************************************/
   @Test
   public void test_dataDelete() throws Exception
   {
      HttpResponse<String> response = Unirest.delete(BASE_URL + "/data/person/3").asString();
      assertEquals(200, response.getStatus());

      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals(1, jsonObject.getInt("deletedRecordCount"));
      TestUtils.runTestSql("SELECT id FROM person", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertNotEquals(3, rs.getInt(1));
         }
         assertEquals(4, rowsFound);
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportCsvPerFileName()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/export/MyPersonExport.csv").asString();
      assertEquals(200, response.getStatus());
      assertEquals("text/csv", response.getHeaders().get("Content-Type").get(0));
      assertEquals("filename=MyPersonExport.csv", response.getHeaders().get("Content-Disposition").get(0));
      String[] csvLines = response.getBody().split("\n");
      assertEquals(6, csvLines.length);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportNoFormat()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/export/").asString();
      assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
      assertThat(response.getBody()).contains("Report format was not specified");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportExcelPerFormatQueryParam()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/export/?format=xlsx").asString();
      assertEquals(200, response.getStatus());
      assertEquals(ReportFormat.XLSX.getMimeType(), response.getHeaders().get("Content-Type").get(0));
      assertEquals("filename=person.xlsx", response.getHeaders().get("Content-Disposition").get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportFilterQueryParam()
   {
      String               filterJson = getFirstNameEqualsTimFilterJSON();
      HttpResponse<String> response   = Unirest.get(BASE_URL + "/data/person/export/Favorite People.csv?filter=" + URLEncoder.encode(filterJson, StandardCharsets.UTF_8)).asString();
      assertEquals("filename=Favorite People.csv", response.getHeaders().get("Content-Disposition").get(0));
      String[] csvLines = response.getBody().split("\n");
      assertEquals(2, csvLines.length);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportFieldsQueryParam()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/export/People.csv?fields=id,birthDate").asString();
      String[]             csvLines = response.getBody().split("\n");
      assertEquals("""
         "Id","Birth Date\"""", csvLines[0]);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExportSupportedFormat()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/export/?format=docx").asString();
      assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
      assertThat(response.getBody()).contains("Unsupported report format");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWidget()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/widget/" + PersonsByCreateDateBarChart.class.getSimpleName()).asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals(WidgetType.CHART.getType(), jsonObject.getString("type"));
      assertNotNull(jsonObject.getString("title"));
      assertNotNull(jsonObject.getJSONObject("chartData"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueUnfiltered()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/possibleValues/partnerPersonId").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertNotNull(jsonObject.getJSONArray("options"));
      assertEquals(5, jsonObject.getJSONArray("options").length());
      assertEquals(1, jsonObject.getJSONArray("options").getJSONObject(0).getInt("id"));
      assertEquals("Darin Kelkhoff (1)", jsonObject.getJSONArray("options").getJSONObject(0).getString("label"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueWithSearchTerm()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/possibleValues/partnerPersonId?searchTerm=Chamber").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertNotNull(jsonObject.getJSONArray("options"));
      assertEquals(1, jsonObject.getJSONArray("options").length());
      assertEquals("Tim Chamberlain (3)", jsonObject.getJSONArray("options").getJSONObject(0).getString("label"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueWithIds()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/possibleValues/partnerPersonId?ids=4,5").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertNotNull(jsonObject.getJSONArray("options"));
      assertEquals(2, jsonObject.getJSONArray("options").length());
      assertEquals(4, jsonObject.getJSONArray("options").getJSONObject(0).getInt("id"));
      assertEquals(5, jsonObject.getJSONArray("options").getJSONObject(1).getInt("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetRecordDeveloperMode() throws QException
   {
      UpdateInput updateInput = new UpdateInput(TestUtils.defineInstance());
      updateInput.setSession(new QSession());
      updateInput.setTableName("person");
      updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("testScriptId", 47)));
      new UpdateAction().execute(updateInput);

      InsertInput insertInput = new InsertInput(TestUtils.defineInstance());
      insertInput.setSession(new QSession());
      insertInput.setTableName("script");
      insertInput.setRecords(List.of(new QRecord().withValue("id", 47).withValue("currentScriptRevisionId", 100)));
      new InsertAction().execute(insertInput);

      insertInput.setTableName("scriptRevision");
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1000).withValue("scriptId", 47).withValue("content", "var i;")));
      new InsertAction().execute(insertInput);

      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/1/developer").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      System.out.println(jsonObject.toString(3));
      assertNotNull(jsonObject);
      assertNotNull(jsonObject.getJSONObject("record"));
      assertEquals("Darin", jsonObject.getJSONObject("record").getJSONObject("values").getString("firstName"));
      assertEquals("Darin", jsonObject.getJSONObject("record").getJSONObject("displayValues").getString("firstName"));
      assertNotNull(jsonObject.getJSONArray("associatedScripts"));
      assertNotNull(jsonObject.getJSONArray("associatedScripts").getJSONObject(0));
      assertNotNull(jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONArray("scriptRevisions"));
      assertEquals("var i;", jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONArray("scriptRevisions").getJSONObject(0).getJSONObject("values").getString("content"));
      assertNotNull(jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONObject("script"));
      assertEquals(100, jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONObject("script").getJSONObject("values").getInt("currentScriptRevisionId"));
      assertNotNull(jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONObject("associatedScript"));
      assertEquals("testScriptId", jsonObject.getJSONArray("associatedScripts").getJSONObject(0).getJSONObject("associatedScript").getString("fieldName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testStoreRecordAssociatedScript() throws QException
   {
      InsertInput insertInput = new InsertInput(TestUtils.defineInstance());
      insertInput.setSession(new QSession());
      insertInput.setTableName("scriptType");
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("name", "Test")));
      new InsertAction().execute(insertInput);

      HttpResponse<String> response = Unirest.post(BASE_URL + "/data/person/1/developer/associatedScript/testScriptId")
         .field("contents", "var j = 0;")
         .field("commitMessage", "Javalin Commit")
         .asString();

      QueryInput queryInput = new QueryInput(TestUtils.defineInstance());
      queryInput.setSession(new QSession());
      queryInput.setTableName("scriptRevision");
      queryInput.setFilter(new QQueryFilter()
         .withCriteria(new QFilterCriteria("contents", QCriteriaOperator.EQUALS, List.of("var j = 0;")))
         .withCriteria(new QFilterCriteria("commitMessage", QCriteriaOperator.EQUALS, List.of("Javalin Commit")))
      );
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTestAssociatedScript() throws QException
   {
      InsertInput insertInput = new InsertInput(TestUtils.defineInstance());
      insertInput.setSession(new QSession());
      insertInput.setTableName("scriptType");
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("name", "Test")));
      new InsertAction().execute(insertInput);

      HttpResponse<String> response = Unirest.post(BASE_URL + "/data/person/1/developer/associatedScript/testScriptId/test")
         .field("code", "var j = 0;")
         .field("scriptTypeId", "1")
         .field("x", "47")
         .asString();

      /////////////////////////////////////////
      // todo - assertions after implemented //
      /////////////////////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetAssociatedScriptLogs() throws QException
   {
      InsertInput insertInput = new InsertInput(TestUtils.defineInstance());
      insertInput.setSession(new QSession());
      insertInput.setTableName("scriptLog");
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("output", "testOutput").withValue("scriptRevisionId", 100)));
      new InsertAction().execute(insertInput);

      insertInput.setTableName("scriptLogLine");
      insertInput.setRecords(List.of(
         new QRecord().withValue("scriptLogId", 1).withValue("text", "line one"),
         new QRecord().withValue("scriptLogId", 1).withValue("text", "line two")
      ));
      new InsertAction().execute(insertInput);

      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person/1/developer/associatedScript/testScriptId/100/logs").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject.getJSONArray("scriptLogRecords"));
      assertEquals(1, jsonObject.getJSONArray("scriptLogRecords").length());
      assertNotNull(jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values"));
      assertEquals("testOutput", jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values").getString("output"));
      assertNotNull(jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values").getJSONArray("scriptLogLine"));
      assertEquals(2, jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values").getJSONArray("scriptLogLine").length());
      assertEquals("line one", jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values").getJSONArray("scriptLogLine").getJSONObject(0).getJSONObject("values").getString("text"));
      assertEquals("line two", jsonObject.getJSONArray("scriptLogRecords").getJSONObject(0).getJSONObject("values").getJSONArray("scriptLogLine").getJSONObject(1).getJSONObject("values").getString("text"));
   }

}
