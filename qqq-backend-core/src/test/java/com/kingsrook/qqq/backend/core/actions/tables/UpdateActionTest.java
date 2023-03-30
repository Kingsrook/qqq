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
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
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
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());
      assertEquals(17, orderLines.get(0).getValueInteger("quantity"));

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsUpdateOneGrandChild() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());
      assertEquals("LINE-VAL-1-updated", lineItemExtrinsics.get(0).getValueString("value"));

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteOneChild() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(2, orderLines.size());
      assertTrue(orderLines.stream().noneMatch(r -> r.getValueInteger("id").equals(1))); // id=1 should be deleted

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(2, lineItemExtrinsics.size()); // one was deleted (when its parent was deleted)

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteGrandchildren() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(3, orderLines.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(1, lineItemExtrinsics.size()); // deleted the two beneath line item id=2

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsInsertOneChild() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());
      assertEquals("ORD123-b", orders.get(0).getValueString("orderNo"));

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(4, orderLines.size());
      assertEquals("BASIC4", orderLines.get(3).getValueString("sku"));
      assertEquals(47, orderLines.get(3).getValueInteger("quantity"));

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(3, lineItemExtrinsics.size());

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
      assertEquals(4, orderExtrinsics.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdateAssociationsDeleteAllChildren() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
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

      List<QRecord> orders = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER);
      assertEquals(2, orders.size());

      List<QRecord> lineItemExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
      assertEquals(0, lineItemExtrinsics.size());

      List<QRecord> orderLines = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_LINE_ITEM);
      assertEquals(0, orderLines.size()); // all of these got deleted too.

      List<QRecord> orderExtrinsics = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
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
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

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
}
