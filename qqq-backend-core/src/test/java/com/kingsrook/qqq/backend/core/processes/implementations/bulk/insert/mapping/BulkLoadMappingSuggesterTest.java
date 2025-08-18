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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfile;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadProfileField;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for BulkLoadMappingSuggester
 *******************************************************************************/
class BulkLoadMappingSuggesterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleFlat()
   {
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_PERSON_MEMORY);
      List<String>           headerRow      = List.of("Id", "First Name", "lastname", "email", "homestate");

      BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
      assertEquals("v1", bulkLoadProfile.getVersion());
      assertEquals("FLAT", bulkLoadProfile.getLayout());
      assertNull(getFieldByName(bulkLoadProfile, "id"));
      assertEquals(1, getFieldByName(bulkLoadProfile, "firstName").getColumnIndex());
      assertEquals(2, getFieldByName(bulkLoadProfile, "lastName").getColumnIndex());
      assertEquals(3, getFieldByName(bulkLoadProfile, "email").getColumnIndex());
      assertEquals(4, getFieldByName(bulkLoadProfile, "homeStateId").getColumnIndex());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleTall()
   {
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
      List<String>           headerRow      = List.of("orderNo", "shipto name", "sku", "quantity");

      BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
      assertEquals("v1", bulkLoadProfile.getVersion());
      assertEquals("TALL", bulkLoadProfile.getLayout());
      assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
      assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
      assertEquals(2, getFieldByName(bulkLoadProfile, "orderLine.sku").getColumnIndex());
      assertEquals(3, getFieldByName(bulkLoadProfile, "orderLine.quantity").getColumnIndex());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTallWithTableNamesOnAssociations()
   {
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
      List<String>           headerRow      = List.of("Order No", "Ship To Name", "Order Line: SKU", "Order Line: Quantity");

      BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
      assertEquals("v1", bulkLoadProfile.getVersion());
      assertEquals("TALL", bulkLoadProfile.getLayout());
      assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
      assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
      assertEquals(2, getFieldByName(bulkLoadProfile, "orderLine.sku").getColumnIndex());
      assertEquals(3, getFieldByName(bulkLoadProfile, "orderLine.quantity").getColumnIndex());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testChallengingAddress1And2()
   {
      try
      {
         {
            QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_ORDER);
            table.addField(new QFieldMetaData("address1", QFieldType.STRING));
            table.addField(new QFieldMetaData("address2", QFieldType.STRING));

            BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
            List<String>           headerRow      = List.of("orderNo", "ship to name", "address 1", "address 2");

            BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
            assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
            assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
            assertEquals(2, getFieldByName(bulkLoadProfile, "address1").getColumnIndex());
            assertEquals(3, getFieldByName(bulkLoadProfile, "address2").getColumnIndex());
            reInitInstanceInContext(TestUtils.defineInstance());
         }

         {
            QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_ORDER);
            table.addField(new QFieldMetaData("address", QFieldType.STRING));
            table.addField(new QFieldMetaData("address2", QFieldType.STRING));

            BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
            List<String>           headerRow      = List.of("orderNo", "ship to name", "address 1", "address 2");

            BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
            assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
            assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
            assertEquals(2, getFieldByName(bulkLoadProfile, "address").getColumnIndex());
            assertEquals(3, getFieldByName(bulkLoadProfile, "address2").getColumnIndex());
            reInitInstanceInContext(TestUtils.defineInstance());
         }

         {
            QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_ORDER);
            table.addField(new QFieldMetaData("address1", QFieldType.STRING));
            table.addField(new QFieldMetaData("address2", QFieldType.STRING));

            BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
            List<String>           headerRow      = List.of("orderNo", "ship to name", "address", "address 2");

            BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
            assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
            assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
            assertEquals(2, getFieldByName(bulkLoadProfile, "address1").getColumnIndex());
            assertEquals(3, getFieldByName(bulkLoadProfile, "address2").getColumnIndex());
            reInitInstanceInContext(TestUtils.defineInstance());
         }
      }
      finally
      {
         reInitInstanceInContext(TestUtils.defineInstance());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleWide()
   {
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER);
      List<String>           headerRow      = List.of("orderNo", "ship to name", "sku", "quantity1", "sku 2", "quantity 2");

      BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, false);
      assertEquals("v1", bulkLoadProfile.getVersion());
      assertEquals("WIDE", bulkLoadProfile.getLayout());
      assertEquals(0, getFieldByName(bulkLoadProfile, "orderNo").getColumnIndex());
      assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());
      assertEquals(2, getFieldByName(bulkLoadProfile, "orderLine.sku,0").getColumnIndex());
      assertEquals(3, getFieldByName(bulkLoadProfile, "orderLine.quantity,0").getColumnIndex());
      assertEquals(4, getFieldByName(bulkLoadProfile, "orderLine.sku,1").getColumnIndex());
      assertEquals(5, getFieldByName(bulkLoadProfile, "orderLine.quantity,1").getColumnIndex());

      /////////////////////////////////////////////////////////////////
      // assert that the order of fields matches the file's ordering //
      /////////////////////////////////////////////////////////////////
      assertEquals(List.of("orderNo", "shipToName", "orderLine.sku,0", "orderLine.quantity,0", "orderLine.sku,1", "orderLine.quantity,1"),
         bulkLoadProfile.getFieldList().stream().map(f -> f.getFieldName()).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testIdMatchedChildrenForBulkEditOriginally()
   {
      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(TestUtils.TABLE_NAME_ORDER, true);
      List<String>           headerRow      = List.of("id", "ship to name");

      BulkLoadProfile bulkLoadProfile = new BulkLoadMappingSuggester().suggestBulkLoadMappingProfile(tableStructure, headerRow, true);
      assertEquals("v1", bulkLoadProfile.getVersion());
      assertEquals("FLAT", bulkLoadProfile.getLayout());
      assertEquals(2, bulkLoadProfile.getFieldList().size());
      assertEquals(0, getFieldByName(bulkLoadProfile, "id").getColumnIndex());
      assertEquals(1, getFieldByName(bulkLoadProfile, "shipToName").getColumnIndex());

      /////////////////////////////////////////////////////////////////
      // assert that the order of fields matches the file's ordering //
      /////////////////////////////////////////////////////////////////
      assertEquals(List.of("id", "shipToName"),
         bulkLoadProfile.getFieldList().stream().map(f -> f.getFieldName()).toList());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private BulkLoadProfileField getFieldByName(BulkLoadProfile bulkLoadProfile, String fieldName)
   {
      return (bulkLoadProfile.getFieldList().stream()
         .filter(f -> f.getFieldName().equals(fieldName))
         .findFirst().orElse(null));
   }

}
