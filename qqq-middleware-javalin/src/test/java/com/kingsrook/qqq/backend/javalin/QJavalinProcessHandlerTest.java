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


import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobState;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for the javalin process handler methods.
 *******************************************************************************/
class QJavalinProcessHandlerTest extends QJavalinTestBase
{
   private static final int MORE_THAN_TIMEOUT = 500;
   private static final int LESS_THAN_TIMEOUT = 50;



   /*******************************************************************************
    ** test running a process
    **
    *******************************************************************************/
   @Test
   public void test_processGreetInit()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?recordsParam=recordIds&recordIds=2,3").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals("null X null", jsonObject.getJSONObject("values").getString("outputMessage"));
   }



   /*******************************************************************************
    ** test running a process that requires rows, but we didn't tell it how to get them.
    **
    *******************************************************************************/
   @Test
   public void test_processRequiresRowsButNotSpecified()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertTrue(jsonObject.has("error"));
      assertTrue(jsonObject.getString("error").contains("Missing input records"));
   }



   /*******************************************************************************
    ** test running a process and telling it rows to load via recordIds param
    **
    *******************************************************************************/
   @Test
   public void test_processRequiresRowsWithRecordIdParam()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?recordsParam=recordIds&recordIds=2,3").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      String processUUID = jsonObject.getString("processUUID");

      getProcessRecords(processUUID, 2);
   }



   /*******************************************************************************
    ** test running a process and telling it rows to load via filter JSON
    **
    *******************************************************************************/
   @Test
   public void test_processRequiresRowsWithFilterJSON()
   {
      QQueryFilter queryFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria()
            .withFieldName("id")
            .withOperator(QCriteriaOperator.IN)
            .withValues(List.of(3, 4, 5)));
      String filterJSON = JsonUtils.toJson(queryFilter);

      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?recordsParam=filterJSON&filterJSON=" + URLEncoder.encode(filterJSON, Charset.defaultCharset())).asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      String processUUID = jsonObject.getString("processUUID");

      getProcessRecords(processUUID, 3);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private JSONObject getProcessRecords(String processUUID, int expectedNoOfRecords)
   {
      return (getProcessRecords(processUUID, expectedNoOfRecords, 0, 20));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private JSONObject getProcessRecords(String processUUID, int expectedNoOfRecords, int skip, int limit)
   {
      HttpResponse<String> response;
      JSONObject           jsonObject;
      response = Unirest.get(BASE_URL + "/processes/greet/" + processUUID + "/records?skip=" + skip + "&limit=" + limit).asString();
      jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);

      if(expectedNoOfRecords == 0)
      {
         assertFalse(jsonObject.has("records"));
      }
      else
      {
         assertTrue(jsonObject.has("records"));
         JSONArray records = jsonObject.getJSONArray("records");
         assertEquals(expectedNoOfRecords, records.length());
      }
      return (jsonObject);
   }



   /*******************************************************************************
    ** test running a process with field values on the query string
    **
    *******************************************************************************/
   @Test
   public void test_processGreetInitWithQueryValues()
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/processes/greet/init?recordsParam=recordIds&recordIds=2,3&greetingPrefix=Hey&greetingSuffix=Jude").asString();
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
    ** every time a process step (or init) has gone async, expect what the
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



   /*******************************************************************************
    ** test getting records back from a process
    **
    *******************************************************************************/
   @Test
   public void test_processRecords()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?recordsParam=recordIds&recordIds=2,3&greetingPrefix=Hey&greetingSuffix=Jude").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      String processUUID = jsonObject.getString("processUUID");

      jsonObject = getProcessRecords(processUUID, 2);
      JSONArray  records = jsonObject.getJSONArray("records");
      JSONObject record0 = records.getJSONObject(0);
      JSONObject values  = record0.getJSONObject("values");
      assertTrue(values.has("id"));
      assertTrue(values.has("firstName"));
   }



   /*******************************************************************************
    ** test getting records back from a process with skip & Limit
    **
    *******************************************************************************/
   @Test
   public void test_processRecordsSkipAndLimit()
   {
      HttpResponse<String> response    = Unirest.get(BASE_URL + "/processes/greet/init?recordsParam=recordIds&recordIds=1,2,3,4,5").asString();
      JSONObject           jsonObject  = JsonUtils.toJSONObject(response.getBody());
      String               processUUID = jsonObject.getString("processUUID");

      getProcessRecords(processUUID, 5);
      getProcessRecords(processUUID, 1, 4, 5);
      getProcessRecords(processUUID, 0, 5, 5);
   }

}