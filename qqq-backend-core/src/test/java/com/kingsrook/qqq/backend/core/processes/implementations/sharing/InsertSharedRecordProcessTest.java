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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for InsertSharedRecordProcess 
 *******************************************************************************/
class InsertSharedRecordProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      new SavedReportsMetaDataProvider().defineAll(QContext.getQInstance(), TestUtils.MEMORY_BACKEND_NAME, TestUtils.MEMORY_BACKEND_NAME, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFailCases() throws QException
   {
      RunBackendStepInput       input       = new RunBackendStepInput();
      RunBackendStepOutput      output      = new RunBackendStepOutput();
      InsertSharedRecordProcess processStep = new InsertSharedRecordProcess();

      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("Missing required input: tableName");
      input.addValue("tableName", SavedReport.TABLE_NAME);
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("Missing required input: recordId");
      input.addValue("recordId", 1);
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("Missing required input: audienceType");
      input.addValue("audienceType", "user");
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("Missing required input: audienceId");
      input.addValue("audienceId", "darin@kingsrook.com");
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("Missing required input: scopeId");
      input.addValue("scopeId", ShareScope.READ_WRITE);

      //////////////////////////////
      // try a non-sharable table //
      //////////////////////////////
      input.addValue("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("is not shareable");
      input.addValue("tableName", SavedReport.TABLE_NAME);

      ///////////////////////////////////////////////////
      // fail because the requested record isn't found //
      ///////////////////////////////////////////////////
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("record could not be found in table, savedReport, with primary key: 1");
      new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withLabel("Test")
         .withColumnsJson(JsonUtils.toJson(new ReportColumns().withColumn("id")))
      ));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // now fail because a different user (than the owner, who did the initial insert) is trying to share //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      QContext.setQSession(newSession("not-" + DEFAULT_USER_ID));
      assertThatThrownBy(() -> processStep.run(input, output)).hasMessageContaining("not the owner of this record");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSuccess() throws QException
   {
      RunBackendStepInput       input       = new RunBackendStepInput();
      RunBackendStepOutput      output      = new RunBackendStepOutput();
      InsertSharedRecordProcess processStep = new InsertSharedRecordProcess();

      input.addValue("tableName", SavedReport.TABLE_NAME);
      input.addValue("recordId", 1);
      input.addValue("audienceType", "user");
      input.addValue("audienceId", "darin@kingsrook.com");
      input.addValue("scopeId", ShareScope.READ_WRITE);

      new InsertAction().execute(new InsertInput(SavedReport.TABLE_NAME).withRecordEntity(new SavedReport()
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withLabel("Test")
         .withColumnsJson(JsonUtils.toJson(new ReportColumns().withColumn("id")))
      ));

      ////////////////////////////////////////
      // assert the shared record got built //
      ////////////////////////////////////////
      processStep.run(input, output);

      QRecord sharedSavedReportRecord = new GetAction().executeForRecord(new GetInput(SharedSavedReport.TABLE_NAME).withPrimaryKey(1));
      assertNotNull(sharedSavedReportRecord);
      assertEquals(1, sharedSavedReportRecord.getValueInteger("savedReportId"));
      assertEquals("darin@kingsrook.com", sharedSavedReportRecord.getValueString("userId"));
      assertEquals(ShareScope.READ_WRITE.getPossibleValueId(), sharedSavedReportRecord.getValueString("scope"));
   }

}