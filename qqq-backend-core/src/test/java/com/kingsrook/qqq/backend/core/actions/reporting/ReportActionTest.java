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

package com.kingsrook.qqq.backend.core.actions.reporting;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for the ReportAction
 *******************************************************************************/
class ReportActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCSV() throws Exception
   {
      int    recordCount = 1000;
      String filename    = "/tmp/ReportActionTest.csv";

      runReport(recordCount, filename, ReportFormat.CSV, false);

      File         file      = new File(filename);
      List<String> fileLines = FileUtils.readLines(file, StandardCharsets.UTF_8.name());
      assertEquals(recordCount + 1, fileLines.size());
      assertTrue(file.delete());
   }



   /*******************************************************************************
    ** This test runs for more records, to stress more of the pipe-filling and
    ** other bits of the ReportAction.
    *******************************************************************************/
   @Test
   public void testBigger() throws Exception
   {
      // int    recordCount = 2_000_000; // to really stress locally, use this.
      int    recordCount = 50_000;
      String filename    = "/tmp/ReportActionTest.csv";

      runReport(recordCount, filename, ReportFormat.CSV, false);

      File         file      = new File(filename);
      List<String> fileLines = FileUtils.readLines(file, StandardCharsets.UTF_8.name());
      assertEquals(recordCount + 1, fileLines.size());
      assertTrue(file.delete());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testExcel() throws Exception
   {
      int    recordCount = 1000;
      String filename    = "/tmp/ReportActionTest.xlsx";

      runReport(recordCount, filename, ReportFormat.XLSX, true);

      File file = new File(filename);

      assertTrue(file.delete());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runReport(int recordCount, String filename, ReportFormat reportFormat, boolean specifyFields) throws IOException, QException
   {
      try(FileOutputStream outputStream = new FileOutputStream(filename))
      {
         ReportInput reportInput = new ReportInput(TestUtils.defineInstance(), TestUtils.getMockSession());
         reportInput.setTableName("person");
         QTableMetaData table = reportInput.getTable();

         reportInput.setReportFormat(reportFormat);
         reportInput.setReportOutputStream(outputStream);
         reportInput.setQueryFilter(new QQueryFilter());
         reportInput.setLimit(recordCount);

         if(specifyFields)
         {
            reportInput.setFieldNames(table.getFields().values().stream().map(QFieldMetaData::getName).collect(Collectors.toList()));
         }
         ReportOutput reportOutput = new ReportAction().execute(reportInput);
         assertNotNull(reportOutput);
         assertEquals(recordCount, reportOutput.getRecordCount());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadFieldNames()
   {
      ReportInput reportInput = new ReportInput(TestUtils.defineInstance(), TestUtils.getMockSession());
      reportInput.setTableName("person");
      reportInput.setFieldNames(List.of("Foo", "Bar", "Baz"));
      assertThrows(QUserFacingException.class, () ->
      {
         new ReportAction().execute(reportInput);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPreExecuteCount() throws QException
   {
      ReportInput reportInput = new ReportInput(TestUtils.defineInstance(), TestUtils.getMockSession());
      reportInput.setTableName("person");

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // use xlsx, which has a max-rows limit, to verify that code runs, but doesn't throw when there aren't too many rows //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      reportInput.setReportFormat(ReportFormat.XLSX);

      new ReportAction().preExecute(reportInput);

      ////////////////////////////////////////////////////////////////////////////
      // nothing to assert - but if preExecute throws, then the test will fail. //
      ////////////////////////////////////////////////////////////////////////////
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTooManyColumns() throws QException
   {
      QTableMetaData wideTable = new QTableMetaData()
         .withName("wide")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME);
      for(int i = 0; i < ReportFormat.XLSX.getMaxCols() + 1; i++)
      {
         wideTable.addField(new QFieldMetaData("field" + i, QFieldType.STRING));
      }

      QInstance qInstance = TestUtils.defineInstance();
      qInstance.addTable(wideTable);

      ReportInput reportInput = new ReportInput(qInstance, TestUtils.getMockSession());
      reportInput.setTableName("wide");

      ////////////////////////////////////////////////////////////////
      // use xlsx, which has a max-cols limit, to verify that code. //
      ////////////////////////////////////////////////////////////////
      reportInput.setReportFormat(ReportFormat.XLSX);

      assertThrows(QUserFacingException.class, () ->
      {
         new ReportAction().preExecute(reportInput);
      });
   }

}