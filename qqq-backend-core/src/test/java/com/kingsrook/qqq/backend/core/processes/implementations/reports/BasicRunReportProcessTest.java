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


import java.time.LocalDate;
import java.time.Month;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for BasicRunReportProcess
 *******************************************************************************/
class BasicRunReportProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRunReport() throws QException
   {
      QInstance        instance         = TestUtils.defineInstance();
      QReportMetaData  report           = GenerateReportActionTest.definePersonShoesSummaryReport(true);
      QProcessMetaData runReportProcess = BasicRunReportProcess.defineProcessMetaData();

      instance.addReport(report);
      report.setProcessName(runReportProcess.getName());
      instance.addProcess(runReportProcess);

      reInitInstanceInContext(instance);

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(report.getProcessName());
      runProcessInput.addValue(BasicRunReportProcess.FIELD_REPORT_NAME, report.getName());
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      String           processUUID      = runProcessOutput.getProcessUUID();
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo(BasicRunReportProcess.STEP_NAME_INPUT);

      runProcessInput.addValue("startDate", LocalDate.of(1980, Month.JANUARY, 1));
      runProcessInput.addValue("endDate", LocalDate.of(2099, Month.DECEMBER, 31));
      runProcessInput.setStartAfterStep(BasicRunReportProcess.STEP_NAME_INPUT);
      runProcessInput.setProcessUUID(processUUID);

      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo(BasicRunReportProcess.STEP_NAME_ACCESS);
      assertThat(runProcessOutput.getValues()).containsKeys("downloadFileName", "serverFilePath");

      ///////////////////////////////////
      // assert we get xlsx by default //
      ///////////////////////////////////
      assertThat(runProcessOutput.getValueString("downloadFileName")).endsWith(".xlsx");

      /////////////////////////////////////////////////////
      // re-run, requesting CSV, then assert we get that //
      /////////////////////////////////////////////////////
      runProcessInput.addValue(BasicRunReportProcess.FIELD_REPORT_FORMAT, ReportFormat.CSV.name());
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getValueString("downloadFileName")).endsWith(".csv");
   }

}