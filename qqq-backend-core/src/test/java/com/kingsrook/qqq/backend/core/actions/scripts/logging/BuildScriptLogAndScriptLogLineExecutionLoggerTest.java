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
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scripts.ScriptsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for BuildScriptLogAndScriptLogLineExecutionLogger
 *******************************************************************************/
class BuildScriptLogAndScriptLogLineExecutionLoggerTest extends BaseTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance instance = QContext.getQInstance();
      new ScriptsMetaDataProvider().defineAll(instance, TestUtils.MEMORY_BACKEND_NAME, null);
      ExecuteCodeInput executeCodeInput = new ExecuteCodeInput();
      executeCodeInput.setInput(Map.of("a", 1));

      BuildScriptLogAndScriptLogLineExecutionLogger logger = new BuildScriptLogAndScriptLogLineExecutionLogger(9999, 8888);
      logger.acceptExecutionStart(executeCodeInput);
      logger.acceptLogLine("This is a log");
      logger.acceptLogLine("This is also a log");
      logger.acceptExecutionEnd(true);

      QRecord scriptLog = logger.getScriptLog();
      assertNull(scriptLog.getValueInteger("id"));
      assertNotNull(scriptLog.getValue("startTimestamp"));
      assertNotNull(scriptLog.getValue("endTimestamp"));
      assertNotNull(scriptLog.getValue("runTimeMillis"));
      assertEquals(9999, scriptLog.getValueInteger("scriptId"));
      assertEquals(8888, scriptLog.getValueInteger("scriptRevisionId"));
      assertEquals("{a=1}", scriptLog.getValueString("input"));
      assertEquals("true", scriptLog.getValueString("output"));
      assertNull(scriptLog.getValueString("exception"));
      assertFalse(scriptLog.getValueBoolean("hadError"));

      List<QRecord> scriptLogLineRecords = logger.getScriptLogLines();
      assertEquals(2, scriptLogLineRecords.size());
      QRecord scriptLogLine = scriptLogLineRecords.get(0);
      assertNull(scriptLogLine.getValueInteger("scriptLogId"));
      assertNotNull(scriptLogLine.getValue("timestamp"));
      assertEquals("This is a log", scriptLogLine.getValueString("text"));
      scriptLogLine = scriptLogLineRecords.get(1);
      assertEquals("This is also a log", scriptLogLine.getValueString("text"));
   }
}