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

package com.kingsrook.qqq.backend.core.processes.implementations.sharing;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.ReportColumns;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReportsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.savedreports.SharedSavedReport;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for GetSharedRecordsProcess 
 *******************************************************************************/
class GetSharedRecordsProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      new SavedReportsMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);

      new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withLabel("Test")
         .withColumnsJson(JsonUtils.toJson(new ReportColumns().withColumn("id")))));

      new InsertAction().execute(new InsertInput(SharedSavedReport.TABLE_NAME).withRecordEntity(new SharedSavedReport()
         .withScope(ShareScope.READ_WRITE.getPossibleValueId())
         .withUserId("007")
         .withSavedReportId(1)
      ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      RunBackendStepInput     input       = new RunBackendStepInput();
      RunBackendStepOutput    output      = new RunBackendStepOutput();
      GetSharedRecordsProcess processStep = new GetSharedRecordsProcess();

      input.addValue("tableName", SavedReport.TABLE_NAME);
      input.addValue("recordId", 1);
      processStep.run(input, output);

      List<QRecord> resultList = (List<QRecord>) output.getValue("resultList");
      assertEquals(1, resultList.size());

      QRecord outputRecord = resultList.get(0);
      assertEquals(1, outputRecord.getValueInteger("shareId"));
      assertEquals(ShareScope.READ_WRITE.getPossibleValueId(), outputRecord.getValueString("scopeId"));
      assertEquals("user", outputRecord.getValueString("audienceType"));
      assertEquals("007", outputRecord.getValueString("audienceId"));
      assertEquals("user 007", outputRecord.getValueString("audienceLabel"));
   }

}