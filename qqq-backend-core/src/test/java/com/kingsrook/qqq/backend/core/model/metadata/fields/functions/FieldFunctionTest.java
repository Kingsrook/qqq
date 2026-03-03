/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields.functions;


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.StringLengthFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.SubStringFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for FieldFunction
 *******************************************************************************/
class FieldFunctionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testClone()
   {
      FieldFunction original = new FieldFunction()
         .withFieldName("firstName")
         .withFunctionTypeIdentifier(SubStringFunction.IDENTIFIER)
         .withArguments(Map.of(SubStringFunction.FROM_INDEX_PARAM, 2, SubStringFunction.LENGTH_PARAM, 3));

      FieldFunction clone = original.clone();
      assertNotSame(original, clone);
      assertNotSame(original.getArguments(), clone.getArguments());
      assertEquals("firstName", clone.getFieldName());
      assertEquals("SubString", clone.getFunctionTypeIdentifier().getName());
      assertEquals(2, clone.getArguments().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCloneWithNullArguments()
   {
      FieldFunction original = new FieldFunction()
         .withFieldName("test")
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER);

      FieldFunction clone = original.clone();
      assertNotSame(original, clone);
      assertEquals("test", clone.getFieldName());
      assertNull(clone.getArguments());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetArgumentValueOrDefaultWithExplicitArg()
   {
      FieldFunction ff = new FieldFunction()
         .withFieldName("name")
         .withFunctionTypeIdentifier(SubStringFunction.IDENTIFIER)
         .withArguments(Map.of(SubStringFunction.FROM_INDEX_PARAM, 5));

      Integer fromIndex = ff.getArgumentValueOrDefault(Integer.class, SubStringFunction.FROM_INDEX_PARAM);
      assertEquals(5, fromIndex);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetArgumentValueOrDefaultFallsBackToDefault()
   {
      FieldFunction ff = new FieldFunction()
         .withFieldName("name")
         .withFunctionTypeIdentifier(SubStringFunction.IDENTIFIER)
         .withArguments(Map.of(SubStringFunction.FROM_INDEX_PARAM, 1));

      /////////////////////////////////////////////////////////////////////////////////
      // length param is not in arguments, and is not required - should get null     //
      // (SubStringFunction.LENGTH_PARAM has no default value defined in parameters) //
      /////////////////////////////////////////////////////////////////////////////////
      Integer length = ff.getArgumentValueOrDefault(Integer.class, SubStringFunction.LENGTH_PARAM);
      assertNull(length);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetFunctionTypeIdentifierName()
   {
      FieldFunction ff = new FieldFunction()
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER);
      assertEquals("StringLength", ff.getFunctionTypeIdentifierName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetFunctionTypeIdentifierNameWhenNull()
   {
      FieldFunction ff = new FieldFunction();
      assertNull(ff.getFunctionTypeIdentifierName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFluentSetters()
   {
      FieldFunction ff = new FieldFunction()
         .withFieldName("test")
         .withFunctionTypeIdentifier(StringLengthFunction.IDENTIFIER)
         .withArguments(Map.of("key", "value"));

      assertEquals("test", ff.getFieldName());
      assertNotNull(ff.getFunctionTypeIdentifier());
      assertNotNull(ff.getArguments());
   }

}
