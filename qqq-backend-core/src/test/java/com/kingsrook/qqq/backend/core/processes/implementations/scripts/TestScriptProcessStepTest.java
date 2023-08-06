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

package com.kingsrook.qqq.backend.core.processes.implementations.scripts;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.scripts.RecordScriptTestInterface;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptType;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableAccessor;
import com.kingsrook.qqq.backend.core.model.tables.QQQTablesMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for TestScriptProcessStep
 *******************************************************************************/
class TestScriptProcessStepTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      new ScriptsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);
      new QQQTablesMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(ScriptType.TABLE_NAME);
      insertInput.setRecords(List.of(new ScriptType()
         .withName("TestScriptType")
         .withTestScriptInterfaceName(RecordScriptTestInterface.class.getName())
         .toQRecord()));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      insertInput = new InsertInput();
      insertInput.setTableName(Script.TABLE_NAME);
      insertInput.setRecords(List.of(new Script()
         .withName("TestScript")
         .withScriptTypeId(insertOutput.getRecords().get(0).getValueInteger("id"))
         .withTableId(QQQTableAccessor.getTableId(TestUtils.TABLE_NAME_SHAPE))
         .toQRecord()));
      insertOutput = new InsertAction().execute(insertInput);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue("scriptId", insertOutput.getRecords().get(0).getValueInteger("id"));
      TestUtils.insertDefaultShapes(qInstance);
      input.addValue("recordPrimaryKeyList", "1");
      input.addValue("fileNames", new ArrayList<>(List.of("script.js")));
      input.addValue("fileContents:script.js", "logger.log('oh my.')");

      RunBackendStepOutput output = new RunBackendStepOutput();
      new TestScriptProcessStep().run(input, output);

      //////////////////////////////////////////////////////////////////
      // expect an error because the javascript module isn't available //
      //////////////////////////////////////////////////////////////////
      assertNotNull(output.getValue("exception"));
      assertThat((Exception) output.getValue("exception"))
         .hasRootCauseInstanceOf(ClassNotFoundException.class)
         .rootCause()
         .hasMessageContaining("QJavaScriptExecutor");
   }

}