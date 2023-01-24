/*
 * Copyright © 2022-2023. Nutrifresh Services <contact@nutrifreshservices.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class BaseTest
{
   private static final QLogger LOG = QLogger.getLogger(BaseTest.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void baseBeforeEach()
   {
      QContext.init(TestUtils.defineInstance(), new QSession());
      resetMemoryRecordStore();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void baseAfterEach()
   {
      QContext.clear();
      resetMemoryRecordStore();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void resetMemoryRecordStore()
   {
      MemoryRecordStore.getInstance().reset();
      MemoryRecordStore.resetStatistics();
      MemoryRecordStore.setCollectStatistics(false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected static void reInitInstanceInContext(QInstance qInstance)
   {
      if(qInstance.equals(QContext.getQInstance()))
      {
         LOG.warn("Unexpected condition - the same qInstance that is already in the QContext was passed into reInit.  You probably want a new QInstance object instance.");
      }
      QContext.init(qInstance, new QSession());
   }

}
