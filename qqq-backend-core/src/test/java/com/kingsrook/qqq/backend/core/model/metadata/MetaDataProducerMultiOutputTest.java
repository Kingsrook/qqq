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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for MetaDataProducerMultiOutput 
 *******************************************************************************/
class MetaDataProducerMultiOutputTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetEachAndGet()
   {
      //////////////////////
      // given this setup //
      //////////////////////
      MetaDataProducerMultiOutput metaDataProducerMultiOutput = new MetaDataProducerMultiOutput();
      metaDataProducerMultiOutput.add(new QTableMetaData().withName("tableA"));
      metaDataProducerMultiOutput.add(new QProcessMetaData().withName("processB"));
      metaDataProducerMultiOutput.add(new QBackendMetaData().withName("backendC"));
      metaDataProducerMultiOutput.add(new QTableMetaData().withName("tableD"));

      ///////////////////////////
      // test calls to getEach //
      ///////////////////////////
      List<QTableMetaData> tables = metaDataProducerMultiOutput.getEach(QTableMetaData.class);
      assertEquals(2, tables.size());
      assertEquals("tableA", tables.get(0).getName());
      assertEquals("tableD", tables.get(1).getName());

      List<QProcessMetaData> processes = metaDataProducerMultiOutput.getEach(QProcessMetaData.class);
      assertEquals(1, processes.size());
      assertEquals("processB", processes.get(0).getName());

      List<QBackendMetaData> backends = metaDataProducerMultiOutput.getEach(QBackendMetaData.class);
      assertEquals(1, backends.size());
      assertEquals("backendC", backends.get(0).getName());

      List<QQueueProviderMetaData> queueProviders = metaDataProducerMultiOutput.getEach(QQueueProviderMetaData.class);
      assertEquals(0, queueProviders.size());

      //////////////////////////////////////////////
      // test some calls to get that takes a name //
      //////////////////////////////////////////////
      assertEquals("tableA", metaDataProducerMultiOutput.get(QTableMetaData.class, "tableA").getName());
      assertNull(metaDataProducerMultiOutput.get(QProcessMetaData.class, "tableA"));
      assertNull(metaDataProducerMultiOutput.get(QQueueMetaData.class, "queueQ"));
   }

}