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


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.reporting.GenerateReportActionTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.ReportColumns;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReportsMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for SavedReportsTableFullVerifier 
 *******************************************************************************/
class SavedReportsTableFullVerifierTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      new SavedReportsMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);
      GenerateReportActionTest.insertPersonRecords(QContext.getQInstance());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      ReportColumns reportColumns = new ReportColumns();
      reportColumns.withColumn("id");

      //////////////////////////////////
      // insert a saved report record //
      //////////////////////////////////
      SavedReport savedReport = new SavedReport();
      savedReport.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      savedReport.setLabel("Test");
      savedReport.setColumnsJson(JsonUtils.toJson(reportColumns));
      savedReport.setQueryFilterJson(JsonUtils.toJson(new QQueryFilter()));
      List<QRecord> reportRecordList = new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(savedReport)).getRecords();

      SavedReportsTableFullVerifier savedReportsTableFullVerifier = new SavedReportsTableFullVerifier();
      savedReportsTableFullVerifier.verify(reportRecordList, SavedReportsMetaDataProvider.REPORT_STORAGE_TABLE_NAME);
   }

}