/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for BulkInsertPrepareValueMappingStep 
 *******************************************************************************/
class BulkInsertPrepareValueMappingStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      assertEquals(TestUtils.TABLE_NAME_ORDER, BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderNo").table().getName());
      assertEquals("orderNo", BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderNo").field().getName());

      assertEquals(TestUtils.TABLE_NAME_LINE_ITEM, BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderLine.sku").table().getName());
      assertEquals("sku", BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderLine.sku").field().getName());

      assertEquals(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC, BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderLine.extrinsics.key").table().getName());
      assertEquals("key", BulkInsertPrepareValueMappingStep.getTableAndField(TestUtils.TABLE_NAME_ORDER, "orderLine.extrinsics.key").field().getName());

   }

}