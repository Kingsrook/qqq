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

package com.kingsrook.qqq.backend.core.processes.implementations.reports;


import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for BasicRunReportProcess
 *******************************************************************************/
class RunReportForRecordProcessTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRunReport() throws QException
   {
      QInstance instance = TestUtils.defineInstance();
      TestUtils.insertDefaultShapes(instance);

      RunProcessInput runProcessInput = new RunProcessInput(instance);
      runProcessInput.setSession(TestUtils.getMockSession());
      runProcessInput.setProcessName(TestUtils.PROCESS_NAME_RUN_SHAPES_PERSON_REPORT);
      runProcessInput.addValue(BasicRunReportProcess.FIELD_REPORT_NAME, TestUtils.REPORT_NAME_SHAPES_PERSON);
      runProcessInput.addValue("recordsParam", "recordIds");
      runProcessInput.addValue("recordIds", "1");

      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);

      // runProcessOutput = new RunProcessAction().execute(runProcessInput);
      // assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo(BasicRunReportProcess.STEP_NAME_ACCESS);
      // assertThat(runProcessOutput.getValues()).containsKeys("downloadFileName", "serverFilePath");
   }

}