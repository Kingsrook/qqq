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

package com.kingsrook.qqq.backend.core.actions.scripts.logging;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for StoreScriptLogAndScriptLogLineExecutionLogger
 *******************************************************************************/
class StoreScriptLogAndScriptLogLineExecutionLoggerTest
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
      new ScriptsMetaDataProvider().defineAll(instance, TestUtils.MEMORY_BACKEND_NAME, null);
      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput(instance);
      executeCodeInput.setSession(new QSession());
      executeCodeInput.setInput(Map.of("a", 1));

      StoreScriptLogAndScriptLogLineExecutionLogger logger = new StoreScriptLogAndScriptLogLineExecutionLogger(9999, 8888);
      logger.acceptExecutionStart(executeCodeInput);
      logger.acceptLogLine("This is a log");
      logger.acceptLogLine("This is also a log");
      logger.acceptExecutionEnd(true);

      List<QRecord> scriptLogRecords = TestUtils.queryTable(instance, "scriptLog");
      assertEquals(1, scriptLogRecords.size());
      QRecord scriptLog = scriptLogRecords.get(0);
      assertNotNull(scriptLog.getValueInteger("id"));
      assertNotNull(scriptLog.getValue("startTimestamp"));
      assertNotNull(scriptLog.getValue("endTimestamp"));
      assertNotNull(scriptLog.getValue("runTimeMillis"));
      assertEquals(9999, scriptLog.getValueInteger("scriptId"));
      assertEquals(8888, scriptLog.getValueInteger("scriptRevisionId"));
      assertEquals("{a=1}", scriptLog.getValueString("input"));
      assertEquals("true", scriptLog.getValueString("output"));
      assertNull(scriptLog.getValueString("exception"));
      assertFalse(scriptLog.getValueBoolean("hadError"));

      List<QRecord> scriptLogLineRecords = TestUtils.queryTable(instance, "scriptLogLine");
      assertEquals(2, scriptLogLineRecords.size());
      QRecord scriptLogLine = scriptLogLineRecords.get(0);
      assertEquals(scriptLog.getValueInteger("id"), scriptLogLine.getValueInteger("scriptLogId"));
      assertNotNull(scriptLogLine.getValue("timestamp"));
      assertEquals("This is a log", scriptLogLine.getValueString("text"));
      scriptLogLine = scriptLogLineRecords.get(1);
      assertEquals("This is also a log", scriptLogLine.getValueString("text"));
   }
}