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
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.model.metadata.automation.QAutomationProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public interface QSchedulerInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   void setupProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData, QScheduleMetaData schedule, boolean allowedToStart);

   /*******************************************************************************
    **
    *******************************************************************************/
   void setupSqsPoller(SQSQueueProviderMetaData queueProvider, QQueueMetaData queue, QScheduleMetaData schedule, boolean allowedToStart);

   /*******************************************************************************
    **
    *******************************************************************************/
   void setupTableAutomation(QAutomationProviderMetaData automationProvider, PollingAutomationPerTableRunner.TableActionsInterface tableActions, QScheduleMetaData schedule, boolean allowedToStart);

   /*******************************************************************************
    **
    *******************************************************************************/
   void unscheduleProcess(QProcessMetaData process);

   /*******************************************************************************
    **
    *******************************************************************************/
   void start();

   /*******************************************************************************
    **
    *******************************************************************************/
   void stopAsync();

   /*******************************************************************************
    **
    *******************************************************************************/
   void stop();

   /*******************************************************************************
    ** Handle a whole shutdown of the scheduler system (e.g., between unit tests).
    *******************************************************************************/
   default void unInit()
   {

   }

   /*******************************************************************************
    ** let the scheduler know when the schedule manager is at the start of setting up schedules.
    *******************************************************************************/
   default void startOfSetupSchedules()
   {

   }

   /*******************************************************************************
    ** let the scheduler know when the schedule manager is at the end of setting up schedules.
    *******************************************************************************/
   default void endOfSetupSchedules()
   {

   }
}
