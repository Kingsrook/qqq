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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit;


import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for full bulk edit process
 *******************************************************************************/
class BulkEditTest extends BaseTest
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
   @Test
   void test() throws QException
   {
      //////////////////////////////
      // insert some test records //
      //////////////////////////////
      QInstance qInstance = QContext.getQInstance();
      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin").withValue("lastName", "Kelkhoff").withValue("email", "darin.kelkhoff@kingsrook.com"),
         new QRecord().withValue("id", 2).withValue("firstName", "Tim").withValue("lastName", "Chamberlain").withValue("email", "tim.chamberlain@kingsrook.com"),
         new QRecord().withValue("id", 3).withValue("firstName", "James").withValue("lastName", "Maes").withValue("email", "james.maes@kingsrook.com")
      ));

      //////////////////////////////////
      // set up the run-process input //
      //////////////////////////////////
      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(TestUtils.TABLE_NAME_PERSON_MEMORY + ".bulkEdit");
      runProcessInput.addValue(StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER,
         new QQueryFilter().withCriteria(new QFilterCriteria("id", QCriteriaOperator.IN, List.of(1, 2))));

      RunProcessOutput runProcessOutput = new RunProcessAction().execute(runProcessInput);
      String           processUUID      = runProcessOutput.getProcessUUID();

      runProcessInput.addValue(BulkEditTransformStep.FIELD_ENABLED_FIELDS, "firstName,email,birthDate");
      runProcessInput.addValue("firstName", "Johnny");
      runProcessInput.addValue("email", null);
      runProcessInput.addValue("birthDate", "1909-01-09");

      runProcessInput.setProcessUUID(processUUID);
      runProcessInput.setStartAfterStep("edit");
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

      @SuppressWarnings("unchecked")
      List<ProcessSummaryLine> processSummaryLines = (List<ProcessSummaryLine>) runProcessOutput.getValues().get(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY);
      assertThat(processSummaryLines).hasSize(4);
      assertThat(processSummaryLines.stream().filter(psl -> psl.getStatus().equals(Status.OK))).hasSize(1);
      List<ProcessSummaryLine> infoLines = processSummaryLines.stream().filter(psl -> psl.getStatus().equals(Status.INFO)).collect(Collectors.toList());
      assertThat(infoLines).hasSize(3);
      assertThat(infoLines.stream().map(ProcessSummaryLine::getMessage)).anyMatch(m -> m.matches("(?s).*First Name.*Johnny.*"));
      assertThat(infoLines.stream().map(ProcessSummaryLine::getMessage)).anyMatch(m -> m.matches("(?s).*Email was cleared.*"));
      assertThat(infoLines.stream().map(ProcessSummaryLine::getMessage)).anyMatch(m -> m.matches("(?s).*Birth Date.*1909-01-09.*"));

      /////////////////////////////////////////////////////////////////////////////////
      // query for the edited records - assert that id 1 & 2 were updated, 3 was not //
      /////////////////////////////////////////////////////////////////////////////////
      List<QRecord> records = TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertEquals("Johnny", records.get(0).getValueString("firstName"));
      assertEquals("Johnny", records.get(1).getValueString("firstName"));
      assertEquals("James", records.get(2).getValueString("firstName"));
      assertEquals("1909-01-09", records.get(0).getValueString("birthDate"));
      assertEquals("1909-01-09", records.get(1).getValueString("birthDate"));
      assertNull(records.get(2).getValueString("birthDate"));
      assertNull(records.get(0).getValue("email"));
      assertNull(records.get(1).getValue("email"));
      assertEquals("james.maes@kingsrook.com", records.get(2).getValue("email"));
   }

}