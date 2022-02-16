/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
