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
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobParameter;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobType;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;
import com.kingsrook.qqq.backend.core.utils.TestUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class SchedulerTestUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessMetaData buildTestProcess(String name, String schedulerName)
   {
      return new QProcessMetaData()
         .withName(name)
         .withSchedule(new QScheduleMetaData()
            .withSchedulerName(schedulerName)
            .withRepeatMillis(2)
            .withInitialDelaySeconds(0))
         .withStepList(List.of(new QBackendStepMetaData()
            .withName("step")
            .withCode(new QCodeReference(BasicStep.class))));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ScheduledJob newScheduledJob(ScheduledJobType type, Map<String, String> params)
   {
      ScheduledJob scheduledJob = new ScheduledJob()
         .withId(1)
         .withIsActive(true)
         .withSchedulerName(TestUtils.SIMPLE_SCHEDULER_NAME)
         .withType(type.name())
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
   public static void afterEach()
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
         QuartzScheduler.getInstance().stop();
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
   public static class BasicStep implements BackendStep
   {
      public static int counter = 0;



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
      {
         counter++;
      }
   }
}
