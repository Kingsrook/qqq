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
import java.util.Map;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.api.model.actions.ApiFieldCustomValueMapper;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.tables.ApiAssociationMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ApiImplementation 
 *******************************************************************************/
class ApiImplementationTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExcludedAssociation() throws QException
   {
      QInstance           qInstance           = QContext.getQInstance();
      ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaDataContainer.of(qInstance).getApiInstanceMetaData(TestUtils.API_NAME);

      TestUtils.insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic();

      /////////////////////////////////////////////////
      // get the order - make sure it has extrinsics //
      /////////////////////////////////////////////////
      Map<String, Serializable> order = ApiImplementation.get(apiInstanceMetaData, TestUtils.CURRENT_API_VERSION, "order", "1");
      assertTrue(order.containsKey("extrinsics"));

      /////////////////////////////////////////////////////
      // turn off the extrinsics association for the api //
      /////////////////////////////////////////////////////
      QTableMetaData            table                     = qInstance.getTable(TestUtils.TABLE_NAME_ORDER);
      ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
      ApiTableMetaData          apiTableMetaData          = apiTableMetaDataContainer.getApiTableMetaData(TestUtils.API_NAME);
      apiTableMetaData.withApiAssociationMetaData("extrinsics", new ApiAssociationMetaData().withIsExcluded(true));

      /////////////////////////////////////////////////
      // re-fetch - should no longer have extrinsics //
      /////////////////////////////////////////////////
      order = ApiImplementation.get(apiInstanceMetaData, TestUtils.CURRENT_API_VERSION, "order", "1");
      assertFalse(order.containsKey("extrinsics"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAssociationVersions() throws QException
   {
      QInstance           qInstance           = QContext.getQInstance();
      ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaDataContainer.of(qInstance).getApiInstanceMetaData(TestUtils.API_NAME);

      TestUtils.insert1Order3Lines4LineExtrinsicsAnd1OrderExtrinsic();

      /////////////////////////////////////////////////
      // get the order - make sure it has extrinsics //
      /////////////////////////////////////////////////
      Map<String, Serializable> order = ApiImplementation.get(apiInstanceMetaData, TestUtils.CURRENT_API_VERSION, "order", "1");
      assertTrue(order.containsKey("extrinsics"));

      /////////////////////////////////////////////////
      // set the initial version for the association //
      /////////////////////////////////////////////////
      QTableMetaData            table                     = qInstance.getTable(TestUtils.TABLE_NAME_ORDER);
      ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
      ApiTableMetaData          apiTableMetaData          = apiTableMetaDataContainer.getApiTableMetaData(TestUtils.API_NAME);
      apiTableMetaData.withApiAssociationMetaData("extrinsics", new ApiAssociationMetaData().withInitialVersion(TestUtils.V2023_Q1));

      ////////////////////////////////////////////////////
      // re-fetch - should have or not based on version //
      ////////////////////////////////////////////////////
      assertFalse(ApiImplementation.get(apiInstanceMetaData, TestUtils.V2022_Q4, "order", "1").containsKey("extrinsics"));
      assertTrue(ApiImplementation.get(apiInstanceMetaData, TestUtils.V2023_Q1, "order", "1").containsKey("extrinsics"));

      /////////////////////////////////////////////////
      // set the final version for the association //
      /////////////////////////////////////////////////
      apiTableMetaData.withApiAssociationMetaData("extrinsics", new ApiAssociationMetaData().withInitialVersion(TestUtils.V2022_Q4).withFinalVersion(TestUtils.V2022_Q4));

      ////////////////////////////////////////////////////
      // re-fetch - should have or not based on version //
      ////////////////////////////////////////////////////
      assertTrue(ApiImplementation.get(apiInstanceMetaData, TestUtils.V2022_Q4, "order", "1").containsKey("extrinsics"));
      assertFalse(ApiImplementation.get(apiInstanceMetaData, TestUtils.V2023_Q1, "order", "1").containsKey("extrinsics"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValueCustomizer() throws QException
   {
      QInstance           qInstance           = QContext.getQInstance();
      ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaDataContainer.of(qInstance).getApiInstanceMetaData(TestUtils.API_NAME);
      TestUtils.insertSimpsons();

      ////////////////////////////////////////////////////////////////////
      // set up a custom value mapper on lastName field of person table //
      ////////////////////////////////////////////////////////////////////
      QTableMetaData table = qInstance.getTable(TestUtils.TABLE_NAME_PERSON);
      QFieldMetaData field = table.getField("lastName");
      field.withSupplementalMetaData(new ApiFieldMetaDataContainer()
         .withApiFieldMetaData(TestUtils.API_NAME, new ApiFieldMetaData()
            .withInitialVersion(TestUtils.V2022_Q4)
            .withCustomValueMapper(new QCodeReference(PersonLastNameApiValueCustomizer.class))));

      ////////////////////////////////////////////////
      // get a person - make sure custom method ran //
      ////////////////////////////////////////////////
      Map<String, Serializable> person = ApiImplementation.get(apiInstanceMetaData, TestUtils.CURRENT_API_VERSION, "person", "1");
      assertEquals("customValue-Simpson", person.get("lastName"));

      ////////////////////////////////////////////////////
      // insert a person - make sure custom method runs //
      ////////////////////////////////////////////////////
      ApiImplementation.insert(apiInstanceMetaData, TestUtils.CURRENT_API_VERSION, "person", """
         {"firstName": "Ned", "lastName": "stripThisAway-Flanders"}
         """);
      QRecord insertedPerson = new GetAction().executeForRecord(new GetInput(TestUtils.TABLE_NAME_PERSON).withPrimaryKey(6));
      assertEquals("Flanders", insertedPerson.getValueString("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PersonLastNameApiValueCustomizer extends ApiFieldCustomValueMapper
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public Serializable produceApiValue(QRecord record)
      {
         return ("customValue-" + record.getValueString("lastName"));
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void consumeApiValue(QRecord record, Object value, JSONObject fullApiJsonObject)
      {
         String valueString = ValueUtils.getValueAsString(value);
         valueString = valueString.replaceFirst("^stripThisAway-", "");
         record.setValue("lastName", valueString);
      }

   }

}