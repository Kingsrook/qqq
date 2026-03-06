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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.StringLengthFunction;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.SubStringFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;


/*******************************************************************************
 ** Unit test for FieldFunctionTypeRegistry
 *******************************************************************************/
class FieldFunctionTypeRegistryTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegisterAndGetByClass()
   {
      FieldFunctionTypeRegistry registry = new FieldFunctionTypeRegistry();
      registry.register(StringLengthFunction.IDENTIFIER, StringLengthFunction.class);

      FieldFunctionType result = registry.getFieldFunctionType(StringLengthFunction.IDENTIFIER);
      assertNotNull(result);
      assertEquals("StringLength", result.getIdentifier().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegisterAndGetByCodeReference()
   {
      FieldFunctionTypeRegistry registry = new FieldFunctionTypeRegistry();
      registry.register(SubStringFunction.IDENTIFIER, new QCodeReference(SubStringFunction.class));

      FieldFunctionType result = registry.getFieldFunctionType(SubStringFunction.IDENTIFIER);
      assertNotNull(result);
      assertEquals("SubString", result.getIdentifier().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetUnregisteredReturnsNull()
   {
      FieldFunctionTypeRegistry registry = new FieldFunctionTypeRegistry();
      FieldFunctionTypeIdentifier bogus = () -> "NotRegistered";

      FieldFunctionType result = registry.getFieldFunctionType(bogus);
      assertNull(result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOfReturnsNullWhenNotSet()
   {
      QInstance qInstance = new QInstance();
      assertNull(FieldFunctionTypeRegistry.of(qInstance));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOfOrWithNewCreatesRegistry()
   {
      QInstance qInstance = QContext.getQInstance();
      FieldFunctionTypeRegistry registry = FieldFunctionTypeRegistry.ofOrWithNew(qInstance);
      assertNotNull(registry);

      FieldFunctionTypeRegistry sameRegistry = FieldFunctionTypeRegistry.ofOrWithNew(qInstance);
      assertSame(registry, sameRegistry);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEnricherRegistersBuiltInTypes()
   {
      /////////////////////////////////////////////////////////////////////////////////////
      // After BaseTest sets up the context, the enricher should have run and registered //
      // the built-in field function types. Verify some are available.                   //
      /////////////////////////////////////////////////////////////////////////////////////
      FieldFunctionTypeRegistry registry = FieldFunctionTypeRegistry.of(QContext.getQInstance());
      assertNotNull(registry);

      assertNotNull(registry.getFieldFunctionType(StringLengthFunction.IDENTIFIER));
      assertNotNull(registry.getFieldFunctionType(SubStringFunction.IDENTIFIER));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegisterInvalidCodeReferenceDoesNotThrow()
   {
      FieldFunctionTypeRegistry registry = new FieldFunctionTypeRegistry();
      registry.register(StringLengthFunction.IDENTIFIER, new QCodeReference(String.class));

      assertNull(registry.getFieldFunctionType(StringLengthFunction.IDENTIFIER));
   }

}
