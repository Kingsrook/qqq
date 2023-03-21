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


import java.util.List;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


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
   public static void afterAll()
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
   void testGet404()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertEquals(404, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals("Could not find Person with Id of 1", jsonObject.getString("error"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGet200() throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/1").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(1, jsonObject.getInt("id"));
      assertEquals("Darin", jsonObject.getString("firstName"));
      assertEquals("Kelkhoff", jsonObject.getString("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery400()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query?asdf=Darin&orderBy=asdf asdf").asString();
      assertEquals(400, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      String     error      = jsonObject.getString("error");
      assertThat(error).contains("orderBy direction for field asdf must be either ASC or DESC");
      assertThat(error).contains("Unrecognized orderBy field name: asdf");
      assertThat(error).contains("Unrecognized filter criteria field: asdf");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery()
   {
      HttpResponse<String> response = Unirest.get(BASE_URL + "/api/" + VERSION + "/person/query?firstName=Darin&orderBy=firstName desc").asString();
      assertEquals(200, response.getStatus());
      JSONObject jsonObject = new JSONObject(response.getBody());
      assertEquals(0, jsonObject.getInt("count"));
      assertEquals(1, jsonObject.getInt("pageNo"));
      assertEquals(50, jsonObject.getInt("pageSize"));
   }

}