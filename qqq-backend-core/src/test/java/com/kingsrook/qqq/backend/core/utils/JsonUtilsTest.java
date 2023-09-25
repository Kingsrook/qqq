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

package com.kingsrook.qqq.backend.core.utils;


import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for JsonUtils.
 **
 *******************************************************************************/
class JsonUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJsonQRecordInput()
   {
      QRecord qRecord = getQRecord();
      String  json    = JsonUtils.toJson(qRecord);
      assertEquals("""
         {"tableName":"foo","values":{"foo":"Foo","bar":3.14159}}""", json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJsonQRecordInputWithNullValues()
   {
      QRecord qRecord = getQRecord();
      String json = JsonUtils.toJson(qRecord, objectMapper ->
      {
         objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
      });

      assertThat(json).contains("""
         "values":{"foo":"Foo","bar":3.14159,"baz":null}""");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class LooksLikeAnEntityButJustThrowsInItsGetterMethod
   {
      public String getValue()
      {
         throw new IllegalStateException("Test");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJsonNull()
   {
      assertThrowsExactly(IllegalArgumentException.class,
         () -> JsonUtils.toJson(new LooksLikeAnEntityButJustThrowsInItsGetterMethod()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toPrettyJsonNull()
   {
      assertThrowsExactly(IllegalArgumentException.class,
         () -> JsonUtils.toPrettyJson(new LooksLikeAnEntityButJustThrowsInItsGetterMethod()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toPrettyJsonQRecordInput()
   {
      QRecord qRecord = getQRecord();
      String  json    = JsonUtils.toPrettyJson(qRecord);
      // todo assertEquals("{\"tableName\":\"foo\",\"primaryKey\":1,\"values\":{\"foo\":\"Foo\",\"bar\":3.14159}}", json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_sortJSONArray() throws Exception
   {
      JSONArray jsonArray1 = new JSONArray("""
         [
            {
               "Foo": "Bar",
               "Baz": [1, 2, 3]
            }
         ]
         """);

      JSONArray jsonArray2 = new JSONArray("""
         [
            {
               "Baz": [1, 2, 3],
               "Foo": "Bar"
            }
         ]
         """);

      JSONArray sortedJSONArray1 = (JSONArray) JsonUtils.sortJSON(jsonArray1);
      JSONArray sortedJSONArray2 = (JSONArray) JsonUtils.sortJSON(jsonArray2);
      assertEquals(sortedJSONArray1.toString(), sortedJSONArray2.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_sortJSONObject() throws Exception
   {
      JSONObject jsonObject1 = new JSONObject("""
         {
            "Foo": "Bar",
            "Baz": [1, 2, 3]
         }
         """);
      JSONObject jsonObject2 = new JSONObject("""
         {
            "Baz": [1, 2, 3],
            "Foo": "Bar"
         }
         """);

      JSONObject sortedJSONObject1 = (JSONObject) JsonUtils.sortJSON(jsonObject1);
      JSONObject sortedJSONObject2 = (JSONObject) JsonUtils.sortJSON(jsonObject2);
      assertEquals(sortedJSONObject1.toString(), sortedJSONObject2.toString());

      JSONObject bigObject1 = new JSONObject("""
         { "cubeiq": { "settings": { "setting": [ { "maxruntime": 60, "maxnonimproveiters": 750, "settingsid": "Box Default", "sequencemixok": true } ] }, "containerstoload": { "containertoload": [ { "containernum": 99, "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "containerid": "25_4976f3f8-1208-430f-aee4-59fc1d1f4b3f" } ] }, "loads": { "load": [ { "date": "1-1-2015", "notes": "", "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0" } ] }, "stages": { "stage": [ { "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "stage": 1 } ] }, "containers": { "container": [ { "depth": 5, "bottomtotoploading": false, "width": 5, "action": "overwrite", "partialloadonfloor": true, "settingsid": "Box Default", "containerid": "25_4976f3f8-1208-430f-aee4-59fc1d1f4b3f", "type": "Rectangular", "height": 10 } ] }, "productstoload": { "producttoload": [ { "quantity": 1, "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "productid": "DRY-ICE", "batch": 1 } ] }, "products": { "product": [ { "productid": "DRY-ICE", "color": 16760576, "length": 3, "weight": 1, "maxinlayer": 999999, "type": "Box", "sideupok": true, "endupok": true, "turnable": true, "bottomonly": false, "width": 3, "toponly": false, "flatok": true, "height": 1 } ] } } }
         """);

      JSONObject bigObject2 = new JSONObject("""
         { "cubeiq": { "containerstoload": { "containertoload": [ { "containernum": 99, "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "containerid": "25_4976f3f8-1208-430f-aee4-59fc1d1f4b3f" } ] }, "settings": { "setting": [ { "maxruntime": 60, "maxnonimproveiters": 750, "settingsid": "Box Default", "sequencemixok": true } ] }, "loads": { "load": [ { "date": "1-1-2015", "notes": "", "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0" } ] }, "stages": { "stage": [ { "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "stage": 1 } ] }, "containers": { "container": [ { "depth": 5, "bottomtotoploading": false, "action": "overwrite", "width": 5, "partialloadonfloor": true, "settingsid": "Box Default", "containerid": "25_4976f3f8-1208-430f-aee4-59fc1d1f4b3f", "type": "Rectangular", "height": 10 } ] }, "products": { "product": [ { "productid": "DRY-ICE", "color": 16760576, "weight": 1, "length": 3, "sideupok": true, "maxinlayer": 999999, "type": "Box", "endupok": true, "toponly": false, "turnable": true, "bottomonly": false, "width": 3, "flatok": true, "height": 1 } ] }, "productstoload": { "producttoload": [ { "quantity": 1, "loadid": "4814b7a4-dc8a-43b8-8772-af86cb11d9a0", "productid": "DRY-ICE", "batch": 1 } ] } } }
         """);
      sortedJSONObject1 = (JSONObject) JsonUtils.sortJSON(bigObject1);
      sortedJSONObject2 = (JSONObject) JsonUtils.sortJSON(bigObject2);
      assertEquals(sortedJSONObject1.toString(), sortedJSONObject2.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_sortNonJSONThrows() throws Exception
   {
      assertThatThrownBy(() -> JsonUtils.sortJSON("derp"))
         .hasMessageContaining("must be of type");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJSONObject()
   {
      JSONObject jsonObject = JsonUtils.toJSONObject("""
         {
            "Foo": "Bar",
            "Baz": [1, 2, 3]
         }
         """);
      assertNotNull(jsonObject);
      assertTrue(jsonObject.has("Foo"));
      assertEquals("Bar", jsonObject.getString("Foo"));
      assertTrue(jsonObject.has("Baz"));
      assertEquals(3, jsonObject.getJSONArray("Baz").length());
      assertEquals(1, jsonObject.getJSONArray("Baz").get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJSONObject_malformed()
   {
      // todo - what do we want to throw here?
      assertThrows(JSONException.class, () ->
      {
         JsonUtils.toJSONObject("""
            {
               "Foo": "Bar",
            """);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJSONObjectNonJsonObject()
   {
      try
      {
         JsonUtils.toJSONObject("");
      }
      catch(JSONException je)
      {
         System.out.println("Caught Expected exception");
         return;
      }

      fail("Did not catch expected exception");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJSONArray()
   {
      JSONArray jsonArray = JsonUtils.toJSONArray("""
         [
            {"Foo": "Bar"},
            {"Baz": [1, 2, 3]}
         ]
         """);
      assertNotNull(jsonArray);
      assertEquals(2, jsonArray.length());
      assertTrue(jsonArray.getJSONObject(0).has("Foo"));
      assertEquals("Bar", jsonArray.getJSONObject(0).getString("Foo"));
      assertTrue(jsonArray.getJSONObject(1).has("Baz"));
      assertEquals(3, jsonArray.getJSONObject(1).getJSONArray("Baz").length());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord getQRecord()
   {
      QRecord qRecord = new QRecord();
      qRecord.setTableName("foo");
      qRecord.setValue("id", 1);
      Map<String, Serializable> values = new LinkedHashMap<>();
      qRecord.setValues(values);
      values.put("foo", "Foo");
      values.put("bar", new BigDecimal("3.14159"));
      values.put("baz", null);
      return qRecord;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toJsonNullInput()
   {
      String json = JsonUtils.toJson(null);
      assertEquals("null", json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_looksLikeObject()
   {
      assertFalse(JsonUtils.looksLikeObject(""));
      assertFalse(JsonUtils.looksLikeObject(null));
      assertFalse(JsonUtils.looksLikeObject("json"));
      assertFalse(JsonUtils.looksLikeObject("[]"));
      assertTrue(JsonUtils.looksLikeObject("{}"));
      assertTrue(JsonUtils.looksLikeObject("  {}"));
      assertTrue(JsonUtils.looksLikeObject("\n\n\n{}"));
      assertTrue(JsonUtils.looksLikeObject("\n{\n[]\n}\n"));
      assertTrue(JsonUtils.looksLikeObject("\n\n\n  { \n}\n\n\n"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_looksLikeArray()
   {
      assertFalse(JsonUtils.looksLikeArray(""));
      assertFalse(JsonUtils.looksLikeArray(null));
      assertFalse(JsonUtils.looksLikeArray("json"));
      assertFalse(JsonUtils.looksLikeArray("{json[]}"));
      assertFalse(JsonUtils.looksLikeArray("{}"));
      assertTrue(JsonUtils.looksLikeArray("[]"));
      assertTrue(JsonUtils.looksLikeArray("  []"));
      assertTrue(JsonUtils.looksLikeArray("\n\n\n[]"));
      assertTrue(JsonUtils.looksLikeArray("\n[\n{}\n}\n"));
      assertTrue(JsonUtils.looksLikeArray("\n\n\n  [ \n]\n\n\n"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_toObject() throws IOException
   {
      String json = JsonUtils.toJson(new QQueryFilter()
         .withCriteria(new QFilterCriteria().withFieldName("id").withOperator(QCriteriaOperator.EQUALS).withValues(List.of(1)))
         .withCriteria(new QFilterCriteria().withFieldName("name").withOperator(QCriteriaOperator.IN).withValues(List.of("Darin", "James")))
         .withOrderBy(new QFilterOrderBy().withFieldName("age").withIsAscending(true)));

      System.out.println(json);

      QQueryFilter qQueryFilter = JsonUtils.toObject(json, QQueryFilter.class);
      assertEquals(2, qQueryFilter.getCriteria().size());
      assertEquals("id", qQueryFilter.getCriteria().get(0).getFieldName());
      assertEquals(QCriteriaOperator.EQUALS, qQueryFilter.getCriteria().get(0).getOperator());
      assertEquals(List.of(1), qQueryFilter.getCriteria().get(0).getValues());
      assertEquals("name", qQueryFilter.getCriteria().get(1).getFieldName());
      assertEquals(QCriteriaOperator.IN, qQueryFilter.getCriteria().get(1).getOperator());
      assertEquals(List.of("Darin", "James"), qQueryFilter.getCriteria().get(1).getValues());
      assertEquals(1, qQueryFilter.getOrderBys().size());
      assertEquals("age", qQueryFilter.getOrderBys().get(0).getFieldName());
      assertTrue(qQueryFilter.getOrderBys().get(0).getIsAscending());
   }
}
