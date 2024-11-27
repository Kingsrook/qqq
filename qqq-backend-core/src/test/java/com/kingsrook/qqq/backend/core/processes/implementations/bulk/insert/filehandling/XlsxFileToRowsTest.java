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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.filehandling;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportAction;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest;
import com.kingsrook.qqq.backend.core.actions.reporting.excel.fastexcel.ExcelFastexcelExportStreamer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportDestination;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadFileRow;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest.REPORT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/*******************************************************************************
 ** Unit test for XlsxFileToRows 
 *******************************************************************************/
class XlsxFileToRowsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException, IOException
   {
      byte[] byteArray = writeExcelBytes();

      FileToRowsInterface fileToRowsInterface = FileToRowsInterface.forFile("someFile.xlsx", new ByteArrayInputStream(byteArray));

      BulkLoadFileRow headerRow = fileToRowsInterface.next();
      BulkLoadFileRow bodyRow   = fileToRowsInterface.next();

      assertEquals(new BulkLoadFileRow(new String[] {"Id", "First Name", "Last Name", "Birth Date"}, 1), headerRow);
      assertEquals(new BulkLoadFileRow(new Serializable[] {1, "Darin", "Jonson", LocalDateTime.of(1980, Month.JANUARY, 31, 0, 0)}, 2), bodyRow);

      ///////////////////////////////////////////////////////////////////////////////////////
      // make sure there's at least a limit (less than 20) to how many more rows there are //
      ///////////////////////////////////////////////////////////////////////////////////////
      int otherRowCount = 0;
      while(fileToRowsInterface.hasNext() && otherRowCount < 20)
      {
         fileToRowsInterface.next();
         otherRowCount++;
      }
      assertFalse(fileToRowsInterface.hasNext());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static byte[] writeExcelBytes() throws QException, IOException
   {
      ReportFormat          format = ReportFormat.XLSX;
      ByteArrayOutputStream baos   = new ByteArrayOutputStream();

      QInstance qInstance = QContext.getQInstance();
      qInstance.addReport(GenerateReportActionTest.defineTableOnlyReport());
      GenerateReportActionTest.insertPersonRecords(qInstance);

      ReportInput reportInput = new ReportInput();
      reportInput.setReportName(REPORT_NAME);
      reportInput.setReportDestination(new ReportDestination().withReportFormat(format).withReportOutputStream(baos));
      reportInput.setInputValues(Map.of("startDate", LocalDate.of(1970, Month.MAY, 15), "endDate", LocalDate.now()));
      reportInput.setOverrideExportStreamerSupplier(ExcelFastexcelExportStreamer::new);
      new GenerateReportAction().execute(reportInput);

      byte[] byteArray = baos.toByteArray();
      // FileUtils.writeByteArrayToFile(new File("/tmp/xlsx.xlsx"), byteArray);
      return byteArray;
   }

}