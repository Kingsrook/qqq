/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.savedreports;


import java.io.File;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReportsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.LocalMacDevUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for RenderSavedReportExecuteStep 
 *******************************************************************************/
class RenderSavedReportProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      new SavedReportsMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, null);

      String label = "Test Report";

      //////////////////////////////////////////////////////////////////////////////////////////
      // do columns json as a string, rather than a toJson'ed ReportColumns object,           //
      // to help verify that we don't choke on un-recognized properties (e.g., as QFMD sends) //
      //////////////////////////////////////////////////////////////////////////////////////////
      String columnsJson = """
         {"columns":[
               {"name": "k"},
               {"name": "id"},
               {"name": "firstName", "isVisible": true},
               {"name": "lastName", "pinned": "left"},
               {"name": "createDate", "isVisible": false}
         ]}
         """;

      QRecord savedReport = new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withLabel(label)
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withColumnsJson(columnsJson)
         .withQueryFilterJson(JsonUtils.toJson(new QQueryFilter()))
      )).getRecords().get(0);

      GenerateReportActionTest.insertPersonRecords(QContext.getQInstance());

      RunProcessInput input = new RunProcessInput();
      input.setProcessName(RenderSavedReportMetaDataProducer.NAME);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      input.setCallback(QProcessCallbackFactory.forRecord(savedReport));
      input.addValue("reportFormat", ReportFormatPossibleValueEnum.CSV.getPossibleValueId());
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

      String downloadFileName = runProcessOutput.getValueString("downloadFileName");
      String serverFilePath = runProcessOutput.getValueString("serverFilePath");

      assertThat(downloadFileName)
         .startsWith(label + " - ")
         .matches(".*\\d\\d\\d\\d-\\d\\d-\\d\\d-\\d\\d\\d\\d.*")
         .endsWith(".csv");

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      List<String> lines = FileUtils.readLines(serverFile);
      assertEquals("""
         "Id","First Name","Last Name"
         """.trim(), lines.get(0));
      assertEquals("""
         "1","Darin","Jonson"
         """.trim(), lines.get(1));

      LocalMacDevUtils.openFile(serverFilePath);
   }

}