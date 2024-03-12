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

package com.kingsrook.qqq.backend.core.scheduler.simple;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.actions.queues.SQSQueuePoller;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.QSchedulerInterface;
import com.kingsrook.qqq.backend.core.scheduler.SchedulerUtils;


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
public class SimpleScheduler implements QSchedulerInterface
{
   private static final QLogger LOG = QLogger.getLogger(SimpleScheduler.class);

   private static SimpleScheduler simpleScheduler = null;
   private final  QInstance       qInstance;
   private        String          schedulerName;

   protected Supplier<QSession> sessionSupplier;

   /////////////////////////////////////////////////////////////////////////////////////
   // for jobs that don't define a delay index, auto-stagger them, using this counter //
   /////////////////////////////////////////////////////////////////////////////////////
   private int delayIndex = 0;

   private List<StandardScheduledExecutor> executors = new ArrayList<>();



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private SimpleScheduler(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static SimpleScheduler getInstance(QInstance qInstance)
   {
      if(simpleScheduler == null)
      {
         simpleScheduler = new SimpleScheduler(qInstance);
      }
      return (simpleScheduler);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void start()
   {
      for(StandardScheduledExecutor executor : executors)
      {
         executor.start();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
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
   @Override
   public void stop()
   {
      for(StandardScheduledExecutor scheduledExecutor : executors)
      {
         scheduledExecutor.stop();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupAutomationProviderPerTable(QAutomationProviderMetaData automationProvider, boolean allowedToStartProvider)
   {
      if(!allowedToStartProvider)
      {
         return;
      }

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
            executors.add(executor);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupSqsProvider(SQSQueueProviderMetaData queueProvider, boolean allowedToStartProvider)
   {
      if(!allowedToStartProvider)
      {
         return;
      }

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
            executors.add(executor);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void setupProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData, boolean allowedToStart)
   {
      if(!allowedToStart)
      {
         return;
      }

      Runnable runProcess = () ->
      {
         SchedulerUtils.runProcess(qInstance, sessionSupplier, process, backendVariantData);
      };

      StandardScheduledExecutor executor = new StandardScheduledExecutor(runProcess);
      executor.setName("process:" + process.getName());
      setScheduleInExecutor(process.getSchedule(), executor);
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
   static void resetSingleton()
   {
      simpleScheduler = null;
   }



   /*******************************************************************************
    ** Getter for schedulerName
    *******************************************************************************/
   public String getSchedulerName()
   {
      return (this.schedulerName);
   }



   /*******************************************************************************
    ** Setter for schedulerName
    *******************************************************************************/
   public void setSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
   }



   /*******************************************************************************
    ** Fluent setter for schedulerName
    *******************************************************************************/
   public SimpleScheduler withSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
      return (this);
   }

}
