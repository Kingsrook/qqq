/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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


import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QIndexBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
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
