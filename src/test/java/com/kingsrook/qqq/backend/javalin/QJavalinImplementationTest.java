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
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobState;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class QJavalinImplementationTest
{
   private static final int    PORT     = 6262;
   private static final String BASE_URL = "http://localhost:" + PORT;

   private static final int MORE_THAN_TIMEOUT = 500;
   private static final int LESS_THAN_TIMEOUT = 50;



   /*******************************************************************************
    ** Before the class (all) runs, start a javalin server.
    **
    *******************************************************************************/
   @BeforeAll
   public static void beforeAll()
   {
      QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(TestUtils.defineInstance());
      QJavalinImplementation.setAsyncStepTimeoutMillis(250);
      qJavalinImplementation.startJavalinServer(PORT);
   }



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
      assertEquals(1, tables.length());
      JSONObject table0 = tables.getJSONObject("person");
      assertTrue(table0.has("name"));
      assertEquals("person", table0.getString("name"));
      assertTrue(table0.has("label"));
      assertEquals("Person", table0.getString("label"));
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
      assertEquals(4, process.keySet().size(), "Number of mid-level keys");
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
      String               filterJson = "{\"criteria\":[{\"fieldName\":\"firstName\",\"operator\":\"EQUALS\",\"values\":[\"Tim\"]}]}";
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
      //? body.put("id", 4);

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
      // mmm, whole record isn't loaded.  should it be? assertEquals("Samples", values0.getString("lastName"));
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
      assertEquals(1, jsonObject.getJSONArray("records").length());
      assertEquals(3, jsonObject.getJSONArray("records").getJSONObject(0).getJSONObject("values").getInt("id"));
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
    ** test running a process
    **
    *******************************************************************************/
   @Test
   public void test_processGreetInit()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals("null X null", jsonObject.getJSONObject("values").getString("outputMessage"));
   }



   /*******************************************************************************
    ** test running a process with field values on the query string
    **
    *******************************************************************************/
   @Test
   public void test_processGreetInitWithQueryValues()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?greetingPrefix=Hey&greetingSuffix=Jude").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals("Hey X Jude", jsonObject.getJSONObject("values").getString("outputMessage"));
   }



   /*******************************************************************************
    ** test init'ing a process that goes async
    **
    *******************************************************************************/
   @Test
   public void test_processInitGoingAsync() throws InterruptedException
   {
      String               processBasePath = BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SIMPLE_SLEEP;
      HttpResponse<String> response        = Unirest.get(processBasePath + "/init?" + TestUtils.SleeperStep.FIELD_SLEEP_MILLIS + "=" + MORE_THAN_TIMEOUT).asString();

      JSONObject jsonObject  = assertProcessStepWentAsyncResponse(response);
      String     processUUID = jsonObject.getString("processUUID");
      String     jobUUID     = jsonObject.getString("jobUUID");
      assertNotNull(processUUID, "Process UUID should not be null.");
      assertNotNull(jobUUID, "Job UUID should not be null");

      /////////////////////////////////////////////
      // request job status before sleep is done //
      /////////////////////////////////////////////
      response = Unirest.get(processBasePath + "/" + processUUID + "/status/" + jobUUID).asString();
      jsonObject = assertProcessStepRunningResponse(response);

      ///////////////////////////////////
      // sleep, to let that job finish //
      ///////////////////////////////////
      Thread.sleep(MORE_THAN_TIMEOUT);

      ////////////////////////////////////////////////////////
      // request job status again, get back results instead //
      ////////////////////////////////////////////////////////
      response = Unirest.get(processBasePath + "/" + processUUID + "/status/" + jobUUID).asString();
      jsonObject = assertProcessStepCompleteResponse(response);
   }



   /*******************************************************************************
    ** test init'ing a process that does NOT goes async
    **
    *******************************************************************************/
   @Test
   public void test_processInitNotGoingAsync()
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SIMPLE_SLEEP + "/init?" + TestUtils.SleeperStep.FIELD_SLEEP_MILLIS + "=" + LESS_THAN_TIMEOUT)
         .header("Content-Type", "application/json").asString();
      assertProcessStepCompleteResponse(response);
   }



   /*******************************************************************************
    ** test running a step a process that goes async
    **
    *******************************************************************************/
   @Test
   public void test_processStepGoingAsync() throws InterruptedException
   {
      /////////////////////////////////////////////
      // first init the process, to get its UUID //
      /////////////////////////////////////////////
      String processBasePath = BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SLEEP_INTERACTIVE;
      HttpResponse<String> response = Unirest.post(processBasePath + "/init?" + TestUtils.SleeperStep.FIELD_SLEEP_MILLIS + "=" + MORE_THAN_TIMEOUT)
         .header("Content-Type", "application/json").asString();

      JSONObject jsonObject  = assertProcessStepCompleteResponse(response);
      String     processUUID = jsonObject.getString("processUUID");
      String     nextStep    = jsonObject.getString("nextStep");
      assertNotNull(processUUID, "Process UUID should not be null.");
      assertNotNull(nextStep, "There should be a next step");

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // second, run the 'nextStep' (the backend step, that sleeps). run it with a long enough sleep so that it'll go async //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      response = Unirest.post(processBasePath + "/" + processUUID + "/step/" + nextStep)
         .header("Content-Type", "application/json").asString();

      jsonObject = assertProcessStepWentAsyncResponse(response);
      String jobUUID = jsonObject.getString("jobUUID");

      ///////////////////////////////////
      // sleep, to let that job finish //
      ///////////////////////////////////
      Thread.sleep(MORE_THAN_TIMEOUT);

      ///////////////////////////////
      // third, request job status //
      ///////////////////////////////
      response = Unirest.get(processBasePath + "/" + processUUID + "/status/" + jobUUID).asString();

      jsonObject = assertProcessStepCompleteResponse(response);
      String nextStep2 = jsonObject.getString("nextStep");
      assertNotNull(nextStep2, "There be one more next step");
      assertNotEquals(nextStep, nextStep2, "The next step should be different this time.");
   }



   /*******************************************************************************
    ** test running a step a process that does NOT goes async
    **
    *******************************************************************************/
   @Test
   public void test_processStepNotGoingAsync()
   {
      /////////////////////////////////////////////
      // first init the process, to get its UUID //
      /////////////////////////////////////////////
      String processBasePath = BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SLEEP_INTERACTIVE;
      HttpResponse<String> response = Unirest.post(processBasePath + "/init?" + TestUtils.SleeperStep.FIELD_SLEEP_MILLIS + "=" + LESS_THAN_TIMEOUT)
         .header("Content-Type", "application/json").asString();

      JSONObject jsonObject  = assertProcessStepCompleteResponse(response);
      String     processUUID = jsonObject.getString("processUUID");
      String     nextStep    = jsonObject.getString("nextStep");
      assertNotNull(nextStep, "There should be a next step");

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // second, run the 'nextStep' (the backend step, that sleeps). run it with a short enough sleep so that it won't go async //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      response = Unirest.post(processBasePath + "/" + processUUID + "/step/" + nextStep)
         .header("Content-Type", "application/json").asString();

      jsonObject = assertProcessStepCompleteResponse(response);
      String nextStep2 = jsonObject.getString("nextStep");
      assertNotNull(nextStep2, "There be one more next step");
      assertNotEquals(nextStep, nextStep2, "The next step should be different this time.");
   }



   /*******************************************************************************
    ** test init'ing a process that goes async and then throws
    **
    *******************************************************************************/
   @Test
   public void test_processInitGoingAsyncThenThrowing() throws InterruptedException
   {
      String               processBasePath = BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SIMPLE_THROW;
      HttpResponse<String> response        = Unirest.get(processBasePath + "/init?" + TestUtils.ThrowerStep.FIELD_SLEEP_MILLIS + "=" + MORE_THAN_TIMEOUT).asString();

      JSONObject jsonObject  = assertProcessStepWentAsyncResponse(response);
      String     processUUID = jsonObject.getString("processUUID");
      String     jobUUID     = jsonObject.getString("jobUUID");

      /////////////////////////////////////////////
      // request job status before sleep is done //
      /////////////////////////////////////////////
      response = Unirest.get(processBasePath + "/" + processUUID + "/status/" + jobUUID).asString();
      jsonObject = assertProcessStepRunningResponse(response);

      ///////////////////////////////////
      // sleep, to let that job finish //
      ///////////////////////////////////
      Thread.sleep(MORE_THAN_TIMEOUT);

      /////////////////////////////////////////////////////////////
      // request job status again, get back error status instead //
      /////////////////////////////////////////////////////////////
      response = Unirest.get(processBasePath + "/" + processUUID + "/status/" + jobUUID).asString();
      jsonObject = assertProcessStepErrorResponse(response);
   }



   /*******************************************************************************
    ** test init'ing a process that does NOT goes async, but throws.
    **
    *******************************************************************************/
   @Test
   public void test_processInitNotGoingAsyncButThrowing()
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/processes/" + TestUtils.PROCESS_NAME_SIMPLE_THROW + "/init?" + TestUtils.ThrowerStep.FIELD_SLEEP_MILLIS + "=" + LESS_THAN_TIMEOUT)
         .header("Content-Type", "application/json").asString();
      assertProcessStepErrorResponse(response);
   }



   /*******************************************************************************
    ** every time a process step (sync or async) has gone async, expect what the
    ** response should look like
    *******************************************************************************/
   private JSONObject assertProcessStepWentAsyncResponse(HttpResponse<String> response)
   {
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("processUUID"), "Async-started response should have a processUUID");
      assertTrue(jsonObject.has("jobUUID"), "Async-started response should have a jobUUID");

      assertFalse(jsonObject.has("values"), "Async-started response should NOT have values");
      assertFalse(jsonObject.has("error"), "Async-started response should NOT have error");

      return (jsonObject);
   }



   /*******************************************************************************
    ** every time a process step (sync or async) is still running, expect certain things
    ** to be (and not to be) in the json response.
    *******************************************************************************/
   private JSONObject assertProcessStepRunningResponse(HttpResponse<String> response)
   {
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("jobStatus"), "Step Running response should have a jobStatus");

      assertFalse(jsonObject.has("values"), "Step Running response should NOT have values");
      assertFalse(jsonObject.has("error"), "Step Running response should NOT have error");

      assertEquals(AsyncJobState.RUNNING.name(), jsonObject.getJSONObject("jobStatus").getString("state"));

      return (jsonObject);
   }



   /*******************************************************************************
    ** every time a process step (sync or async) completes, expect certain things
    ** to be (and not to be) in the json response.
    *******************************************************************************/
   private JSONObject assertProcessStepCompleteResponse(HttpResponse<String> response)
   {
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("values"), "Step Complete response should have values");

      assertFalse(jsonObject.has("jobUUID"), "Step Complete response should not have a jobUUID");
      assertFalse(jsonObject.has("error"), "Step Complete response should not have an error");

      return (jsonObject);
   }



   /*******************************************************************************
    ** every time a process step (sync or async) has an error, expect certain things
    ** to be (and not to be) in the json response.
    *******************************************************************************/
   private JSONObject assertProcessStepErrorResponse(HttpResponse<String> response)
   {
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());

      assertTrue(jsonObject.has("error"), "Step Error response should have an error");

      assertFalse(jsonObject.has("jobUUID"), "Step Error response should not have a jobUUID");
      assertFalse(jsonObject.has("values"), "Step Error response should not have values");

      return (jsonObject);
   }

}
