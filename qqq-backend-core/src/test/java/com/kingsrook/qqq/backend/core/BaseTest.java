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

package com.kingsrook.qqq.backend.core;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
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
      QContext.init(TestUtils.defineInstance(), new QSession()
         .withUser(new QUser()
            .withIdReference("001")
            .withFullName("Anonymous")));
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
