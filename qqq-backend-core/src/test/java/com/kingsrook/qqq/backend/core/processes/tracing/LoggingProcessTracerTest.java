/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.tracing;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QCollectingLogger;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.CollectionAssert;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for LoggingProcessTracer 
 *******************************************************************************/
class LoggingProcessTracerTest extends BaseTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QLogger.deactivateCollectingLoggerForClass(LoggingProcessTracer.class);
   }



   /*******************************************************************************
    ** this test is based on RunProcessTest#testBreakOnFrontendSteps
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      /////////////////////////////////////////////////////
      // activate the tracer for this run of the process //
      /////////////////////////////////////////////////////
      RunProcessInput input = new RunProcessInput();
      input.addValue(RunProcessAction.PROCESS_TRACER_CODE_REFERENCE_FIELD, new QCodeReference(LoggingProcessTracer.class));
      QCollectingLogger collectingLogger = QLogger.activateCollectingLoggerForClass(LoggingProcessTracer.class);

      input.setProcessName(TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);
      input.setCallback(QProcessCallbackFactory.forRecord(new QRecord().withValue("id", 1)));
      RunProcessOutput result0 = new RunProcessAction().execute(input);
      assertNotNull(result0);

      CollectionAssert.assertThat(collectingLogger.getCollectedMessages())
         .matchesAll(List.of("Starting process", "Breaking process"),
            (s, clm) -> clm.getMessageAsJSONObject().getString("message").equals(s));
      collectingLogger.clear();

      ///////////////////////////////////////////////////
      // now re-run (resume) to the end of the process //
      ///////////////////////////////////////////////////
      input.setStartAfterStep(result0.getProcessState().getNextStepName().get());
      RunProcessOutput result1 = new RunProcessAction().execute(input);
      CollectionAssert.assertThat(collectingLogger.getCollectedMessages())
         .matchesAll(List.of("Resuming process", "Starting process step", "Finished process step", "Finished process"),
            (s, clm) -> clm.getMessageAsJSONObject().getString("message").equals(s));
   }

}