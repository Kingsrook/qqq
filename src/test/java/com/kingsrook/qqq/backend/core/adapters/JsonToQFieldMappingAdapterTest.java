/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.adapters;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QIndexBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QKeyBasedFieldMapping;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for JsonToQFieldMappingAdapter
 **
 *******************************************************************************/
class JsonToQFieldMappingAdapterTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_nullInput()
   {
      testExpectedToThrow(null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_emptyStringInput()
   {
      testExpectedToThrow("");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_malformedJsonInput()
   {
      testExpectedToThrow("{foo=bar}");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_validKeyBasedInput()
   {
      JsonToQFieldMappingAdapter jsonToQFieldMappingAdapter = new JsonToQFieldMappingAdapter();
      AbstractQFieldMapping<String> mapping = (QKeyBasedFieldMapping) jsonToQFieldMappingAdapter.buildMappingFromJson("""
         {
            "Field1": "source1",
            "Field2": "source2",
         }
         """);
      System.out.println(mapping);
      assertNotNull(mapping);

      assertEquals("source1", mapping.getFieldSource("Field1"));
      assertEquals("source2", mapping.getFieldSource("Field2"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_validIndexBasedInput()
   {
      JsonToQFieldMappingAdapter jsonToQFieldMappingAdapter = new JsonToQFieldMappingAdapter();
      AbstractQFieldMapping<Integer> mapping = (QIndexBasedFieldMapping) jsonToQFieldMappingAdapter.buildMappingFromJson("""
         {
            "Field1": 1,
            "Field2": 2,
         }
         """);
      System.out.println(mapping);
      assertNotNull(mapping);

      assertEquals(1, mapping.getFieldSource("Field1"));
      assertEquals(2, mapping.getFieldSource("Field2"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_unsupportedTypeForSource()
   {
      testExpectedToThrow("""
         {
            "Field1": [1, 2],
            "Field2": {"A": "B"}
         }
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_emptyMapping()
   {
      testExpectedToThrow("{}");
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_buildMappingFromJson_inputJsonList()
   {
      testExpectedToThrow("[]");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void testExpectedToThrow(String json)
   {
      try
      {
         JsonToQFieldMappingAdapter jsonToQFieldMappingAdapter = new JsonToQFieldMappingAdapter();
         AbstractQFieldMapping<?> mapping = jsonToQFieldMappingAdapter.buildMappingFromJson(json);
         System.out.println(mapping);
      }
      catch(IllegalArgumentException iae)
      {
         System.out.println("Threw expected exception");
         return;
      }

      fail("Didn't throw expected exception");
   }

}
