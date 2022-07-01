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
   private static final int PORT = 6262;
   private static final String BASE_URL = "http://localhost:" + PORT;



   /*******************************************************************************
    ** Before the class (all) runs, start a javalin server.
    **
    *******************************************************************************/
   @BeforeAll
   public static void beforeAll() throws Exception
   {
      QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(TestUtils.defineInstance());
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
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/person").asString();

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
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/notAnActualTable").asString();

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus()); // todo 404?
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      String error = jsonObject.getString("error");
      assertTrue(error.contains("not found"));
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
      String filterJson = "{\"criteria\":[{\"fieldName\":\"firstName\",\"operator\":\"EQUALS\",\"values\":[\"Tim\"]}]}";
      HttpResponse<String> response = Unirest.get(BASE_URL + "/data/person?filter=" + URLEncoder.encode(filterJson, StandardCharsets.UTF_8)).asString();

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
      HttpResponse<String> response = Unirest.delete(BASE_URL + "/data/person/3")
         .header("Content-Type", "application/json")
         .asString();

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
   public void test_processGreetInit() throws Exception
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init")
         .header("Content-Type", "application/json")
         .asString();

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
   public void test_processGreetInitWithQueryValues() throws Exception
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/processes/greet/init?greetingPrefix=Hey&greetingSuffix=Jude")
         .header("Content-Type", "application/json")
         .asString();

      assertEquals(200, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertNotNull(jsonObject);
      assertEquals("Hey X Jude", jsonObject.getJSONObject("values").getString("outputMessage"));
   }

}
