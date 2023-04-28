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

package com.kingsrook.qqq.api.utils;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.api.TestUtils.insertSimpsons;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.api.utils.ApiScriptUtils
 *******************************************************************************/
class ApiScriptUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSetApiNameAndApiVersion()
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();

      assertThatThrownBy(() -> apiScriptUtils.setApiName("not an api"))
         .isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not a valid API name");

      assertThatThrownBy(() -> apiScriptUtils.setApiVersion("not a version"))
         .isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not a supported version");

      assertThatThrownBy(() -> new ApiScriptUtils("not an api", TestUtils.CURRENT_API_VERSION))
         .isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not a valid API name");

      assertThatThrownBy(() -> new ApiScriptUtils(TestUtils.ALTERNATIVE_API_NAME, "not a version"))
         .isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not a supported version");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGet() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();

      assertThatThrownBy(() -> apiScriptUtils.get(TestUtils.TABLE_NAME_PERSON, 1))
         .isInstanceOf(QNotFoundException.class);

      insertSimpsons();

      Map<String, Serializable> result = apiScriptUtils.get(TestUtils.TABLE_NAME_PERSON, 1);
      assertEquals("Homer", result.get("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQuery() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();

      assertThatThrownBy(() -> apiScriptUtils.query(TestUtils.TABLE_NAME_PERSON + "?foo=bar"))
         .isInstanceOf(QBadRequestException.class)
         .hasMessageContaining("Unrecognized filter criteria field: foo");

      insertSimpsons();

      Map<String, Serializable> result = apiScriptUtils.query(TestUtils.TABLE_NAME_PERSON + "?id=2");
      assertEquals(1, result.get("count"));
      assertEquals(1, ((List<?>) result.get("records")).size());
      assertEquals("Marge", ((Map<?, ?>) ((List<?>) result.get("records")).get(0)).get("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsert() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();
      Map<String, Serializable> result = apiScriptUtils.insert(TestUtils.TABLE_NAME_PERSON, """
         { "firstName": "Mr.", "lastName": "Burns" }
         """);
      assertEquals(1, result.get("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkInsert() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();
      List<Map<String, Serializable>> result = apiScriptUtils.bulkInsert(TestUtils.TABLE_NAME_PERSON, """
         [
            { "firstName": "Mr.", "lastName": "Burns" },
            { "firstName": "Waylon", "lastName": "Smithers" }
         ]
         """);
      assertEquals(2, result.size());
      assertEquals(1, result.get(0).get("id"));
      assertEquals(2, result.get(1).get("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();
      String updateJSON = """
         { "firstName": "Homer J." }
         """;

      assertThatThrownBy(() -> apiScriptUtils.update(TestUtils.TABLE_NAME_PERSON, 1, updateJSON))
         .isInstanceOf(QNotFoundException.class);

      insertSimpsons();

      apiScriptUtils.update(TestUtils.TABLE_NAME_PERSON, 1, updateJSON);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals("Homer J.", getOutput.getRecord().getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkUpdate() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();

      insertSimpsons();

      String updateJSON = """
         [
            { "id": 1, "firstName": "Homer J." },
            { "id": 6, "firstName": "C.M." }
         ]
         """;

      List<Map<String, Serializable>> result = apiScriptUtils.bulkUpdate(TestUtils.TABLE_NAME_PERSON, updateJSON);

      assertEquals(2, result.size());
      assertEquals(1, result.get(0).get("id"));
      assertEquals(6, result.get(1).get("id"));
      assertEquals(404, result.get(1).get("statusCode"));
      assertNotNull(result.get(1).get("error"));

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertEquals("Homer J.", getOutput.getRecord().getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDelete() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();
      assertThatThrownBy(() -> apiScriptUtils.delete(TestUtils.TABLE_NAME_PERSON, 1))
         .isInstanceOf(QNotFoundException.class);

      insertSimpsons();

      apiScriptUtils.delete(TestUtils.TABLE_NAME_PERSON, 1);

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertNull(getOutput.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBulkDelete() throws QException
   {
      ApiScriptUtils apiScriptUtils = newDefaultApiScriptUtils();

      insertSimpsons();

      List<Map<String, Serializable>> result = apiScriptUtils.bulkDelete(TestUtils.TABLE_NAME_PERSON, "[1,6]");

      assertEquals(2, result.size());
      assertEquals(1, ValueUtils.getValueAsInteger(result.get(0).get("id")));
      assertEquals(6, ValueUtils.getValueAsInteger(result.get(1).get("id")));
      assertEquals(404, result.get(1).get("statusCode"));
      assertNotNull(result.get(1).get("error"));

      GetInput getInput = new GetInput();
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      assertNull(getOutput.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ApiScriptUtils newDefaultApiScriptUtils()
   {
      ApiScriptUtils apiScriptUtils = new ApiScriptUtils();
      apiScriptUtils.setApiName(TestUtils.API_NAME);
      apiScriptUtils.setApiVersion(TestUtils.CURRENT_API_VERSION);
      return apiScriptUtils;
   }

}