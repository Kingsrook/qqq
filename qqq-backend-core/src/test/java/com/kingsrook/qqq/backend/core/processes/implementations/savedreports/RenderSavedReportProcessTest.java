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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormatPossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableDefinition;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableFunction;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableGroupBy;
import com.kingsrook.qqq.backend.core.model.actions.reporting.pivottable.PivotTableValue;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.ReportColumns;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReportsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.LocalMacDevUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
   @BeforeEach
   void beforeEach() throws Exception
   {
      new SavedReportsMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, null);
      GenerateReportActionTest.insertPersonRecords(QContext.getQInstance());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled
   void testForDevPrintAPivotDefinitionAsJson()
   {
      System.out.println(JsonUtils.toPrettyJson(new PivotTableDefinition()
         .withRow(new PivotTableGroupBy()
            .withFieldName("homeStateId"))
         .withRow(new PivotTableGroupBy()
            .withFieldName("firstName"))
         .withValue(new PivotTableValue()
            .withFieldName("id")
            .withFunction(PivotTableFunction.COUNT))
         .withValue(new PivotTableValue()
            .withFieldName("cost")
            .withFunction(PivotTableFunction.SUM))
      ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTableOnlyReport() throws Exception
   {
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
         ]}""";

      QRecord savedReport = new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withLabel(label)
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withColumnsJson(columnsJson)
         .withQueryFilterJson(JsonUtils.toJson(new QQueryFilter()))
      )).getRecords().get(0);

      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.CSV);

      String downloadFileName = runProcessOutput.getValueString("downloadFileName");
      String serverFilePath   = runProcessOutput.getValueString("serverFilePath");

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



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord insertBasicSavedPivotReport(String label) throws QException
   {
      return new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withLabel(label)
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withColumnsJson(JsonUtils.toJson(new ReportColumns()
            .withColumn("id")
            .withColumn("firstName")
            .withColumn("lastName")
            .withColumn("cost")
            .withColumn("birthDate")
            .withColumn("homeStateId")))
         .withQueryFilterJson(JsonUtils.toJson(new QQueryFilter()))
         .withPivotTableJson(JsonUtils.toJson(new PivotTableDefinition()
            .withRow(new PivotTableGroupBy()
               .withFieldName("homeStateId"))
            .withRow(new PivotTableGroupBy()
               .withFieldName("firstName"))
            .withValue(new PivotTableValue()
               .withFieldName("id")
               .withFunction(PivotTableFunction.COUNT))
            .withValue(new PivotTableValue()
               .withFieldName("cost")
               .withFunction(PivotTableFunction.SUM))
            .withValue(new PivotTableValue()
               .withFieldName("birthDate")
               .withFunction(PivotTableFunction.MIN))
            .withValue(new PivotTableValue()
               .withFieldName("birthDate")
               .withFunction(PivotTableFunction.MAX))
         ))
      )).getRecords().get(0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivotXlsx() throws Exception
   {
      String           label            = "Test Pivot Report";
      QRecord          savedReport      = insertBasicSavedPivotReport(label);
      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.XLSX);

      String serverFilePath = runProcessOutput.getValueString("serverFilePath");
      System.out.println(serverFilePath);

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      LocalMacDevUtils.mayOpenFiles = true;
      LocalMacDevUtils.openFile(serverFilePath, "/Applications/Numbers.app");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivotJson() throws Exception
   {
      String           label            = "Test Pivot Report JSON";
      QRecord          savedReport      = insertBasicSavedPivotReport(label);
      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.JSON);

      String serverFilePath = runProcessOutput.getValueString("serverFilePath");
      System.out.println(serverFilePath);

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      String json = FileUtils.readFileToString(serverFile, StandardCharsets.UTF_8);
      System.out.println(json);

      JSONArray jsonArray = new JSONArray(json);
      assertEquals(2, jsonArray.length());

      JSONObject firstView = jsonArray.getJSONObject(0);
      assertEquals(label, firstView.getString("name"));
      JSONArray firstViewData = firstView.getJSONArray("data");
      assertEquals(6, firstViewData.length());
      assertThat(firstViewData.getJSONObject(0).toMap())
         .hasFieldOrPropertyWithValue("id", 1)
         .hasFieldOrPropertyWithValue("firstName", "Darin");

      JSONObject pivotView = jsonArray.getJSONObject(1);
      assertEquals("Pivot Table", pivotView.getString("name"));
      JSONArray pivotViewData = pivotView.getJSONArray("data");
      assertEquals(4, pivotViewData.length());
      assertThat(pivotViewData.getJSONObject(0).toMap())
         .hasFieldOrPropertyWithValue("homeState", "IL")
         .hasFieldOrPropertyWithValue("firstName", "Darin")
         .hasFieldOrPropertyWithValue("countOfId", 3)
         .hasFieldOrPropertyWithValue("sumOfCost", new BigDecimal("1.50"));
      assertThat(pivotViewData.getJSONObject(3).toMap())
         .hasFieldOrPropertyWithValue("homeState", "Totals")
         .hasFieldOrPropertyWithValue("countOfId", 6)
         .hasFieldOrPropertyWithValue("sumOfCost", new BigDecimal("12.00"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivotCSV() throws Exception
   {
      String           label            = "Test Pivot Report CSV";
      QRecord          savedReport      = insertBasicSavedPivotReport(label);
      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.CSV);

      String serverFilePath = runProcessOutput.getValueString("serverFilePath");
      System.out.println(serverFilePath);

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      List<String> csv = FileUtils.readLines(serverFile, StandardCharsets.UTF_8);
      System.out.println(csv);

      assertEquals("""
         "Home State","First Name","Count Of Id","Sum Of Cost","Min Of Birth Date","Max Of Birth Date\"""", csv.get(0));

      assertEquals("""
         "Totals","","6","12.00","1979-12-30","1980-03-20\"""", csv.get(4));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord insertSavedPivotReportWithAllFunctions(String label) throws QException
   {
      PivotTableDefinition pivotTableDefinition = new PivotTableDefinition()
         .withRow(new PivotTableGroupBy().withFieldName("firstName"));

      for(PivotTableFunction function : PivotTableFunction.values())
      {
         pivotTableDefinition.withValue(new PivotTableValue().withFieldName("cost").withFunction(function));
      }

      return new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withLabel(label)
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withColumnsJson(JsonUtils.toJson(new ReportColumns()
            .withColumn("id")
            .withColumn("firstName")
            .withColumn("cost")))
         .withQueryFilterJson(JsonUtils.toJson(new QQueryFilter()))
         .withPivotTableJson(JsonUtils.toJson(pivotTableDefinition))
      )).getRecords().get(0);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivotXlsxAllFunctions() throws Exception
   {
      String           label            = "Test Pivot Report";
      QRecord          savedReport      = insertSavedPivotReportWithAllFunctions(label);
      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.XLSX);

      String serverFilePath = runProcessOutput.getValueString("serverFilePath");
      System.out.println(serverFilePath);

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      // LocalMacDevUtils.mayOpenFiles = true;
      LocalMacDevUtils.openFile(serverFilePath, "/Applications/Numbers.app");
      LocalMacDevUtils.openFile(serverFilePath);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPivotCSVAllFunctions() throws Exception
   {
      String           label            = "Test Pivot Report CSV";
      QRecord          savedReport      = insertSavedPivotReportWithAllFunctions(label);
      RunProcessOutput runProcessOutput = runRenderReportProcess(savedReport, ReportFormatPossibleValueEnum.CSV);

      String serverFilePath = runProcessOutput.getValueString("serverFilePath");
      System.out.println(serverFilePath);

      File serverFile = new File(serverFilePath);
      assertTrue(serverFile.exists());

      List<String> csv = FileUtils.readLines(serverFile, StandardCharsets.UTF_8);
      System.out.println(csv);

      assertEquals("""
         "First Name","Average Of Cost","Count Of Cost","Count_nums Of Cost","Max Of Cost","Min Of Cost","Product Of Cost","Std_dev Of Cost","Std_devp Of Cost","Sum Of Cost","Var Of Cost","Varp Of Cost\"""", csv.get(0));

      assertEquals("""
         "Totals","2.0","6","6","3.50","0.50","5.359375000000","1.6432","1.5000","12.00","2.7000","2.2500\"""", csv.get(4));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static RunProcessOutput runRenderReportProcess(QRecord savedReport, ReportFormatPossibleValueEnum reportFormat) throws QException
   {
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(RenderSavedReportMetaDataProducer.NAME);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      input.setCallback(QProcessCallbackFactory.forRecord(savedReport));
      input.addValue("reportFormat", reportFormat.getPossibleValueId());
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);
      return runProcessOutput;
   }

}