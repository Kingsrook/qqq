/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestAbstractMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestDisabledMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestImplementsMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestNoInterfacesExtendsObject;
import com.kingsrook.qqq.backend.core.model.metadata.producers.TestNoValidConstructorMetaDataProducer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for MetaDataProducerHelper
 *******************************************************************************/
class MetaDataProducerHelperTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = new QInstance();
      MetaDataProducerHelper.processAllMetaDataProducersInPackage(qInstance, "com.kingsrook.qqq.backend.core.model.metadata.producers");
      assertTrue(qInstance.getTables().containsKey(TestMetaDataProducer.NAME));
      assertTrue(qInstance.getTables().containsKey(TestImplementsMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestNoValidConstructorMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestNoInterfacesExtendsObject.NAME));
      assertFalse(qInstance.getTables().containsKey(TestAbstractMetaDataProducer.NAME));
      assertFalse(qInstance.getTables().containsKey(TestDisabledMetaDataProducer.NAME));
   }

}