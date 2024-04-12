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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.QUploadedFile;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.state.StateType;
import com.kingsrook.qqq.backend.core.state.TempFileStateProvider;
import com.kingsrook.qqq.backend.core.state.UUIDAndTypeStateKey;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for full bulk insert process
 *******************************************************************************/
class BulkInsertTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow1()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","John","Doe","1980-01-01","john@doe.com"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvRow2()
   {
      return ("""
         "0","2021-10-26 14:39:37","2021-10-26 14:39:37","Jane","Doe","1981-01-01","john@doe.com"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getPersonCsvHeaderUsingLabels()
   {
      return ("""
         "Id","Create Date","Modify Date","First Name","Last Name","Birth Date","Email"
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      ///////////////////////////////////////
      // make sure table is empty to start //
      ///////////////////////////////////////
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY)).isEmpty();

      ////////////////////////////////////////////////////////////////
      // create an uploaded file, similar to how an http server may //
      ////////////////////////////////////////////////////////////////
      QUploadedFile qUploadedFile = new QUploadedFile();
      qUploadedFile.setBytes((getPersonCsvHeaderUsingLabels() + getPersonCsvRow1() + getPersonCsvRow2()).getBytes());
      qUploadedFile.setFilename("test.csv");
      UUIDAndTypeStateKey uploadedFileKey = new UUIDAndTypeStateKey(StateType.UPLOADED_FILE);
      TempFileStateProvider.getInstance().put(uploadedFileKey, qUploadedFile);

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.TABLE_NAME_PERSON_MEMORY + ".bulkInsert");
      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      String           processUUID      = runProcessOutput.getProcessUUID();

      runProcessInput.setProcessUUID(processUUID);
      runProcessInput.setStartAfterStep("upload");
      runProcessInput.addValue(QUploadedFile.DEFAULT_UPLOADED_FILE_FIELD_NAME, uploadedFileKey);
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getRecords()).hasSize(2);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("review");

      runProcessInput.addValue(StreamedETLWithFrontendProcess.FIELD_DO_FULL_VALIDATION, true);
      runProcessInput.setStartAfterStep("review");
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getRecords()).hasSize(2);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("review");
      assertThat(runProcessOutput.getValues().get(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY)).isNotNull().isInstanceOf(List.class);

      runProcessInput.setStartAfterStep("review");
      runProcessOutput = new RunProcessAction().execute(runProcessInput);
      assertThat(runProcessOutput.getRecords()).hasSize(2);
      assertThat(runProcessOutput.getProcessState().getNextStepName()).isPresent().get().isEqualTo("result");
      assertThat(runProcessOutput.getValues().get(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY)).isNotNull().isInstanceOf(List.class);
      assertThat(runProcessOutput.getException()).isEmpty();

      ////////////////////////////////////
      // query for the inserted records //
      ////////////////////////////////////
      List<QRecord> records = TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertEquals("John", records.get(0).getValueString("firstName"));
      assertEquals("Jane", records.get(1).getValueString("firstName"));
      assertNotNull(records.get(0).getValue("id"));
      assertNotNull(records.get(1).getValue("id"));
   }

}