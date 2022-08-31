package com.kingsrook.qqq.backend.core.actions.automation.polling;


import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.automation.RecordAutomationInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for PollingAutomationExecutor
 *******************************************************************************/
class PollingAutomationExecutorTest
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
   void testInsert() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      /////////////////////////////////////////////////////////////////////////////
      // insert 2 people - one who should be updated by the check-age automation //
      /////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "John").withValue("birthDate", LocalDate.of(1970, Month.JANUARY, 1)),
         new QRecord().withValue("id", 2).withValue("firstName", "Jim").withValue("birthDate", LocalDate.now().minusDays(30))
      ));
      new InsertAction().execute(insertInput);

      ////////////////////////////////////////////////
      // have the polling executor run "for awhile" //
      ////////////////////////////////////////////////
      runPollingAutomationExecutorForAwhile(qInstance);

      /////////////////////////////////////////////////
      // query for the records - assert their status //
      /////////////////////////////////////////////////
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size());

      Optional<QRecord> optionalPerson1 = queryOutput.getRecords().stream().filter(r -> r.getValueInteger("id") == 1).findFirst();
      assertThat(optionalPerson1).isPresent();
      QRecord person1 = optionalPerson1.get();
      assertThat(person1.getValueString("firstName")).isEqualTo("John");
      assertThat(person1.getValueInteger(TestUtils.standardQqqAutomationStatusField().getName())).isEqualTo(AutomationStatus.OK.getId());

      Optional<QRecord> optionalPerson2 = queryOutput.getRecords().stream().filter(r -> r.getValueInteger("id") == 2).findFirst();
      assertThat(optionalPerson2).isPresent();
      QRecord person2 = optionalPerson2.get();
      assertThat(person2.getValueString("firstName")).isEqualTo("Jim" + TestUtils.CheckAge.SUFFIX_FOR_MINORS);
      assertThat(person2.getValueInteger(TestUtils.standardQqqAutomationStatusField().getName())).isEqualTo(AutomationStatus.OK.getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUpdate() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      ///////////////////////////////////////////////////////////////////////////////
      // insert 2 people - one who should be logged by logger-on-update automation //
      ///////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim"),
         new QRecord().withValue("id", 2).withValue("firstName", "Darin")
      ));
      new InsertAction().execute(insertInput);

      ////////////////////////////////////////////////
      // have the polling executor run "for awhile" //
      ////////////////////////////////////////////////
      runPollingAutomationExecutorForAwhile(qInstance);

      //////////////////////////////////////////////////
      // assert that the update-automation didn't run //
      //////////////////////////////////////////////////
      assertThat(TestUtils.LogPersonUpdate.updatedIds).isNullOrEmpty();

      UpdateInput updateInput = new UpdateInput(qInstance);
      updateInput.setSession(new QSession());
      updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      updateInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("lastName", "now with a LastName"),
         new QRecord().withValue("id", 2).withValue("lastName", "now with a LastName")
      ));
      new UpdateAction().execute(updateInput);

      ////////////////////////////////////////////////
      // have the polling executor run "for awhile" //
      ////////////////////////////////////////////////
      runPollingAutomationExecutorForAwhile(qInstance);

      ///////////////////////////////////////////////////
      // assert that the update-automation DID run now //
      ///////////////////////////////////////////////////
      assertThat(TestUtils.LogPersonUpdate.updatedIds).contains(2);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSessionSupplier() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      //////////////////////////////////////////////////////////////////////
      // make the person-memory table's insert-action run a class in here //
      //////////////////////////////////////////////////////////////////////
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .getAutomationDetails().getActions().get(0)
         .setCodeReference(new QCodeReference(CaptureSessionIdAutomationHandler.class));

      /////////////////////
      // insert a person //
      /////////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Tim")
      ));
      new InsertAction().execute(insertInput);

      String uuid = UUID.randomUUID().toString();
      QSession session = new QSession();
      session.setIdReference(uuid);
      PollingAutomationExecutor.getInstance().setSessionSupplier(() -> session);

      ////////////////////////////////////////////////
      // have the polling executor run "for awhile" //
      ////////////////////////////////////////////////
      runPollingAutomationExecutorForAwhile(qInstance);

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // assert that the uuid we put in our session was present in the CaptureSessionIdAutomationHandler //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      assertThat(CaptureSessionIdAutomationHandler.sessionId).isEqualTo(uuid);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class CaptureSessionIdAutomationHandler extends RecordAutomationHandler
   {
      static String sessionId;



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void execute(RecordAutomationInput recordAutomationInput) throws QException
      {
         sessionId = recordAutomationInput.getSession().getIdReference();
      }

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void runPollingAutomationExecutorForAwhile(QInstance qInstance)
   {
      PollingAutomationExecutor pollingAutomationExecutor = PollingAutomationExecutor.getInstance();
      pollingAutomationExecutor.setInitialDelayMillis(0);
      pollingAutomationExecutor.setDelayMillis(100);
      pollingAutomationExecutor.start(qInstance, TestUtils.POLLING_AUTOMATION);
      SleepUtils.sleep(1, TimeUnit.SECONDS);
      pollingAutomationExecutor.stop();
   }

}