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
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.functions.implementations.StringLengthFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for BackendFieldFunctionAdapterRegistry
 *******************************************************************************/
class BackendFieldFunctionAdapterRegistryTest extends BaseTest
{

   /****************************************************************************
    * A minimal test adapter to verify registration works.
    ****************************************************************************/
   public static class TestAdapter implements BackendFieldFunctionAdapterInterface
   {
   }



   /****************************************************************************
    * A second test adapter to verify replacement registration.
    ****************************************************************************/
   public static class ReplacementAdapter implements BackendFieldFunctionAdapterInterface
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegisterAndGetAdapter()
   {
      BackendFieldFunctionAdapterRegistry registry = new BackendFieldFunctionAdapterRegistry();
      registry.register(StringLengthFunction.IDENTIFIER, "testBackend", new QCodeReference(TestAdapter.class));

      BackendFieldFunctionAdapterInterface adapter = registry.getFieldFunctionAdapter(StringLengthFunction.IDENTIFIER);
      assertNotNull(adapter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetUnregisteredReturnsNull()
   {
      BackendFieldFunctionAdapterRegistry registry = new BackendFieldFunctionAdapterRegistry();
      FieldFunctionTypeIdentifier bogus = () -> "NoSuchFunction";

      assertNull(registry.getFieldFunctionAdapter(bogus));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRegisterReplacesPreviousRegistration()
   {
      BackendFieldFunctionAdapterRegistry registry = new BackendFieldFunctionAdapterRegistry();
      registry.register(StringLengthFunction.IDENTIFIER, "testBackend", new QCodeReference(TestAdapter.class));
      registry.register(StringLengthFunction.IDENTIFIER, "testBackend", new QCodeReference(ReplacementAdapter.class));

      BackendFieldFunctionAdapterInterface adapter = registry.getFieldFunctionAdapter(StringLengthFunction.IDENTIFIER);
      assertNotNull(adapter);
      assertInstanceOf(ReplacementAdapter.class, adapter, "Should return the replacement adapter, not the original");
   }

}
