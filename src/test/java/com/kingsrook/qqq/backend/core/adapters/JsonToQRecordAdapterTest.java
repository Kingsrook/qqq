/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.adapters;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for JsonToQRecordAdapter
 **
 *******************************************************************************/
class JsonToQRecordAdapterTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_nullInput()
   {
      testExpectedToThrow(null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_emptyStringInput()
   {
      testExpectedToThrow("");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_inputDoesntLookLikeJson()
   {
      testExpectedToThrow("<HTML>");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_inputLooksLikeJsonButIsMalformed()
   {
      testExpectedToThrow("{json=not}");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void testExpectedToThrow(String json)
   {
      try
      {
         JsonToQRecordAdapter jsonToQRecordAdapter = new JsonToQRecordAdapter();
         List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson(json, TestUtils.defineTablePerson(), null);
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
   public void test_buildRecordsFromJson_emptyList()
   {
      JsonToQRecordAdapter jsonToQRecordAdapter = new JsonToQRecordAdapter();
      List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson("[]", TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertTrue(qRecords.isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_inputObject()
   {
      JsonToQRecordAdapter jsonToQRecordAdapter = new JsonToQRecordAdapter();
      List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson("""
         {
            "firstName":"Joe",
            "lastName":"Dimagio"
         }
         """, TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertEquals(1, qRecords.size());
      assertEquals("Joe", qRecords.get(0).getValue("firstName"));
      assertEquals("Dimagio", qRecords.get(0).getValue("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_inputList()
   {
      JsonToQRecordAdapter jsonToQRecordAdapter = new JsonToQRecordAdapter();
      List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson("""
         [
            { "firstName":"Tyler", "lastName":"Samples" },
            { "firstName":"Tim", "lastName":"Chamberlain" }
         ]
         """, TestUtils.defineTablePerson(), null);
      assertNotNull(qRecords);
      assertEquals(2, qRecords.size());
      assertEquals("Tyler", qRecords.get(0).getValue("firstName"));
      assertEquals("Samples", qRecords.get(0).getValue("lastName"));
      assertEquals("Tim", qRecords.get(1).getValue("firstName"));
      assertEquals("Chamberlain", qRecords.get(1).getValue("lastName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildRecordsFromJson_inputListWithNonObjectMembers()
   {
      testExpectedToThrow("[ 1701 ]");
   }

}
