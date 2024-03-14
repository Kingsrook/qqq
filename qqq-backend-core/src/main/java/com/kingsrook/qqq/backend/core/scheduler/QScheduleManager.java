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


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QSchedulerMetaData;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJobType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.apache.commons.lang.NotImplementedException;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** QQQ service to manage scheduled jobs, using 1 or more Schedulers - implementations
 ** of the QSchedulerInterface
 *******************************************************************************/
public class QScheduleManager
{
   private static final QLogger LOG = QLogger.getLogger(QScheduleManager.class);

   private static QScheduleManager   qScheduleManager = null;
   private final  QInstance          qInstance;
   private final  Supplier<QSession> systemUserSessionSupplier;

   private Map<String, QSchedulerInterface> schedulers = new HashMap<>();



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private QScheduleManager(QInstance qInstance, Supplier<QSession> systemUserSessionSupplier)
   {
      this.qInstance = qInstance;
      this.systemUserSessionSupplier = systemUserSessionSupplier;
   }



   /*******************************************************************************
    ** Singleton initiator - e.g., must be called to initially initialize the singleton
    ** before anyone else calls getInstance (they'll get an error if they call that first).
    *******************************************************************************/
   public static QScheduleManager initInstance(QInstance qInstance, Supplier<QSession> systemUserSessionSupplier)
   {
      if(qScheduleManager == null)
      {
         qScheduleManager = new QScheduleManager(qInstance, systemUserSessionSupplier);
      }
      return (qScheduleManager);
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static QScheduleManager getInstance()
   {
      if(qScheduleManager == null)
      {
         throw (new IllegalStateException("QScheduleManager singleton has not been init'ed (call initInstance)."));
      }
      return (qScheduleManager);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void start() throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // initialize the scheduler(s) we're configured to use                                                                     //
      // do this, even if we won't start them - so, for example, a web server can still be aware of schedules in the application //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QSchedulerMetaData schedulerMetaData : CollectionUtils.nonNullMap(qInstance.getSchedulers()).values())
      {
         QSchedulerInterface scheduler = schedulerMetaData.initSchedulerInstance(qInstance, systemUserSessionSupplier);
         schedulers.put(schedulerMetaData.getName(), scheduler);
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now, exist w/o setting up schedules and not starting schedules, if schedule manager isn't enabled here //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(!new QMetaDataVariableInterpreter().getBooleanFromPropertyOrEnvironment("qqq.scheduleManager.enabled", "QQQ_SCHEDULE_MANAGER_ENABLED", true))
      {
         LOG.info("Not starting ScheduleManager per settings.");
         return;
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // ensure that everything which should be scheduled is scheduled, in the appropriate scheduler //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      QContext.withTemporaryContext(new CapturedContext(qInstance, systemUserSessionSupplier.get()), () -> setupSchedules());

      //////////////////////////
      // start each scheduler //
      //////////////////////////
      schedulers.values().forEach(s -> s.start());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void stop()
   {
      schedulers.values().forEach(s -> s.stop());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void stopAsync()
   {
      schedulers.values().forEach(s -> s.stopAsync());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupSchedules()
   {
      /////////////////////////////////////////////
      // read dynamic schedules                  //
      // e.g., user-scheduled processes, reports //
      /////////////////////////////////////////////
      List<ScheduledJob> scheduledJobList = null;
      try
      {
         if(QContext.getQInstance().getTables().containsKey(ScheduledJob.TABLE_NAME))
         {
            scheduledJobList = new QueryAction()
               .execute(new QueryInput(ScheduledJob.TABLE_NAME)
                  .withIncludeAssociations(true))
               .getRecordEntities(ScheduledJob.class);
         }
      }
      catch(Exception e)
      {
         throw (new QRuntimeException("Failed to query for scheduled jobs - will not set up scheduler!", e));
      }

      /////////////////////////////////////////////////////////
      // let the schedulers know we're starting this process //
      /////////////////////////////////////////////////////////
      schedulers.values().forEach(s -> s.startOfSetupSchedules());

      //////////////////////////////////
      // schedule all queue providers //
      //////////////////////////////////
      for(QQueueProviderMetaData queueProvider : qInstance.getQueueProviders().values())
      {
         setupQueueProvider(queueProvider);
      }

      ///////////////////////////////////////
      // schedule all automation providers //
      ///////////////////////////////////////
      for(QAutomationProviderMetaData automationProvider : qInstance.getAutomationProviders().values())
      {
         setupAutomationProviderPerTable(automationProvider);
      }

      /////////////////////////////////////////
      // schedule all processes that need it //
      /////////////////////////////////////////
      for(QProcessMetaData process : qInstance.getProcesses().values())
      {
         if(process.getSchedule() != null)
         {
            setupProcess(process);
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////
      // todo- before, or after meta-datas?                                                      //
      // like quartz, it'd just re-schedule if a dupe - but, should we do our own dupe checking? //
      /////////////////////////////////////////////////////////////////////////////////////////////
      for(ScheduledJob scheduledJob : CollectionUtils.nonNullList(scheduledJobList))
      {
         try
         {
            setupScheduledJob(scheduledJob);
         }
         catch(Exception e)
         {
            LOG.info("Caught exception while scheduling a job", e, logPair("id", scheduledJob.getId()));
         }
      }

      //////////////////////////////////////////////////////////
      // let the schedulers know we're done with this process //
      //////////////////////////////////////////////////////////
      schedulers.values().forEach(s -> s.endOfSetupSchedules());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setupScheduledJob(ScheduledJob scheduledJob)
   {
      ///////////////////////////////////////////////////////////////////////////////////////////
      // non-active jobs should be deleted from the scheduler.  they get re-added              //
      // if they get re-activated.  but we don't want to rely on (e.g., for quartz) the paused //
      // state to be drive by is-active.  else, devops-pause & unpause ops would clobber       //
      // scheduled-job record facts                                                            //
      ///////////////////////////////////////////////////////////////////////////////////////////
      if(!scheduledJob.getIsActive())
      {
         unscheduleScheduledJob(scheduledJob);
         return;
      }

      QSchedulerInterface scheduler = getScheduler(scheduledJob.getSchedulerName());

      QScheduleMetaData scheduleMetaData = new QScheduleMetaData();
      scheduleMetaData.setCronExpression(scheduledJob.getCronExpression());
      scheduleMetaData.setCronTimeZoneId(scheduledJob.getCronTimeZoneId());

      switch(ScheduledJobType.getById(scheduledJob.getType()))
      {
         case PROCESS ->
         {
            Map<String, String> paramMap    = scheduledJob.getJobParametersMap();
            String              processName = paramMap.get("processName");
            QProcessMetaData    process     = qInstance.getProcess(processName);

            // todo - variants... serial vs parallel?
            scheduler.setupProcess(process, null, scheduleMetaData, true);
         }
         case QUEUE_PROCESSOR ->
         {
            throw new NotImplementedException("ScheduledJob queue processors are not yet implemented...");
         }
         case TABLE_AUTOMATIONS ->
         {
            throw new NotImplementedException("ScheduledJob table automations are not yet implemented...");
         }
         default -> throw new IllegalStateException("Unexpected value: " + ScheduledJobType.getById(scheduledJob.getType()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void unscheduleScheduledJob(ScheduledJob scheduledJob)
   {
      QSchedulerInterface scheduler = getScheduler(scheduledJob.getSchedulerName());

      switch(ScheduledJobType.getById(scheduledJob.getType()))
      {
         case PROCESS ->
         {
            Map<String, String> paramMap    = scheduledJob.getJobParametersMap();
            String              processName = paramMap.get("processName");
            QProcessMetaData    process     = qInstance.getProcess(processName);
            scheduler.unscheduleProcess(process);
         }
         case QUEUE_PROCESSOR ->
         {
            throw new NotImplementedException("ScheduledJob queue processors are not yet implemented...");
         }
         case TABLE_AUTOMATIONS ->
         {
            throw new NotImplementedException("ScheduledJob table automations are not yet implemented...");
         }
         default -> throw new IllegalStateException("Unexpected value: " + ScheduledJobType.getById(scheduledJob.getType()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupProcess(QProcessMetaData process)
   {
      QScheduleMetaData scheduleMetaData = process.getSchedule();
      if(process.getSchedule().getVariantBackend() == null || QScheduleMetaData.RunStrategy.SERIAL.equals(process.getSchedule().getVariantRunStrategy()))
      {
         ///////////////////////////////////////////////
         // if no variants, or variant is serial mode //
         ///////////////////////////////////////////////
         setupProcess(process, null);
      }
      else if(QScheduleMetaData.RunStrategy.PARALLEL.equals(process.getSchedule().getVariantRunStrategy()))
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // if this a "parallel", which for example means we want to have a thread for each backend variant //
         // running at the same time, get the variant records and schedule each separately                  //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         QBackendMetaData backendMetaData = qInstance.getBackend(scheduleMetaData.getVariantBackend());
         for(QRecord qRecord : CollectionUtils.nonNullList(SchedulerUtils.getBackendVariantFilteredRecords(process)))
         {
            try
            {
               setupProcess(process, MapBuilder.of(backendMetaData.getVariantOptionsTableTypeValue(), qRecord.getValue(backendMetaData.getVariantOptionsTableIdField())));
            }
            catch(Exception e)
            {
               LOG.error("An error starting process [" + process.getLabel() + "], with backend variant data.", e, new LogPair("variantQRecord", qRecord));
            }
         }
      }
      else
      {
         LOG.error("Unsupported Schedule Run Strategy [" + process.getSchedule().getVariantRunStrategy() + "] was provided.");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData)
   {
      QSchedulerInterface scheduler = getScheduler(process.getSchedule().getSchedulerName());
      scheduler.setupProcess(process, backendVariantData, process.getSchedule(), SchedulerUtils.allowedToStart(process));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupQueueProvider(QQueueProviderMetaData queueProvider)
   {
      switch(queueProvider.getType())
      {
         case SQS:
            setupSqsProvider((SQSQueueProviderMetaData) queueProvider);
            break;
         default:
            throw new IllegalArgumentException("Unhandled queue provider type: " + queueProvider.getType());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupSqsProvider(SQSQueueProviderMetaData queueProvider)
   {
      boolean allowedToStartProvider = SchedulerUtils.allowedToStart(queueProvider);

      for(QQueueMetaData queue : qInstance.getQueues().values())
      {
         QSchedulerInterface scheduler = getScheduler(queue.getSchedule().getSchedulerName());

         boolean allowedToStart = allowedToStartProvider && SchedulerUtils.allowedToStart(queue.getName());
         if(queueProvider.getName().equals(queue.getProviderName()))
         {
            scheduler.setupSqsPoller(queueProvider, queue, queue.getSchedule(), allowedToStart);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setupAutomationProviderPerTable(QAutomationProviderMetaData automationProvider)
   {
      boolean allowedToStartProvider = SchedulerUtils.allowedToStart(automationProvider);

      ///////////////////////////////////////////////////////////////////////////////////
      // ask the PollingAutomationPerTableRunner how many threads of itself need setup //
      // then schedule each one of them.                                               //
      ///////////////////////////////////////////////////////////////////////////////////
      List<PollingAutomationPerTableRunner.TableActionsInterface> tableActionList = PollingAutomationPerTableRunner.getTableActions(qInstance, automationProvider.getName());
      for(PollingAutomationPerTableRunner.TableActionsInterface tableActions : tableActionList)
      {
         boolean allowedToStart = allowedToStartProvider && SchedulerUtils.allowedToStart(tableActions.tableName());

         QScheduleMetaData   schedule  = tableActions.tableAutomationDetails().getSchedule();
         QSchedulerInterface scheduler = getScheduler(schedule.getSchedulerName());
         scheduler.setupTableAutomation(automationProvider, tableActions, schedule, allowedToStart);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QSchedulerInterface getScheduler(String schedulerName)
   {
      QSchedulerInterface scheduler = schedulers.get(schedulerName);
      if(scheduler == null)
      {
         throw new NotImplementedException("default scheduler...");
      }

      return (scheduler);
   }



   /*******************************************************************************
    ** reset the singleton instance (to null); clear the map of schedulers.
    ** Not clear it's ever useful to call in main-code - but can be used for tests.
    *******************************************************************************/
   public void unInit()
   {
      qScheduleManager = null;
      schedulers.values().forEach(s -> s.unInit());
      schedulers.clear();
   }
}
