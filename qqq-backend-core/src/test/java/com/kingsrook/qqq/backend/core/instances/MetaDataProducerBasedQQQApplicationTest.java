/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.producers.TestMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MetaDataProducerBasedQQQApplication 
 *******************************************************************************/
class MetaDataProducerBasedQQQApplicationTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = new MetaDataProducerBasedQQQApplication(getClass().getPackage().getName() + ".producers").defineQInstance();
      assertEquals(1, qInstance.getTables().size());
      assertEquals("fromProducer", qInstance.getTables().get("fromProducer").getName());
      assertEquals(1, qInstance.getProcesses().size());
      assertEquals("fromProducer", qInstance.getProcesses().get("fromProducer").getName());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testConstructorThatTakeClass() throws QException
   {
      QInstance qInstance = new MetaDataProducerBasedQQQApplication(TestMetaDataProducer.class).defineQInstance();
      assertEquals(1, qInstance.getTables().size());
      assertEquals("fromProducer", qInstance.getTables().get("fromProducer").getName());
      assertEquals(1, qInstance.getProcesses().size());
      assertEquals("fromProducer", qInstance.getProcesses().get("fromProducer").getName());
   }

}