/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for loading a QProcessMetaData (doesn't need its own loader yet,
 ** but is still a valuable high-level test target).
 *******************************************************************************/
class QProcessMetaDataLoaderTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testYaml() throws QMetaDataLoaderException
   {
      ClassDetectingMetaDataLoader metaDataLoader = new ClassDetectingMetaDataLoader();
      QProcessMetaData process = (QProcessMetaData) metaDataLoader.fileToMetaDataObject(new QInstance(), IOUtils.toInputStream("""
         class:  QProcessMetaData
         version:  1.0
         name:  myProcess
         stepList:
         - name: myBackendStep
           stepType: backend
           code:
             name: com.kingsrook.test.processes.MyBackendStep
         - name: myFrontendStep
           stepType: frontend
           components:
             - type: HELP_TEXT
               values:
                  foo: bar
             - type: VIEW_FORM
           viewFields:
             - name: myField
               type: STRING
             - name: yourField
               type: DATE
         """, StandardCharsets.UTF_8), "myProcess.yaml");

      CollectionUtils.nonNullList(metaDataLoader.getProblems()).forEach(System.out::println);

      assertEquals("myProcess", process.getName());
      assertEquals(2, process.getAllSteps().size());

      QBackendStepMetaData myBackendStep = process.getBackendStep("myBackendStep");
      assertNotNull(myBackendStep, "myBackendStep should not be null");
      // todo - propagate this? assertEquals("myBackendStep", myBackendStep.getName());
      assertEquals("com.kingsrook.test.processes.MyBackendStep", myBackendStep.getCode().getName());

      QFrontendStepMetaData myFrontendStep = process.getFrontendStep("myFrontendStep");
      assertNotNull(myFrontendStep, "myFrontendStep should not be null");
      assertEquals(2, myFrontendStep.getComponents().size());
      assertEquals(QComponentType.HELP_TEXT, myFrontendStep.getComponents().get(0).getType());
      assertEquals(Map.of("foo", "bar"), myFrontendStep.getComponents().get(0).getValues());
      assertEquals(QComponentType.VIEW_FORM, myFrontendStep.getComponents().get(1).getType());

      assertEquals(2, myFrontendStep.getViewFields().size());
      assertEquals("myField", myFrontendStep.getViewFields().get(0).getName());
      assertEquals(QFieldType.STRING, myFrontendStep.getViewFields().get(0).getType());
      assertEquals("yourField", myFrontendStep.getViewFields().get(1).getName());
      assertEquals(QFieldType.DATE, myFrontendStep.getViewFields().get(1).getType());
   }

}