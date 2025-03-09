/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.time.LocalDate;
import java.time.Month;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QRecordWithJoinedRecords 
 *******************************************************************************/
class QRecordWithJoinedRecordsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      QRecord order = new QRecord().withValue("id", 1).withValue("orderNo", "101").withValue("orderDate", LocalDate.of(2025, Month.JANUARY, 1));
      QRecord lineItem = new QRecord().withValue("id", 2).withValue("sku", "ABC").withValue("quantity", 47);
      QRecord extrinsic = new QRecord().withValue("id", 3).withValue("key", "MyKey").withValue("value", "MyValue");

      QRecordWithJoinedRecords joinedRecords = new QRecordWithJoinedRecords(order);
      joinedRecords.addJoinedRecordValues("lineItem", lineItem);
      joinedRecords.addJoinedRecordValues("extrinsic", extrinsic);

      assertEquals(1, joinedRecords.getValue("id"));
      assertEquals("101", joinedRecords.getValue("orderNo"));
      assertEquals(LocalDate.of(2025, Month.JANUARY, 1), joinedRecords.getValue("orderDate"));
      assertEquals(2, joinedRecords.getValue("lineItem.id"));
      assertEquals("ABC", joinedRecords.getValue("lineItem.sku"));
      assertEquals(47, joinedRecords.getValue("lineItem.quantity"));
      assertEquals(3, joinedRecords.getValue("extrinsic.id"));
      assertEquals("MyKey", joinedRecords.getValue("extrinsic.key"));
      assertEquals("MyValue", joinedRecords.getValue("extrinsic.value"));

      assertEquals(9, joinedRecords.getValues().size());
      assertEquals(1, joinedRecords.getValues().get("id"));
      assertEquals(2, joinedRecords.getValues().get("lineItem.id"));
      assertEquals(3, joinedRecords.getValues().get("extrinsic.id"));

      joinedRecords.setValue("lineItem.color", "RED");
      assertEquals("RED", joinedRecords.getValue("lineItem.color"));
      assertEquals("RED", lineItem.getValue("color"));

      joinedRecords.setValue("shipToCity", "St. Louis");
      assertEquals("St. Louis", joinedRecords.getValue("shipToCity"));
      assertEquals("St. Louis", order.getValue("shipToCity"));
   }

}