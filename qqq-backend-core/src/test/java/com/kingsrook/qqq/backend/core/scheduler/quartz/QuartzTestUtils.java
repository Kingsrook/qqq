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

package com.kingsrook.qqq.backend.core.scheduler.quartz;


import java.util.List;
import java.util.Properties;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.quartz.QuartzSchedulerMetaData;
import org.quartz.SchedulerException;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuartzTestUtils
{
   public static final String QUARTZ_SCHEDULER_NAME = "TestQuartzScheduler";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Properties getQuartzProperties()
   {
      Properties quartzProperties = new Properties();
      quartzProperties.put("org.quartz.scheduler.instanceName", QUARTZ_SCHEDULER_NAME);
      quartzProperties.put("org.quartz.threadPool.threadCount", "3");
      quartzProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
      return (quartzProperties);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setupInstanceForQuartzTests()
   {
      QInstance qInstance = QContext.getQInstance();

      ///////////////////////////////////////////////////
      // remove the simple scheduler from the instance //
      ///////////////////////////////////////////////////
      qInstance.getSchedulers().clear();

      ////////////////////////////////////////////////////////
      // add the quartz scheduler meta-data to the instance //
      ////////////////////////////////////////////////////////
      qInstance.addScheduler(new QuartzSchedulerMetaData()
         .withProperties(getQuartzProperties())
         .withName(QUARTZ_SCHEDULER_NAME));

      ///////////////////////////////////////////////////////////////////////////////////
      // set the queue providers & automation providers to use the quartz scheduler    //
      // also, set their initial delay to avoid default delay done by our scheduler    //
      // (that gives us a chance to re-pause if re-scheduling a previously paused job) //
      ///////////////////////////////////////////////////////////////////////////////////
      qInstance.getTables().values().forEach(t ->
      {
         if(t.getAutomationDetails() != null)
         {
            t.getAutomationDetails().getSchedule()
               .withSchedulerName(QUARTZ_SCHEDULER_NAME)
               .withInitialDelayMillis(1);
         }
      });

      qInstance.getQueues().values()
         .forEach(q -> q.getSchedule()
            .withSchedulerName(QUARTZ_SCHEDULER_NAME)
            .withInitialDelayMillis(1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QuartzJobAndTriggerWrapper> queryQuartz() throws SchedulerException
   {
      return QuartzScheduler.getInstance().queryQuartz();
   }

}
