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
import java.util.Map;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.scheduler.SchedulerUtils;
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
public class QuartzRunProcessJob implements Job
{
   private static final QLogger LOG = QLogger.getLogger(QuartzRunProcessJob.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      try
      {
         JobDataMap jobDataMap  = jobExecutionContext.getJobDetail().getJobDataMap();
         String     processName = jobDataMap.getString("processName");

         ///////////////////////////////////
         // todo - variants from job data //
         ///////////////////////////////////
         Map<String, Serializable> backendVariantData = null;

         LOG.debug("Running quartz process", logPair("processName", processName));

         QInstance qInstance = QuartzScheduler.getInstance().getQInstance();
         SchedulerUtils.runProcess(qInstance, QuartzScheduler.getInstance().getSessionSupplier(), qInstance.getProcess(processName), backendVariantData);

      }
      finally
      {
         QContext.clear();
      }
   }

}
