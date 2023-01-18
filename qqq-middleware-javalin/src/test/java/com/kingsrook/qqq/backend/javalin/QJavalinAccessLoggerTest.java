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

package com.kingsrook.qqq.backend.javalin;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for QJavalinAccessLogger
 *******************************************************************************/
class QJavalinAccessLoggerTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTurnedOff() throws QInstanceValidationException
   {
      QInstance qInstance = TestUtils.defineInstance();
      new QJavalinImplementation(qInstance, new QJavalinMetaData()
         .withLogAllAccessStarts(false)
         .withLogAllAccessEnds(false));

      QJavalinAccessLogger.logStart("test");
      QJavalinAccessLogger.logEndSuccess();
      QJavalinAccessLogger.logEndFail(new Exception());
      QJavalinAccessLogger.logEndSuccessIfSlow(1000);
      QJavalinAccessLogger.logProcessSummary("testProcess", UUID.randomUUID().toString(), new RunProcessOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSlow()
   {
      QJavalinAccessLogger.logStart("test");
      SleepUtils.sleep(2, TimeUnit.MILLISECONDS);
      QJavalinAccessLogger.logEndSuccessIfSlow(1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLogProcessSummary()
   {
      RunProcessOutput runProcessOutput = new RunProcessOutput();
      runProcessOutput.addValue(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY, new ArrayList<>(List.of(
         new ProcessSummaryLine(Status.OK, 5, "Test")
      )));
      QJavalinAccessLogger.logProcessSummary("testProcess", UUID.randomUUID().toString(), runProcessOutput);

      runProcessOutput = new RunProcessOutput();
      runProcessOutput.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, new ArrayList<>(List.of(
         new ProcessSummaryLine(Status.OK, 5, "Test")
      )));
      QJavalinAccessLogger.logProcessSummary("testProcess", UUID.randomUUID().toString(), runProcessOutput);

   }

}