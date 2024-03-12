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


import com.kingsrook.qqq.backend.core.actions.queues.SQSQueuePoller;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
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
public class QuartzSqsPollerJob implements Job
{
   private static final QLogger LOG = QLogger.getLogger(QuartzSqsPollerJob.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      String queueProviderName = null;
      String queueName         = null;

      try
      {
         JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
         queueProviderName = jobDataMap.getString("queueProviderName");
         queueName = jobDataMap.getString("queueName");
         QInstance qInstance = QuartzScheduler.getInstance().getQInstance();

         SQSQueuePoller sqsQueuePoller = new SQSQueuePoller();
         sqsQueuePoller.setQueueProviderMetaData((SQSQueueProviderMetaData) qInstance.getQueueProvider(queueProviderName));
         sqsQueuePoller.setQueueMetaData(qInstance.getQueue(queueName));
         sqsQueuePoller.setQInstance(qInstance);
         sqsQueuePoller.setSessionSupplier(QuartzScheduler.getInstance().getSessionSupplier());

         /////////////
         // run it. //
         /////////////
         LOG.debug("Running quartz SQS Poller", logPair("queueName", queueName), logPair("queueProviderName", queueProviderName));
         sqsQueuePoller.run();
      }
      catch(Exception e)
      {
         LOG.warn("Error running SQS Poller", e, logPair("queueName", queueName), logPair("queueProviderName", queueProviderName));
      }
      finally
      {
         QContext.clear();
      }
   }

}
