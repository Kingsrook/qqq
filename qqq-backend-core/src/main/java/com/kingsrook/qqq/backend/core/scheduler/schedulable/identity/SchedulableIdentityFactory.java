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

package com.kingsrook.qqq.backend.core.scheduler.schedulable.identity;


import java.util.HashMap;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.scheduledjobs.ScheduledJob;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.SchedulableType;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.runner.SchedulableRunner;


/*******************************************************************************
 ** Factory to produce SchedulableIdentity objects
 *******************************************************************************/
public class SchedulableIdentityFactory
{

   /*******************************************************************************
    ** Factory to create one of these for a scheduled job record
    *******************************************************************************/
   public static BasicSchedulableIdentity of(ScheduledJob scheduledJob)
   {
      String          description     = "";
      SchedulableType schedulableType = QContext.getQInstance().getSchedulableType(scheduledJob.getType());
      if(schedulableType != null)
      {
         try
         {
            SchedulableRunner runner = QCodeLoader.getAdHoc(SchedulableRunner.class, schedulableType.getRunner());
            description = runner.getDescription(new HashMap<>(scheduledJob.getJobParametersMap()));
         }
         catch(Exception e)
         {
            description = "type: " + schedulableType.getName();
         }
      }

      return new BasicSchedulableIdentity("scheduledJob:" + scheduledJob.getId(), description);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static BasicSchedulableIdentity of(QProcessMetaData process)
   {
      return new BasicSchedulableIdentity("process:" + process.getName(), "Process: " + process.getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static SchedulableIdentity of(QQueueMetaData queue)
   {
      return new BasicSchedulableIdentity("queue:" + queue.getName(), "Queue: " + queue.getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static SchedulableIdentity of(PollingAutomationPerTableRunner.TableActionsInterface tableActions)
   {
      return new BasicSchedulableIdentity("tableAutomations:" + tableActions.tableName() + "." + tableActions.status(), "TableAutomations: " + tableActions.tableName() + "." + tableActions.status());
   }
}
