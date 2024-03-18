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


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.SchedulableType;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.runner.SchedulableRunner;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuartzJobRunner implements Job
{
   private static final QLogger LOG = QLogger.getLogger(QuartzJobRunner.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(JobExecutionContext context) throws JobExecutionException
   {
      CapturedContext capturedContext = QContext.capture();
      try
      {
         QuartzScheduler quartzScheduler = QuartzScheduler.getInstance();
         QInstance       qInstance       = quartzScheduler.getQInstance();
         QContext.init(qInstance, quartzScheduler.getSessionSupplier().get());

         SchedulableType     schedulableType = qInstance.getSchedulableType(context.getJobDetail().getJobDataMap().getString("type"));
         Map<String, Object> params          = (Map<String, Object>) context.getJobDetail().getJobDataMap().get("params");

         SchedulableRunner schedulableRunner = QCodeLoader.getAdHoc(SchedulableRunner.class, schedulableType.getRunner());
         schedulableRunner.run(params);
      }
      catch(Exception e)
      {
         LOG.warn("Error running QuartzJob", e, logPair("jobContext", context));
      }
      finally
      {
         QContext.init(capturedContext);
      }
   }

}
