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

package com.kingsrook.qqq.backend.core.modules;


import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 ** Unit test for QModuleDispatcher
 **
 *******************************************************************************/
class QBackendModuleDispatcherTest
{

   /*******************************************************************************
    ** Test success case - a valid backend.
    **
    *******************************************************************************/
   @Test
   public void test_getQModule_valid() throws QModuleDispatchException
   {
      QBackendModuleInterface qModule = new QBackendModuleDispatcher().getQModule(TestUtils.defineBackend());
      assertNotNull(qModule);
   }



   /*******************************************************************************
    ** Test failure case, a backend name that doesn't exist
    **
    *******************************************************************************/
   @Test
   public void test_getQModule_typeNotFound()
   {
      assertThrows(QModuleDispatchException.class, () ->
      {
         QBackendMetaData qBackendMetaData = TestUtils.defineBackend();
         qBackendMetaData.setType("aTypeThatWontEverExist");
         new QBackendModuleDispatcher().getQModule(qBackendMetaData);
      });
   }

}
