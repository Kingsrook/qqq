/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules;


import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;
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
