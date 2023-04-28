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

package com.kingsrook.qqq.api;


import java.time.LocalDate;
import java.util.List;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String MEMORY_BACKEND_NAME = "memory";

   public static final String TABLE_NAME_PERSON              = "person";
   public static final String TABLE_NAME_ORDER               = "order";
   public static final String TABLE_NAME_LINE_ITEM           = "orderLine";
   public static final String TABLE_NAME_LINE_ITEM_EXTRINSIC = "orderLineExtrinsic";
   public static final String TABLE_NAME_ORDER_EXTRINSIC     = "orderExtrinsic";

   public static final String API_NAME             = "test-api";
   public static final String ALTERNATIVE_API_NAME = "person-api";

   public static final String V2022_Q4 = "2022.Q4";
   public static final String V2023_Q1 = "2023.Q1";
   public static final String V2023_Q2 = "2023.Q2";

   public static final String CURRENT_API_VERSION = V2023_Q1;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();

      qInstance.addBackend(defineMemoryBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.addTable(defineTableOrder());
      qInstance.addTable(defineTableLineItem());
      qInstance.addTable(defineTableLineItemExtrinsic());
      qInstance.addTable(defineTableOrderExtrinsic());

      qInstance.addJoin(defineJoinOrderLineItem());
      qInstance.addJoin(defineJoinLineItemLineItemExtrinsic());
      qInstance.addJoin(defineJoinOrderOrderExtrinsic());

      qInstance.setAuthentication(new Auth0AuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS).withName("anonymous"));

      qInstance.withMiddlewareMetaData(new ApiInstanceMetaDataContainer()
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(API_NAME)
            .withPath("/api/")
            .withLabel("Test API")
            .withDescription("QQQ Test API")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V2022_Q4), new APIVersion(V2023_Q1)))
            .withPastVersions(List.of(new APIVersion(V2022_Q4)))
            .withFutureVersions(List.of(new APIVersion(V2023_Q2))))
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(ALTERNATIVE_API_NAME)
            .withPath("/person-api/")
            .withLabel("Person-Only API")
            .withDescription("QQQ Test API, that only has the Person table.")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V2022_Q4), new APIVersion(V2023_Q1)))
            .withPastVersions(List.of(new APIVersion(V2022_Q4)))
            .withFutureVersions(List.of(new APIVersion(V2023_Q2))))
      );

      return (qInstance);
   }



   /*******************************************************************************
    ** Define the in-memory backend used in standard tests
    *******************************************************************************/
   public static QBackendMetaData defineMemoryBackend()
   {
      return new QBackendMetaData()
         .withName(MEMORY_BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class);
   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      QTableMetaData table = new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("email"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         // .withField(new QFieldMetaData("homeStateId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_STATE))
         // .withField(new QFieldMetaData("favoriteShapeId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_SHAPE))
         // .withField(new QFieldMetaData("customValue", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_CUSTOM))
         .withField(new QFieldMetaData("noOfShoes", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS))
         .withField(new QFieldMetaData("cost", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("price", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // make some changes to this table in the "main" api (but leave it like the backend in the ALTERNATIVE_API_NAME) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      table.withMiddlewareMetaData(new ApiTableMetaDataContainer()
         .withApiTableMetaData(API_NAME, new ApiTableMetaData()
            .withInitialVersion(V2022_Q4)

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // in 2022.Q4, this table had a "shoeCount" field. but for the 2023.Q1 version, we renamed it to noOfShoes! //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            .withRemovedApiField(new QFieldMetaData("shoeCount", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS)
               .withMiddlewareMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME,
                  new ApiFieldMetaData().withFinalVersion(V2022_Q4).withReplacedByFieldName("noOfShoes"))))
         )
         .withApiTableMetaData(ALTERNATIVE_API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)));

      /////////////////////////////////////////////////////
      // change the name for this field for the main api //
      /////////////////////////////////////////////////////
      table.getField("birthDate").withMiddlewareMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withApiFieldName("birthDay")));

      ////////////////////////////////////////////////////////////////////////////////
      // See above - we renamed this field (in the backend) for the 2023_Q1 version //
      ////////////////////////////////////////////////////////////////////////////////
      table.getField("noOfShoes").withMiddlewareMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V2023_Q1)));

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // 2 new fields - one will appear in a future version of the API, the other is always excluded //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      table.getField("cost").withMiddlewareMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V2023_Q2)));
      table.getField("price").withMiddlewareMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withIsExcluded(true)));

      return (table);
   }



   /*******************************************************************************
    ** Define the order table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableOrder()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_ORDER)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withMiddlewareMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withAssociation(new Association().withName("orderLines").withAssociatedTableName(TABLE_NAME_LINE_ITEM).withJoinName("orderLineItem"))
         .withAssociation(new Association().withName("extrinsics").withAssociatedTableName(TABLE_NAME_ORDER_EXTRINSIC).withJoinName("orderOrderExtrinsic"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderNo", QFieldType.STRING))
         .withField(new QFieldMetaData("orderDate", QFieldType.DATE))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("total", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY));
   }



   /*******************************************************************************
    ** Define the lineItem table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableLineItem()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_LINE_ITEM)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withMiddlewareMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withAssociation(new Association().withName("extrinsics").withAssociatedTableName(TABLE_NAME_LINE_ITEM_EXTRINSIC).withJoinName("lineItemLineItemExtrinsic"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("lineNumber", QFieldType.STRING))
         .withField(new QFieldMetaData("sku", QFieldType.STRING))
         .withField(new QFieldMetaData("quantity", QFieldType.INTEGER));
   }



   /*******************************************************************************
    ** Define the lineItemExtrinsic table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableLineItemExtrinsic()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_LINE_ITEM_EXTRINSIC)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withMiddlewareMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("lineItemId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("key", QFieldType.STRING))
         .withField(new QFieldMetaData("value", QFieldType.STRING));
   }



   /*******************************************************************************
    ** Define the orderExtrinsic table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableOrderExtrinsic()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_ORDER_EXTRINSIC)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withMiddlewareMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("key", QFieldType.STRING))
         .withField(new QFieldMetaData("value", QFieldType.STRING));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinOrderLineItem()
   {
      return new QJoinMetaData()
         .withName("orderLineItem")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_LINE_ITEM)
         .withJoinOn(new JoinOn("id", "orderId"))
         .withOrderBy(new QFilterOrderBy("lineNumber"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinLineItemLineItemExtrinsic()
   {
      return new QJoinMetaData()
         .withName("lineItemLineItemExtrinsic")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_LINE_ITEM)
         .withRightTable(TABLE_NAME_LINE_ITEM_EXTRINSIC)
         .withJoinOn(new JoinOn("id", "lineItemId"))
         .withOrderBy(new QFilterOrderBy("key"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinOrderOrderExtrinsic()
   {
      return new QJoinMetaData()
         .withName("orderOrderExtrinsic")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_ORDER_EXTRINSIC)
         .withJoinOn(new JoinOn("id", "orderId"))
         .withOrderBy(new QFilterOrderBy("key"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertPersonRecord(Integer id, String firstName, String lastName) throws QException
   {
      insertPersonRecord(id, firstName, lastName, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertPersonRecord(Integer id, String firstName, String lastName, LocalDate birthDate) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      insertInput.setRecords(List.of(new QRecord().withValue("id", id).withValue("firstName", firstName).withValue("lastName", lastName).withValue("birthDate", birthDate)));
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertSimpsons() throws QException
   {
      insertPersonRecord(1, "Homer", "Simpson");
      insertPersonRecord(2, "Marge", "Simpson");
      insertPersonRecord(3, "Bart", "Simpson");
      insertPersonRecord(4, "Lisa", "Simpson");
      insertPersonRecord(5, "Maggie", "Simpson");
   }
}
