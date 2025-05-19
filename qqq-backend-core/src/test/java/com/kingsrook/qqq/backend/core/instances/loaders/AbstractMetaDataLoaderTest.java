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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.nio.charset.StandardCharsets;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.GenericMetaDataLoader;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.QTableMetaDataLoader;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for AbstractMetaDataLoader 
 *******************************************************************************/
class AbstractMetaDataLoaderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVariousPropertyTypes() throws QMetaDataLoaderException
   {
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
    **
    *******************************************************************************/
   @Test
   void testProblems() throws QMetaDataLoaderException
   {
      {
         QTableMetaDataLoader loader = new QTableMetaDataLoader();
         loader.fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
            class: QTableMetaData
            version: 1.0
            name: myTable
            something: foo
            isHidden: hi
            icon:
               name: account_tree
               size: big
               weight: bold
            fields:
               id:
                  type: number
            uniqueKeys: sure!
            """, StandardCharsets.UTF_8), "myTable.yaml");

         for(LoadingProblem problem : loader.getProblems())
         {
            System.out.println(problem);
         }
      }

      {
         GenericMetaDataLoader<QProcessMetaData> loader = new GenericMetaDataLoader<>(QProcessMetaData.class);
         loader.fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
            class: QProcessMetaData
            version: 1.0
            name: myProcess
            maxInputRecords: many
            """, StandardCharsets.UTF_8), "myProcess.yaml");

         for(LoadingProblem problem : loader.getProblems())
         {
            System.out.println(problem);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEnvironmentValues() throws QMetaDataLoaderException
   {
      System.setProperty("myProcess.tableName", "someTable");
      System.setProperty("myProcess.maxInputRecords", "47");

      GenericMetaDataLoader<QProcessMetaData> loader = new GenericMetaDataLoader<>(QProcessMetaData.class);
      QProcessMetaData processMetaData = loader.fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         class: QProcessMetaData
         version: 1.0
         name: myProcess
         tableName: ${prop.myProcess.tableName}
         maxInputRecords: ${prop.myProcess.maxInputRecords}
         """, StandardCharsets.UTF_8), "myProcess.yaml");

      assertEquals("someTable", processMetaData.getTableName());
      assertEquals(47, processMetaData.getMaxInputRecords());
   }

}