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
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.StoreAssociatedScriptOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.AssociatedScript;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for StoreAssociatedScriptAction
 *******************************************************************************/
class StoreAssociatedScriptActionTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



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
         )
         .withField(new QFieldMetaData("otherScriptId", QFieldType.INTEGER))
         .withAssociatedScript(new AssociatedScript()
            .withScriptTypeId(2)
            .withFieldName("otherScriptId")
         );

      new ScriptsMetaDataProvider().defineStandardScriptsTables(instance, TestUtils.MEMORY_BACKEND_NAME, null);

      TestUtils.insertRecords(instance, table, List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2),
         new QRecord().withValue("id", 3)
      ));

      TestUtils.insertRecords(instance, instance.getTable("scriptType"), List.of(
         new QRecord().withValue("id", 1).withValue("name", "Test Script"),
         new QRecord().withValue("id", 2).withValue("name", "Other Script")
      ));

      StoreAssociatedScriptInput storeAssociatedScriptInput = new StoreAssociatedScriptInput(instance);
      storeAssociatedScriptInput.setSession(new QSession());
      storeAssociatedScriptInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      storeAssociatedScriptInput.setRecordPrimaryKey(1);
      storeAssociatedScriptInput.setCode("var i = 0;");
      storeAssociatedScriptInput.setCommitMessage("Test commit");
      storeAssociatedScriptInput.setFieldName("testScriptId");
      StoreAssociatedScriptOutput storeAssociatedScriptOutput = new StoreAssociatedScriptOutput();

      ///////////////////////////////////////////////
      // insert 1st version of script for record 1 //
      ///////////////////////////////////////////////
      new StoreAssociatedScriptAction().run(storeAssociatedScriptInput, storeAssociatedScriptOutput);
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 1, "testScriptId", 1);
      assertValueInField(instance, "script", 1, "currentScriptRevisionId", 1);

      ////////////////////////////////////////////
      // add 2nd version of script for record 1 //
      ////////////////////////////////////////////
      storeAssociatedScriptInput.setCode("var i = 1;");
      storeAssociatedScriptInput.setCommitMessage("2nd commit");
      new StoreAssociatedScriptAction().run(storeAssociatedScriptInput, storeAssociatedScriptOutput);
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 1, "testScriptId", 1);
      assertValueInField(instance, "script", 1, "currentScriptRevisionId", 2);

      ///////////////////////////////////////////////
      // insert 1st version of script for record 3 //
      ///////////////////////////////////////////////
      storeAssociatedScriptInput.setRecordPrimaryKey(3);
      storeAssociatedScriptInput.setCode("var i = 2;");
      storeAssociatedScriptInput.setCommitMessage("First Commit here");
      new StoreAssociatedScriptAction().run(storeAssociatedScriptInput, storeAssociatedScriptOutput);
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 3, "testScriptId", 2);
      assertValueInField(instance, "script", 2, "currentScriptRevisionId", 3);

      /////////////////////////////////////
      // make sure no script on record 2 //
      /////////////////////////////////////
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 2, "testScriptId", null);

      ////////////////////////////////////
      // add another script to record 1 //
      ////////////////////////////////////
      storeAssociatedScriptInput.setRecordPrimaryKey(1);
      storeAssociatedScriptInput.setCode("var i = 3;");
      storeAssociatedScriptInput.setCommitMessage("Other field");
      storeAssociatedScriptInput.setFieldName("otherScriptId");
      new StoreAssociatedScriptAction().run(storeAssociatedScriptInput, storeAssociatedScriptOutput);
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 1, "testScriptId", 1);
      assertValueInField(instance, TestUtils.TABLE_NAME_PERSON_MEMORY, 1, "otherScriptId", 3);
      assertValueInField(instance, "script", 3, "currentScriptRevisionId", 4);

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Serializable assertValueInField(QInstance instance, String tableName, Serializable recordId, String fieldName, Serializable value) throws QException
   {
      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(tableName);
      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria("id", QCriteriaOperator.EQUALS, List.of(recordId))));
      QueryOutput  queryOutput = new QueryAction().execute(queryInput);
      Serializable actual      = queryOutput.getRecords().get(0).getValue(fieldName);
      assertEquals(value, actual);
      return (actual);
   }

}