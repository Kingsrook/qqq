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


import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.core.actions.automation.polling.PollingAutomationPerTableRunner;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
@DisallowConcurrentExecution
public class QuartzTableAutomationsJob implements Job
{
   private static final QLogger LOG = QLogger.getLogger(QuartzTableAutomationsJob.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      try
      {
         JobDataMap       jobDataMap             = jobExecutionContext.getJobDetail().getJobDataMap();
         String           tableName              = jobDataMap.getString("tableName");
         String           automationProviderName = jobDataMap.getString("automationProviderName");
         AutomationStatus automationStatus       = AutomationStatus.valueOf(jobDataMap.getString("automationStatus"));
         QInstance        qInstance              = QuartzScheduler.getInstance().getQInstance();

         PollingAutomationPerTableRunner.TableActionsInterface tableAction = new PollingAutomationPerTableRunner.TableActions(tableName, automationStatus);
         PollingAutomationPerTableRunner                       runner      = new PollingAutomationPerTableRunner(qInstance, automationProviderName, QuartzScheduler.getInstance().getSessionSupplier(), tableAction);

         /////////////
         // run it. //
         /////////////
         LOG.debug("Running Table Automations", logPair("tableName", tableName), logPair("automationStatus", automationStatus));
         runner.run();
      }
      finally
      {
         QContext.clear();
      }
   }

}
