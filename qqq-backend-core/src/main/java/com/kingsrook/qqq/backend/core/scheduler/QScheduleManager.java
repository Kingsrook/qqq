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
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
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
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.apache.commons.lang.NotImplementedException;


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
      if(!new QMetaDataVariableInterpreter().getBooleanFromPropertyOrEnvironment("qqq.scheduleManager.enabled", "QQQ_SCHEDULE_MANAGER_ENABLED", true))
      {
         LOG.info("Not starting ScheduleManager per settings.");
         return;
      }

      /////////////////////////////////////////////////////////
      // initialize the scheduler(s) we're configured to use //
      /////////////////////////////////////////////////////////
      for(QSchedulerMetaData schedulerMetaData : qInstance.getSchedulers().values())
      {
         QSchedulerInterface scheduler = schedulerMetaData.initSchedulerInstance(qInstance, systemUserSessionSupplier);
         schedulers.put(schedulerMetaData.getName(), scheduler);
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
      }

      /////////////////////////////////////////////////////////////
      // todo - read dynamic schedules and schedule those things //
      // e.g., user-scheduled processes, reports                 //
      /////////////////////////////////////////////////////////////
      // ScheduledJob scheduledJob = new ScheduledJob();
      // setupScheduledJob(scheduledJob);

      //////////////////////////////////////////////////////////
      // let the schedulers know we're done with this process //
      //////////////////////////////////////////////////////////
      schedulers.values().forEach(s -> s.endOfSetupSchedules());
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
