package com.kingsrook.qqq.backend.core.modules;


import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/*******************************************************************************
 **
 *******************************************************************************/
class QModuleDispatcherTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_getQModule_valid() throws QModuleDispatchException
   {
      QModuleInterface qModule = new QModuleDispatcher().getQModule(TestUtils.defineBackend());
      assertNotNull(qModule);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test_getQModule_typeNotFound()
   {
      assertThrows(QModuleDispatchException.class, () ->
      {
         QBackendMetaData qBackendMetaData = TestUtils.defineBackend();
         qBackendMetaData.setType("aTypeThatWontEverExist");
         new QModuleDispatcher().getQModule(qBackendMetaData);
      });
   }

}