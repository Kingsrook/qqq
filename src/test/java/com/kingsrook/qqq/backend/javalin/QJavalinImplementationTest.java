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
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** based on https://javalin.io/tutorials/testing
 **
 *******************************************************************************/
class QJavalinImplementationTest
{
   private static final int PORT = 6262;
   private static final String BASE_URL = "http://localhost:" + PORT;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeAll
   public static void beforeAll() throws Exception
   {
      QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(TestUtils.defineInstance());
      qJavalinImplementation.startJavalinServer(PORT);
   }



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
    **
    *******************************************************************************/
   @Test
   public void test_tableMetaData_notFound()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/metaData/notAnActualTable").asString();

      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
      JSONObject jsonObject = JsonUtils.toJSONObject(response.getBody());
      assertEquals(1, jsonObject.keySet().size(), "Number of top-level keys");
      String error = jsonObject.getString("error");
      assertTrue(error.contains("not found"));
   }



   /*******************************************************************************
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
      assertTrue(record0.has("primaryKey"));
      JSONObject values0 = record0.getJSONObject("values");
      assertTrue(values0.has("firstName"));
   }


   /*******************************************************************************
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
    **
    *******************************************************************************/
   @Test
   public void test_dataInsert()
   {
      Map<String, Serializable> body = new HashMap<>();
      body.put("firstName", "Bobby");
      body.put("lastName", "Hull");

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
      assertTrue(record0.has("primaryKey"));
      JSONObject values0 = record0.getJSONObject("values");
      assertTrue(values0.has("firstName"));
      assertEquals("Bobby", values0.getString("firstName"));
   }
}