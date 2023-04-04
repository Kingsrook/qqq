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

package com.kingsrook.qqq.api.actions;


import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/*******************************************************************************
 ** Unit test for QRecordApiAdapter
 *******************************************************************************/
class QRecordApiAdapterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordToApiMap() throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // QRecord has values corresponding to what's defined in the QInstance (and the underlying backend system) //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QRecord person = new QRecord()
         .withValue("firstName", "Tim")
         .withValue("noOfShoes", 2)
         .withValue("birthDate", LocalDate.of(1980, Month.MAY, 31))
         .withValue("cost", new BigDecimal("3.50"))
         .withValue("price", new BigDecimal("9.99"));

      Map<String, Serializable> pastApiRecord = QRecordApiAdapter.qRecordToApiMap(person, TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2022_Q4);
      assertEquals(2, pastApiRecord.get("shoeCount")); // old field name - not currently in the QTable, but we can still get its value!
      assertFalse(pastApiRecord.containsKey("noOfShoes")); // current field name - doesn't appear in old api-version
      assertFalse(pastApiRecord.containsKey("cost")); // a current field name, but also not in this old api version

      Map<String, Serializable> currentApiRecord = QRecordApiAdapter.qRecordToApiMap(person, TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q1);
      assertFalse(currentApiRecord.containsKey("shoeCount")); // old field name - not in this current api version
      assertEquals(2, currentApiRecord.get("noOfShoes")); // current field name - value here as we expect
      assertFalse(currentApiRecord.containsKey("cost")); // future field name - not in the current api (we added the field during new dev, and didn't change the api)

      Map<String, Serializable> futureApiRecord = QRecordApiAdapter.qRecordToApiMap(person, TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q2);
      assertFalse(futureApiRecord.containsKey("shoeCount")); // old field name - also not in this future api version
      assertEquals(2, futureApiRecord.get("noOfShoes")); // current field name - still here.
      assertEquals(new BigDecimal("3.50"), futureApiRecord.get("cost")); // future field name appears now that we've requested this future api version.

      for(Map<String, Serializable> apiRecord : List.of(pastApiRecord, currentApiRecord, futureApiRecord))
      {
         assertEquals(LocalDate.parse("1980-05-31"), apiRecord.get("birthDay")); // use the apiFieldName
         assertFalse(apiRecord.containsKey("price")); // excluded field never appears
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // confirm that for the alternative api, we get a record that looks just like the input record (per its api meta data) //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(String version : List.of(TestUtils.V2022_Q4, TestUtils.V2023_Q1, TestUtils.V2023_Q2))
      {
         Map<String, Serializable> alternativeApiRecord = QRecordApiAdapter.qRecordToApiMap(person, TestUtils.TABLE_NAME_PERSON, TestUtils.ALTERNATIVE_API_NAME, version);
         for(String key : person.getValues().keySet())
         {
            assertEquals(person.getValueString(key), ValueUtils.getValueAsString(alternativeApiRecord.get(key)));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testApiJsonObjectToQRecord() throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // past version took shoeCount - so we still take that, but now put it in noOfShoes field of qRecord //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      QRecord recordFromOldApi = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "shoeCount": 2}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2022_Q4, true);
      assertEquals(2, recordFromOldApi.getValueInteger("noOfShoes"));

      ///////////////////////////////////////////
      // current version takes it as noOfShoes //
      ///////////////////////////////////////////
      QRecord recordFromCurrentApi = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "noOfShoes": 2}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q1, true);
      assertEquals(2, recordFromCurrentApi.getValueInteger("noOfShoes"));

      /////////////////////////////////////////////
      // future version supports cost field too! //
      /////////////////////////////////////////////
      QRecord recordFromFutureApi = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "noOfShoes": 2, "cost": 3.50}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q2, true);
      assertEquals(2, recordFromFutureApi.getValueInteger("noOfShoes"));
      assertEquals(new BigDecimal("3.50"), recordFromFutureApi.getValueBigDecimal("cost"));

      ///////////////////////////////////////////////////////////////////
      // make sure apiFieldName is used (instead of table's fieldName) //
      ///////////////////////////////////////////////////////////////////
      QRecord recordWithApiFieldName = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "birthDay": "1976-05-28"}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q2, true);
      assertEquals("1976-05-28", recordWithApiFieldName.getValueString("birthDate"));

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // past version didn't have noOfShoes field (they called it shoeCount) -- fail if it was sent //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "noOfShoes": 2}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2022_Q4, true))
         .isInstanceOf(QBadRequestException.class)
         .hasMessageContaining("unrecognized field name: noOfShoes");

      /////////////////////////////////////////////////////////////////////////
      // current version doesn't have cost field - fail if you send it to us //
      /////////////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "cost": 2}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q1, true))
         .isInstanceOf(QBadRequestException.class)
         .hasMessageContaining("unrecognized field name: cost");

      /////////////////////////////////
      // excluded field always fails //
      /////////////////////////////////
      for(String version : List.of(TestUtils.V2022_Q4, TestUtils.V2023_Q1, TestUtils.V2023_Q2))
      {
         assertThatThrownBy(() -> QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
            {"firstName": "Tim", "price": 2}
            """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, version, true))
            .isInstanceOf(QBadRequestException.class)
            .hasMessageContaining("unrecognized field name: price");
      }

      ////////////////////////////////////////////
      // assert non-editable fields are omitted //
      ////////////////////////////////////////////
      QRecord recordWithoutNonEditableFields = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "birthDay": "1976-05-28", "createDate": "2023-03-31T11:44:28Z", "id": 256}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q1, false);
      assertFalse(recordWithoutNonEditableFields.getValues().containsKey("createDate"));
      assertFalse(recordWithoutNonEditableFields.getValues().containsKey("id"));

      /////////////////////////////////////////////////////////////////////////
      // assert non-editable primary key fields IS included, if so requested //
      /////////////////////////////////////////////////////////////////////////
      QRecord recordWithoutNonEditablePrimaryKeyFields = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject("""
         {"firstName": "Tim", "birthDay": "1976-05-28", "createDate": "2023-03-31T11:44:28Z", "id": 256}
         """), TestUtils.TABLE_NAME_PERSON, TestUtils.API_NAME, TestUtils.V2023_Q1, true);
      assertFalse(recordWithoutNonEditablePrimaryKeyFields.getValues().containsKey("createDate"));
      assertEquals(256, recordWithoutNonEditablePrimaryKeyFields.getValues().get("id"));

   }

}