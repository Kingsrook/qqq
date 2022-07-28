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

package com.kingsrook.qqq.backend.core.adapters;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QIndexBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for CsvToQRecordAdapter
 **
 *******************************************************************************/
class CsvToQRecordAdapterTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_nullInput()
   {
      testExpectedToThrow(null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_emptyStringInput()
   {
      testExpectedToThrow("");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void testExpectedToThrow(String csv)
   {
      try
      {
         CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
         List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(csv, TestUtils.defineTablePerson(), null);
         System.out.println(qRecords);
      }
      catch(IllegalArgumentException iae)
      {
         System.out.println("Threw expected exception");
         return;
      }

      fail("Didn't throw expected exception");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_emptyList()
   {
      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(getPersonCsvHeader(), TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertTrue(qRecords.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getPersonCsvHeader()
   {
      return ("""
         "id","createDate","modifyDate","firstName","lastName","birthDate","email"\r
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getPersonCsvRow2()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","Jane","Doe","1981-01-01","john@doe.com"\r
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getPersonCsvRow1()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1980-01-01","john@doe.com"\r
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_oneRowStandardHeaderNoMapping()
   {
      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(getPersonCsvHeader() + getPersonCsvRow1(), TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertEquals(1, qRecords.size());
      QRecord qRecord = qRecords.get(0);
      assertEquals("John", qRecord.getValue("firstName"));
      assertEquals("1980-01-01", qRecord.getValue("birthDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_twoRowsStandardHeaderNoMapping()
   {
      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(getPersonCsvHeader() + getPersonCsvRow1() + getPersonCsvRow2(), TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertEquals(2, qRecords.size());
      QRecord qRecord1 = qRecords.get(0);
      assertEquals("John", qRecord1.getValue("firstName"));
      assertEquals("1980-01-01", qRecord1.getValue("birthDate"));
      QRecord qRecord2 = qRecords.get(1);
      assertEquals("Jane", qRecord2.getValue("firstName"));
      assertEquals("1981-01-01", qRecord2.getValue("birthDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_oneRowCustomKeyBasedMapping()
   {
      String csvCustomHeader = """
         "id","created","modified","first","last","birthday","email"\r
         """;

      QKeyBasedFieldMapping mapping = new QKeyBasedFieldMapping()
         .withMapping("id", "id")
         .withMapping("createDate", "created")
         .withMapping("modifyDate", "modified")
         .withMapping("firstName", "first")
         .withMapping("lastName", "last")
         .withMapping("birthDate", "birthday")
         .withMapping("email", "email");

      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(csvCustomHeader + getPersonCsvRow1(), TestUtils.defineTablePerson(), mapping);
      assertNotNull(qRecords);
      assertEquals(1, qRecords.size());
      QRecord qRecord = qRecords.get(0);
      assertEquals("John", qRecord.getValue("firstName"));
      assertEquals("1980-01-01", qRecord.getValue("birthDate"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromCsv_twoRowsCustomIndexBasedMapping()
   {
      int index = 1;
      QIndexBasedFieldMapping mapping = new QIndexBasedFieldMapping()
         .withMapping("id", index++)
         .withMapping("createDate", index++)
         .withMapping("modifyDate", index++)
         .withMapping("firstName", index++)
         .withMapping("lastName", index++)
         .withMapping("birthDate", index++)
         .withMapping("email", index++);

      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv(getPersonCsvRow1() + getPersonCsvRow2(), TestUtils.defineTablePerson(), mapping);
      assertNotNull(qRecords);
      assertEquals(2, qRecords.size());
      QRecord qRecord1 = qRecords.get(0);
      assertEquals("John", qRecord1.getValue("firstName"));
      assertEquals("1980-01-01", qRecord1.getValue("birthDate"));
      QRecord qRecord2 = qRecords.get(1);
      assertEquals("Jane", qRecord2.getValue("firstName"));
      assertEquals("1981-01-01", qRecord2.getValue("birthDate"));
   }



   /*******************************************************************************
    ** In this test - we've got CSV data with duplicated header names.
    ** In our mapping, we're seeing the suffixes of " 2" and " 3" addd to those
    ** header names on the RHS.
    *******************************************************************************/
   @Test
   public void test_duplicatedColumnHeaders()
   {
      QKeyBasedFieldMapping mapping = new QKeyBasedFieldMapping()
         .withMapping("id", "id")
         .withMapping("createDate", "date")
         .withMapping("modifyDate", "date 2")
         .withMapping("firstName", "name")
         .withMapping("lastName", "name 2")
         .withMapping("birthDate", "date 3")
         .withMapping("email", "email");

      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      List<QRecord> qRecords = csvToQRecordAdapter.buildRecordsFromCsv("""
         id,date,date,name,name,date,email
         1,2022-06-26,2022-06-26,John,Doe,1980-01-01,john@kingsrook.com
         """, TestUtils.defineTablePerson(), mapping);
      assertNotNull(qRecords);
      assertEquals(1, qRecords.size());
      QRecord qRecord1 = qRecords.get(0);
      assertEquals("John", qRecord1.getValue("firstName"));
      assertEquals("Doe", qRecord1.getValue("lastName"));
      assertEquals("1980-01-01", qRecord1.getValue("birthDate"));
      assertEquals("2022-06-26", qRecord1.getValue("createDate"));
      assertEquals("2022-06-26", qRecord1.getValue("modifyDate"));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testMakeHeadersUnique()
   {
      CsvToQRecordAdapter csvToQRecordAdapter = new CsvToQRecordAdapter();
      Assertions.assertEquals(List.of("A", "B", "C"), csvToQRecordAdapter.makeHeadersUnique(List.of("A", "B", "C")));
      Assertions.assertEquals(List.of("A", "B", "C", "C 2", "C 3"), csvToQRecordAdapter.makeHeadersUnique(List.of("A", "B", "C", "C", "C")));
      Assertions.assertEquals(List.of("C", "A", "C 2", "B", "C 3"), csvToQRecordAdapter.makeHeadersUnique(List.of("C", "A", "C", "B", "C")));
      Assertions.assertEquals(List.of("A", "B", "C", "C 2", "C 3"), csvToQRecordAdapter.makeHeadersUnique(List.of("A", "B", "C", "C 2", "C")));
      Assertions.assertEquals(List.of("A", "B", "C", "C 2", "C 3"), csvToQRecordAdapter.makeHeadersUnique(List.of("A", "B", "C", "C 2", "C 3")));
      // todo - this is what the method header comment means when it says we don't handle all cases well...
      //  Assertions.assertEquals(List.of("A", "B", "C", "C 2", "C 3"), csvToQRecordAdapter.makeHeadersUnique(List.of("A", "B", "C 2", "C", "C 3")));
   }
}
