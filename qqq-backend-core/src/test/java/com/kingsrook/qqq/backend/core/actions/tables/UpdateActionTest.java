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


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for UpdateAction
 **
 *******************************************************************************/
class UpdateActionTest extends BaseTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      UpdateInput request = new UpdateInput();
      request.setTableName("person");
      List<QRecord> records = new ArrayList<>();
      QRecord       record  = new QRecord();
      record.setValue("id", "47");
      record.setValue("firstName", "James");
      records.add(record);
      request.setRecords(records);
      UpdateOutput result = new UpdateAction().execute(request);
      assertNotNull(result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsUpdateOneChild() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", "ORD123-b")
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 1).withValue("quantity", 17))
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 2))
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());
      assertEquals(17, orderLines.get(0).getValueInteger("quantity"));

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsUpdateOneGrandChild() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", "ORD123-b")
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 1)
               .withAssociatedRecord("extrinsics", new QRecord().withValue("id", 1).withValue("value", "LINE-VAL-1-updated")))
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 2))
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());
      assertEquals("LINE-VAL-1-updated", lineItemExtrinsics.get(0).getValueString("value"));

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteOneChild() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", "ORD123-b")
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 2))
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(2, orderLines.size());
      assertTrue(orderLines.stream().noneMatch(r -> r.getValueInteger("id").equals(1))); // id=1 should be deleted

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(2, lineItemExtrinsics.size()); // one was deleted (when its parent was deleted)

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteGrandchildren() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", "ORD123-b")
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 1))
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 2).withAssociatedRecords("extrinsics", new ArrayList<>()))
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(1, lineItemExtrinsics.size()); // deleted the two beneath line item id=2

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsInsertOneChild() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", "ORD123-b")
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 1))
            .withAssociatedRecord("orderLine", new QRecord().withValue("id", 2))
            .withAssociatedRecord("orderLine", new QRecord().withValue("sku", "BASIC4").withValue("quantity", 47))
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(4, orderLines.size());
      assertEquals("BASIC4", orderLines.get(3).getValueString("sku"));
      assertEquals(47, orderLines.get(3).getValueInteger("quantity"));

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteAllChildren() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations();

      //////////////////////////////////////////////////////////////////////
      // update the order's orderNo, and the quantity on one of the lines //
      //////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withAssociatedRecords("orderLine", new ArrayList<>()),
         new QRecord().withValue("id", 2).withAssociatedRecords("orderLine", new ArrayList<>())
      ));
      new UpdateAction().execute(updateInput);

      List<QRecord> orders = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(0, lineItemExtrinsics.size());

      List<QRecord> orderLines = TestUtils.queryTable(TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(0, orderLines.size()); // all of these got deleted too.

      List<QRecord> orderExtrinsics = TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void insert2OrdersWith3Lines3LineExtrinsicsAnd4OrderExtrinsicAssociations() throws QException
   {
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
            .withAssociatedRecord("orderLine", new QRecord().withValue("sku", "BASIC3").withValue("quantity", 3))
            .withAssociatedRecord("extrinsics", new QRecord().withValue("key", "YOUR-FIELD-1").withValue("value", "YOUR-VALUE-1"))
      ));
      new InsertAction().execute(insertInput);
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

      ///////////////////////////////////////////////////
      // insert records that we'll later try to update //
      ///////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("storeId", 999).withValue("orderNo", "ORD1"),
         new QRecord().withValue("id", 2).withValue("storeId", 999).withValue("orderNo", "ORD2"),
         new QRecord().withValue("id", 3).withValue("storeId", 999).withValue("orderNo", "ORD3"),
         new QRecord().withValue("id", 4).withValue("storeId", 999).withValue("orderNo", "ORD4")
      ));
      new InsertAction().execute(insertInput);

      //////////////////////////////////////////////////
      // do our update that we'll test the results of //
      //////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("orderNo", null),
         new QRecord().withValue("id", 2).withValue("total", new BigDecimal("3.50")),
         new QRecord().withValue("id", 3).withValue("orderNo", "ORD3B"),
         new QRecord().withValue("id", 4).withValue("orderNo", "   ")
      ));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);

      ////////////////////////////////////////////////////////////////
      // 1st record tried to set a null orderNo - assert it errored //
      ////////////////////////////////////////////////////////////////
      assertEquals(1, updateOutput.getRecords().get(0).getErrors().size());
      assertEquals("Missing value in required field: Order No", updateOutput.getRecords().get(0).getErrors().get(0));

      ////////////////////////////////////////////////////////////////
      // 2nd record didn't try to change orderNo, so should be fine //
      ////////////////////////////////////////////////////////////////
      assertEquals(0, updateOutput.getRecords().get(1).getErrors().size());

      ///////////////////////////////////////////////////////////////////
      // 3rd record should have actually set a new order no - no error //
      ///////////////////////////////////////////////////////////////////
      assertEquals(0, updateOutput.getRecords().get(2).getErrors().size());

      ///////////////////////////////////////////////////////////////////////
      // 4th record tried to set orderNo to all spaces - assert it errored //
      ///////////////////////////////////////////////////////////////////////
      assertEquals(1, updateOutput.getRecords().get(3).getErrors().size());
      assertEquals("Missing value in required field: Order No", updateOutput.getRecords().get(3).getErrors().get(0));
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
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
         assertEquals("You do not have permission to insert this record.", insertLineItemExtrinsicOutput.getRecords().get(0).getErrors().get(0));
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
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(2).getErrors().get(0));
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
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(4).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(5).getErrors().get(0));
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(6).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(7).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(7).getValueInteger("id"));
      }
   }
    */

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
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
         assertEquals("You do not have permission to insert this record.", insertLineItemOutput.getRecords().get(0).getErrors().get(0));
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
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(2).getErrors().get(0));
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
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(2).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(3).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(3).getValueInteger("id"));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(4).getErrors().get(0));
         assertEquals("You do not have permission to insert this record - the referenced Order was not found.", insertOutput.getRecords().get(5).getErrors().get(0));
         assertEquals("You do not have permission to insert this record.", insertOutput.getRecords().get(6).getErrors().get(0));
         assertEquals(0, insertOutput.getRecords().get(7).getErrors().size());
         assertNotNull(insertOutput.getRecords().get(7).getValueInteger("id"));
      }
   }
    */



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateNotFoundFails() throws QException
   {
      QContext.getQSession().withSecurityKeyValues(MapBuilder.of(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, ListBuilder.of(true)));

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(new QRecord().withValue("id", 999).withValue("orderNo", "updated")));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);
      assertEquals("No record was found to update for Id = 999", updateOutput.getRecords().get(0).getErrors().get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSecurityKeyValueDenied() throws QException
   {
      ////////////////////////////////
      // insert an order in store 1 //
      ////////////////////////////////
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      insertInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("orderNo", "original").withValue("storeId", 1)));
      new InsertAction().execute(insertInput);

      //////////////////////////////////////////////////////////////////////
      // now, as a session with store 2, try to update that store 1 order //
      // it should error as "not found"                                   //
      //////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(MapBuilder.of(TestUtils.SECURITY_KEY_TYPE_STORE, ListBuilder.of(2)));

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("orderNo", "updated")));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);
      assertEquals("No record was found to update for Id = 1", updateOutput.getRecords().get(0).getErrors().get(0));

      QContext.getQSession().withSecurityKeyValues(MapBuilder.of(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, ListBuilder.of(true)));
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER)).noneMatch(r -> r.getValueString("orderNo").equals("updated"));
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER)).anyMatch(r -> r.getValueString("orderNo").equals("original"));

      /////////////////////////////////////////////////////////////////////////////////
      // now, go back to store 1 in session, and try to change the order to store 2. //
      // that should fail, as you don't have permission to write to store 2.         //
      /////////////////////////////////////////////////////////////////////////////////
      QContext.getQSession().withSecurityKeyValues(MapBuilder.of(TestUtils.SECURITY_KEY_TYPE_STORE, ListBuilder.of(1)));

      updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("orderNo", "updated").withValue("storeId", 2)));
      updateOutput = new UpdateAction().execute(updateInput);
      assertEquals("You do not have permission to update a record with a value of 2 in the field: Store Id", updateOutput.getRecords().get(0).getErrors().get(0));

      QContext.getQSession().withSecurityKeyValues(MapBuilder.of(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, ListBuilder.of(true)));
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER)).noneMatch(r -> r.getValueString("orderNo").equals("updated"));
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER)).anyMatch(r -> r.getValueString("orderNo").equals("original"));
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
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
      assertEquals(0, TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER).size());
   }
   */

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
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
      assertEquals(1, TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER).size());
   }
   */

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
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
      assertEquals(2, TestUtils.queryTable(TestUtils.TABLE_NAME_ORDER).size());
   }
   */

}
