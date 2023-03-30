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

package com.kingsrook.qqq.api.javalin;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.authentication.implementations.FullyAnonymousAuthenticationModule;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QJavalinApiHandler
 *******************************************************************************/
class QJavalinApiHandlerTest extends BaseTest
{
   private static final   int    PORT     = 6263;
   protected static final String BASE_URL = "http://localhost:" + PORT;

   private static final String VERSION = "2023.Q1";

   protected static QJavalinImplementation qJavalinImplementation;



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeAll
   static void beforeAll() throws QInstanceValidationException
   {
      QInstance qInstance = TestUtils.defineInstance();

      qInstance.addTable(new QTableMetaData()
         .withName("internalName")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withApiTableName("externalName")
            .withInitialVersion(TestUtils.V2022_Q4)));

      qJavalinImplementation = new QJavalinImplementation(qInstance);
      qJavalinImplementation.startJavalinServer(PORT);
      qJavalinImplementation.getJavalinService().routes(new QJavalinApiHandler(qInstance).getRoutes());
   }



   /*******************************************************************************
    ** Before the class (all) runs, start a javalin server.
    **
    *******************************************************************************/
   @AfterAll
   static void afterAll()
   {
      qJavalinImplementation.stopJavalinServer();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSpec()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/openapi.yaml").asString();
      System.out.println(response.getBody());
      assertThat(response.getBody())
         .contains("""
            title: "TestAPI"
            """)
         .contains("""
            /person/query:
            """)
      ;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRandom404s()
   {
      for(String method : new String[] { "get", "post", "patch", "delete" })
      {
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/" + VERSION + "/notATable/").asString());
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/" + VERSION + "/notATable/notAnId").asString());
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/" + VERSION + "/person/1/2").asString());
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/foo").asString());

         if(method.equals("get"))
         {
            //////////////////////////////////////////////
            // this path returns the doc site for a GET //
            //////////////////////////////////////////////
            assertErrorResponse(HttpStatus.OK_200, null, Unirest.request(method, BASE_URL + "/api/").asString());
         }
         else
         {
            assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/").asString());
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGet404()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals("Could not find Person with Id of 1", jsonObject.getString("error"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGet200() throws QException
   {
      insertPersonRecord(1, "Homer", "Simpson");

      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(1, jsonObject.getInt("id"));
      assertEquals("Homer", jsonObject.getString("firstName"));
      assertEquals("Simpson", jsonObject.getString("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetAssociations() throws QException
   {
      insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic();

      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/order/1").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      System.out.println(jsonObject.toString(3));
      JSONArray orderLines = jsonObject.getJSONArray("orderLines");
      assertEquals(3, orderLines.length());
      JSONObject orderLine0     = orderLines.getJSONObject(0);
      JSONArray  lineExtrinsics = orderLine0.getJSONArray("extrinsics");
      assertEquals(3, lineExtrinsics.length());
      assertEquals("Size", lineExtrinsics.getJSONObject(0).getString("key"));
      assertEquals("Medium", lineExtrinsics.getJSONObject(0).getString("value"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery404()
   {
      assertError(HttpStatus.NOT_FOUND_404, BASE_URL + "/api/" + VERSION + "/notATable/query?");
      assertError(HttpStatus.NOT_FOUND_404, BASE_URL + "/api/notAVersion/person/query?");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery400()
   {
      String base = BASE_URL + "/api/" + VERSION + "/person/query?";

      assertError("Could not parse pageNo as an integer", base + "pageNo=foo");
      assertError("pageNo must be greater than 0", base + "pageNo=0");

      assertError("Could not parse pageSize as an integer", base + "pageSize=foo");
      assertError("pageSize must be between 1 and 1000.", base + "pageSize=0");
      assertError("pageSize must be between 1 and 1000.", base + "pageSize=1001");

      assertError("booleanOperator must be either AND or OR", base + "booleanOperator=not");
      assertError("includeCount must be either true or false", base + "includeCount=maybe");

      assertError("orderBy direction for field firstName must be either ASC or DESC", base + "orderBy=firstName foo");
      assertError("Unrecognized format for orderBy clause: firstName asc foo", base + "orderBy=firstName asc foo");
      assertError("Unrecognized orderBy field name: foo", base + "orderBy=foo");
      assertError("Unrecognized filter criteria field: foo", base + "foo=bar");

      assertError("Request failed with 2 reasons", base + "foo=bar&bar=baz");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200NoParams()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(0, jsonObject.getInt("count"));
      assertEquals(1, jsonObject.getInt("pageNo"));
      assertEquals(50, jsonObject.getInt("pageSize"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200SomethingFound() throws QException
   {
      insertPersonRecord(1, "Homer", "Simpson");

      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(1, jsonObject.getInt("count"));
      assertEquals(1, jsonObject.getInt("pageNo"));
      assertEquals(50, jsonObject.getInt("pageSize"));

      JSONArray jsonArray = jsonObject.getJSONArray("records");
      jsonObject = jsonArray.getJSONObject(0);
      assertEquals(1, jsonObject.getInt("id"));
      assertEquals("Homer", jsonObject.getString("firstName"));
      assertEquals("Simpson", jsonObject.getString("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200OrQuery() throws QException
   {
      insertSimpsons();
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge"), "booleanOperator=OR&firstName=Homer&firstName=Marge&orderBy=firstName");
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart", "Lisa", "Maggie"), "booleanOperator=OR&firstName=!Homer&firstName=!Marge&orderBy=id");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200EqualsAndNot() throws QException
   {
      insertSimpsons();
      assertPersonQueryFindsFirstNames(List.of("Homer"), "firstName=Homer"); // no operator implies =
      assertPersonQueryFindsFirstNames(List.of("Homer"), "firstName==Homer"); // == is an explicit equals operator.
      assertPersonQueryFindsFirstNames(List.of(), "firstName===Homer"); /// === is = "=Homer"

      assertPersonQueryFindsFirstNames(List.of("Marge", "Bart", "Lisa", "Maggie"), "firstName=!Homer&orderBy=id"); // ! alone implies not-equals
      assertPersonQueryFindsFirstNames(List.of("Marge", "Bart", "Lisa", "Maggie"), "firstName=!=Homer&orderBy=id"); // != is explicit not-equals
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart", "Lisa", "Maggie"), "firstName=!==Homer&orderBy=id"); // !== is not-equals "=Homer"
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200LikeAndNotLike() throws QException
   {
      insertSimpsons();
      String PERCENT = "%25";
      assertPersonQueryFindsFirstNames(List.of("Homer"), "firstName=LIKE Ho" + PERCENT);
      assertPersonQueryFindsFirstNames(List.of("Homer"), "firstName=LIKE Ho_er");

      assertPersonQueryFindsFirstNames(List.of("Marge", "Bart", "Lisa", "Maggie"), "firstName=!LIKE Homer&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of("Homer"), "firstName=!LIKE " + PERCENT + "a" + PERCENT + "&orderBy=id");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200In() throws QException
   {
      insertSimpsons();
      assertPersonQueryFindsFirstNames(List.of("Bart", "Lisa", "Maggie"), "firstName=IN Bart,Lisa,Maggie&orderBy=firstName");
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge"), "firstName=!IN Bart,Lisa,Maggie&orderBy=firstName");
      assertPersonQueryFindsFirstNames(List.of("Maggie"), "firstName=IN Maggie");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200Between() throws QException
   {
      insertSimpsons();
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart"), "id=BETWEEN 1,3&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of("Homer", "Maggie"), "id=!BETWEEN 2,4&orderBy=id");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200Empty() throws QException
   {
      insertSimpsons();
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart", "Lisa", "Maggie"), "noOfShoes=EMPTY&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of(), "noOfShoes=!EMPTY");
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart", "Lisa", "Maggie"), "id=!EMPTY&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of(), "id=EMPTY");

      assertError("Unexpected value after operator EMPTY for field id", BASE_URL + "/api/" + VERSION + "/person/query?id=EMPTY 3");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200LessThanGreaterThanEquals() throws QException
   {
      insertSimpsons();
      String GT = "%3E";
      assertPersonQueryFindsFirstNames(List.of("Lisa", "Maggie"), "id=" + GT + "3&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of("Bart", "Lisa", "Maggie"), "id=" + GT + "=3&orderBy=id");

      String LT = "%3C";
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge"), "id=" + LT + "3&orderBy=id");
      assertPersonQueryFindsFirstNames(List.of("Homer", "Marge", "Bart"), "id=" + LT + "=3&orderBy=id");

      assertError("Unsupported operator: !>", BASE_URL + "/api/" + VERSION + "/person/query?id=!" + GT + "3");
      assertError("Unsupported operator: !>=", BASE_URL + "/api/" + VERSION + "/person/query?id=!" + GT + "=3");
      assertError("Unsupported operator: !<", BASE_URL + "/api/" + VERSION + "/person/query?id=!" + LT + "3");
      assertError("Unsupported operator: !<=", BASE_URL + "/api/" + VERSION + "/person/query?id=!" + LT + "=3");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery200ManyParams()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query?pageSize=49&pageNo=2&includeCount=true&booleanOperator=AND&firstName=Homer&orderBy=firstName desc").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(0, jsonObject.getInt("count"));
      assertEquals(2, jsonObject.getInt("pageNo"));
      assertEquals(49, jsonObject.getInt("pageSize"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQueryAssociations() throws QException
   {
      insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic();

      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/order/query?id=1").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      System.out.println(jsonObject.toString(3));
      JSONObject order0     = jsonObject.getJSONArray("records").getJSONObject(0);
      JSONArray  orderLines = order0.getJSONArray("orderLines");
      assertEquals(3, orderLines.length());
      JSONObject orderLine0     = orderLines.getJSONObject(0);
      JSONArray  lineExtrinsics = orderLine0.getJSONArray("extrinsics");
      assertEquals(3, lineExtrinsics.length());
      assertEquals("Size", lineExtrinsics.getJSONObject(0).getString("key"));
      assertEquals("Medium", lineExtrinsics.getJSONObject(0).getString("value"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsert201() throws QException
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe"}
            """)
         .asString();
      assertEquals(HttpStatus.CREATED_201, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(1, jsonObject.getInt("id"));

      QRecord record = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("Moe", record.getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsert201WithAssociatedRecords() throws QException
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/" + VERSION + "/order/")
         .body("""
            {"orderNo": "ORD123", "storeId": 47, "orderLines":
               [
                  {"lineNumber": 1, "sku": "BASIC1", "quantity": 17, "extrinsics": [{"key": "size", "value": "Large"}]},
                  {"lineNumber": 2, "sku": "BASIC2", "quantity": 23}
               ], "extrinsics":
               [
                  {"key": "storeName", "value": "My Shopify"},
                  {"key": "shopifyOrderNo", "value": "#2820503"}
               ]
            }
            """)
         .asString();
      System.out.println(response.getBody());
      assertEquals(HttpStatus.CREATED_201, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(1, jsonObject.getInt("id"));

      QRecord record = getRecord(TestUtils.TABLE_NAME_ORDER, 1);
      assertEquals("ORD123", record.getValueString("orderNo"));

      List<QRecord> lines = queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(2, lines.size());
      assertTrue(lines.stream().allMatch(r -> r.getValueInteger("orderId").equals(1)));
      assertTrue(lines.stream().anyMatch(r -> r.getValueString("sku").equals("BASIC1")));
      assertTrue(lines.stream().anyMatch(r -> r.getValueString("sku").equals("BASIC2")));

      List<QRecord> orderExtrinsics = queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(2, orderExtrinsics.size());
      assertTrue(orderExtrinsics.stream().allMatch(r -> r.getValueInteger("orderId").equals(1)));
      assertTrue(orderExtrinsics.stream().anyMatch(r -> r.getValueString("key").equals("storeName") && r.getValueString("value").equals("My Shopify")));
      assertTrue(orderExtrinsics.stream().anyMatch(r -> r.getValueString("key").equals("shopifyOrderNo") && r.getValueString("value").equals("#2820503")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsert400s() throws QException
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            [{"firstName": "Moe"}]
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: A JSONObject text must begin with '{'", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            "firstName"="Moe"
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: A JSONObject text must begin with '{'", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         // no body
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required POST body", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe", "firstName": "Barney"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: Duplicate key \"firstName\"", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe", "foo": "bar"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 1 unrecognized field name: foo", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe", "foo": "bar", "bar": true, "baz": 1}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 3 unrecognized field names: ", response);

      ///////////////////////////////////
      // assert it didn't get inserted //
      ///////////////////////////////////
      QRecord personRecord = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertNull(personRecord);

      ///////////////////////////////////////////
      // If more than just a json object, fail //
      ///////////////////////////////////////////
      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe"}
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body contained more than a single JSON object", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkInsert207() throws QException
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [
               {"firstName": "Moe", "email": "moe@moes.com"},
               {"firstName": "Barney", "email": "barney@moes.com"},
               {"firstName": "CM", "email": "boss@snpp.com"},
               {"firstName": "Waylon", "email": "boss@snpp.com"}
            ]
            """)
         .asString();
      assertEquals(HttpStatus.MULTI_STATUS_207, response.getStatus());
      JSONArray jsonArray = new JSONArray(response.getBody());
      assertEquals(4, jsonArray.length());

      assertEquals(HttpStatus.CREATED_201, jsonArray.getJSONObject(0).getInt("statusCode"));
      assertEquals(1, jsonArray.getJSONObject(0).getInt("id"));

      assertEquals(HttpStatus.CREATED_201, jsonArray.getJSONObject(1).getInt("statusCode"));
      assertEquals(2, jsonArray.getJSONObject(1).getInt("id"));

      assertEquals(HttpStatus.CREATED_201, jsonArray.getJSONObject(2).getInt("statusCode"));
      assertEquals(3, jsonArray.getJSONObject(2).getInt("id"));

      assertEquals(HttpStatus.BAD_REQUEST_400, jsonArray.getJSONObject(3).getInt("statusCode"));
      assertEquals("Error inserting Person: Another record already exists with this Email", jsonArray.getJSONObject(3).getString("error"));

      QRecord record = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("Moe", record.getValueString("firstName"));

      record = getRecord(TestUtils.TABLE_NAME_PERSON, 2);
      assertEquals("Barney", record.getValueString("firstName"));

      record = getRecord(TestUtils.TABLE_NAME_PERSON, 3);
      assertEquals("CM", record.getValueString("firstName"));

      record = getRecord(TestUtils.TABLE_NAME_PERSON, 4);
      assertNull(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkInsert400s() throws QException
   {
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            {"firstName": "Moe"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON array: A JSONArray text must start with '['", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         // no body
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required POST body", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("[]")
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "No records were found in the POST body", response);

      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [{"firstName": "Moe", "foo": "bar"}]
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 1 unrecognized field name: foo", response);

      /////////////////////////////////
      // assert nothing got inserted //
      /////////////////////////////////
      QRecord personRecord = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertNull(personRecord);

      //////////////////////////////////////////
      // If more than just a json array, fail //
      //////////////////////////////////////////
      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [{"firstName": "Moe"}]
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body contained more than a single JSON array", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate204() throws QException
   {
      insertPersonRecord(1, "CM", "Burns");

      HttpResponse<String> response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Charles"}
            """)
         .asString();
      assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
      assertFalse(StringUtils.hasContent(response.getBody()));

      QRecord record = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("Charles", record.getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate404()
   {
      HttpResponse<String> response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Charles"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find Person with Id of 1", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate400s() throws QException
   {
      insertPersonRecord(1, "Mo", "Szyslak");

      HttpResponse<String> response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            [{"firstName": "Moe"}]
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: A JSONObject text must begin with '{'", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            "firstName"="Moe"
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: A JSONObject text must begin with '{'", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         // no body
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required PATCH body", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Moe", "firstName": "Barney"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON object: Duplicate key \"firstName\"", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Moe", "foo": "bar"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 1 unrecognized field name: foo", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Moe", "foo": "bar", "bar": true, "baz": 1}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 3 unrecognized field names: ", response);

      ///////////////////////////////////
      // assert it didn't get updated. //
      ///////////////////////////////////
      QRecord personRecord = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("Mo", personRecord.getValueString("firstName"));

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Moe"}
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body contained more than a single JSON object", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkUpdate207() throws QException
   {
      insertSimpsons();

      HttpResponse<String> response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [
               {"id": 1, "email": "homer@simpson.com"},
               {"id": 2, "email": "marge@simpson.com"},
               {"email": "nobody@simpson.com"}
            ]
            """)
         .asString();
      assertEquals(HttpStatus.MULTI_STATUS_207, response.getStatus());
      JSONArray jsonArray = new JSONArray(response.getBody());
      assertEquals(3, jsonArray.length());

      assertEquals(HttpStatus.NO_CONTENT_204, jsonArray.getJSONObject(0).getInt("statusCode"));
      assertEquals(HttpStatus.NO_CONTENT_204, jsonArray.getJSONObject(1).getInt("statusCode"));

      assertEquals(HttpStatus.BAD_REQUEST_400, jsonArray.getJSONObject(2).getInt("statusCode"));
      assertEquals("Error updating Person: Missing value in primary key field", jsonArray.getJSONObject(2).getString("error"));

      QRecord record = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("homer@simpson.com", record.getValueString("email"));

      record = getRecord(TestUtils.TABLE_NAME_PERSON, 2);
      assertEquals("marge@simpson.com", record.getValueString("email"));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      queryInput.setFilter(new QQueryFilter(new QFilterCriteria("email", QCriteriaOperator.EQUALS, "nobody@simpson.com")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(0, queryOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkUpdate400s() throws QException
   {
      HttpResponse<String> response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            {"firstName": "Moe"}
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON array: A JSONArray text must start with '['", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         // no body
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required PATCH body", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("[]")
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "No records were found in the PATCH body", response);

      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [{"firstName": "Moe", "foo": "bar"}]
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Request body contained 1 unrecognized field name: foo", response);

      ////////////////////////////////
      // assert nothing got updated //
      ////////////////////////////////
      QRecord personRecord = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertNull(personRecord);

      //////////////////////////////////////////
      // If more than just a json array, fail //
      //////////////////////////////////////////
      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [{"firstName": "Moe"}]
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body contained more than a single JSON array", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkDelete207() throws QException
   {
      insertSimpsons();

      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [ 1, 3, 5 ]
            """)
         .asString();
      assertEquals(HttpStatus.MULTI_STATUS_207, response.getStatus());
      JSONArray jsonArray = new JSONArray(response.getBody());
      assertEquals(3, jsonArray.length());

      assertEquals(HttpStatus.NO_CONTENT_204, jsonArray.getJSONObject(0).getInt("statusCode"));
      assertEquals(HttpStatus.NO_CONTENT_204, jsonArray.getJSONObject(1).getInt("statusCode"));
      assertEquals(HttpStatus.NO_CONTENT_204, jsonArray.getJSONObject(2).getInt("statusCode"));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size());
      assertEquals(List.of(2, 4), queryOutput.getRecords().stream().map(r -> r.getValueInteger("id")).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkDelete400s() throws QException
   {
      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            1, 2, 3
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body could not be parsed as a JSON array: A JSONArray text must start with '['", response);

      response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         // no body
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required DELETE body", response);

      response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("[]")
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "No primary keys were found in the DELETE body", response);

      response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [{"id": 1}]
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "One or more elements inside the DELETE body JSONArray was not a primitive value", response);

      ////////////////////////////////
      // assert nothing got deleted //
      ////////////////////////////////
      QRecord personRecord = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertNull(personRecord);

      //////////////////////////////////////////
      // If more than just a json array, fail //
      //////////////////////////////////////////
      response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/bulk")
         .body("""
            [1,2,3]
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Body contained more than a single JSON array", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDeleteWithoutPkey() throws QException
   {
      insertPersonRecord(1, "CM", "Burns");

      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/").asString();
      assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDelete204() throws QException
   {
      insertPersonRecord(1, "CM", "Burns");

      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
      assertFalse(StringUtils.hasContent(response.getBody()));

      QRecord record = getRecord(TestUtils.TABLE_NAME_PERSON, 1);
      assertNull(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDelete404()
   {
      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find Person with Id of 1", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDeleteAssociations() throws QException
   {
      insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic();

      assertEquals(1, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(4, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(1, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());

      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/order/1").asString();
      assertEquals(HttpStatus.NO_CONTENT_204, response.getStatus());
      assertFalse(StringUtils.hasContent(response.getBody()));

      assertEquals(0, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic() throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("orderNo", "ORD123").withValue("storeId", 47)
         .withAssociatedRecord("orderLines", new QRecord().withValue("lineNumber", 1).withValue("sku", "BASIC1").withValue("quantity", 42)
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "Size").withValue("value", "Medium"))
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "Discount").withValue("value", "3.50"))
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "Color").withValue("value", "Red")))
         .withAssociatedRecord("orderLines", new QRecord().withValue("lineNumber", 2).withValue("sku", "BASIC2").withValue("quantity", 42)
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "Size").withValue("value", "Medium")))
         .withAssociatedRecord("orderLines", new QRecord().withValue("lineNumber", 3).withValue("sku", "BASIC3").withValue("quantity", 42))
         .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "shopifyOrderNo").withValue("value", "#1032"))
      ));
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRenamedTable()
   {
      {
         HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/internalName/query").asString();
         assertEquals(HttpStatus.NOT_FOUND_404, response.getStatus());
      }

      {
         HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/externalName/query").asString();
         assertEquals(HttpStatus.OK_200, response.getStatus());
         JSONObject jsonObject = new JSONObject(response.getBody());
         assertEquals(0, jsonObject.getInt("count"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuthorizeNoParams()
   {
      ///////////////
      // no params //
      ///////////////
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/oauth/token").asString();
      assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
      assertThat(response.getBody()).contains("client_id");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuthorizeOneParam()
   {
      ///////////////
      // no params //
      ///////////////
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/oauth/token")
         .body("client_id=XXXXXXXXXX").asString();
      assertEquals(HttpStatus.BAD_REQUEST_400, response.getStatus());
      assertThat(response.getBody()).contains("client_secret");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuthorizeAllParams()
   {
      ///////////////
      // no params //
      ///////////////
      HttpResponse<String> response = Unirest.post(BASE_URL + "/api/oauth/token")
         .body("client_id=XXXXXXXXXX&client_secret=YYYYYYYYYYYY").asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      assertThat(response.getBody()).isEqualTo(FullyAnonymousAuthenticationModule.TEST_ACCESS_TOKEN);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord getRecord(String tableName, Integer id) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(tableName);
      getInput.setPrimaryKey(id);
      GetOutput getOutput = new GetAction().execute(getInput);
      QRecord   record    = getOutput.getRecord();
      return record;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> queryTable(String tableName) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(tableName);
      queryInput.setFilter(new QQueryFilter().withOrderBy(new QFilterOrderBy("id")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void insertPersonRecord(Integer id, String firstName, String lastName) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      insertInput.setRecords(List.of(new QRecord().withValue("id", id).withValue("firstName", firstName).withValue("lastName", lastName)));
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void insertSimpsons() throws QException
   {
      insertPersonRecord(1, "Homer", "Simpson");
      insertPersonRecord(2, "Marge", "Simpson");
      insertPersonRecord(3, "Bart", "Simpson");
      insertPersonRecord(4, "Lisa", "Simpson");
      insertPersonRecord(5, "Maggie", "Simpson");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertPersonQueryFindsFirstNames(List<String> expectedFirstNames, String queryString)
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query?" + queryString).asString();
      assertEquals(HttpStatus.OK_200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(expectedFirstNames.size(), jsonObject.getInt("count"));

      if(expectedFirstNames.isEmpty() && !jsonObject.has("records"))
      {
         return;
      }

      JSONArray    jsonArray        = jsonObject.getJSONArray("records");
      List<String> actualFirstNames = new ArrayList<>();
      for(int i = 0; i < jsonArray.length(); i++)
      {
         actualFirstNames.add(jsonArray.getJSONObject(i).getString("firstName"));
      }

      assertEquals(expectedFirstNames, actualFirstNames);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertError(Integer statusCode, String url)
   {
      HttpResponse<String> response = Unirest.get(url).asString();
      assertEquals(statusCode, response.getStatus());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertError(String expectedErrorMessage, String url)
   {
      HttpResponse<String> response = Unirest.get(url).asString();
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, expectedErrorMessage, response);
      JSONObject jsonObject = new JSONObject(response.getBody());
      String     error      = jsonObject.getString("error");
      assertThat(error).contains(expectedErrorMessage);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertErrorResponse(Integer expectedStatusCode, String expectedErrorMessage, HttpResponse<String> response)
   {
      if(expectedStatusCode != null)
      {
         assertEquals(expectedStatusCode, response.getStatus());
      }

      if(expectedErrorMessage != null)
      {
         JSONObject jsonObject = new JSONObject(response.getBody());
         String     error      = jsonObject.getString("error");
         assertThat(error).contains(expectedErrorMessage);
      }
   }

}
