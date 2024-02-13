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

package com.kingsrook.qqq.backend.core.processes.implementations.automation;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.RecordAutomationStatusUpdater;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DynamicDefaultValueBehavior;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for HealBadRecordAutomationStatusesProcessStep 
 *******************************************************************************/
class HealBadRecordAutomationStatusesProcessStepTest extends BaseTest
{
   private static String tableName = TestUtils.TABLE_NAME_PERSON_MEMORY;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTwoFailedUpdates() throws QException
   {
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(new QRecord(), new QRecord())));
      List<QRecord> records = queryAllRecords();
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(QContext.getQInstance().getTable(tableName), records, AutomationStatus.FAILED_UPDATE_AUTOMATIONS, null);

      assertThat(queryAllRecords()).allMatch(r -> AutomationStatus.FAILED_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)));

      RunBackendStepOutput output = runProcessStep();

      assertEquals(2, output.getValueInteger("totalRecordsUpdated"));
      assertThat(queryAllRecords()).allMatch(r -> AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneFailedUpdateOneFailedInsert() throws QException
   {
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(new QRecord(), new QRecord())));
      List<QRecord> records = queryAllRecords();
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(QContext.getQInstance().getTable(tableName), records.subList(0, 1), AutomationStatus.FAILED_UPDATE_AUTOMATIONS, null);
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(QContext.getQInstance().getTable(tableName), records.subList(1, 2), AutomationStatus.FAILED_INSERT_AUTOMATIONS, null);

      assertThat(queryAllRecords())
         .anyMatch(r -> AutomationStatus.FAILED_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)))
         .anyMatch(r -> AutomationStatus.FAILED_INSERT_AUTOMATIONS.getId().equals(getAutomationStatus(r)));

      RunBackendStepOutput output = runProcessStep();

      assertEquals(2, output.getValueInteger("totalRecordsUpdated"));
      assertThat(queryAllRecords())
         .anyMatch(r -> AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)))
         .anyMatch(r -> AutomationStatus.PENDING_INSERT_AUTOMATIONS.getId().equals(getAutomationStatus(r)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOldRunning() throws QException
   {
      /////////////////////////////////////////////////
      // temporarily remove the modify-date behavior //
      /////////////////////////////////////////////////
      QContext.getQInstance().getTable(tableName).getField("modifyDate").withBehavior(DynamicDefaultValueBehavior.NONE);

      //////////////////////////////////////////////////////////////////////////
      // insert 2 records, one with an old modifyDate, one with 6 minutes ago //
      //////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(tableName).withRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("modifyDate", Instant.parse("2023-01-01T12:00:00Z")),
         new QRecord().withValue("firstName", "Tim").withValue("modifyDate", Instant.now().minus(6, ChronoUnit.MINUTES))
      )));
      List<QRecord> records = queryAllRecords();

      ///////////////////////////////////////////////////////
      // put those records both in status: running-updates //
      ///////////////////////////////////////////////////////
      RecordAutomationStatusUpdater.setAutomationStatusInRecordsAndUpdate(QContext.getQInstance().getTable(tableName), records, AutomationStatus.RUNNING_UPDATE_AUTOMATIONS, null);

      assertThat(queryAllRecords())
         .allMatch(r -> AutomationStatus.RUNNING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)));

      /////////////////////////////////////
      // restore the modifyDate behavior //
      /////////////////////////////////////
      QContext.getQInstance().getTable(tableName).getField("modifyDate").withBehavior(DynamicDefaultValueBehavior.MODIFY_DATE);

      /////////////////////////
      // run code under test //
      /////////////////////////
      RunBackendStepOutput output = runProcessStep();

      /////////////////////////////////////////////////////////////////////////////////////////////
      // assert we updated 1 (the old one) to pending-updates, the other left as running-updates //
      /////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals(1, output.getValueInteger("totalRecordsUpdated"));
      assertThat(queryAllRecords())
         .anyMatch(r -> AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)))
         .anyMatch(r -> AutomationStatus.RUNNING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)));

      /////////////////////////////////
      // re-run, with 3-minute limit //
      /////////////////////////////////
      output = runProcessStep(new RunBackendStepInput().withValues(Map.of("minutesOldLimit", 3)));

      /////////////////////////////////////////////////////////////////
      // assert that one updated too, and all are now pending-update //
      /////////////////////////////////////////////////////////////////
      assertEquals(1, output.getValueInteger("totalRecordsUpdated"));
      assertThat(queryAllRecords())
         .allMatch(r -> AutomationStatus.PENDING_UPDATE_AUTOMATIONS.getId().equals(getAutomationStatus(r)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer getAutomationStatus(QRecord r)
   {
      return r.getValueInteger(TestUtils.standardQqqAutomationStatusField().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> queryAllRecords() throws QException
   {
      return new QueryAction().execute(new QueryInput(tableName)).getRecords();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static RunBackendStepOutput runProcessStep() throws QException
   {
      RunBackendStepInput input = new RunBackendStepInput();
      return runProcessStep(input);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static RunBackendStepOutput runProcessStep(RunBackendStepInput input) throws QException
   {
      RunBackendStepOutput output = new RunBackendStepOutput();
      new HealBadRecordAutomationStatusesProcessStep().run(input, output);
      return output;
   }

}