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

package com.kingsrook.qqq.backend.core.scheduler.quartz.processes;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.QScheduleManager;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzJobAndTriggerWrapper;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzTestUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit tests for the various quartz management processes
 *******************************************************************************/
class QuartzJobsProcessTest extends BaseTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      qInstance.addTable(new QTableMetaData()
         .withName("quartzTriggers")
         .withBackendName(TestUtils.MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.LONG)));
      MetaDataProducerHelper.processAllMetaDataProducersInPackage(qInstance, QuartzScheduler.class.getPackageName());

      QuartzTestUtils.setupInstanceForQuartzTests();

      //////////////////////////////////////////////////////////////////////////////
      // start the schedule manager, which will schedule things, and start quartz //
      //////////////////////////////////////////////////////////////////////////////
      QSession         qSession         = QContext.getQSession();
      QScheduleManager qScheduleManager = QScheduleManager.initInstance(qInstance, () -> qSession);
      qScheduleManager.start();
      qScheduleManager.setupAllSchedules();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      try
      {
         QScheduleManager.getInstance().stop();
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
   @Test
   void testPauseAllQuartzJobs() throws QException, SchedulerException
   {
      ////////////////////////////////////////
      // make sure nothing starts as paused //
      ////////////////////////////////////////
      assertNoneArePaused();

      ///////////////////////////////
      // run the pause-all process //
      ///////////////////////////////
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(PauseAllQuartzJobsProcess.class.getSimpleName());
      new RunProcessAction().execute(input);

      //////////////////////////////////////
      // assert everything becomes paused //
      //////////////////////////////////////
      assertAllArePaused();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testResumeAllQuartzJobs() throws QException, SchedulerException
   {
      ///////////////////////////////
      // run the pause-all process //
      ///////////////////////////////
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(PauseAllQuartzJobsProcess.class.getSimpleName());
      new RunProcessAction().execute(input);

      //////////////////////////////////////
      // assert everything becomes paused //
      //////////////////////////////////////
      assertAllArePaused();

      ////////////////////
      // run resume all //
      ////////////////////
      input = new RunProcessInput();
      input.setProcessName(ResumeAllQuartzJobsProcess.class.getSimpleName());
      new RunProcessAction().execute(input);

      ////////////////////////////////////////
      // make sure nothing ends up as paused //
      ////////////////////////////////////////
      assertNoneArePaused();

      ////////////////////
      // pause just one //
      ////////////////////
      List<QuartzJobAndTriggerWrapper> quartzJobAndTriggerWrappers = QuartzTestUtils.queryQuartz();
      new InsertAction().execute(new InsertInput("quartzTriggers").withRecord(new QRecord()
         .withValue("jobName", quartzJobAndTriggerWrappers.get(0).jobDetail().getKey().getName())
         .withValue("jobGroup", quartzJobAndTriggerWrappers.get(0).jobDetail().getKey().getGroup())
      ));

      input = new RunProcessInput();
      input.setProcessName(PauseQuartzJobsProcess.class.getSimpleName());
      input.setCallback(QProcessCallbackFactory.forFilter(new QQueryFilter()));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      new RunProcessAction().execute(input);

      /////////////////////////////////////////////////////////
      // make sure at least 1 is paused, some are not paused //
      /////////////////////////////////////////////////////////
      assertAnyAre(Trigger.TriggerState.PAUSED);
      assertAnyAreNot(Trigger.TriggerState.PAUSED);

      //////////////////////////
      // run resume all again //
      //////////////////////////
      input = new RunProcessInput();
      input.setProcessName(ResumeAllQuartzJobsProcess.class.getSimpleName());
      new RunProcessAction().execute(input);

      ////////////////////////////////////////
      // make sure nothing ends up as paused //
      ////////////////////////////////////////
      assertNoneArePaused();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPauseOneResumeOne() throws QException, SchedulerException
   {
      /////////////////////////////////////
      // make sure nothing starts paused //
      /////////////////////////////////////
      assertNoneArePaused();

      ////////////////////
      // pause just one //
      ////////////////////
      List<QuartzJobAndTriggerWrapper> quartzJobAndTriggerWrappers = QuartzTestUtils.queryQuartz();
      new InsertAction().execute(new InsertInput("quartzTriggers").withRecord(new QRecord()
         .withValue("jobName", quartzJobAndTriggerWrappers.get(0).jobDetail().getKey().getName())
         .withValue("jobGroup", quartzJobAndTriggerWrappers.get(0).jobDetail().getKey().getGroup())
      ));

      RunProcessInput input = new RunProcessInput();
      input.setProcessName(PauseQuartzJobsProcess.class.getSimpleName());
      input.setCallback(QProcessCallbackFactory.forFilter(new QQueryFilter()));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      new RunProcessAction().execute(input);

      /////////////////////////////////////////////////////////
      // make sure at least 1 is paused, some are not paused //
      /////////////////////////////////////////////////////////
      assertAnyAre(Trigger.TriggerState.PAUSED);
      assertAnyAreNot(Trigger.TriggerState.PAUSED);

      /////////////////////////////////////////////////////////////////////////////
      // now resume the same one (will still be only row in our in-memory table) //
      /////////////////////////////////////////////////////////////////////////////
      input = new RunProcessInput();
      input.setProcessName(ResumeQuartzJobsProcess.class.getSimpleName());
      input.setCallback(QProcessCallbackFactory.forFilter(new QQueryFilter()));
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      new RunProcessAction().execute(input);

      //////////////////////////////////////
      // make sure nothing ends up paused //
      //////////////////////////////////////
      assertNoneArePaused();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void assertAnyAre(Trigger.TriggerState triggerState) throws SchedulerException
   {
      assertThat(QuartzTestUtils.queryQuartz()).anyMatch(qjtw -> qjtw.triggerState().equals(triggerState));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void assertAnyAreNot(Trigger.TriggerState triggerState) throws SchedulerException
   {
      assertThat(QuartzTestUtils.queryQuartz()).anyMatch(qjtw -> !qjtw.triggerState().equals(triggerState));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void assertNoneArePaused() throws SchedulerException
   {
      assertThat(QuartzTestUtils.queryQuartz()).noneMatch(qjtw -> qjtw.triggerState().equals(Trigger.TriggerState.PAUSED));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void assertAllArePaused() throws SchedulerException
   {
      assertThat(QuartzTestUtils.queryQuartz()).allMatch(qjtw -> qjtw.triggerState().equals(Trigger.TriggerState.PAUSED));
   }

}