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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.RunAssociatedScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptOutput;
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
      QInstance instance = TestUtils.defineInstance();
      QTableMetaData table = instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withField(new QFieldMetaData("testScriptId", QFieldType.INTEGER))
         .withAssociatedScript(new AssociatedScript()
            .withScriptTypeId(1)
            .withFieldName("testScriptId")
         );

      new ScriptsMetaDataProvider().defineStandardScriptsTables(instance, TestUtils.MEMORY_BACKEND_NAME, null);

      TestUtils.insertRecords(instance, table, List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)
      ));

      TestUtils.insertRecords(instance, instance.getTable("scriptType"), List.of(
         new QRecord().withValue("id", 1).withValue("name", "Test Script Type")
      ));

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

      assertThatThrownBy(() -> new RunAssociatedScriptAction().run(runAssociatedScriptInput, runAssociatedScriptOutput))
         .isInstanceOf(QException.class)
         .hasRootCauseInstanceOf(ClassNotFoundException.class)
         .hasRootCauseMessage("com.kingsrook.qqq.languages.javascript.QJavaScriptExecutor");
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