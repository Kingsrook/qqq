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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.scripts.logging.Log4jCodeExecutionLogger;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAdHocRecordScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.AdHocScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for RunAdHocRecordScriptAction
 *******************************************************************************/
class RunAdHocRecordScriptActionTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      setupInstance();

      Integer scriptId = insertScript("""
         return "Hello";
         """);

      RunAdHocRecordScriptInput runAdHocRecordScriptInput = new RunAdHocRecordScriptInput();
      runAdHocRecordScriptInput.setRecordPrimaryKey(1);
      runAdHocRecordScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAdHocRecordScriptInput.setCodeReference(new AdHocScriptCodeReference().withScriptId(scriptId));
      runAdHocRecordScriptInput.setLogger(new Log4jCodeExecutionLogger());

      RunAdHocRecordScriptOutput runAdHocRecordScriptOutput = new RunAdHocRecordScriptOutput();
      new RunAdHocRecordScriptAction().run(runAdHocRecordScriptInput, runAdHocRecordScriptOutput);

      /*
      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput();
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(1)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // ok - since the core module doesn't have the javascript language support module as a dep, this action will fail - but at least we can confirm it fails with this specific exception! //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QException.class)
         .hasRootCauseInstanceOf(ClassNotFoundException.class)
         .hasRootCauseMessage("com.kingsrook.qqq.languages.javascript.QJavaScriptExecutor");

      /////////////////////////////////////
      // assert that a log was generated //
      /////////////////////////////////////
      assertEquals(1, TestUtils.queryTable(ScriptLog.TABLE_NAME).size());
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupInstance() throws QException
   {
      QInstance instance = QContext.getQInstance();
      QTableMetaData personMemory = instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withField(new QFieldMetaData("testScriptId", QFieldType.INTEGER))
         .withAssociatedScript(new AssociatedScript()
            .withScriptTypeId(1)
            .withFieldName("testScriptId")
         );

      new ScriptsMetaDataProvider().defineAll(instance, TestUtils.MEMORY_BACKEND_NAME, null);

      TestUtils.insertRecords(instance, personMemory, List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)
      ));

      TestUtils.insertRecords(instance, instance.getTable("scriptType"), List.of(
         new QRecord().withValue("id", 1).withValue("name", "Test Script Type")
      ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Integer insertScript(String code) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName("script");
      insertInput.setRecords(List.of(new QRecord().withValue("name", "Test script")));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      Integer      scriptId     = insertOutput.getRecords().get(0).getValueInteger("id");

      insertInput = new InsertInput();
      insertInput.setTableName("scriptRevision");
      insertInput.setRecords(List.of(new QRecord().withValue("scriptId", scriptId).withValue("code", code)));
      insertOutput = new InsertAction().execute(insertInput);
      Integer scriptRevisionId = insertOutput.getRecords().get(0).getValueInteger("id");

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName("script");
      updateInput.setRecords(List.of(new QRecord().withValue("id", scriptId).withValue("currentScriptRevisionId", scriptRevisionId)));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);

      return (scriptId);
   }
}