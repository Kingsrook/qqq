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

package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTracking;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.AutomationStatusTrackingType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.QTableAutomationDetails;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TableAutomationAction;
import com.kingsrook.qqq.backend.core.model.metadata.tables.automation.TriggerEvent;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcessTest;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for PollingAutomationPerTableRunner
 *******************************************************************************/
class PollingAutomationPerTableRunnerTest extends BaseTest
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
    ** Test a cycle that does an insert, some automations, then and an update, and more automations.
    *******************************************************************************/
   @Test
   void testInsertAndUpdate() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // insert 2 person records, both updated by the insert action, and 1 logged by logger-on-update automation //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("birthDate", LocalDate.now()),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin").withValue("birthDate", LocalDate.now())
      ));
      new InsertAction().execute(insertInput);
      assertAllRecordsAutomationStatus(AutomationStatus.PENDING_INSERT_AUTOMATIONS);

      //////////////////////////////////////////////////////////////////////////////////////////
      // assert that the update-automation won't run - as no UPDATE has happened on the table //
      // even though the insert action does update the records!!                              //
      //////////////////////////////////////////////////////////////////////////////////////////
      runAllTableActions(qInstance);
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      /////////////////////////////////////////
      // make sure both persons were updated //
      /////////////////////////////////////////
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY))
         .allMatch(r -> r.getValueString("firstName").endsWith(TestUtils.CheckAge.SUFFIX_FOR_MINORS));

      /////////////////////////////////////////////////////////////////////////////////////////
      // run automations again - make sure that there haven't been any updates triggered yet //
      /////////////////////////////////////////////////////////////////////////////////////////
      runAllTableActions(qInstance);
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now do an user-driven update - this SHOULD trigger the update automation next time we run automations. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("lastName", "now with a LastName"),
         new QRecord().withValue("id", 2).withValue("lastName", "now with a LastName")
      ));
      new UpdateAction().execute(updateInput);
      assertAllRecordsAutomationStatus(AutomationStatus.PENDING_UPDATE_AUTOMATIONS);

      /////////////////////////////////////////////////////////////////////////////////
      // assert that the update-automation DOES run now - and that it only runs once //
      // note that it will only run on a sub-set of the records                      //
      /////////////////////////////////////////////////////////////////////////////////
      runAllTableActions(qInstance);
      assertThat(TestUtils.LogPersonUpdate.updatedIds)
         .contains(2)
         .hasSize(1);
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      /////////////////////////////////////////////////////
      // re-run and assert no further automations happen //
      /////////////////////////////////////////////////////
      TestUtils.LogPersonUpdate.updatedIds.clear();
      runAllTableActions(qInstance);
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runAllTableActions(QInstance qInstance) throws QException
   {
      List<PollingAutomationPerTableRunner.TableActions> tableActions = PollingAutomationPerTableRunner.getTableActions(qInstance, TestUtils.POLLING_AUTOMATION);
      for(PollingAutomationPerTableRunner.TableActions tableAction : tableActions)
      {
         PollingAutomationPerTableRunner pollingAutomationPerTableRunner = new PollingAutomationPerTableRunner(qInstance, TestUtils.POLLING_AUTOMATION, QSession::new, tableAction);

         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // note - don't call run - it is meant to be called async - e.g., it sets & clears thread context. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         pollingAutomationPerTableRunner.processTableInsertOrUpdate(qInstance.getTable(tableAction.tableName()), QContext.getQSession(), tableAction.status());

      }
   }



   /*******************************************************************************
    ** Test a large-ish number - to demonstrate paging working.
    **
    ** Note - this caught an issue during original development, where the QueryFilter
    ** attached to the Action was being re-used, w/ new "id IN *" criteria being re-added
    ** to it - so, good test.
    *******************************************************************************/
   @Test
   void testMultiPages() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      //////////////////////////////////////////////////////////////////////////////////
      // insert many people - half who should be updated by the AgeChecker automation //
      //////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      insertInput.setRecords(new ArrayList<>());
      int SIZE = 2_500;
      for(int i = 0; i < SIZE; i++)
      {
         insertInput.getRecords().add(new QRecord().withValue("firstName", "Tim").withValue("lastName", "Number " + i).withValue("birthDate", LocalDate.now()));
         insertInput.getRecords().add(new QRecord().withValue("firstName", "Darin").withValue("lastName", "Number " + i));
      }

      new InsertAction().execute(insertInput);
      assertAllRecordsAutomationStatus(AutomationStatus.PENDING_INSERT_AUTOMATIONS);

      /////////////////////////
      // run the automations //
      /////////////////////////
      runAllTableActions(qInstance);
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      ///////////////////////////////////////////////////////////////////////////
      // make sure that all 'minor' persons were updated (e.g., all the Tim's) //
      ///////////////////////////////////////////////////////////////////////////
      int updatedMinorsCount = 0;
      for(QRecord qRecord : TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY))
      {
         if(qRecord.getValueString("firstName").startsWith("Tim"))
         {
            assertEquals("Tim" + TestUtils.CheckAge.SUFFIX_FOR_MINORS, qRecord.getValueString("firstName"));
            updatedMinorsCount++;
         }
      }

      assertEquals(SIZE, updatedMinorsCount, "Expected number of updated records");
   }



   /*******************************************************************************
    ** Test running a process for automation, instead of a code ref.
    *******************************************************************************/
   @Test
   void testRunningProcess() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      ////////////////////////////////////////////////////////////////////
      // insert 2 person records, 1 to trigger the "increaseAge" action //
      ////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("birthDate", LocalDate.of(1886, Month.JUNE, 6)),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin").withValue("birthDate", LocalDate.of(1904, Month.APRIL, 4))
      ));
      new InsertAction().execute(insertInput);

      runAllTableActions(qInstance);

      /////////////////////////////////////////////////////////////////////////////////////////////
      // make sure the process ran - which means, it would have updated Tim's birth year to 1900 //
      /////////////////////////////////////////////////////////////////////////////////////////////
      Optional<QRecord> updatedMinorRecord = TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY).stream().filter(r -> r.getValueInteger("id").equals(1)).findFirst();
      assertThat(updatedMinorRecord)
         .isPresent()
         .get()
         .extracting(r -> r.getValueLocalDate("birthDate").getYear())
         .isEqualTo(1900);

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRunningEtlWithFrontendProcess() throws QException
   {
      QInstance instance = QContext.getQInstance();

      ////////////////////////////////////////////////////////
      // define the process - an ELT from Shapes to Persons //
      ////////////////////////////////////////////////////////
      QProcessMetaData process = StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_PERSON,
         ExtractViaQueryStep.class,
         StreamedETLWithFrontendProcessTest.TestTransformShapeToPersonStep.class,
         LoadViaInsertStep.class);
      process.setName("shapeToPersonETLProcess");
      process.setTableName(TestUtils.TABLE_NAME_SHAPE);
      instance.addProcess(process);

      ///////////////////////////////////////////////////////
      // switch the person table to use the memory backend //
      ///////////////////////////////////////////////////////
      instance.getTable(TestUtils.TABLE_NAME_PERSON).setBackendName(TestUtils.MEMORY_BACKEND_NAME);

      ///////////////////////////////////////////////////////////////////////
      // add a post-insert process to the shape table, to run this ELT job //
      ///////////////////////////////////////////////////////////////////////
      instance.getTable(TestUtils.TABLE_NAME_SHAPE)
         .withField(new QFieldMetaData("automationStatus", QFieldType.INTEGER))
         .setAutomationDetails(new QTableAutomationDetails()
            .withProviderName(TestUtils.POLLING_AUTOMATION)
            .withStatusTracking(new AutomationStatusTracking().withType(AutomationStatusTrackingType.FIELD_IN_TABLE).withFieldName("automationStatus"))
            .withAction(new TableAutomationAction()
               .withName("shapeToPerson")
               .withTriggerEvent(TriggerEvent.POST_INSERT)
               .withProcessName("shapeToPersonETLProcess")
            )
         );

      TestUtils.insertDefaultShapes(instance);

      runAllTableActions(instance);

      List<QRecord> postList = TestUtils.queryTable(instance, TestUtils.TABLE_NAME_PERSON);
      assertThat(postList)
         .as("Should have inserted Circle").anyMatch(qr -> qr.getValue("lastName").equals("Circle"))
         .as("Should have inserted Triangle").anyMatch(qr -> qr.getValue("lastName").equals("Triangle"))
         .as("Should have inserted Square").anyMatch(qr -> qr.getValue("lastName").equals("Square"));
   }



   /*******************************************************************************
    ** Test that sub-filters in filters work correctly to limit the records that get
    ** applied (as at one point in time, they didn't!!
    *******************************************************************************/
   @Test
   void testFilterWithSubFilter() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      ///////////////////////////////////////////////////////////////////////////////////////////
      // update the CheckAge automation to have a sub-filter that should make Tim not be found //
      ///////////////////////////////////////////////////////////////////////////////////////////
      QTableMetaData table = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      TableAutomationAction checkAgeOnInsert = table.getAutomationDetails().getActions().stream()
         .filter(a -> a.getName().equals("checkAgeOnInsert"))
         .findFirst()
         .orElseThrow();

      QQueryFilter filter = checkAgeOnInsert.getFilter();
      filter.addSubFilter(new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.NOT_EQUALS, "Tim")));

      ////////////////////////////////////////////////////////////////////////
      // insert 2 person records - but only Darin should get the automation //
      ////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("birthDate", LocalDate.now()),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin").withValue("birthDate", LocalDate.now())));
      new InsertAction().execute(insertInput);

      runAllTableActions(qInstance);

      /////////////////////////////////
      // make sure Darin was updated //
      /////////////////////////////////
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .stream().filter(r -> r.getValueString("firstName").startsWith("Darin"))).first()
         .matches(r -> r.getValueString("firstName").endsWith(TestUtils.CheckAge.SUFFIX_FOR_MINORS));

      ////////////////////////////////////
      // make sure Tim was not updated. //
      ////////////////////////////////////
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .stream().filter(r -> r.getValueString("firstName").startsWith("Tim"))).first()
         .matches(r -> !r.getValueString("firstName").endsWith(TestUtils.CheckAge.SUFFIX_FOR_MINORS));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertAllRecordsAutomationStatus(AutomationStatus pendingInsertAutomations) throws QException
   {
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY))
         .isNotEmpty()
         .allMatch(r -> pendingInsertAutomations.getId().equals(r.getValue(TestUtils.standardQqqAutomationStatusField().getName())));
   }
}