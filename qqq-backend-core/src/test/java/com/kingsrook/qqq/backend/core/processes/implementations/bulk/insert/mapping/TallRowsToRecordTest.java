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
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for TallRowsToRecord 
 *******************************************************************************/
class TallRowsToRecordTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderAndLines() throws QException
   {
      CsvFileToRows fileToRows = CsvFileToRows.forString("""
         orderNo, Ship To, lastName, SKU,       Quantity
         1,       Homer,   Simpson,  DONUT,     12
         ,        Homer,   Simpson,  BEER,      500
         ,        Homer,   Simpson,  COUCH,     1
         2,       Ned,     Flanders, BIBLE,     7
         ,        Ned,     Flanders, LAWNMOWER, 1
         """);

      BulkLoadFileRow header = fileToRows.next();

      TallRowsToRecord rowsToRecord = new TallRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To",
            "orderLine.sku", "SKU",
            "orderLine.quantity", "Quantity"
         ))
         .withTallLayoutGroupByIndexMap(Map.of(
            TestUtils.TABLE_NAME_ORDER, List.of(1, 2),
            "orderLine", List.of(3)
         ))
         .withMappedAssociations(List.of("orderLine"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.TALL)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(3, order.getAssociatedRecords().get("orderLine").size());
      assertEquals(List.of("DONUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(12, 500, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(2, order.getAssociatedRecords().get("orderLine").size());
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(7, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderLinesAndOrderExtrinsic() throws QException
   {
      CsvFileToRows fileToRows = CsvFileToRows.forString("""
         orderNo, Ship To, lastName, SKU,       Quantity, Extrinsic Key, Extrinsic Value
         1,       Homer,   Simpson,  DONUT,     12,       Store Name,    QQQ Mart
         1,       ,        ,         BEER,      500,      Coupon Code,   10QOff
         1,       ,        ,         COUCH,     1
         2,       Ned,     Flanders, BIBLE,     7
         2,       ,        ,         LAWNMOWER, 1
         """);

      BulkLoadFileRow header = fileToRows.next();

      TallRowsToRecord rowsToRecord = new TallRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To",
            "orderLine.sku", "SKU",
            "orderLine.quantity", "Quantity",
            "extrinsics.key", "Extrinsic Key",
            "extrinsics.value", "Extrinsic Value"
         ))
         .withTallLayoutGroupByIndexMap(Map.of(
            TestUtils.TABLE_NAME_ORDER, List.of(0),
            "orderLine", List.of(3),
            "extrinsics", List.of(5)
         ))
         .withMappedAssociations(List.of("orderLine", "extrinsics"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.TALL)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(3, order.getAssociatedRecords().get("orderLine").size());
      assertEquals(List.of("DONUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(12, 500, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(2, order.getAssociatedRecords().get("extrinsics").size());
      assertEquals(List.of("Store Name", "Coupon Code"), getValues(order.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("QQQ Mart", "10QOff"), getValues(order.getAssociatedRecords().get("extrinsics"), "value"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(2, order.getAssociatedRecords().get("orderLine").size());
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(7, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertThat(order.getAssociatedRecords().get("extrinsics")).isNullOrEmpty();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOrderLinesWithLineExtrinsicsAndOrderExtrinsic() throws QException
   {
      Integer defaultStoreId              = 101;
      String  defaultLineNo               = "102";
      String  defaultOrderLineExtraSource = "file";

      CsvFileToRows fileToRows = CsvFileToRows.forString("""
         orderNo, Ship To, lastName, SKU,       Quantity, Extrinsic Key, Extrinsic Value, Line Extrinsic Key, Line Extrinsic Value
         1,       Homer,   Simpson,  DONUT,     12,       Store Name,    QQQ Mart,        Flavor,             Chocolate
         1,       ,        ,         DONUT,     ,         Coupon Code,   10QOff,          Size,               Large
         1,       ,        ,         BEER,      500,      ,              ,                Flavor,             Hops
         1,       ,        ,         COUCH,     1
         2,       Ned,     Flanders, BIBLE,     7,        ,              ,                Flavor,             King James
         2,       ,        ,         LAWNMOWER, 1
         """);

      BulkLoadFileRow header = fileToRows.next();

      TallRowsToRecord rowsToRecord = new TallRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To",
            "orderLine.sku", "SKU",
            "orderLine.quantity", "Quantity",
            "extrinsics.key", "Extrinsic Key",
            "extrinsics.value", "Extrinsic Value",
            "orderLine.extrinsics.key", "Line Extrinsic Key",
            "orderLine.extrinsics.value", "Line Extrinsic Value"
         ))
         .withFieldNameToDefaultValueMap(Map.of(
            "storeId", defaultStoreId,
            "orderLine.lineNumber", defaultLineNo,
            "orderLine.extrinsics.source", defaultOrderLineExtraSource
         ))
         .withFieldNameToValueMapping(Map.of("orderLine.sku", Map.of("DONUT", "D'OH-NUT")))
         .withTallLayoutGroupByIndexMap(Map.of(
            TestUtils.TABLE_NAME_ORDER, List.of(0),
            "orderLine", List.of(3),
            "extrinsics", List.of(5),
            "orderLine.extrinsics", List.of(7)
         ))
         .withMappedAssociations(List.of("orderLine", "extrinsics", "orderLine.extrinsics"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.TALL)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("D'OH-NUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(12, 500, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertEquals(List.of("Store Name", "Coupon Code"), getValues(order.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("QQQ Mart", "10QOff"), getValues(order.getAssociatedRecords().get("extrinsics"), "value"));

      QRecord lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor", "Size"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Chocolate", "Large"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));
      assertEquals(List.of(defaultOrderLineExtraSource, defaultOrderLineExtraSource), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "source"));

      lineItem = order.getAssociatedRecords().get("orderLine").get(1);
      assertEquals(List.of("Flavor"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Hops"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(7, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertThat(order.getAssociatedRecords().get("extrinsics")).isNullOrEmpty();

      lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("King James"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));
      assertEquals(List.of(defaultOrderLineExtraSource), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "source"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAutomaticGroupByAllIndexes() throws QException
   {
      Integer defaultStoreId              = 101;
      String  defaultLineNo               = "102";
      String  defaultOrderLineExtraSource = "file";

      CsvFileToRows fileToRows = CsvFileToRows.forString("""
         orderNo, Ship To, lastName, SKU,       Quantity, Extrinsic Key, Extrinsic Value, Line Extrinsic Key, Line Extrinsic Value
         1,       Homer,   Simpson,  DONUT,     12,       Store Name,    QQQ Mart,        Flavor,             Chocolate
         1,       Homer,   Simpson,  DONUT,     12,       Coupon Code,   10QOff,          Size,               Large
         1,       Homer,   Simpson,  BEER,      500,      ,              ,                Flavor,             Hops
         1,       Homer,   Simpson,  COUCH,     1
         2,       Ned,     Flanders, BIBLE,     7,        ,              ,                Flavor,             King James
         2,       Ned,     Flanders, LAWNMOWER, 1
         """);

      BulkLoadFileRow header = fileToRows.next();

      TallRowsToRecord rowsToRecord = new TallRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "orderNo", "orderNo",
            "shipToName", "Ship To",
            "orderLine.sku", "SKU",
            "orderLine.quantity", "Quantity",
            "extrinsics.key", "Extrinsic Key",
            "extrinsics.value", "Extrinsic Value",
            "orderLine.extrinsics.key", "Line Extrinsic Key",
            "orderLine.extrinsics.value", "Line Extrinsic Value"
         ))
         .withFieldNameToDefaultValueMap(Map.of(
            "storeId", defaultStoreId,
            "orderLine.lineNumber", defaultLineNo,
            "orderLine.extrinsics.source", defaultOrderLineExtraSource
         ))
         .withFieldNameToValueMapping(Map.of("orderLine.sku", Map.of("DONUT", "D'OH-NUT")))
         .withMappedAssociations(List.of("orderLine", "extrinsics", "orderLine.extrinsics"))
         .withTableName(TestUtils.TABLE_NAME_ORDER)
         .withLayout(BulkInsertMapping.Layout.TALL)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(2, records.size());

      QRecord order = records.get(0);
      assertEquals(1, order.getValueInteger("orderNo"));
      assertEquals("Homer", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("D'OH-NUT", "BEER", "COUCH"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(12, 500, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertEquals(List.of("Store Name", "Coupon Code"), getValues(order.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("QQQ Mart", "10QOff"), getValues(order.getAssociatedRecords().get("extrinsics"), "value"));

      QRecord lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor", "Size"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Chocolate", "Large"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));
      assertEquals(List.of(defaultOrderLineExtraSource, defaultOrderLineExtraSource), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "source"));

      lineItem = order.getAssociatedRecords().get("orderLine").get(1);
      assertEquals(List.of("Flavor"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("Hops"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));

      order = records.get(1);
      assertEquals(2, order.getValueInteger("orderNo"));
      assertEquals("Ned", order.getValueString("shipToName"));
      assertEquals(defaultStoreId, order.getValue("storeId"));
      assertEquals(List.of("BIBLE", "LAWNMOWER"), getValues(order.getAssociatedRecords().get("orderLine"), "sku"));
      assertEquals(List.of(7, 1), getValues(order.getAssociatedRecords().get("orderLine"), "quantity"));
      assertEquals(List.of(defaultLineNo, defaultLineNo), getValues(order.getAssociatedRecords().get("orderLine"), "lineNumber"));
      assertThat(order.getAssociatedRecords().get("extrinsics")).isNullOrEmpty();

      lineItem = order.getAssociatedRecords().get("orderLine").get(0);
      assertEquals(List.of("Flavor"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "key"));
      assertEquals(List.of("King James"), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "value"));
      assertEquals(List.of(defaultOrderLineExtraSource), getValues(lineItem.getAssociatedRecords().get("extrinsics"), "source"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testShouldProcessAssociation()
   {
      TallRowsToRecord tallRowsToRecord = new TallRowsToRecord();
      assertTrue(tallRowsToRecord.shouldProcessAssociation(null, "foo"));
      assertTrue(tallRowsToRecord.shouldProcessAssociation("", "foo"));
      assertTrue(tallRowsToRecord.shouldProcessAssociation("foo", "foo.bar"));
      assertTrue(tallRowsToRecord.shouldProcessAssociation("foo.bar", "foo.bar.baz"));

      assertFalse(tallRowsToRecord.shouldProcessAssociation(null, "foo.bar"));
      assertFalse(tallRowsToRecord.shouldProcessAssociation("", "foo.bar"));
      assertFalse(tallRowsToRecord.shouldProcessAssociation("fiz", "foo.bar"));
      assertFalse(tallRowsToRecord.shouldProcessAssociation("fiz.biz", "foo.bar"));
      assertFalse(tallRowsToRecord.shouldProcessAssociation("foo", "foo.bar.baz"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<Serializable> getValues(List<QRecord> records, String fieldName)
   {
      return (records.stream().map(r -> r.getValue(fieldName)).toList());
   }

}