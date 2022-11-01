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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.AssociatedScriptCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for RunAssociatedScriptAction
 *******************************************************************************/
class RunAssociatedScriptActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance instance = setupInstance();

      insertScript(instance, 1, """
         return "Hello";
         """);

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
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
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QInstance setupInstance() throws QException
   {
      QInstance instance = TestUtils.defineInstance();
      QTableMetaData table = instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withField(new QFieldMetaData("testScriptId", QFieldType.INTEGER))
         .withAssociatedScript(new AssociatedScript()
            .withScriptTypeId(1)
            .withFieldName("testScriptId")
         );

      new ScriptsMetaDataProvider().defineAll(instance, TestUtils.MEMORY_BACKEND_NAME, null);

      TestUtils.insertRecords(instance, table, List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)
      ));

      TestUtils.insertRecords(instance, instance.getTable("scriptType"), List.of(
         new QRecord().withValue("id", 1).withValue("name", "Test Script Type")
      ));
      return instance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRecordNotFound() throws QException
   {
      QInstance instance = setupInstance();

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(-9999)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QNotFoundException.class)
         .hasMessageMatching("The requested record.*was not found.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoScriptInRecord() throws QException
   {
      QInstance instance = setupInstance();

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(1)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QNotFoundException.class)
         .hasMessageMatching("The input record.*does not have a script specified for.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadScriptIdInRecord() throws QException
   {
      QInstance instance = setupInstance();

      UpdateInput updateInput = new UpdateInput(instance);
      updateInput.setSession(new QSession());
      updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("testScriptId", -9998)));
      new UpdateAction().execute(updateInput);

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(1)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QNotFoundException.class)
         .hasMessageMatching("The script for record .* was not found.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoCurrentScriptRevisionOnScript() throws QException
   {
      QInstance instance = setupInstance();

      insertScript(instance, 1, """
         return "Hello";
         """);

      GetInput getInput = new GetInput(instance);
      getInput.setSession(new QSession());
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      Integer   scriptId  = getOutput.getRecord().getValueInteger("testScriptId");

      UpdateInput updateInput = new UpdateInput(instance);
      updateInput.setSession(new QSession());
      updateInput.setTableName("script");
      updateInput.setRecords(List.of(new QRecord().withValue("id", scriptId).withValue("currentScriptRevisionId", null)));
      new UpdateAction().execute(updateInput);

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(1)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QNotFoundException.class)
         .hasMessageMatching("The script for record .* does not have a current version.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadCurrentScriptRevisionOnScript() throws QException
   {
      QInstance instance = setupInstance();

      insertScript(instance, 1, """
         return "Hello";
         """);

      GetInput getInput = new GetInput(instance);
      getInput.setSession(new QSession());
      getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      getInput.setPrimaryKey(1);
      GetOutput getOutput = new GetAction().execute(getInput);
      Integer   scriptId  = getOutput.getRecord().getValueInteger("testScriptId");

      UpdateInput updateInput = new UpdateInput(instance);
      updateInput.setSession(new QSession());
      updateInput.setTableName("script");
      updateInput.setRecords(List.of(new QRecord().withValue("id", scriptId).withValue("currentScriptRevisionId", 9997)));
      new UpdateAction().execute(updateInput);

      RunAssociatedScriptInput runAssociatedScriptInput = new RunAssociatedScriptInput(instance);
      runAssociatedScriptInput.setSession(new QSession());
      runAssociatedScriptInput.setInputValues(Map.of());
      runAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      runAssociatedScriptInput.setCodeReference(new AssociatedScriptCodeReference()
         .withRecordTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withRecordPrimaryKey(1)
         .withFieldName("testScriptId")
      );
      RunAssociatedScriptOutput runAssociatedScriptOutput = new RunAssociatedScriptOutput();

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QNotFoundException.class)
         .hasMessageMatching("The current revision of the script for record .* was not found.*");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void insertScript(QInstance instance, Serializable recordId, String code) throws QException
   {
      StoreAssociatedScriptInput storeAssociatedScriptInput = new StoreAssociatedScriptInput(instance);
      storeAssociatedScriptInput.setSession(new QSession());
      storeAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      storeAssociatedScriptInput.setRecordPrimaryKey(recordId);
      storeAssociatedScriptInput.setCode(code);
      storeAssociatedScriptInput.setFieldName("testScriptId");
      StoreAssociatedScriptOutput storeAssociatedScriptOutput = new StoreAssociatedScriptOutput();
      new StoreAssociatedScriptAction().run(storeAssociatedScriptInput, storeAssociatedScriptOutput);
   }

}