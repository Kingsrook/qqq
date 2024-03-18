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

package com.kingsrook.qqq.backend.core.scheduler;


import java.util.ArrayList;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QCollectingLogger;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobParameter;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobType;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzTestUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Unit test for QScheduleManager 
 *******************************************************************************/
class QScheduleManagerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QLogger.deactivateCollectingLoggerForClass(QuartzScheduler.class);

      try
      {
         QScheduleManager.getInstance().unInit();
      }
      catch(IllegalStateException ise)
      {
         /////////////////////////////////////////////////////////////////
         // ok, might just mean that this test didn't init the instance //
         /////////////////////////////////////////////////////////////////
      }

      try
      {
         QuartzScheduler.getInstance().unInit();
      }
      catch(IllegalStateException ise)
      {
         /////////////////////////////////////////////////////////////////
         // ok, might just mean that this test didn't init the instance //
         /////////////////////////////////////////////////////////////////
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private ScheduledJob newScheduledJob(ScheduledJobType type, Map<String, String> params)
   {
      ScheduledJob scheduledJob = new ScheduledJob()
         .withId(1)
         .withIsActive(true)
         .withSchedulerName(TestUtils.SIMPLE_SCHEDULER_NAME)
         .withType(type.getId())
         .withRepeatSeconds(1)
         .withJobParameters(new ArrayList<>());

      for(Map.Entry<String, String> entry : params.entrySet())
      {
         scheduledJob.getJobParameters().add(new ScheduledJobParameter().withKey(entry.getKey()).withValue(entry.getValue()));
      }

      return (scheduledJob);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSetupScheduledJobErrorCases() throws QException
   {
      QScheduleManager qScheduleManager = QScheduleManager.initInstance(QContext.getQInstance(), () -> QContext.getQSession());

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of()).withRepeatSeconds(null)))
         .hasMessageContaining("Missing a schedule");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of()).withType(null)))
         .hasMessageContaining("Missing a type");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of()).withType("notAType")))
         .hasMessageContaining("Unrecognized type");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of())))
         .hasMessageContaining("Missing scheduledJobParameter with key [processName]");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of("processName", "notAProcess"))))
         .hasMessageContaining("Unrecognized processName");

      QContext.getQInstance().getProcess(TestUtils.PROCESS_NAME_BASEPULL).withSchedule(new QScheduleMetaData());
      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS, Map.of("processName", TestUtils.PROCESS_NAME_BASEPULL))))
         .hasMessageContaining("has a schedule in its metaData - so it should not be dynamically scheduled");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.QUEUE_PROCESSOR, Map.of())))
         .hasMessageContaining("Missing scheduledJobParameter with key [queueName]");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.QUEUE_PROCESSOR, Map.of("queueName", "notAQueue"))))
         .hasMessageContaining("Unrecognized queueName");

      QContext.getQInstance().getQueue(TestUtils.TEST_SQS_QUEUE).withSchedule(new QScheduleMetaData());
      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.QUEUE_PROCESSOR, Map.of("queueName", TestUtils.TEST_SQS_QUEUE))))
         .hasMessageContaining("has a schedule in its metaData - so it should not be dynamically scheduled");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of())))
         .hasMessageContaining("Missing scheduledJobParameter with key [tableName]");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of("tableName", "notATable"))))
         .hasMessageContaining("Missing scheduledJobParameter with key [automationStatus]");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of("tableName", "notATable", "automationStatus", AutomationStatus.PENDING_INSERT_AUTOMATIONS.name()))))
         .hasMessageContaining("Unrecognized tableName");

      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of("tableName", TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC, "automationStatus", AutomationStatus.PENDING_INSERT_AUTOMATIONS.name()))))
         .hasMessageContaining("does not have automationDetails");

      QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().withSchedule(null);
      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY, "automationStatus", "foobar"))))
         .hasMessageContaining("Did not find table automation actions matching automationStatus")
         .hasMessageContaining("Found: PENDING_INSERT_AUTOMATIONS,PENDING_UPDATE_AUTOMATIONS");

      QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).getAutomationDetails().withSchedule(new QScheduleMetaData());
      assertThatThrownBy(() -> qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS, Map.of("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY, "automationStatus", AutomationStatus.PENDING_INSERT_AUTOMATIONS.name()))))
         .hasMessageContaining("has a schedule in its metaData - so it should not be dynamically scheduled");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSuccessfulScheduleWithQuartz() throws QException
   {
      QCollectingLogger quartzLogger = QLogger.activateCollectingLoggerForClass(QuartzScheduler.class);

      QInstance qInstance = QContext.getQInstance();
      QuartzTestUtils.setupInstanceForQuartzTests();

      QScheduleManager qScheduleManager = QScheduleManager.initInstance(qInstance, () -> QContext.getQSession());
      qScheduleManager.start();

      qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.PROCESS,
         Map.of("processName", TestUtils.PROCESS_NAME_GREET_PEOPLE))
         .withId(2)
         .withSchedulerName(QuartzTestUtils.QUARTZ_SCHEDULER_NAME));

      qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.QUEUE_PROCESSOR,
         Map.of("queueName", TestUtils.TEST_SQS_QUEUE))
         .withId(3)
         .withSchedulerName(QuartzTestUtils.QUARTZ_SCHEDULER_NAME));

      qScheduleManager.setupScheduledJob(newScheduledJob(ScheduledJobType.TABLE_AUTOMATIONS,
         Map.of("tableName", TestUtils.TABLE_NAME_PERSON_MEMORY, "automationStatus", AutomationStatus.PENDING_UPDATE_AUTOMATIONS.name()))
         .withId(4)
         .withSchedulerName(QuartzTestUtils.QUARTZ_SCHEDULER_NAME));

      assertThat(quartzLogger.getCollectedMessages())
         .anyMatch(l -> l.getMessage().matches(".*Scheduled new job.*PROCESS.scheduledJob:2.*"))
         .anyMatch(l -> l.getMessage().matches(".*Scheduled new job.*QUEUE_PROCESSOR.scheduledJob:3.*"))
         .anyMatch(l -> l.getMessage().matches(".*Scheduled new job.*TABLE_AUTOMATIONS.scheduledJob:4.*"));
   }

}