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

package com.kingsrook.qqq.api.actions;


import java.util.Set;
import com.kingsrook.qqq.api.BaseTest;
import com.kingsrook.qqq.api.TestUtils;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for GenerateOpenApiSpecAction
 *******************************************************************************/
class GenerateOpenApiSpecActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecAction().execute(new GenerateOpenApiSpecInput().withVersion(TestUtils.CURRENT_API_VERSION));
      System.out.println(output.getYaml());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExcludedTables() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      qInstance.addTable(new QTableMetaData()
         .withName("supportedTable")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(TestUtils.V2022_Q4)));

      qInstance.addTable(new QTableMetaData()
         .withName("hiddenTable")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withIsHidden(true)
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(TestUtils.V2022_Q4)));

      qInstance.addTable(new QTableMetaData()
         .withName("excludedTable")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withIsExcluded(true)));

      qInstance.addTable(new QTableMetaData()
         .withName("tableWithoutApiMetaData")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER)));

      qInstance.addTable(new QTableMetaData()
         .withName("tableWithFutureVersion")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(TestUtils.V2023_Q2)));

      qInstance.addTable(new QTableMetaData()
         .withName("tableWithOnlyPastVersions")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(TestUtils.V2022_Q4)
            .withFinalVersion(TestUtils.V2022_Q4)));

      qInstance.addTable(new QTableMetaData()
         .withName("tableWithNoSupportedCapabilities")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withoutCapabilities(Capability.TABLE_QUERY, Capability.TABLE_GET, Capability.TABLE_INSERT, Capability.TABLE_UPDATE, Capability.TABLE_DELETE)
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withInitialVersion(TestUtils.V2022_Q4)));

      GenerateOpenApiSpecOutput output   = new GenerateOpenApiSpecAction().execute(new GenerateOpenApiSpecInput().withVersion(TestUtils.CURRENT_API_VERSION));
      Set<String>               apiPaths = output.getOpenAPI().getPaths().keySet();
      assertTrue(apiPaths.stream().anyMatch(s -> s.contains("/supportedTable/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/hiddenTable/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/excludedTable/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/tableWithoutApiMetaData/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/tableWithoutApiMetaData/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/tableWithFutureVersion/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/tableWithNoSupportedCapabilities/")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testApiTableName() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      qInstance.addTable(new QTableMetaData()
         .withName("internalName")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withMiddlewareMetaData(new ApiTableMetaData()
            .withApiTableName("externalName")
            .withInitialVersion(TestUtils.V2022_Q4)));

      GenerateOpenApiSpecOutput output   = new GenerateOpenApiSpecAction().execute(new GenerateOpenApiSpecInput().withVersion(TestUtils.CURRENT_API_VERSION));
      Set<String>               apiPaths = output.getOpenAPI().getPaths().keySet();
      assertTrue(apiPaths.stream().anyMatch(s -> s.contains("/externalName/")));
      assertTrue(apiPaths.stream().noneMatch(s -> s.contains("/internalName/")));
   }

}