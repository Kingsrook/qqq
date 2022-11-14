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

package com.kingsrook.qqq.backend.core.processes.implementations.tablesync;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for TableSyncProcess
 *******************************************************************************/
class TableSyncProcessTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      QTableMetaData personTable = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      String TABLE_NAME_PEOPLE_SYNC = "peopleSync";
      qInstance.addTable(new QTableMetaData()
         .withName(TABLE_NAME_PEOPLE_SYNC)
         .withPrimaryKeyField("id")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withFields(personTable.getFields())
         .withField(new QFieldMetaData("sourcePersonId", QFieldType.INTEGER)));

      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin"),
         new QRecord().withValue("id", 2).withValue("firstName", "Tim"),
         new QRecord().withValue("id", 3).withValue("firstName", "Tyler"),
         new QRecord().withValue("id", 4).withValue("firstName", "James"),
         new QRecord().withValue("id", 5).withValue("firstName", "Homer")
      ));

      TestUtils.insertRecords(qInstance, qInstance.getTable(TABLE_NAME_PEOPLE_SYNC), List.of(
         new QRecord().withValue("sourcePersonId", 3).withValue("firstName", "Garret"),
         new QRecord().withValue("sourcePersonId", 5).withValue("firstName", "Homer")
      ));

      String PROCESS_NAME = "testSyncProcess";
      qInstance.addProcess(TableSyncProcess.processMetaDataBuilder(false)
         .withName(PROCESS_NAME)
         .withTableName(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withSourceTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withDestinationTable(TABLE_NAME_PEOPLE_SYNC)
         .withSourceTableKeyField("id")
         .withDestinationTableForeignKeyField("sourcePersonId")
         .withSyncTransformStepClass(PersonTransformClass.class)
         .getProcessMetaData());

      RunProcessInput runProcessInput = new RunProcessInput(qInstance);
      runProcessInput.setSession(new QSession());
      runProcessInput.setProcessName(PROCESS_NAME);
      runProcessInput.addValue("recordIds", "1,2,3,4,5");
      runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      RunProcessAction runProcessAction = new RunProcessAction();
      RunProcessOutput runProcessOutput = runProcessAction.execute(runProcessInput);

      @SuppressWarnings("unchecked")
      ArrayList<ProcessSummaryLineInterface> processResults = (ArrayList<ProcessSummaryLineInterface>) runProcessOutput.getValues().get("processResults");

      assertThat(processResults.get(0))
         .hasFieldOrPropertyWithValue("message", "were inserted")
         .hasFieldOrPropertyWithValue("count", 3);

      assertThat(processResults.get(1))
         .hasFieldOrPropertyWithValue("message", "were updated")
         .hasFieldOrPropertyWithValue("count", 2);

      List<QRecord> syncedRecords = TestUtils.queryTable(qInstance, TABLE_NAME_PEOPLE_SYNC);
      assertEquals(5, syncedRecords.size());

      /////////////////////////////////////////////////////////////////
      // make sure the record referencing 3 has had its name updated //
      // and the one referencing 5 stayed the same                   //
      /////////////////////////////////////////////////////////////////
      Map<Serializable, QRecord> syncPersonsBySourceId = GeneralProcessUtils.loadTableToMap(runProcessInput, TABLE_NAME_PEOPLE_SYNC, "sourcePersonId");
      assertEquals("Tyler", syncPersonsBySourceId.get(3).getValueString("firstName"));
      assertEquals("Homer", syncPersonsBySourceId.get(5).getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PersonTransformClass extends AbstractTableSyncTransformStep
   {

      @Override
      public QRecord populateRecordToStore(RunBackendStepInput runBackendStepInput, QRecord destinationRecord, QRecord sourceRecord) throws QException
      {
         destinationRecord.setValue("sourcePersonId", sourceRecord.getValue("id"));
         destinationRecord.setValue("firstName", sourceRecord.getValue("firstName"));
         destinationRecord.setValue("lastName", sourceRecord.getValue("lastName"));
         return (destinationRecord);
      }

   }

}