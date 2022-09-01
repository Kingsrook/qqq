package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for PollingAutomationRunner
 *******************************************************************************/
class PollingAutomationRunnerTest
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
      QInstance               qInstance               = TestUtils.defineInstance();
      PollingAutomationRunner pollingAutomationRunner = new PollingAutomationRunner(qInstance, TestUtils.POLLING_AUTOMATION, null);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // insert 2 person records, one who should be both updated by the insert action, and should be logged by logger-on-update automation //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("birthDate", LocalDate.now()),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin")
      ));
      new InsertAction().execute(insertInput);
      assertAllRecordsAutomationStatus(AutomationStatus.PENDING_INSERT_AUTOMATIONS);

      //////////////////////////////////////////////////////////////////////////////////////////
      // assert that the update-automation won't run - as no UPDATE has happened on the table //
      // even though the insert action does update the records!!                              //
      //////////////////////////////////////////////////////////////////////////////////////////
      pollingAutomationRunner.run();
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      ////////////////////////////////////////////
      // make sure the minor person was updated //
      ////////////////////////////////////////////
      Optional<QRecord> updatedMinorRecord = TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY).stream().filter(r -> r.getValueInteger("id").equals(1)).findFirst();
      assertThat(updatedMinorRecord)
         .isPresent()
         .get()
         .extracting(r -> r.getValueString("firstName"))
         .isEqualTo("Tim" + TestUtils.CheckAge.SUFFIX_FOR_MINORS);

      /////////////////////////////////////////////////////////////////////////////////////////
      // run automations again - make sure that there haven't been any updates triggered yet //
      /////////////////////////////////////////////////////////////////////////////////////////
      pollingAutomationRunner.run();
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now do an user-driven update - this SHOULD trigger the update automation next time we run automations. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      UpdateInput updateInput = new UpdateInput(qInstance);
      updateInput.setSession(new QSession());
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
      pollingAutomationRunner.run();
      assertThat(TestUtils.LogPersonUpdate.updatedIds)
         .contains(2)
         .hasSize(1);
      assertAllRecordsAutomationStatus(AutomationStatus.OK);

      /////////////////////////////////////////////////////
      // re-run and assert no further automations happen //
      /////////////////////////////////////////////////////
      TestUtils.LogPersonUpdate.updatedIds.clear();
      pollingAutomationRunner.run();
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();
      assertAllRecordsAutomationStatus(AutomationStatus.OK);
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
      QInstance               qInstance               = TestUtils.defineInstance();
      PollingAutomationRunner pollingAutomationRunner = new PollingAutomationRunner(qInstance, TestUtils.POLLING_AUTOMATION, null);

      //////////////////////////////////////////////////////////////////////////////////
      // insert many people - half who should be updated by the AgeChecker automation //
      //////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
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
      pollingAutomationRunner.run();
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
    ** Test a cycle that does an insert, some automations, then and an update, and more automations.
    *******************************************************************************/
   @Test
   void testRunningProcess() throws QException
   {
      QInstance               qInstance               = TestUtils.defineInstance();
      PollingAutomationRunner pollingAutomationRunner = new PollingAutomationRunner(qInstance, TestUtils.POLLING_AUTOMATION, null);

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // insert 2 person records, one who should be both updated by the insert action, and should be logged by logger-on-update automation //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("birthDate", LocalDate.of(1886, Month.JUNE, 6)),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin").withValue("birthDate", LocalDate.of(1904, Month.APRIL, 4))
      ));
      new InsertAction().execute(insertInput);

      pollingAutomationRunner.run();

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
   private void assertAllRecordsAutomationStatus(AutomationStatus pendingInsertAutomations) throws QException
   {
      assertThat(TestUtils.queryTable(TestUtils.TABLE_NAME_PERSON_MEMORY))
         .isNotEmpty()
         .allMatch(r -> pendingInsertAutomations.getId().equals(r.getValue(TestUtils.standardQqqAutomationStatusField().getName())));
   }
}