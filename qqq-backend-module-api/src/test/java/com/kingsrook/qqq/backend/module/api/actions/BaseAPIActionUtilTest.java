/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.api.actions;


import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.module.api.BaseTest;
import com.kingsrook.qqq.backend.module.api.TestUtils;
import com.kingsrook.qqq.backend.module.api.exceptions.RateLimitException;
import com.kingsrook.qqq.backend.module.api.mocks.MockApiActionUtils;
import com.kingsrook.qqq.backend.module.api.mocks.MockApiUtilsHelper;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILog;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILogMetaDataProvider;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.module.api.actions.BaseAPIActionUtil
 *******************************************************************************/
class BaseAPIActionUtilTest extends BaseTest
{
   private static MockApiUtilsHelper mockApiUtilsHelper = new MockApiUtilsHelper();



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      mockApiUtilsHelper = new MockApiUtilsHelper();
      mockApiUtilsHelper.setUseMock(true);
      MockApiActionUtils.mockApiUtilsHelper = mockApiUtilsHelper;

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName("variant");
      QueryOutput        output = new QueryAction().execute(queryInput);
      List<Serializable> ids    = output.getRecords().stream().map(r -> r.getValue("id")).toList();

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName("variant");
      deleteInput.setPrimaryKeys(ids);
      new DeleteAction().execute(deleteInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCount() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         [
            {"id": 1, "name": "Homer"},
            {"id": 2, "name": "Marge"},
            {"id": 3, "name": "Bart"},
            {"id": 4, "name": "Lisa"},
            {"id": 5, "name": "Maggie"}
         ]
         """);

      CountInput countInput = new CountInput();
      countInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      CountOutput countOutput = new CountAction().execute(countInput);
      assertEquals(5, countOutput.getCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCountError() throws QException
   {
      ////////////////////////////////////////
      // avoid the fully mocked makeRequest //
      ////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);

      //////////////////////////
      // set to retry 3 times //
      //////////////////////////
      for(int i = 0; i < 4; i++)
      {
         mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(500).withContent("""
            {"error": "Server error"}
            """));
      }

      CountInput countInput = new CountInput();
      countInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      assertThatThrownBy(() -> new CountAction().execute(countInput)).hasRootCauseInstanceOf(Exception.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGet() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setPrimaryKey(3);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals(3, getOutput.getRecord().getValueInteger("id"));
      assertEquals("Bart", getOutput.getRecord().getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetByKey() throws QException
   {
      QContext.getQInstance().getTable(TestUtils.MOCK_TABLE_NAME).withUniqueKey(new UniqueKey("id"));

      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setUniqueKey(Map.of("id", 3));
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals(3, getOutput.getRecord().getValueInteger("id"));
      assertEquals("Bart", getOutput.getRecord().getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         [
            {"id": 1, "name": "Homer"},
            {"id": 2, "name": "Marge"},
            {"id": 3, "name": "Bart"},
            {"id": 4, "name": "Lisa"},
            {"id": 5, "name": "Maggie"}
         ]
         """);

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size());
      assertEquals(1, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals("Homer", queryOutput.getRecords().get(0).getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryObjectWrappingList() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         {"mocks": [
            {"id": 1, "name": "Homer"},
            {"id": 2, "name": "Marge"},
            {"id": 3, "name": "Bart"},
            {"id": 4, "name": "Lisa"},
            {"id": 5, "name": "Maggie"}
         ]}
         """);

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(5, queryOutput.getRecords().size());
      assertEquals(1, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals("Homer", queryOutput.getRecords().get(0).getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryObjectWrappingSingleObject() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         {"mocks": 
            {"id": 1, "name": "Homer"}
         }
         """);

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size());
      assertEquals(1, queryOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals("Homer", queryOutput.getRecords().get(0).getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryPaginate() throws QException
   {
      String oneObject = """
         {"id": 1, "name": "Homer"}
         """;
      StringBuilder response = new StringBuilder("[");
      for(int i = 0; i < 19; i++)
      {
         response.append(oneObject).append(",");
      }
      response.append(oneObject);
      response.append("]");
      mockApiUtilsHelper.enqueueMockResponse(response.toString());
      mockApiUtilsHelper.enqueueMockResponse(response.toString());
      mockApiUtilsHelper.enqueueMockResponse(response.toString());
      mockApiUtilsHelper.enqueueMockResponse("[]");

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(60, queryOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryError() throws QException
   {
      ////////////////////////////////////////
      // avoid the fully mocked makeRequest //
      ////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);

      //////////////////////////
      // set to retry 3 times //
      //////////////////////////
      for(int i = 0; i < 4; i++)
      {
         mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(500).withContent("""
            {"error": "Server error"}
            """));
      }

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      assertThatThrownBy(() -> new QueryAction().execute(queryInput)).hasRootCauseInstanceOf(Exception.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsert() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 6}
         """);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      insertInput.setRecords(List.of(new QRecord().withValue("name", "Milhouse")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(6, insertOutput.getRecords().get(0).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertEmptyInputList() throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      insertInput.setRecords(List.of());
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertError() throws QException
   {
      ////////////////////////////////////////
      // avoid the fully mocked makeRequest //
      ////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);

      //////////////////////////
      // set to retry 3 times //
      //////////////////////////
      for(int i = 0; i < 4; i++)
      {
         mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(500).withContent("""
            {"error": "Server error"}
            """));
      }

      InsertInput insertInput = new InsertInput();
      insertInput.setRecords(List.of(new QRecord().withValue("name", "Milhouse")));
      insertInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertTrue(CollectionUtils.nullSafeHasContents(insertOutput.getRecords().get(0).getErrors()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate() throws QException
   {
      mockApiUtilsHelper.enqueueMockResponse("");

      mockApiUtilsHelper.setMockRequestAsserter(httpRequestBase ->
      {
         String     requestBody   = MockApiUtilsHelper.readRequestBody(httpRequestBase);
         JSONObject requestObject = new JSONObject(requestBody);

         JSONArray  mocks  = requestObject.getJSONArray("mocks");
         JSONObject record = mocks.getJSONObject(0);

         assertEquals("Bartholomew", record.getString("name"));
         assertEquals(3, record.getInt("id"));
      });

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      updateInput.setRecords(List.of(new QRecord().withValue("id", "3").withValue("name", "Bartholomew")));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);

      // not sure what to assert in here...
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateEmptyInputList() throws QException
   {
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      updateInput.setRecords(List.of());
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateError() throws QException
   {
      ////////////////////////////////////////
      // avoid the fully mocked makeRequest //
      ////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);

      for(int i = 0; i < 4; i++)
      {
         mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(500).withContent("""
            {"error": "Server error"}
            """));
      }

      UpdateInput updateInput = new UpdateInput();
      updateInput.setRecords(List.of(new QRecord().withValue("name", "Milhouse")));
      updateInput.setTableName(TestUtils.MOCK_TABLE_NAME);

      /////////////////////////////////////////////////////////////////////////////////
      // note - right now this is inconsistent with insertAction (and rdbms update), //
      // where errors are placed in the records, rather than thrown...               //
      /////////////////////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> new UpdateAction().execute(updateInput)).hasRootCauseInstanceOf(Exception.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMakeRequest() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setPrimaryKey(3);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals(3, getOutput.getRecord().getValueInteger("id"));
      assertEquals("Bart", getOutput.getRecord().getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test429Then200() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      // specifically, that we can get one 429, and then eventually a 200                       //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(429).withContent("Try again"));
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setPrimaryKey(3);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals(3, getOutput.getRecord().getValueInteger("id"));
      assertEquals("Bart", getOutput.getRecord().getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTooMany429() throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      // specifically, that after too many 429's we get an error                                //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(429).withContent("Try again"));
      mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(429).withContent("Try again"));
      mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(429).withContent("Try again"));
      mockApiUtilsHelper.enqueueMockResponse(new QHttpResponse().withStatusCode(429).withContent("Try again"));

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setPrimaryKey(3);

      assertThatThrownBy(() -> new GetAction().execute(getInput)).hasRootCauseInstanceOf(RateLimitException.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testApiLogs() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      OutboundAPILogMetaDataProvider.defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 6}
         """);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      insertInput.setRecords(List.of(new QRecord().withValue("name", "Milhouse")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(6, insertOutput.getRecords().get(0).getValueInteger("id"));

      //////////////////////////////////////////////////////////////////////////////////////////
      // the outbound api log is inserted async, so... do or do not, and sleep some if needed //
      //////////////////////////////////////////////////////////////////////////////////////////
      QueryOutput apiLogRecords = null;
      int         tries         = 0;
      do
      {
         SleepUtils.sleep(10, TimeUnit.MILLISECONDS);
         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(OutboundAPILog.TABLE_NAME);
         apiLogRecords = new QueryAction().execute(queryInput);
      }
      while(apiLogRecords.getRecords().isEmpty() && tries++ < 10);

      assertEquals(1, apiLogRecords.getRecords().size());
      assertEquals("POST", apiLogRecords.getRecords().get(0).getValueString("method"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBasicAuthApiKey() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.BASIC_AUTH_API_KEY);
      backend.setApiKey("9876-WXYZ");

      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      mockApiUtilsHelper.setMockRequestAsserter(request ->
      {
         Header authHeader = request.getFirstHeader("Authorization");
         assertTrue(authHeader.getValue().startsWith("Basic "));
         String apiKey = new String(Base64.getDecoder().decode(authHeader.getValue().replace("Basic ", "")), StandardCharsets.UTF_8);
         assertEquals("9876-WXYZ", apiKey);
      });

      runSimpleGetAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBackendWithVariantsApiKey() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.API_KEY_HEADER);
      backend.setUsesVariants(true);
      backend.setVariantOptionsTableName("variant");
      backend.setVariantOptionsTableIdField("id");
      backend.setVariantOptionsTableApiKeyField("apiKey");
      backend.setVariantOptionsTableTypeValue("API_KEY_TYPE");

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName("variant");
      insertInput.setRecords(List.of(new QRecord()
         .withValue("type", "API_KEY_TYPE")
         .withValue("apiKey", "abcdefg1234567")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      QContext.getQSession().setBackendVariants(Map.of("API_KEY_TYPE", insertOutput.getRecords().get(0).getValue("id")));
      HttpGet           httpGet = new HttpGet();
      BaseAPIActionUtil util    = new BaseAPIActionUtil();
      util.setBackendMetaData(backend);
      util.setupAuthorizationInRequest(httpGet);
      Header authHeader = httpGet.getFirstHeader("API-Key");
      assertTrue(authHeader.getValue().startsWith("abcde"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBackendWithVariantsUsernamePassword() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.BASIC_AUTH_USERNAME_PASSWORD);
      backend.setUsesVariants(true);
      backend.setVariantOptionsTableName("variant");
      backend.setVariantOptionsTableIdField("id");
      backend.setVariantOptionsTableUsernameField("username");
      backend.setVariantOptionsTablePasswordField("password");
      backend.setVariantOptionsTableTypeValue("USER_PASS");

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName("variant");
      insertInput.setRecords(List.of(new QRecord()
         .withValue("type", "USER_PASS")
         .withValue("username", "user")
         .withValue("password", "pass")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      QContext.getQSession().setBackendVariants(Map.of("USER_PASS", insertOutput.getRecords().get(0).getValue("id")));
      HttpGet           httpGet = new HttpGet();
      BaseAPIActionUtil util    = new BaseAPIActionUtil();
      util.setBackendMetaData(backend);
      util.setupAuthorizationInRequest(httpGet);
      Header authHeader = httpGet.getFirstHeader("Authorization");
      assertTrue(authHeader.getValue().equals(util.getBasicAuthenticationHeader("user", "pass")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBasicAuthUsernamePassword() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.BASIC_AUTH_USERNAME_PASSWORD);
      backend.setUsername("god");
      backend.setPassword("5fingers");

      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      mockApiUtilsHelper.setMockRequestAsserter(request ->
      {
         Header authHeader = request.getFirstHeader("Authorization");
         assertTrue(authHeader.getValue().startsWith("Basic "));
         String usernamePassword = new String(Base64.getDecoder().decode(authHeader.getValue().replace("Basic ", "")), StandardCharsets.UTF_8);
         assertEquals("god:5fingers", usernamePassword);
      });

      runSimpleGetAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOAuth2ValidToken() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.OAUTH2);
      backend.withCustomValue("accessToken", "validToken");

      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      mockApiUtilsHelper.setMockRequestAsserter(request ->
      {
         Header authHeader = request.getFirstHeader("Authorization");
         assertTrue(authHeader.getValue().startsWith("Bearer "));
         String token = authHeader.getValue().replace("Bearer ", "");
         assertEquals("validToken", token);
      });

      runSimpleGetAction();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOAuth2NullToken() throws QException
   {
      APIBackendMetaData backend = (APIBackendMetaData) QContext.getQInstance().getBackend(TestUtils.MOCK_BACKEND_NAME);
      backend.setAuthorizationType(AuthorizationType.OAUTH2);

      ////////////////////////////////////////////////////////////////////////////////////////////
      // this will make it not use the mock makeRequest method,                                 //
      // but instead the mock executeHttpRequest, so we can test code from the base makeRequest //
      ////////////////////////////////////////////////////////////////////////////////////////////
      mockApiUtilsHelper.setUseMock(false);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"access_token": "myNewToken"}
         """);
      mockApiUtilsHelper.enqueueMockResponse("""
         {"id": 3, "name": "Bart"},
         """);

      GetOutput getOutput = runSimpleGetAction();
      assertEquals(3, getOutput.getRecord().getValueInteger("id"));
      assertEquals("Bart", getOutput.getRecord().getValueString("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static GetOutput runSimpleGetAction() throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.MOCK_TABLE_NAME);
      getInput.setPrimaryKey(3);
      return (new GetAction().execute(getInput));
   }

}
