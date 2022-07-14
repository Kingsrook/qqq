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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.testentities.Item;
import com.kingsrook.qqq.backend.core.model.data.testentities.ItemWithPrimitives;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class QRecordEntityTest
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
   void testQTableConstructionFromEntity() throws QException
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withField(new QFieldMetaData(Item::getSku))
         .withField(new QFieldMetaData(Item::getDescription))
         .withField(new QFieldMetaData(Item::getQuantity));

      assertEquals(QFieldType.STRING, qTableMetaData.getField("sku").getType());
      assertEquals(QFieldType.INTEGER, qTableMetaData.getField("quantity").getType());
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

}