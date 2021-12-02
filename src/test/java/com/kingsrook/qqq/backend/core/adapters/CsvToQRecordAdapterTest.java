package com.kingsrook.qqq.backend.core.adapters;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.QIndexBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
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
   /* todo?
   @Test
   public void test_buildRecordsFromCsv_inputDoesntLookLikeCsv()
   {
      testExpectedToThrow("CSV");
   }
   */



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


}
