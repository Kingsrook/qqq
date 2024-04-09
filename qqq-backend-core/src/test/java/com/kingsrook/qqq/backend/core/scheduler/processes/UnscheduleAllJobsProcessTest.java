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

package com.kingsrook.qqq.backend.core.scheduler.processes;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerHelper;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobType;
import com.kingsrook.qqq.backend.core.scheduler.QScheduleManager;
import com.kingsrook.qqq.backend.core.scheduler.SchedulerTestUtils;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzJobAndTriggerWrapper;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzTestUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for UnscheduleAllJobsProcess 
 *******************************************************************************/
class UnscheduleAllJobsProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
   {
      QuartzTestUtils.afterEach();
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      QuartzTestUtils.afterEach();
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException, SchedulerException
   {
      QInstance qInstance = QContext.getQInstance();
      MetaDataProducerHelper.processAllMetaDataProducersInPackage(qInstance, UnscheduleAllJobsProcess.class.getPackageName());
      QuartzTestUtils.setupInstanceForQuartzTests();

      QScheduleManager qScheduleManager = QScheduleManager.initInstance(qInstance, () -> QContext.getQSession());
      qScheduleManager.start();

      qScheduleManager.setupScheduledJob(SchedulerTestUtils.newScheduledJob(ScheduledJobType.PROCESS,
         Map.of("processName", TestUtils.PROCESS_NAME_GREET_PEOPLE))
         .withId(2)
         .withSchedulerName(QuartzTestUtils.QUARTZ_SCHEDULER_NAME));

      QuartzScheduler quartzScheduler = QuartzScheduler.getInstance();
      List<QuartzJobAndTriggerWrapper> wrappers = quartzScheduler.queryQuartz();
      assertEquals(1, wrappers.size());

      RunProcessInput input = new RunProcessInput();
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      input.setProcessName(UnscheduleAllJobsProcess.class.getSimpleName());
      new RunProcessAction().execute(input);

      wrappers = quartzScheduler.queryQuartz();
      assertTrue(wrappers.isEmpty());
   }

}