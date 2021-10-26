package com.kingsrook.qqq.backend.core.adapters;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QKeyBasedFieldMapping;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
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
   public void test_buildMappingFromJson_validInput()
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

      // todo - are we backwards here??
      assertEquals("source1", mapping.getMappedField("Field1"));
      assertEquals("source2", mapping.getMappedField("Field2"));
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