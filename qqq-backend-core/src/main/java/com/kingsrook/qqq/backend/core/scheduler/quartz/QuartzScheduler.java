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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.QSchedulerInterface;
import com.kingsrook.qqq.backend.core.utils.memoization.AnyKey;
import com.kingsrook.qqq.backend.core.utils.memoization.Memoization;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
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
 ** Singleton to provide access between QQQ and the quartz Scheduler system.
 *******************************************************************************/
public class QuartzScheduler implements QSchedulerInterface
{
   private static final QLogger LOG = QLogger.getLogger(QuartzScheduler.class);

   private static QuartzScheduler quartzScheduler = null;

   private final QInstance          qInstance;
   private       String             schedulerName;
   private       Properties         quartzProperties;
   private       Supplier<QSession> sessionSupplier;

   private Scheduler scheduler;

   /////////////////////////////////////////////////////////////////////////////////////////
   // create memoization objects for some quartz-query functions, that we'll only want to //
   // use during our setup routine, when we'd query it many times over and over again.    //
   // So default to a timeout of 0 (effectively disabling memoization).  then in the      //
   // start-of-setup and end-of-setup methods, temporarily increase, then re-decrease     //
   /////////////////////////////////////////////////////////////////////////////////////////
   private Memoization<AnyKey, List<String>> jobGroupNamesMemoization = new Memoization<AnyKey, List<String>>()
      .withTimeout(Duration.of(0, ChronoUnit.SECONDS));

   private Memoization<String, Set<JobKey>> jobKeyNamesMemoization = new Memoization<String, Set<JobKey>>()
      .withTimeout(Duration.of(0, ChronoUnit.SECONDS));

   ///////////////////////////////////////////////////////////////////////////////
   // vars used during the setup routine, to figure out what jobs need deleted. //
   ///////////////////////////////////////////////////////////////////////////////
   private boolean insideSetup = false;
   private List<QuartzJobAndTriggerWrapper> scheduledJobsAtStartOfSetup = new ArrayList<>();
   private List<QuartzJobAndTriggerWrapper> scheduledJobsAtEndOfSetup = new ArrayList<>();


   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private QuartzScheduler(QInstance qInstance, String schedulerName, Properties quartzProperties, Supplier<QSession> sessionSupplier)
   {
      this.qInstance = qInstance;
      this.schedulerName = schedulerName;
      this.quartzProperties = quartzProperties;
      this.sessionSupplier = sessionSupplier;
   }



