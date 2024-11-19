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


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.CsvFileToRows;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for WideRowsToRecord
 *******************************************************************************/
class WideRowsToRecordWithExplicitMappingTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderAndLinesWithoutDupes() throws QException
   {
      String csv = """
         orderNo, Ship To, lastName, SKU 1,     Quantity 1, SKU 2,     Quantity 2, SKU 3, Quantity 3
         1,       Homer,   Simpson,  DONUT,     12,         BEER,      500,        COUCH,  1
         2,       Ned,     Flanders, BIBLE,     7,          LAWNMOWER, 1
         """;

      CsvFileToRows   fileToRows = CsvFileToRows.forString(csv);
      BulkLoadFileRow header     = fileToRows.next();

      WideRowsToRecordWithExplicitMapping rowsToRecord = new WideRowsToRecordWithExplicitMapping();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To"
         ))
         .withWideLayoutMapping(Map.of(
            "orderLine", new BulkInsertWideLayoutMapping(List.of(
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 1", "quantity", "Quantity 1")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 2", "quantity", "Quantity 2")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 3", "quantity", "Quantity 3"))
            ))
         ))
         .withMappedAssociations(List.of("orderLine"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.WIDE)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(List.of("DONUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("12", "500", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("7", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderLinesAndOrderExtrinsicWithoutDupes() throws QException
   {
      String csv = """
         orderNo, Ship To, lastName, SKU 1, Quantity 1, SKU 2,     Quantity 2, SKU 3, Quantity 3, Extrinsic Key 1, Extrinsic Value 1, Extrinsic Key 2, Extrinsic Value 2
         1,       Homer,   Simpson,  DONUT, 12,         BEER,      500,        COUCH, 1,          Store Name,      QQQ Mart,          Coupon Code,     10QOff
         2,       Ned,     Flanders, BIBLE, 7,          LAWNMOWER, 1
         """;

      CsvFileToRows   fileToRows = CsvFileToRows.forString(csv);
      BulkLoadFileRow header     = fileToRows.next();

      WideRowsToRecordWithExplicitMapping rowsToRecord = new WideRowsToRecordWithExplicitMapping();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To"
         ))
         .withWideLayoutMapping(Map.of(
            "orderLine", new BulkInsertWideLayoutMapping(List.of(
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 1", "quantity", "Quantity 1")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 2", "quantity", "Quantity 2")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("sku", "SKU 3", "quantity", "Quantity 3"))
            )),
            "extrinsics", new BulkInsertWideLayoutMapping(List.of(
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Extrinsic Key 1", "value", "Extrinsic Value 1")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Extrinsic Key 2", "value", "Extrinsic Value 2"))
            ))
         ))
         .withMappedAssociations(List.of("orderLine", "extrinsics"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.WIDE)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(List.of("DONUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("12", "500", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of("Store Name", "Coupon Code"), getValues(order.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("QQQ Mart", "10QOff"), getValues(order.getAssociatedRecords().get("extrinsics"), "value"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("7", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertThat(order.getAssociatedRecords().get("extrinsics")).isNullOrEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderLinesWithLineExtrinsicsAndOrderExtrinsicWithoutDupes() throws QException
   {
      String csv = """
         orderNo, Ship To, lastName,  Extrinsic Key 1, Extrinsic Value 1, Extrinsic Key 2, Extrinsic Value 2, SKU 1, Quantity 1, Line Extrinsic Key 1.1, Line Extrinsic Value 1.1, Line Extrinsic Key 1.2, Line Extrinsic Value 1.2, SKU 2,     Quantity 2, Line Extrinsic Key 2.1, Line Extrinsic Value 2.1, SKU 3, Quantity 3, Line Extrinsic Key 3.1, Line Extrinsic Value 3.1, Line Extrinsic Key 3.2
         1,       Homer,   Simpson,   Store Name,      QQQ Mart,          Coupon Code,     10QOff,            DONUT, 12,         Flavor,                 Chocolate,                Size,                   Large,                    BEER,      500,        Flavor,                 Hops,                     COUCH, 1,          Color,                  Brown,                    foo,
         2,       Ned,     Flanders,  ,                ,                  ,                ,                  BIBLE, 7,          Flavor,                 King James,               Size,                   X-Large,                  LAWNMOWER, 1
         """;

      Integer defaultStoreId        = 42;
      Integer defaultLineNo         = 47;
      String  defaultLineExtraValue = "bar";

      CsvFileToRows   fileToRows = CsvFileToRows.forString(csv);
      BulkLoadFileRow header     = fileToRows.next();

      WideRowsToRecordWithExplicitMapping rowsToRecord = new WideRowsToRecordWithExplicitMapping();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To"
         ))
         .withMappedAssociations(List.of("orderLine", "extrinsics", "orderLine.extrinsics"))
         .withWideLayoutMapping(Map.of(
            "orderLine", new BulkInsertWideLayoutMapping(List.of(
               new BulkInsertWideLayoutMapping.ChildRecordMapping(
                  Map.of("sku", "SKU 1", "quantity", "Quantity 1"),
                  Map.of("extrinsics", new BulkInsertWideLayoutMapping(List.of(
                     new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 1.1", "value", "Line Extrinsic Value 1.1")),
                     new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 1.2", "value", "Line Extrinsic Value 1.2"))
                  )))),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(
                  Map.of("sku", "SKU 2", "quantity", "Quantity 2"),
                  Map.of("extrinsics", new BulkInsertWideLayoutMapping(List.of(
                     new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 2.1", "value", "Line Extrinsic Value 2.1")),
                     new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 2.2", "value", "Line Extrinsic Value 2.2"))
                  )))),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(
                  Map.of("sku", "SKU 3", "quantity", "Quantity 3"),
                  Map.of("extrinsics", new BulkInsertWideLayoutMapping(List.of(
                        new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 3.1", "value", "Line Extrinsic Value 3.1")),
                        new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Line Extrinsic Key 3.2", "value", "Line Extrinsic Value 3.2"))
                  ))))
            )),
            "extrinsics", new BulkInsertWideLayoutMapping(List.of(
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Extrinsic Key 1", "value", "Extrinsic Value 1")),
               new BulkInsertWideLayoutMapping.ChildRecordMapping(Map.of("key", "Extrinsic Key 2", "value", "Extrinsic Value 2"))
            ))
         ))

         .withFieldNameToValueMapping(Map.of("orderLine.extrinsics.value", Map.of("Large", "L", "X-Large", "XL")))
         .withFieldNameToDefaultValueMap(Map.of(
            "storeId", defaultStoreId,
            "orderLine.lineNumber", defaultLineNo,
            "orderLine.extrinsics.value", defaultLineExtraValue
         ))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.WIDE)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("DONUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("12", "500", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertEquals(List.of("Store Name", "Coupon Code"), getValues(order.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("QQQ Mart", "10QOff"), getValues(order.getAssociatedRecords().get("extrinsics"), "value"));

      QRecord lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor", "Size"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Chocolate", "L"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));

      lineItem = order.getAssociatedRecords().get("orderLine").get(1);
      assertEquals(List.of("Flavor"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Hops"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));

      lineItem = order.getAssociatedRecords().get("orderLine").get(2);
      assertEquals(List.of("Color", "foo"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Brown", defaultLineExtraValue), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of("7", "1"), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertThat(order.getAssociatedRecords().get("extrinsics")).isNullOrEmpty();

      lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor", "Size"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("King James", "XL"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<Serializable> getValues(List<QRecord> records, String fieldName)
   {
      return (records.stream().map(r -> r.getValue(fieldName)).toList());
   }

}