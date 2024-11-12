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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.BulkLoadFileRow;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling.TestFileToRows;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for FlatRowsToRecord 
 *******************************************************************************/
class FlatRowsToRecordTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldNameToHeaderNameMapping() throws QException
   {
      TestFileToRows fileToRows = new TestFileToRows(List.of(
         new Serializable[] { "id", "firstName", "Last Name", "Ignore", "cost" },
         new Serializable[] { 1, "Homer", "Simpson", true, "three fifty" },
         new Serializable[] { 2, "Marge", "Simpson", false, "" },
         new Serializable[] { 3, "Bart", "Simpson", "A", "99.95" },
         new Serializable[] { 4, "Ned", "Flanders", 3.1, "one$" }
      ));

      BulkLoadFileRow header = fileToRows.next();

      FlatRowsToRecord rowsToRecord = new FlatRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToHeaderNameMap(Map.of(
            "firstName", "firstName",
            "lastName", "Last Name",
            "cost", "cost"
         ))
         .withFieldNameToDefaultValueMap(Map.of(
            "noOfShoes", 2
         ))
         .withFieldNameToValueMapping(Map.of("cost", Map.of("three fifty", new BigDecimal("3.50"), "one$", new BigDecimal("1.00"))))
         .withTableName(TestUtils.TABLE_NAME_PERSON)
         .withHasHeaderRow(true);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, 1);
      assertEquals(List.of("Homer"), getValues(records, "firstName"));
      assertEquals(List.of("Simpson"), getValues(records, "lastName"));
      assertEquals(List.of(2), getValues(records, "noOfShoes"));
      assertEquals(List.of(new BigDecimal("3.50")), getValues(records, "cost"));
      assertEquals(4, records.get(0).getValues().size()); // make sure no additional values were set

      records = rowsToRecord.nextPage(fileToRows, header, mapping, 2);
      assertEquals(List.of("Marge", "Bart"), getValues(records, "firstName"));
      assertEquals(List.of(2, 2), getValues(records, "noOfShoes"));
      assertEquals(ListBuilder.of("", "99.95"), getValues(records, "cost"));

      records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(List.of("Ned"), getValues(records, "firstName"));
      assertEquals(List.of(2), getValues(records, "noOfShoes"));
      assertEquals(ListBuilder.of(new BigDecimal("1.00")), getValues(records, "cost"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFieldNameToIndexMapping() throws QException
   {
      TestFileToRows fileToRows = new TestFileToRows(List.of(
         new Serializable[] { 1, "Homer", "Simpson", true },
         new Serializable[] { 2, "Marge", "Simpson", false },
         new Serializable[] { 3, "Bart", "Simpson", "A" },
         new Serializable[] { 4, "Ned", "Flanders", 3.1 }
      ));

      BulkLoadFileRow header = null;

      FlatRowsToRecord rowsToRecord = new FlatRowsToRecord();

      BulkInsertMapping mapping = new BulkInsertMapping()
         .withFieldNameToIndexMap(Map.of(
            "firstName", 1,
            "lastName", 2
         ))
         .withFieldNameToDefaultValueMap(Map.of(
            "noOfShoes", 2
         ))
         .withTableName(TestUtils.TABLE_NAME_PERSON)
         .withHasHeaderRow(false);

      List<QRecord> records = rowsToRecord.nextPage(fileToRows, header, mapping, 1);
      assertEquals(List.of("Homer"), getValues(records, "firstName"));
      assertEquals(List.of("Simpson"), getValues(records, "lastName"));
      assertEquals(List.of(2), getValues(records, "noOfShoes"));
      assertEquals(3, records.get(0).getValues().size()); // make sure no additional values were set

      records = rowsToRecord.nextPage(fileToRows, header, mapping, 2);
      assertEquals(List.of("Marge", "Bart"), getValues(records, "firstName"));
      assertEquals(List.of(2, 2), getValues(records, "noOfShoes"));

      records = rowsToRecord.nextPage(fileToRows, header, mapping, Integer.MAX_VALUE);
      assertEquals(List.of("Ned"), getValues(records, "firstName"));
      assertEquals(List.of(2), getValues(records, "noOfShoes"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<Serializable> getValues(List<QRecord> records, String fieldName)
   {
      return (records.stream().map(r -> r.getValue(fieldName)).toList());
   }

}