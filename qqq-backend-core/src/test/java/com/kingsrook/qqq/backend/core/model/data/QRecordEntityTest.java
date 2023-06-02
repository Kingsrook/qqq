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

package com.kingsrook.qqq.backend.core.model.data;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.testentities.Item;
import com.kingsrook.qqq.backend.core.model.data.testentities.ItemWithPrimitives;
import com.kingsrook.qqq.backend.core.model.data.testentities.LineItem;
import com.kingsrook.qqq.backend.core.model.data.testentities.Order;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class QRecordEntityTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testItemToQRecord() throws QException
   {
      Item item = new Item();
      item.setSku("ABC-123");
      item.setDescription("My Item");
      item.setQuantity(47);
      item.setPrice(new BigDecimal("3.50"));
      item.setFeatured(true);

      QRecord qRecord = item.toQRecord();
      assertEquals("ABC-123", qRecord.getValueString("sku"));
      assertEquals("My Item", qRecord.getValueString("description"));
      assertEquals(47, qRecord.getValueInteger("quantity"));
      assertEquals(new BigDecimal("3.50"), qRecord.getValueBigDecimal("price"));
      assertTrue(qRecord.getValueBoolean("featured"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordToItem() throws QException
   {
      QRecord qRecord = new QRecord()
         .withValue("sku", "WXYZ-9876")
         .withValue("description", "Items are cool")
         .withValue("quantity", 42)
         .withValue("price", new BigDecimal("3.50"))
         .withValue("featured", false);

      Item item = qRecord.toEntity(Item.class);
      assertEquals("WXYZ-9876", item.getSku());
      assertEquals("Items are cool", item.getDescription());
      assertEquals(42, item.getQuantity());
      assertEquals(new BigDecimal("3.50"), item.getPrice());
      assertFalse(item.getFeatured());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testItemWithPrimitivesToQRecord() throws QException
   {
      ItemWithPrimitives item = new ItemWithPrimitives();
      item.setSku("ABC-123");
      item.setDescription(null);
      item.setQuantity(47);
      item.setPrice(new BigDecimal("3.50"));
      item.setFeatured(true);

      QRecord qRecord = item.toQRecord();
      assertEquals("ABC-123", qRecord.getValueString("sku"));
      assertNull(qRecord.getValueString("description"));
      assertEquals(47, qRecord.getValueInteger("quantity"));
      assertEquals(new BigDecimal("3.50"), qRecord.getValueBigDecimal("price"));
      assertTrue(qRecord.getValueBoolean("featured"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordToItemWithPrimitives() throws QException
   {
      QRecord qRecord = new QRecord()
         .withValue("sku", "WXYZ-9876")
         .withValue("description", null)
         .withValue("quantity", 42)
         .withValue("price", new BigDecimal("3.50"))
         .withValue("featured", false);

      ItemWithPrimitives item = qRecord.toEntity(ItemWithPrimitives.class);
      assertEquals("WXYZ-9876", item.getSku());
      assertNull(item.getDescription());
      assertEquals(42, item.getQuantity());
      assertEquals(new BigDecimal("3.50"), item.getPrice());
      assertFalse(item.getFeatured());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordWithAllStringValuesToItem() throws QException
   {
      QRecord qRecord = new QRecord()
         .withValue("sku", "WXYZ-9876")
         .withValue("description", "Items are cool")
         .withValue("quantity", "42")
         .withValue("price", "3.50")
         .withValue("featured", "false");

      Item item = qRecord.toEntity(Item.class);
      assertEquals("WXYZ-9876", item.getSku());
      assertEquals("Items are cool", item.getDescription());
      assertEquals(42, item.getQuantity());
      assertEquals(new BigDecimal("3.50"), item.getPrice());
      assertFalse(item.getFeatured());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("ResultOfMethodCallIgnored")
   @Test
   void testQTableConstructionFromEntityGetterReferences() throws QException
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withField(new QFieldMetaData(Item::getSku))
         .withField(new QFieldMetaData(Item::getDescription))
         .withField(new QFieldMetaData(Item::getQuantity))
         .withField(new QFieldMetaData(Item::getFeatured))
         .withField(new QFieldMetaData(Item::getPrice));

      assertEquals(QFieldType.STRING, qTableMetaData.getField("sku").getType());
      assertEquals(QFieldType.INTEGER, qTableMetaData.getField("quantity").getType());

      ///////////////////////////////////////////////////////////////
      // assert about attributes that came from @QField annotation //
      ///////////////////////////////////////////////////////////////
      assertEquals("SKU", qTableMetaData.getField("sku").getLabel());
      assertEquals(DisplayFormat.COMMAS, qTableMetaData.getField("quantity").getDisplayFormat());
      assertTrue(qTableMetaData.getField("sku").getIsRequired());
      assertFalse(qTableMetaData.getField("quantity").getIsEditable());
      assertEquals("is_featured", qTableMetaData.getField("featured").getBackendName());

      //////////////////////////////////////////////////////////////////////////
      // assert about attributes that weren't specified in @QField annotation //
      //////////////////////////////////////////////////////////////////////////
      assertTrue(qTableMetaData.getField("sku").getIsEditable());
      assertFalse(qTableMetaData.getField("quantity").getIsRequired());
      assertNull(qTableMetaData.getField("sku").getBackendName());

      /////////////////////////////////////////////////////////////////////
      // assert about attributes for fields without a @QField annotation //
      /////////////////////////////////////////////////////////////////////
      assertTrue(qTableMetaData.getField("price").getIsEditable());
      assertFalse(qTableMetaData.getField("price").getIsRequired());
      assertNull(qTableMetaData.getField("price").getBackendName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQTableConstructionFromEntity() throws QException
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withFieldsFromEntity(Item.class);

      assertEquals(QFieldType.STRING, qTableMetaData.getField("sku").getType());
      assertEquals(QFieldType.INTEGER, qTableMetaData.getField("quantity").getType());

      ///////////////////////////////////////////////////////////////
      // assert about attributes that came from @QField annotation //
      ///////////////////////////////////////////////////////////////
      assertTrue(qTableMetaData.getField("sku").getIsRequired());
      assertFalse(qTableMetaData.getField("quantity").getIsEditable());
      assertEquals("is_featured", qTableMetaData.getField("featured").getBackendName());

      //////////////////////////////////////////////////////////////////////////
      // assert about attributes that weren't specified in @QField annotation //
      //////////////////////////////////////////////////////////////////////////
      assertTrue(qTableMetaData.getField("sku").getIsEditable());
      assertFalse(qTableMetaData.getField("quantity").getIsRequired());
      assertNull(qTableMetaData.getField("sku").getBackendName());

      /////////////////////////////////////////////////////////////////////
      // assert about attributes for fields without a @QField annotation //
      /////////////////////////////////////////////////////////////////////
      assertTrue(qTableMetaData.getField("price").getIsEditable());
      assertFalse(qTableMetaData.getField("price").getIsRequired());
      assertNull(qTableMetaData.getField("price").getBackendName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("ResultOfMethodCallIgnored")
   @Test
   void testQTableConstructionWithPrimitives() throws QException
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withField(new QFieldMetaData(ItemWithPrimitives::getSku))
         .withField(new QFieldMetaData(ItemWithPrimitives::getDescription))
         .withField(new QFieldMetaData(ItemWithPrimitives::getQuantity));

      assertEquals(QFieldType.STRING, qTableMetaData.getField("sku").getType());
      assertEquals(QFieldType.INTEGER, qTableMetaData.getField("quantity").getType());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderWithAssociationsToQRecord() throws QException
   {
      Order order = new Order();
      order.setOrderNo("ORD001");
      order.setLineItems(List.of(
         new LineItem().withSku("ABC").withQuantity(1),
         new LineItem().withSku("DEF").withQuantity(2)
      ));

      QRecord qRecord = order.toQRecord();
      assertEquals("ORD001", qRecord.getValueString("orderNo"));
      List<QRecord> lineItems = qRecord.getAssociatedRecords().get("lineItems");
      assertNotNull(lineItems);
      assertEquals(2, lineItems.size());
      assertEquals("ABC", lineItems.get(0).getValueString("sku"));
      assertEquals(1, lineItems.get(0).getValueInteger("quantity"));
      assertEquals("DEF", lineItems.get(1).getValueString("sku"));
      assertEquals(2, lineItems.get(1).getValueInteger("quantity"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderWithoutAssociationsToQRecord() throws QException
   {
      Order order = new Order();
      order.setOrderNo("ORD001");
      order.setLineItems(null);

      QRecord qRecord = order.toQRecord();
      assertEquals("ORD001", qRecord.getValueString("orderNo"));
      List<QRecord> lineItems = qRecord.getAssociatedRecords().get("lineItems");
      assertNull(lineItems);

      order.setLineItems(new ArrayList<>());
      qRecord = order.toQRecord();
      lineItems = qRecord.getAssociatedRecords().get("lineItems");
      assertNotNull(lineItems);
      assertEquals(0, lineItems.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordWithAssociationsToOrder() throws QException
   {
      QRecord qRecord = new QRecord()
         .withValue("orderNo", "ORD002")
         .withAssociatedRecords("lineItems", List.of(
            new QRecord().withValue("sku", "AB12").withValue("quantity", 42),
            new QRecord().withValue("sku", "XY89").withValue("quantity", 47)
         ));

      Order order = qRecord.toEntity(Order.class);
      assertEquals("ORD002", order.getOrderNo());
      assertEquals(2, order.getLineItems().size());
      assertEquals("AB12", order.getLineItems().get(0).getSku());
      assertEquals(42, order.getLineItems().get(0).getQuantity());
      assertEquals("XY89", order.getLineItems().get(1).getSku());
      assertEquals(47, order.getLineItems().get(1).getQuantity());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQRecordWithoutAssociationsToOrder() throws QException
   {
      QRecord qRecord = new QRecord().withValue("orderNo", "ORD002");
      Order   order   = qRecord.toEntity(Order.class);
      assertEquals("ORD002", order.getOrderNo());
      assertNull(order.getLineItems());

      qRecord.withAssociatedRecords("lineItems", null);
      order = qRecord.toEntity(Order.class);
      assertNull(order.getLineItems());

      qRecord.withAssociatedRecords("lineItems", new ArrayList<>());
      order = qRecord.toEntity(Order.class);
      assertNotNull(order.getLineItems());
      assertEquals(0, order.getLineItems().size());
   }

}