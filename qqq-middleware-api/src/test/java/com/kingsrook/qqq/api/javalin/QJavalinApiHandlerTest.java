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
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
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
            title: "QQQ API"
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
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/").asString());
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/foo").asString());
         assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find any resources at path", Unirest.request(method, BASE_URL + "/api/" + VERSION + "/person/1/2").asString());
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

      QRecord record = getPersonRecord(1);
      assertEquals("Moe", record.getValueString("firstName"));
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
      QRecord personRecord = getPersonRecord(1);
      assertNull(personRecord);

      ///////////////////////////////////////////////////////////////////////////////////////////
      // apparently, as long as the body *starts with* json, the JSONObject constructor builds //
      // a json object out of it??  so... this in this case we expected 400, but get 201...    //
      ///////////////////////////////////////////////////////////////////////////////////////////
      response = Unirest.post(BASE_URL + "/api/" + VERSION + "/person/")
         .body("""
            {"firstName": "Moe"}
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.CREATED_201, null, response);
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

      QRecord record = getPersonRecord(1);
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
      assertErrorResponse(HttpStatus.BAD_REQUEST_400, "Missing required POST body", response);

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
      QRecord personRecord = getPersonRecord(1);
      assertEquals("Mo", personRecord.getValueString("firstName"));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // apparently, as long as the body *starts with* json, the JSONObject constructor builds //
      // a json object out of it??  so... this in this case we expected 400, but get 204...    //
      ///////////////////////////////////////////////////////////////////////////////////////////
      response = Unirest.patch(BASE_URL + "/api/" + VERSION + "/person/1")
         .body("""
            {"firstName": "Moe"}
            Not json
            """)
         .asString();
      assertErrorResponse(HttpStatus.NO_CONTENT_204, null, response);
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

      QRecord record = getPersonRecord(1);
      assertNull(record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDelete404()
   {
      HttpResponse<String> response = Unirest.delete(BASE_URL + "/api/" + VERSION + "/person/1")
         .asString();
      assertErrorResponse(HttpStatus.NOT_FOUND_404, "Could not find Person with Id of 1", response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord getPersonRecord(Integer id) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      getInput.setPrimaryKey(id);
      GetOutput getOutput = new GetAction().execute(getInput);
      QRecord   record    = getOutput.getRecord();
      return record;
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