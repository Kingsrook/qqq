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

package com.kingsrook.qqq.backend.core.modules.authentication;


import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for QModuleDispatcher
 **
 *******************************************************************************/
class QAuthenticationModuleDispatcherTest
{

   /*******************************************************************************
    ** Test success case - a valid backend.
    **
    *******************************************************************************/
   @Test
   public void test_getQAuthenticationModule() throws QModuleDispatchException
   {
      QAuthenticationModuleInterface mock = new QAuthenticationModuleDispatcher().getQModule(TestUtils.defineAuthentication());
      assertNotNull(mock);
   }



   /*******************************************************************************
    ** Test success case - a valid backend.
    **
    *******************************************************************************/
   @Test
   public void test_getQAuthenticationModuleByType_valid() throws QModuleDispatchException
   {
      QAuthenticationModuleInterface mock = new QAuthenticationModuleDispatcher().getQModule("mock");
      assertNotNull(mock);
   }



   /*******************************************************************************
    ** Test failure case, a backend name that doesn't exist
    **
    *******************************************************************************/
   @Test
   public void test_getQAuthenticationModule_typeNotFound()
   {
      assertThrows(QModuleDispatchException.class, () ->
      {
         new QAuthenticationModuleDispatcher().getQModule("aTypeThatWontExist");
      });
   }



   /*******************************************************************************
    ** Test failure case, null argument
    **
    *******************************************************************************/
   @Test
   public void test_getQAuthenticationModule_nullArgument()
   {
      assertThrows(QModuleDispatchException.class, () ->
      {
         new QAuthenticationModuleDispatcher().getQModule((QAuthenticationMetaData) null);
      });
   }

}
