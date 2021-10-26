package com.kingsrook.qqq.backend.core.adapters;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
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
         List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson(json);
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
      List<QRecord> qRecords = jsonToQRecordAdapter.buildRecordsFromJson("[]");
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
            "field1":"value1",
            "field2":"value2"
         }
         """);
      assertNotNull(qRecords);
      assertEquals(1, qRecords.size());
      assertEquals("value1", qRecords.get(0).getValue("field1"));
      assertEquals("value2", qRecords.get(0).getValue("field2"));
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
            { "field1":"value1", "field2":"value2" },
            { "fieldA":"valueA", "fieldB":"valueB" }
         ]
         """);
      assertNotNull(qRecords);
      assertEquals(2, qRecords.size());
      assertEquals("value1", qRecords.get(0).getValue("field1"));
      assertEquals("value2", qRecords.get(0).getValue("field2"));
      assertEquals("valueA", qRecords.get(1).getValue("fieldA"));
      assertEquals("valueB", qRecords.get(1).getValue("fieldB"));
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