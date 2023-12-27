/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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


import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.SchedulerUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuartzScheduler
{
   private static final QLogger LOG = QLogger.getLogger(QuartzScheduler.class);

   private static QuartzScheduler quartzScheduler = null;

   private final QInstance          qInstance;
   private       Supplier<QSession> sessionSupplier;

   private Scheduler scheduler;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private QuartzScheduler(QInstance qInstance, Supplier<QSession> sessionSupplier)
   {
      this.qInstance = qInstance;
      this.sessionSupplier = sessionSupplier;
   }



   /*******************************************************************************
    ** Singleton initiator...
    *******************************************************************************/
   public static QuartzScheduler initInstance(QInstance qInstance, Supplier<QSession> sessionSupplier)
   {
      if(quartzScheduler == null)
      {
         quartzScheduler = new QuartzScheduler(qInstance, sessionSupplier);
      }
      return (quartzScheduler);
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static QuartzScheduler getInstance()
   {
      if(quartzScheduler == null)
      {
         throw (new IllegalStateException("QuartzScheduler singleton has not been init'ed."));
      }
      return (quartzScheduler);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void start()
   {
      try
      {
         // Properties properties = new Properties();
         // properties.put("");

         //////////////////////////////////////////////////
         // Grab the Scheduler instance from the Factory //
         //////////////////////////////////////////////////
         StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
         // schedulerFactory.initialize(properties);
         this.scheduler = schedulerFactory.getScheduler();

         ////////////////////////////////////////
         // todo - do we get our own property? //
         ////////////////////////////////////////
         if(!new QMetaDataVariableInterpreter().getBooleanFromPropertyOrEnvironment("qqq.scheduleManager.enabled", "QQQ_SCHEDULE_MANAGER_ENABLED", true))
         {
            LOG.info("Not starting QuartzScheduler per settings.");
            return;
         }

         /////////////////////////////////////////////
         // make sure all of our jobs are scheduled //
         /////////////////////////////////////////////
         scheduleAllJobs();

         //////////////////////
         // and start it off //
         //////////////////////
         scheduler.start();
      }
      catch(Exception e)
      {
         LOG.error("Error starting quartz scheduler", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void scheduleAllJobs()
   {
      for(QProcessMetaData process : qInstance.getProcesses().values())
      {
         if(process.getSchedule() != null && SchedulerUtils.allowedToStart(process.getName()))
         {
            if(process.getSchedule().getVariantBackend() == null || QScheduleMetaData.RunStrategy.SERIAL.equals(process.getSchedule().getVariantRunStrategy()))
            {
               scheduleProcess(process, null);
            }
            else
            {
               LOG.error("Not yet know how to schedule parallel variant jobs");
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void scheduleProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData)
   {
      try
      {
         QScheduleMetaData scheduleMetaData = process.getSchedule();
         long              intervalMillis   = Objects.requireNonNullElse(scheduleMetaData.getRepeatMillis(), scheduleMetaData.getRepeatSeconds() * 1000);

         Date startAt = new Date();
         if(scheduleMetaData.getInitialDelayMillis() != null)
         {
            startAt.setTime(startAt.getTime() + scheduleMetaData.getInitialDelayMillis());
         }
         if(scheduleMetaData.getInitialDelaySeconds() != null)
         {
            startAt.setTime(startAt.getTime() + scheduleMetaData.getInitialDelaySeconds() * 1000);
         }

         /////////////////////////
         // Define job instance //
         /////////////////////////
         JobKey jobKey = new JobKey(process.getName(), "processes");
         JobDetail jobDetail = JobBuilder.newJob(QuartzRunProcessJob.class)
            .withIdentity(jobKey)
            .storeDurably()
            .requestRecovery()
            .build();

         jobDetail.getJobDataMap().put("processName", process.getName());

         ///////////////////////////////////////
         // Define a Trigger for the schedule //
         ///////////////////////////////////////
         Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(new TriggerKey(process.getName(), "processes"))
            .forJob(jobKey)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
               .withIntervalInMilliseconds(intervalMillis)
               .repeatForever())
            .startAt(startAt)
            .build();

         ///////////////////////////////////////
         // Schedule the job with the trigger //
         ///////////////////////////////////////
         boolean isJobAlreadyScheduled = isJobAlreadyScheduled(jobKey);
         if(isJobAlreadyScheduled)
         {
            this.scheduler.addJob(jobDetail, true);
            this.scheduler.rescheduleJob(trigger.getKey(), trigger);
            LOG.info("Re-scheduled process: " + process.getName());
         }
         else
         {
            this.scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("Scheduled new process: " + process.getName());
         }

      }
      catch(Exception e)
      {
         LOG.warn("Error scheduling process", e, logPair("processName", process.getName()));
      }
   }



   /*******************************************************************************
    ** todo - probably rewrite this to not re-query quartz each time
    *******************************************************************************/
   private boolean isJobAlreadyScheduled(JobKey jobKey) throws SchedulerException
   {
      for(String group : scheduler.getJobGroupNames())
      {
         for(JobKey testJobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group)))
         {
            if(testJobKey.equals(jobKey))
            {
               return (true);
            }
         }
      }

      return (false);
   }



   /*
   private void todo() throws SchedulerException
   {
      // https://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/ListJobs.html
      // Listing all Jobs in the scheduler
      for(String group : scheduler.getJobGroupNames())
      {
         for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.groupEquals(group)))
         {
            System.out.println("Found job identified by: " + jobKey);
         }
      }

      // https://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/UpdateJob.html
      // Update an existing job
      // Add the new job to the scheduler, instructing it to "replace"
      //  the existing job with the given name and group (if any)
      JobDetail jobDetail = JobBuilder.newJob(QuartzRunProcessJob.class)
         .withIdentity("job1", "group1")
         .build();
      // store, and set overwrite flag to 'true'
      scheduler.addJob(jobDetail, true);

      // https://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/UpdateTrigger.html
      // Define a new Trigger
      Trigger trigger = TriggerBuilder.newTrigger()
         .withIdentity("newTrigger", "group1")
         .startNow()
         .build();

      // tell the scheduler to remove the old trigger with the given key, and put the new one in its place
      scheduler.rescheduleJob(new TriggerKey("oldTrigger", "group1"), trigger);

      // https://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/UnscheduleJob.html
      // Deleting a Job and Unscheduling All of Its Triggers
      scheduler.deleteJob(new JobKey("job1", "group1"));

   }
   */



   /*******************************************************************************
    ** Getter for qInstance
    **
    *******************************************************************************/
   public QInstance getQInstance()
   {
      return qInstance;
   }



   /*******************************************************************************
    ** Getter for sessionSupplier
    **
    *******************************************************************************/
   public Supplier<QSession> getSessionSupplier()
   {
      return sessionSupplier;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void pauseAll() throws SchedulerException
   {
      this.scheduler.pauseAll();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void resumeAll() throws SchedulerException
   {
      this.scheduler.resumeAll();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void pauseJob(String jobName, String groupName) throws SchedulerException
   {
      this.scheduler.pauseJob(new JobKey(jobName, groupName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void resumeJob(String jobName, String groupName) throws SchedulerException
   {
      this.scheduler.resumeJob(new JobKey(jobName, groupName));
   }
}