   /*******************************************************************************
    ** Singleton initiator - e.g., must be called to initially initialize the singleton
    ** before anyone else calls getInstance (they'll get an error if they call that first).
    *******************************************************************************/
   public static QuartzScheduler initInstance(QInstance qInstance, String schedulerName, Properties quartzProperties, Supplier<QSession> sessionSupplier) throws SchedulerException
   {
      if(quartzScheduler == null)
      {
         quartzScheduler = new QuartzScheduler(qInstance, schedulerName, quartzProperties, sessionSupplier);

         ///////////////////////////////////////////////////////////
         // Grab the Scheduler instance from the Factory          //
         // initialize it with the properties we took in as input //
         ///////////////////////////////////////////////////////////
         StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
         schedulerFactory.initialize(quartzProperties);
         quartzScheduler.scheduler = schedulerFactory.getScheduler();
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
         throw (new IllegalStateException("QuartzScheduler singleton has not been init'ed (call initInstance)."));
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
   @Override
   public void stop()
   {
      try
      {
         scheduler.shutdown(true);
      }
      catch(SchedulerException e)
      {
         LOG.error("Error shutting down (stopping) quartz scheduler", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void stopAsync()
   {
      try
      {
         scheduler.shutdown(false);
      }
      catch(SchedulerException e)
      {
         LOG.error("Error shutting down (stopping) quartz scheduler", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData, QScheduleMetaData schedule, boolean allowedToStart)
   {
      /////////////////////////
      // set up job data map //
      /////////////////////////
      Map<String, Object> jobData = new HashMap<>();
      jobData.put("processName", process.getName());

      if(backendVariantData != null)
      {
         jobData.put("backendVariantData", backendVariantData);
      }

      scheduleJob(process.getName(), "processes", QuartzRunProcessJob.class, jobData, schedule, allowedToStart);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void startOfSetupSchedules()
   {
      this.insideSetup = true;
      this.jobGroupNamesMemoization.setTimeout(Duration.ofSeconds(5));
      this.jobKeyNamesMemoization.setTimeout(Duration.ofSeconds(5));

      try
      {
         this.scheduledJobsAtStartOfSetup = queryQuartz();
      }
      catch(Exception e)
      {
         LOG.warn("Error querying quartz for the currently scheduled jobs during startup - will not be able to delete no-longer-needed jobs!", e);
      }
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void endOfSetupSchedules()
   {
      this.insideSetup = false;
      this.jobGroupNamesMemoization.setTimeout(Duration.ofSeconds(0));
      this.jobKeyNamesMemoization.setTimeout(Duration.ofSeconds(0));

      if(this.scheduledJobsAtStartOfSetup == null)
      {
         return;
      }

      try
      {
         Set<JobKey> startJobKeys = this.scheduledJobsAtStartOfSetup.stream().map(w -> w.jobDetail().getKey()).collect(Collectors.toSet());
         Set<JobKey> endJobKeys = scheduledJobsAtEndOfSetup.stream().map(w -> w.jobDetail().getKey()).collect(Collectors.toSet());

         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // remove all 'end' keys from the set of start keys.  any left-over start-keys need to be deleted. //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         startJobKeys.removeAll(endJobKeys);
         for(JobKey jobKey : startJobKeys)
         {
            LOG.info("Deleting job that had previously been scheduled, but doesn't appear to be any more", logPair("jobKey", jobKey));
            deleteJob(jobKey);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error trying to clean up no-longer-needed jobs at end of scheduler setup", e);
      }

      ////////////////////////////////////////////////////
      // reset these lists, no need to keep them around //
      ////////////////////////////////////////////////////
      this.scheduledJobsAtStartOfSetup = null;
      this.scheduledJobsAtEndOfSetup = null;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean scheduleJob(String jobName, String groupName, Class<? extends Job> jobClass, Map<String, Object> jobData, QScheduleMetaData scheduleMetaData, boolean allowedToStart)
   {
      try
      {
         /////////////////////////
         // Define job instance //
         /////////////////////////
         JobKey jobKey = new JobKey(jobName, groupName);
         JobDetail jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobKey)
            .storeDurably()
            .requestRecovery()
            .build();

         jobDetail.getJobDataMap().putAll(jobData);

         /////////////////////////////////////////////////////////
         // map the qqq schedule meta data to a quartz schedule //
         /////////////////////////////////////////////////////////
         ScheduleBuilder<?> scheduleBuilder;
         if(scheduleMetaData.isCron())
         {
            CronExpression cronExpression = new CronExpression(scheduleMetaData.getCronExpression());
            cronExpression.setTimeZone(TimeZone.getTimeZone(scheduleMetaData.getCronTimeZoneId()));
            scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
         }
         else
         {
            long intervalMillis = Objects.requireNonNullElseGet(scheduleMetaData.getRepeatMillis(), () -> scheduleMetaData.getRepeatSeconds() * 1000);
            scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
               .withIntervalInMilliseconds(intervalMillis)
               .repeatForever();
         }

         Date startAt = new Date();
         if(scheduleMetaData.getInitialDelayMillis() != null)
         {
            startAt.setTime(startAt.getTime() + scheduleMetaData.getInitialDelayMillis());
         }
         else if(scheduleMetaData.getInitialDelaySeconds() != null)
         {
            startAt.setTime(startAt.getTime() + scheduleMetaData.getInitialDelaySeconds() * 1000);
         }

         ///////////////////////////////////////
         // Define a Trigger for the schedule //
         ///////////////////////////////////////
         Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(new TriggerKey(jobName, groupName))
            .forJob(jobKey)
            .withSchedule(scheduleBuilder)
            .startAt(startAt)
            .build();

         ///////////////////////////////////////
         // Schedule the job with the trigger //
         ///////////////////////////////////////
         addOrReplaceJobAndTrigger(jobKey, jobDetail, trigger);

         //////////////////////////////////////////////////////////
         // either pause or resume, based on if allowed to start //
         //////////////////////////////////////////////////////////
         if(!allowedToStart)
         {
            pauseJob(jobKey.getName(), jobKey.getGroup());
         }
         else
         {
            resumeJob(jobKey.getName(), jobKey.getGroup());
         }

         ///////////////////////////////////////////////////////////////////////////
         // if we're inside the setup event (e.g., initial startup), then capture //
         // this job as one that is currently active and should be kept.          //
         ///////////////////////////////////////////////////////////////////////////
         if(insideSetup)
         {
            scheduledJobsAtEndOfSetup.add(new QuartzJobAndTriggerWrapper(jobDetail, trigger, null));
         }

         return (true);
      }
      catch(Exception e)
      {
         LOG.warn("Error scheduling job", e, logPair("name", jobName), logPair("group", groupName));
         return (false);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupSqsPoller(SQSQueueProviderMetaData queueProvider, QQueueMetaData queue, QScheduleMetaData schedule, boolean allowedToStart)
   {
      /////////////////////////
      // set up job data map //
      /////////////////////////
      Map<String, Object> jobData = new HashMap<>();
      jobData.put("queueProviderName", queueProvider.getName());
      jobData.put("queueName", queue.getName());

      scheduleJob(queue.getName(), "sqsQueue", QuartzSqsPollerJob.class, jobData, schedule, allowedToStart);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupTableAutomation(QAutomationProviderMetaData automationProvider, PollingAutomationPerTableRunner.TableActionsInterface tableActions, QScheduleMetaData schedule, boolean allowedToStart)
   {
      /////////////////////////
      // set up job data map //
      /////////////////////////
      Map<String, Object> jobData = new HashMap<>();
      jobData.put("automationProviderName", automationProvider.getName());
      jobData.put("tableName", tableActions.tableName());
      jobData.put("automationStatus", tableActions.status().toString());

      scheduleJob(tableActions.tableName() + "." + tableActions.status(), "tableAutomations", QuartzTableAutomationsJob.class, jobData, schedule, allowedToStart);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   private void addOrReplaceJobAndTrigger(JobKey jobKey, JobDetail jobDetail, Trigger trigger) throws SchedulerException
   {
      boolean isJobAlreadyScheduled = isJobAlreadyScheduled(jobKey);
      if(isJobAlreadyScheduled)
      {
         this.scheduler.addJob(jobDetail, true);
         this.scheduler.rescheduleJob(trigger.getKey(), trigger);
         LOG.info("Re-scheduled job: " + jobKey);
      }
      else
      {
         this.scheduler.scheduleJob(jobDetail, trigger);
         LOG.info("Scheduled new job: " + jobKey);
      }

      // todo - think about... clear memoization - but - when this is used in bulk, that's when we want the memo!
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean isJobAlreadyScheduled(JobKey jobKey) throws SchedulerException
   {
      Optional<List<String>> jobGroupNames = jobGroupNamesMemoization.getResult(AnyKey.getInstance(), (x) -> scheduler.getJobGroupNames());
      if(jobGroupNames.isEmpty())
      {
         throw (new SchedulerException("Error getting job group names"));
      }

      for(String group : jobGroupNames.get())
      {
         Optional<Set<JobKey>> jobKeys = jobKeyNamesMemoization.getResult(group, (x) -> scheduler.getJobKeys(GroupMatcher.groupEquals(group)));
         if(jobKeys.isEmpty())
         {
            throw (new SchedulerException("Error getting job keys"));
         }

         if(jobKeys.get().contains(jobKey))
         {
            return (true);
         }
      }

      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean deleteJob(JobKey jobKey)
   {
      try
      {
         /////////////////////////////////////////////////////////////////////////////////////////////
         // https://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/UnscheduleJob.html //
         // Deleting a Job and Unscheduling All of Its Triggers                                     //
         /////////////////////////////////////////////////////////////////////////////////////////////
         if(isJobAlreadyScheduled(jobKey))
         {
            return scheduler.deleteJob(jobKey);
         }

         /////////////////////////////////////////
         // return true to indicate, we're good //
         /////////////////////////////////////////
         return (true);
      }
      catch(Exception e)
      {
         LOG.warn("Error deleting job", e, logPair("jobKey", jobKey));
         return false;
      }
   }



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



   /*******************************************************************************
    **
    *******************************************************************************/
   List<QuartzJobAndTriggerWrapper> queryQuartz() throws SchedulerException
   {
      List<QuartzJobAndTriggerWrapper> rs = new ArrayList<>();
      List<String> jobGroupNames = scheduler.getJobGroupNames();

      for(String group : jobGroupNames)
      {
         Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(group));
         for(JobKey jobKey : jobKeys)
         {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
            for(Trigger trigger : triggersOfJob)
            {
               Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
               rs.add(new QuartzJobAndTriggerWrapper(jobDetail, trigger, triggerState));
            }
         }
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void unInit()
   {
      ///////////////////////////////////////////////////
      // resetting the singleton should be sufficient! //
      ///////////////////////////////////////////////////
      quartzScheduler = null;
   }
}
