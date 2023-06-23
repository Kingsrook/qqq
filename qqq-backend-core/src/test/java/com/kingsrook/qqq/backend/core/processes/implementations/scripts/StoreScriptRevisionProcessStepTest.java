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
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevision;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptRevisionFile;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for StoreScriptRevisionProcessStep
 *******************************************************************************/
class StoreScriptRevisionProcessStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSingleFileScriptType() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      new ScriptsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      Integer scriptId       = 1701;
      String  scriptContents = "logger.log('Hi');";

      TestUtils.insertRecords(qInstance, qInstance.getTable(Script.TABLE_NAME), List.of(new QRecord().withValue("id", scriptId)));
      List<QRecord> scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertNull(scripts.get(0).getValueInteger("currentScriptRevisionId"));

      new StoreScriptRevisionProcessStep().run(new RunBackendStepInput().withValues(MapBuilder.of(
         "scriptId", scriptId,
         "contents", scriptContents
      )), new RunBackendStepOutput());

      scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertEquals(1, scripts.get(0).getValueInteger("currentScriptRevisionId"));

      List<QRecord> scriptRevisions = TestUtils.queryTable(ScriptRevision.TABLE_NAME);
      QRecord       scriptRevision  = scriptRevisions.get(0);
      assertEquals(scriptId, scriptRevision.getValueInteger("scriptId"));
      assertEquals(1, scriptRevision.getValueInteger("sequenceNo"));
      assertEquals("Initial version", scriptRevision.getValueString("commitMessage"));
      assertEquals(scriptContents, scriptRevision.getValueString("contents"));

      new StoreScriptRevisionProcessStep().run(new RunBackendStepInput().withValues(MapBuilder.of(
         "scriptId", scriptId,
         "contents", scriptContents
      )), new RunBackendStepOutput());

      scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertEquals(2, scripts.get(0).getValueInteger("currentScriptRevisionId"));

      scriptRevisions = TestUtils.queryTable(ScriptRevision.TABLE_NAME).stream().filter(r -> r.getValueInteger("id").equals(2)).collect(Collectors.toList());
      scriptRevision = scriptRevisions.get(0);
      assertEquals(scriptId, scriptRevision.getValueInteger("scriptId"));
      assertEquals(2, scriptRevision.getValueInteger("sequenceNo"));
      assertEquals("No commit message given", scriptRevision.getValueString("commitMessage"));
      assertEquals(scriptContents, scriptRevision.getValueString("contents"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMultiFileScriptType() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      new ScriptsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      Integer scriptId         = 1701;
      String  scriptContents   = "logger.log('Hi');";
      String  templateContents = "<h1>Hey</h1>";

      TestUtils.insertRecords(qInstance, qInstance.getTable(Script.TABLE_NAME), List.of(new QRecord().withValue("id", scriptId)));
      List<QRecord> scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertNull(scripts.get(0).getValueInteger("currentScriptRevisionId"));

      ArrayList<QRecord> fileContents = new ArrayList<>();
      fileContents.add(new QRecord().withValue("fileName", "script").withValue("contents", scriptContents));
      fileContents.add(new QRecord().withValue("fileName", "template").withValue("contents", templateContents));

      RunBackendStepInput runBackendStepInput = new RunBackendStepInput();
      runBackendStepInput.addValue("scriptId", scriptId);
      runBackendStepInput.addValue("fileContents", fileContents);
      new StoreScriptRevisionProcessStep().run(runBackendStepInput, new RunBackendStepOutput());

      scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertEquals(1, scripts.get(0).getValueInteger("currentScriptRevisionId"));

      List<QRecord> scriptRevisions = TestUtils.queryTable(ScriptRevision.TABLE_NAME);
      QRecord       scriptRevision  = scriptRevisions.get(0);
      assertEquals(scriptId, scriptRevision.getValueInteger("scriptId"));
      assertEquals(1, scriptRevision.getValueInteger("sequenceNo"));
      assertEquals("Initial version", scriptRevision.getValueString("commitMessage"));
      assertNull(scriptRevision.getValueString("contents"));

      List<QRecord> scriptRevisionFiles = TestUtils.queryTable(ScriptRevisionFile.TABLE_NAME);
      assertThat(scriptRevisionFiles.stream().filter(srf -> srf.getValueString("fileName").equals("script")).findFirst())
         .isPresent().get()
         .matches(r -> r.getValueString("contents").equals(scriptContents));

      assertThat(scriptRevisionFiles.stream().filter(srf -> srf.getValueString("fileName").equals("template")).findFirst())
         .isPresent().get()
         .matches(r -> r.getValueString("contents").equals(templateContents));

      ////////////////////////////
      // now add a new revision //
      ////////////////////////////
      String updatedScriptContents   = "logger.log('Really, Hi');";
      String updatedTemplateContents = "<h1>Hey, what's up</h1>";

      fileContents = new ArrayList<>();
      fileContents.add(new QRecord().withValue("fileName", "script").withValue("contents", updatedScriptContents));
      fileContents.add(new QRecord().withValue("fileName", "template").withValue("contents", updatedTemplateContents));

      runBackendStepInput = new RunBackendStepInput();
      runBackendStepInput.addValue("scriptId", scriptId);
      runBackendStepInput.addValue("fileContents", fileContents);
      runBackendStepInput.addValue("commitMessage", "Updated files");
      new StoreScriptRevisionProcessStep().run(runBackendStepInput, new RunBackendStepOutput());

      scripts = TestUtils.queryTable(Script.TABLE_NAME);
      assertEquals(2, scripts.get(0).getValueInteger("currentScriptRevisionId"));

      scriptRevisions = TestUtils.queryTable(ScriptRevision.TABLE_NAME).stream().filter(r -> r.getValueInteger("id").equals(2)).collect(Collectors.toList());
      scriptRevision = scriptRevisions.get(0);
      assertEquals(scriptId, scriptRevision.getValueInteger("scriptId"));
      assertEquals(2, scriptRevision.getValueInteger("id"));
      assertEquals(2, scriptRevision.getValueInteger("sequenceNo"));
      assertEquals("Updated files", scriptRevision.getValueString("commitMessage"));
      assertNull(scriptRevision.getValueString("contents"));

      scriptRevisionFiles = TestUtils.queryTable(ScriptRevisionFile.TABLE_NAME);
      assertThat(scriptRevisionFiles.stream().filter(srf -> srf.getValueString("fileName").equals("script") && srf.getValueInteger("scriptRevisionId").equals(2)).findFirst())
         .isPresent().get()
         .matches(r -> r.getValueString("contents").equals(updatedScriptContents));

      assertThat(scriptRevisionFiles.stream().filter(srf -> srf.getValueString("fileName").equals("template") && srf.getValueInteger("scriptRevisionId").equals(2)).findFirst())
         .isPresent().get()
         .matches(r -> r.getValueString("contents").equals(updatedTemplateContents));
   }

}