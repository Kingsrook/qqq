/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.actions.queues.SQSQueuePoller;
import com.kingsrook.qqq.backend.core.context.QContext;
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
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** QQQ Service (Singleton) that starts up repeating, scheduled jobs within QQQ.
 **
 ** These include:
 ** - Automation providers (which require polling)
 ** - Queue pollers
 ** - Scheduled processes.
 **
 ** All of these jobs run using a "system session" - as defined by the sessionSupplier.
 *******************************************************************************/
public class ScheduleManager
{
   private static final QLogger LOG = QLogger.getLogger(ScheduleManager.class);

   private static ScheduleManager scheduleManager = null;
   private final  QInstance       qInstance;

   protected Supplier<QSession> sessionSupplier;

   /////////////////////////////////////////////////////////////////////////////////////
   // for jobs that don't define a delay index, auto-stagger them, using this counter //
   /////////////////////////////////////////////////////////////////////////////////////
   private int delayIndex = 0;

   private List<StandardScheduledExecutor> executors = new ArrayList<>();



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private ScheduleManager(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static ScheduleManager getInstance(QInstance qInstance)
   {
      if(scheduleManager == null)
      {
         scheduleManager = new ScheduleManager(qInstance);
      }
      return (scheduleManager);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void start()
   {
      if(!new QMetaDataVariableInterpreter().getBooleanFromPropertyOrEnvironment("qqq.scheduleManager.enabled", "QQQ_SCHEDULE_MANAGER_ENABLED", true))
      {
         LOG.info("Not starting ScheduleManager per settings.");
         return;
      }

      boolean needToClearContext = false;
      try
      {
         if(QContext.getQInstance() == null)
         {
            needToClearContext = true;
            QContext.init(qInstance, sessionSupplier.get());
         }

         for(QQueueProviderMetaData queueProvider : qInstance.getQueueProviders().values())
         {
            startQueueProvider(queueProvider);
         }

         for(QAutomationProviderMetaData automationProvider : qInstance.getAutomationProviders().values())
         {
            startAutomationProviderPerTable(automationProvider);
         }

         for(QProcessMetaData process : qInstance.getProcesses().values())
         {
            if(process.getSchedule() != null && SchedulerUtils.allowedToStart(process.getName()))
            {
               QScheduleMetaData scheduleMetaData = process.getSchedule();
               if(process.getSchedule().getVariantBackend() == null || QScheduleMetaData.RunStrategy.SERIAL.equals(process.getSchedule().getVariantRunStrategy()))
               {
                  ///////////////////////////////////////////////
                  // if no variants, or variant is serial mode //
                  ///////////////////////////////////////////////
                  startProcess(process, null);
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
                        startProcess(process, MapBuilder.of(backendMetaData.getVariantOptionsTableTypeValue(), qRecord.getValue(backendMetaData.getVariantOptionsTableIdField())));
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
      }
      finally
      {
         if(needToClearContext)
         {
            QContext.clear();
         }
      }
   }




   /*******************************************************************************
    **
    *******************************************************************************/
   private void startAutomationProviderPerTable(QAutomationProviderMetaData automationProvider)
   {
      ///////////////////////////////////////////////////////////////////////////////////
      // ask the PollingAutomationPerTableRunner how many threads of itself need setup //
      // then start a scheduled executor foreach one                                   //
      ///////////////////////////////////////////////////////////////////////////////////
      List<PollingAutomationPerTableRunner.TableActionsInterface> tableActions = PollingAutomationPerTableRunner.getTableActions(qInstance, automationProvider.getName());
      for(PollingAutomationPerTableRunner.TableActionsInterface tableAction : tableActions)
      {
         if(SchedulerUtils.allowedToStart(tableAction.tableName()))
         {
            PollingAutomationPerTableRunner runner   = new PollingAutomationPerTableRunner(qInstance, automationProvider.getName(), sessionSupplier, tableAction);
            StandardScheduledExecutor       executor = new StandardScheduledExecutor(runner);

            QScheduleMetaData schedule = Objects.requireNonNullElseGet(automationProvider.getSchedule(), this::getDefaultSchedule);

            executor.setName(runner.getName());
            setScheduleInExecutor(schedule, executor);
            if(!executor.start())
            {
               LOG.warn("executor.start return false for: " + executor.getName());
            }

            executors.add(executor);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void startQueueProvider(QQueueProviderMetaData queueProvider)
   {
      if(SchedulerUtils.allowedToStart(queueProvider.getName()))
      {
         switch(queueProvider.getType())
         {
            case SQS:
               startSqsProvider((SQSQueueProviderMetaData) queueProvider);
               break;
            default:
               throw new IllegalArgumentException("Unhandled queue provider type: " + queueProvider.getType());
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void startSqsProvider(SQSQueueProviderMetaData queueProvider)
   {
      QInstance          scheduleManagerQueueInstance   = qInstance;
      Supplier<QSession> scheduleManagerSessionSupplier = sessionSupplier;

      for(QQueueMetaData queue : qInstance.getQueues().values())
      {
         if(queueProvider.getName().equals(queue.getProviderName()) && SchedulerUtils.allowedToStart(queue.getName()))
         {
            SQSQueuePoller sqsQueuePoller = new SQSQueuePoller();
            sqsQueuePoller.setQueueProviderMetaData(queueProvider);
            sqsQueuePoller.setQueueMetaData(queue);
            sqsQueuePoller.setQInstance(scheduleManagerQueueInstance);
            sqsQueuePoller.setSessionSupplier(scheduleManagerSessionSupplier);

            StandardScheduledExecutor executor = new StandardScheduledExecutor(sqsQueuePoller);

            QScheduleMetaData schedule = Objects.requireNonNullElseGet(queue.getSchedule(),
               () -> Objects.requireNonNullElseGet(queueProvider.getSchedule(),
                  this::getDefaultSchedule));

            executor.setName(queue.getName());
            setScheduleInExecutor(schedule, executor);
            if(!executor.start())
            {
               LOG.warn("executor.start return false for: " + executor.getName());
            }

            executors.add(executor);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void startProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData)
   {
      Runnable runProcess = () ->
      {
         SchedulerUtils.runProcess(qInstance, sessionSupplier, process, backendVariantData);
      };

      StandardScheduledExecutor executor = new StandardScheduledExecutor(runProcess);
      executor.setName("process:" + process.getName());
      setScheduleInExecutor(process.getSchedule(), executor);
      if(!executor.start())
      {
         LOG.warn("executor.start return false for: " + executor.getName());
      }

      executors.add(executor);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setScheduleInExecutor(QScheduleMetaData schedule, StandardScheduledExecutor executor)
   {
      if(schedule.getRepeatMillis() != null)
      {
         executor.setDelayMillis(schedule.getRepeatMillis());
      }
      else
      {
         executor.setDelayMillis(1000 * schedule.getRepeatSeconds());
      }

      if(schedule.getInitialDelayMillis() != null)
      {
         executor.setInitialDelayMillis(schedule.getInitialDelayMillis());
      }
      else if(schedule.getInitialDelaySeconds() != null)
      {
         executor.setInitialDelayMillis(1000 * schedule.getInitialDelaySeconds());
      }
      else
      {
         executor.setInitialDelayMillis(1000 * ++delayIndex);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QScheduleMetaData getDefaultSchedule()
   {
      QScheduleMetaData schedule;
      schedule = new QScheduleMetaData()
         .withInitialDelaySeconds(delayIndex++)
         .withRepeatSeconds(60);
      return schedule;
   }



   /*******************************************************************************
    ** Setter for sessionSupplier
    **
    *******************************************************************************/
   public void setSessionSupplier(Supplier<QSession> sessionSupplier)
   {
      this.sessionSupplier = sessionSupplier;
   }



   /*******************************************************************************
    ** Getter for managedExecutors
    **
    *******************************************************************************/
   public List<StandardScheduledExecutor> getExecutors()
   {
      return executors;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void stopAsync()
   {
      for(StandardScheduledExecutor scheduledExecutor : executors)
      {
         scheduledExecutor.stopAsync();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void resetSingleton()
   {
      scheduleManager = null;
   }

}
