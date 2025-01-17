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

package com.kingsrook.qqq.backend.core.instances.loaders.implementations;


import java.nio.charset.StandardCharsets;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.instances.loaders.QMetaDataLoaderException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for GenericMetaDataLoader - providing coverage for AbstractMetaDataLoader.
 *******************************************************************************/
class GenericMetaDataLoaderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testProcess() throws QMetaDataLoaderException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // trying to get some coverage of various types in here (for Abstract loader) //
      ////////////////////////////////////////////////////////////////////////////////
      QProcessMetaData process = new GenericMetaDataLoader<>(QProcessMetaData.class).fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         class: QProcessMetaData
         version: 1
         name: myProcess
         tableName: someTable
         maxInputRecords: 1
         isHidden: true
         """, StandardCharsets.UTF_8), "myProcess.yaml");

      assertEquals("myProcess", process.getName());
      assertEquals("someTable", process.getTableName());
      assertEquals(1, process.getMaxInputRecords());
      assertTrue(process.getIsHidden());
   }



   /*******************************************************************************
    ** just here for coverage of this class, as we're failing to hit it otherwise.
    *******************************************************************************/
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Test
   void testNoValueException()
   {
      assertThatThrownBy(() -> new GenericMetaDataLoader(QBackendMetaData.class).reflectivelyMapValue(new QInstance(), null, GenericMetaDataLoaderTest.class, "rawValue"));
   }

}