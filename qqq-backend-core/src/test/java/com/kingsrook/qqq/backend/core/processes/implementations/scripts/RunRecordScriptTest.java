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


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.tables.QQQTablesMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Test full process of RunRecordScript (should its Extract and Load classes).
 *******************************************************************************/
class RunRecordScriptTest extends BaseTest
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

      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(new QRecord().withValue("id", 1)));
      TestUtils.insertRecords(qInstance, qInstance.getTable(Script.TABLE_NAME), List.of(new QRecord().withValue("id", 1).withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)));

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(ScriptsMetaDataProvider.RUN_RECORD_SCRIPT_PROCESS_NAME);
      runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      runProcessInput.setValues(MapBuilder.of(
         "recordIds", "1,2,3",
         "tableName", TestUtils.TABLE_NAME_PERSON_MEMORY,
         "scriptId", 1
      ));

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // so, turns out, we don't know how to do a join yet in memory backend, so this can't quite work //
      // still good to run the code and at least get this far w/ an expected exception.                //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      System.out.println(runProcessOutput);
      assertTrue(((List<ProcessSummaryLineInterface>) runProcessOutput.getValues().get("processResults")).stream().anyMatch(psli -> psli instanceof ProcessSummaryLine psl && psl.getMessage().contains("error that was not logged")));
   }

}