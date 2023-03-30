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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for InsertAction
 **
 *******************************************************************************/
class InsertActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      InsertInput request = new InsertInput();
      request.setTableName("person");
      List<QRecord> records = new ArrayList<>();
      QRecord       record  = new QRecord();
      record.setValue("firstName", "James");
      records.add(record);
      request.setRecords(records);
      InsertOutput result = new InsertAction().execute(request);
      assertNotNull(result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeysPreExisting() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());

      ///////////////////////////////////////////////////////
      // try to insert that person again - shouldn't work. //
      ///////////////////////////////////////////////////////
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      insertOutput = new InsertAction().execute(insertInput);
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertNull(insertOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(0).getErrors().size());
      assertThat(insertOutput.getRecords().get(0).getErrors().get(0)).contains("Another record already exists with this First Name and Last Name");

      //////////////////////////////////////////////////////////////////////////////////////////
      // try to insert that person again, with 2 others - the 2 should work, but the one fail //
      //////////////////////////////////////////////////////////////////////////////////////////
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Smith"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Trevor").withValue("lastName", "Kelkhoff")
      ));
      insertOutput = new InsertAction().execute(insertInput);
      assertEquals(3, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertNotNull(insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertNotNull(insertOutput.getRecords().get(2).getValueInteger("id"));
      assertEquals(0, insertOutput.getRecords().get(0).getErrors().size());
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertEquals(0, insertOutput.getRecords().get(2).getErrors().size());

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeysWithinBatch() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertThat(insertOutput.getRecords().get(1).getErrors().get(0)).contains("Another record already exists with this First Name and Last Name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSingleColumnUniqueKey() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_SHAPE)
         .withUniqueKey(new UniqueKey("name"));

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
      insertInput.setRecords(List.of(
         new QRecord().withValue("name", "Circle"),
         new QRecord().withValue("name", "Circle")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertThat(insertOutput.getRecords().get(1).getErrors().get(0)).contains("Another record already exists with this Name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSkippingUniqueKeys() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setSkipUniqueKeyCheck(true);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(2, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(2, insertOutput.getRecords().get(1).getValueInteger("id"));
      assertTrue(CollectionUtils.nullSafeIsEmpty(insertOutput.getRecords().get(1).getErrors()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertAssociations() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(
         new QRecord().withValue("storeId", 1).withValue("orderNo", "ORD123")

            .withAssociatedRecord("orderLine", new QRecord().withValue("sku", "BASIC1").withValue("quantity", 1)
               .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "LINE-EXT-1.1").withValue("value", "LINE-VAL-1")))

            .withAssociatedRecord("orderLine", new QRecord().withValue("sku", "BASIC2").withValue("quantity", 2)
               .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "LINE-EXT-2.1").withValue("value", "LINE-VAL-2"))
               .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "LINE-EXT-2.2").withValue("value", "LINE-VAL-3")))

            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "MY-FIELD-1").withValue("value", "MY-VALUE-1"))
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "MY-FIELD-2").withValue("value", "MY-VALUE-2"))
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "MY-FIELD-3").withValue("value", "MY-VALUE-3")),

         new QRecord().withValue("storeId", 1).withValue("orderNo", "ORD124")
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "YOUR-FIELD-1").withValue("value", "YOUR-VALUE-1"))
      ));
      new InsertAction().execute(insertInput);

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals(1, orders.get(0).getValueInteger("id"));
      assertEquals(2, orders.get(1).getValueInteger("id"));
      assertEquals("ORD123", orders.get(0).getValueString("orderNo"));
      assertEquals("ORD124", orders.get(1).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(2, orderLines.size());
      assertEquals(1, orderLines.get(0).getValueInteger("orderId"));
      assertEquals(1, orderLines.get(1).getValueInteger("orderId"));
      assertEquals("BASIC1", orderLines.get(0).getValueString("sku"));
      assertEquals("BASIC2", orderLines.get(1).getValueString("sku"));

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
      assertEquals(1, orderExtrinsics.get(0).getValueInteger("orderId"));
      assertEquals(1, orderExtrinsics.get(1).getValueInteger("orderId"));
      assertEquals(1, orderExtrinsics.get(2).getValueInteger("orderId"));
      assertEquals(2, orderExtrinsics.get(3).getValueInteger("orderId"));
      assertEquals("MY-FIELD-1", orderExtrinsics.get(0).getValueString("key"));
      assertEquals("MY-FIELD-2", orderExtrinsics.get(1).getValueString("key"));
      assertEquals("MY-FIELD-3", orderExtrinsics.get(2).getValueString("key"));
      assertEquals("YOUR-FIELD-1", orderExtrinsics.get(3).getValueString("key"));
      assertEquals("MY-VALUE-1", orderExtrinsics.get(0).getValueString("value"));
      assertEquals("MY-VALUE-2", orderExtrinsics.get(1).getValueString("value"));
      assertEquals("MY-VALUE-3", orderExtrinsics.get(2).getValueString("value"));
      assertEquals("YOUR-VALUE-1", orderExtrinsics.get(3).getValueString("value"));

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());
      assertEquals(1, lineItemExtrinsics.get(0).getValueInteger("lineItemId"));
      assertEquals(2, lineItemExtrinsics.get(1).getValueInteger("lineItemId"));
      assertEquals(2, lineItemExtrinsics.get(2).getValueInteger("lineItemId"));
      assertEquals("LINE-EXT-1.1", lineItemExtrinsics.get(0).getValueString("key"));
      assertEquals("LINE-EXT-2.1", lineItemExtrinsics.get(1).getValueString("key"));
      assertEquals("LINE-EXT-2.2", lineItemExtrinsics.get(2).getValueString("key"));
      assertEquals("LINE-VAL-1", lineItemExtrinsics.get(0).getValueString("value"));
      assertEquals("LINE-VAL-2", lineItemExtrinsics.get(1).getValueString("value"));
      assertEquals("LINE-VAL-3", lineItemExtrinsics.get(2).getValueString("value"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertMultiLevelSecurityJoins() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);

      //////////////////////////////////////////////////////////////////////////////////////
      // null value in the foreign key to the join-table that provides the security value //
      //////////////////////////////////////////////////////////////////////////////////////
      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertInput.setRecords(List.of(new QRecord().withValue("lineItemId", null).withValue("key", "kidsCanCallYou").withValue("value", "HoJu")));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // value in the foreign key to the join-table that provides the security value, but the referenced record isn't found //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertInput.setRecords(List.of(new QRecord().withValue("lineItemId", 1701).withValue("key", "kidsCanCallYou").withValue("value", "HoJu")));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
      }

      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // insert an order and lineItem with storeId=2 - then, reset our session to only have storeId=1 in it - and try to insert an order-line referencing that order. //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(2));
         InsertInput insertOrderInput = new InsertInput();
         insertOrderInput.setTableName(TestUtils.TABLE_NAME_ORDER);
         insertOrderInput.setRecords(List.of(new QRecord().withValue("id", 42).withValue("storeId", 2)));
         InsertOutput insertOrderOutput = new InsertAction().execute(insertOrderInput);
         assertEquals(42, insertOrderOutput.getRecords().get(0).getValueInteger("id"));

         InsertInput insertLineItemInput = new InsertInput();
         insertLineItemInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertLineItemInput.setRecords(List.of(new QRecord().withValue("id", 4200).withValue("orderId", 42).withValue("sku", "BASIC1").withValue("quantity", 24)));
         InsertOutput insertLineItemOutput = new InsertAction().execute(insertLineItemInput);
         assertEquals(4200, insertLineItemOutput.getRecords().get(0).getValueInteger("id"));

         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(1));
         InsertInput insertLineItemExtrinsicInput = new InsertInput();
         insertLineItemExtrinsicInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertLineItemExtrinsicInput.setRecords(List.of(new QRecord().withValue("lineItemId", 4200).withValue("key", "kidsCanCallYou").withValue("value", "HoJu")));
         InsertOutput insertLineItemExtrinsicOutput = new InsertAction().execute(insertLineItemExtrinsicInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertLineItemExtrinsicOutput.getRecords().get(0).getErrors().get(0));
      }

      {
         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(1));
         InsertInput insertOrderInput = new InsertInput();
         insertOrderInput.setTableName(TestUtils.TABLE_NAME_ORDER);
         insertOrderInput.setRecords(List.of(new QRecord().withValue("id", 47).withValue("storeId", 1)));
         InsertOutput insertOrderOutput = new InsertAction().execute(insertOrderInput);
         assertEquals(47, insertOrderOutput.getRecords().get(0).getValueInteger("id"));

         InsertInput insertLineItemInput = new InsertInput();
         insertLineItemInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertLineItemInput.setRecords(List.of(new QRecord().withValue("id", 4700).withValue("orderId", 47).withValue("sku", "BASIC1").withValue("quantity", 74)));
         InsertOutput insertLineItemOutput = new InsertAction().execute(insertLineItemInput);
         assertEquals(4700, insertLineItemOutput.getRecords().get(0).getValueInteger("id"));

         ///////////////////////////////////////////////////////
         // combine all the above, plus one record that works //
         ///////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertInput.setRecords(List.of(
            new QRecord().withValue("lineItemId", null).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 1701).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4200).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4700).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu")
         ));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(1).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
      }

      {
         /////////////////////////////////////////////////////////////////////////////////
         // one more time, but with multiple input records referencing each foreign key //
         /////////////////////////////////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertInput.setRecords(List.of(
            new QRecord().withValue("lineItemId", null).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 1701).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4200).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4700).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", null).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 1701).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4200).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu"),
            new QRecord().withValue("lineItemId", 4700).withValue("key", "theKidsCanCallYou").withValue("value", "HoJu")
         ));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(1).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(4).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(5).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(6).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(7).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(7).getValueInteger("id"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInsertSingleLevelSecurityJoins() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);

      //////////////////////////////////////////////////////////////////////////////////////
      // null value in the foreign key to the join-table that provides the security value //
      //////////////////////////////////////////////////////////////////////////////////////
      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertInput.setRecords(List.of(new QRecord().withValue("orderId", null).withValue("sku", "BASIC1").withValue("quantity", 1)));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // value in the foreign key to the join-table that provides the security value, but the referenced record isn't found //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertInput.setRecords(List.of(new QRecord().withValue("orderId", 1701).withValue("sku", "BASIC1").withValue("quantity", 1)));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
      }
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // insert an order with storeId=2 - then, reset our session to only have storeId=1 in it - and try to insert an order-line referencing that order. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(2));
         InsertInput insertOrderInput = new InsertInput();
         insertOrderInput.setTableName(TestUtils.TABLE_NAME_ORDER);
         insertOrderInput.setRecords(List.of(new QRecord().withValue("id", 42).withValue("storeId", 2)));
         InsertOutput insertOrderOutput = new InsertAction().execute(insertOrderInput);
         assertEquals(42, insertOrderOutput.getRecords().get(0).getValueInteger("id"));

         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(1));
         InsertInput insertLineItemInput = new InsertInput();
         insertLineItemInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertLineItemInput.setRecords(List.of(new QRecord().withValue("orderId", 42).withValue("sku", "BASIC1").withValue("quantity", 1)));
         InsertOutput insertLineItemOutput = new InsertAction().execute(insertLineItemInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertLineItemOutput.getRecords().get(0).getErrors().get(0));
      }

      {
         QContext.getQSession().withSecurityKeyValues(new HashMap<>());
         QContext.getQSession().withSecurityKeyValues(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(1));
         InsertInput insertOrderInput = new InsertInput();
         insertOrderInput.setTableName(TestUtils.TABLE_NAME_ORDER);
         insertOrderInput.setRecords(List.of(new QRecord().withValue("id", 47).withValue("storeId", 1)));
         new InsertAction().execute(insertOrderInput);

         ///////////////////////////////////////////////////////
         // combine all the above, plus one record that works //
         ///////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertInput.setRecords(List.of(
            new QRecord().withValue("orderId", null).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 1701).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 42).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 47).withValue("sku", "BASIC1").withValue("quantity", 1)
         ));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(1).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
      }

      {
         /////////////////////////////////////////////////////////////////////////////////
         // one more time, but with multiple input records referencing each foreign key //
         /////////////////////////////////////////////////////////////////////////////////
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertInput.setRecords(List.of(
            new QRecord().withValue("orderId", null).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 1701).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 42).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 47).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", null).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 1701).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 42).withValue("sku", "BASIC1").withValue("quantity", 1),
            new QRecord().withValue("orderId", 47).withValue("sku", "BASIC1").withValue("quantity", 1)
         ));
         InsertOutput insertOutput = new InsertAction().execute(insertInput);
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(0).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(1).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(4).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(5).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(6).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(7).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(7).getValueInteger("id"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKeyValueDenied() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(new QRecord().withValue("storeId", 2)));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals("You do not have permission to insert a record with a value of 2 in the field: Store Id", insertOutput.getRecords().get(0).getErrors().get(0));
      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKeyNullDenied() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(new QRecord()));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals("You do not have permission to insert a record without a value in the field: Store Id", insertOutput.getRecords().get(0).getErrors().get(0));
      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKeyNullAllowed() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_ORDER).getRecordSecurityLocks().get(0).setNullValueBehavior(RecordSecurityLock.NullValueBehavior.ALLOW);
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(new QRecord()));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(0, insertOutput.getRecords().get(0).getErrors().size());
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKeyAllAccess() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_ORDER).getRecordSecurityLocks().get(0).setNullValueBehavior(RecordSecurityLock.NullValueBehavior.ALLOW);
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(
         new QRecord().withValue("storeId", 999),
         new QRecord().withValue("storeId", null)
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(2, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRequiredFields() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_ORDER).getField("orderNo").setIsRequired(true);
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(
         new QRecord().withValue("storeId", 999),
         new QRecord().withValue("storeId", 999).withValue("orderNo", "ORD1"),
         new QRecord().withValue("storeId", 999).withValue("orderNo", "  ")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      ////////////////////////////////////////////////////////////
      // 1st record had no value in orderNo - assert it errored //
      ////////////////////////////////////////////////////////////
      assertEquals(1, insertOutput.getRecords().get(0).getErrors().size());
      assertEquals("Missing value in required field: Order No", insertOutput.getRecords().get(0).getErrors().get(0));

      ///////////////////////////////////////////////
      // 2nd record had a value - it should insert //
      ///////////////////////////////////////////////
      assertEquals(0, insertOutput.getRecords().get(1).getErrors().size());
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER).size());

      //////////////////////////////////////////////////////////////////
      // 3rd record had spaces-only in orderNo - make sure that fails //
      //////////////////////////////////////////////////////////////////
      assertEquals(1, insertOutput.getRecords().get(2).getErrors().size());
      assertEquals("Missing value in required field: Order No", insertOutput.getRecords().get(2).getErrors().get(0));
   }

}
