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

package com.kingsrook.qqq.backend.core.model.metadata.qbits;


import java.util.LinkedHashMap;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.testqbit.TestQBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.testqbit.TestQBitProducer;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.testqbit.metadata.OtherTableMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.testqbit.metadata.SomeTableMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QBitProducer 
 *******************************************************************************/
class QBitProducerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestQBitConfig config = new TestQBitConfig()
         .withOtherTableConfig(ProvidedOrSuppliedTableConfig.provideTableUsingBackendNamed(TestUtils.MEMORY_BACKEND_NAME))
         .withIsSomeTableEnabled(true)
         .withSomeSetting("yes")
         .withTableMetaDataCustomizer((i, table) ->
         {
            if(table.getBackendName() == null)
            {
               table.setBackendName(TestUtils.DEFAULT_BACKEND_NAME);
            }

            table.addField(new QFieldMetaData("custom", QFieldType.STRING));

            return (table);
         });

      QInstance qInstance = QContext.getQInstance();
      new TestQBitProducer().withTestQBitConfig(config).produce(qInstance);

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // OtherTable should have been provided by the qbit, with the backend name we told it above (MEMORY) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      QTableMetaData otherTable = qInstance.getTable(OtherTableMetaDataProducer.NAME);
      assertNotNull(otherTable);
      assertEquals(TestUtils.MEMORY_BACKEND_NAME, otherTable.getBackendName());
      assertNotNull(otherTable.getField("custom"));

      QBitMetaData sourceQBit = otherTable.getSourceQBit();
      assertEquals("testQBit", sourceQBit.getArtifactId());

      ////////////////////////////////////////////////////////////////////////////////
      // SomeTable should have been provided, w/ backend name set by the customizer //
      ////////////////////////////////////////////////////////////////////////////////
      QTableMetaData someTable = qInstance.getTable(SomeTableMetaDataProducer.NAME);
      assertNotNull(someTable);
      assertEquals(TestUtils.DEFAULT_BACKEND_NAME, someTable.getBackendName());
      assertNotNull(otherTable.getField("custom"));

      TestQBitConfig qBitConfig = (TestQBitConfig) someTable.getSourceQBitConfig();
      assertEquals("yes", qBitConfig.getSomeSetting());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDisableThings() throws QException
   {
      TestQBitConfig config = new TestQBitConfig()
         .withOtherTableConfig(ProvidedOrSuppliedTableConfig.useSuppliedTaleNamed(TestUtils.TABLE_NAME_PERSON_MEMORY))
         .withIsSomeTableEnabled(false);

      QInstance qInstance = QContext.getQInstance();
      new TestQBitProducer().withTestQBitConfig(config).produce(qInstance);

      //////////////////////////////////////
      // neither table should be produced //
      //////////////////////////////////////
      QTableMetaData otherTable = qInstance.getTable(OtherTableMetaDataProducer.NAME);
      assertNull(otherTable);

      QTableMetaData someTable = qInstance.getTable(SomeTableMetaDataProducer.NAME);
      assertNull(someTable);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidationErrors() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      TestQBitConfig config = new TestQBitConfig();

      assertThatThrownBy(() -> new TestQBitProducer().withTestQBitConfig(config).produce(qInstance))
         .isInstanceOf(QBitConfigValidationException.class)
         .hasMessageContaining("otherTableConfig must be set")
         .hasMessageContaining("isSomeTableEnabled must be set");
      qInstance.setQBits(new LinkedHashMap<>());

      config.setIsSomeTableEnabled(true);
      assertThatThrownBy(() -> new TestQBitProducer().withTestQBitConfig(config).produce(qInstance))
         .isInstanceOf(QBitConfigValidationException.class)
         .hasMessageContaining("otherTableConfig must be set");
      qInstance.setQBits(new LinkedHashMap<>());

      config.setOtherTableConfig(ProvidedOrSuppliedTableConfig.useSuppliedTaleNamed(TestUtils.TABLE_NAME_PERSON_MEMORY));
      new TestQBitProducer().withTestQBitConfig(config).produce(qInstance);
   }

}