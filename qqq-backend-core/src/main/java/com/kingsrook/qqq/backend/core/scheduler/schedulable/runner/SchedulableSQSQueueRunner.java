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

package com.kingsrook.qqq.backend.core.scheduler.schedulable.runner;


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.queues.SQSQueuePoller;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.identity.SchedulableIdentity;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Schedulable SQSQueue runner - e.g., how an SQSQueuePoller is run by a scheduler.
 *******************************************************************************/
public class SchedulableSQSQueueRunner implements SchedulableRunner
{
   private static final QLogger LOG = QLogger.getLogger(SchedulableSQSQueueRunner.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(Map<String, Object> params)
   {
      QInstance qInstance = QuartzScheduler.getInstance().getQInstance();

      String queueName = ValueUtils.getValueAsString(params.get("queueName"));
      if(!StringUtils.hasContent(queueName))
      {
         LOG.warn("Missing queueName in params.");
         return;
      }

      QQueueMetaData queue = qInstance.getQueue(queueName);
      if(queue == null)
      {
         LOG.warn("Unrecognized queueName [" + queueName + "]");
         return;
      }

      QQueueProviderMetaData queueProvider = qInstance.getQueueProvider(queue.getProviderName());
      if(!(queueProvider instanceof SQSQueueProviderMetaData))
      {
         LOG.warn("Queue [" + queueName + "] is of an unsupported queue provider type (not SQS)");
         return;
      }

      SQSQueuePoller sqsQueuePoller = new SQSQueuePoller();
      sqsQueuePoller.setQueueMetaData(queue);
      sqsQueuePoller.setQueueProviderMetaData((SQSQueueProviderMetaData) queueProvider);
      sqsQueuePoller.setQInstance(qInstance);
      sqsQueuePoller.setSessionSupplier(QuartzScheduler.getInstance().getSessionSupplier());

      /////////////
      // run it. //
      /////////////
      LOG.debug("Running SQS Queue poller", logPair("queueName", queueName));
      sqsQueuePoller.run();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void validateParams(SchedulableIdentity schedulableIdentity, Map<String, Object> paramMap) throws QException
   {
      String queueName = ValueUtils.getValueAsString(paramMap.get("queueName"));
      if(!StringUtils.hasContent(queueName))
      {
         throw (new QException("Missing scheduledJobParameter with key [queueName] in " + schedulableIdentity));
      }

      QQueueMetaData queue = QContext.getQInstance().getQueue(queueName);
      if(queue == null)
      {
         throw (new QException("Unrecognized queueName [" + queueName + "] in " + schedulableIdentity));
      }

      QQueueProviderMetaData queueProvider = QContext.getQInstance().getQueueProvider(queue.getProviderName());
      if(!(queueProvider instanceof SQSQueueProviderMetaData))
      {
         throw (new QException("Queue [" + queueName + "] is of an unsupported queue provider type (not SQS) in " + schedulableIdentity));
      }

      if(queue.getSchedule() != null)
      {
         throw (new QException("Queue [" + queueName + "] has a schedule in its metaData - so it should not be dynamically scheduled via a scheduled job! " + schedulableIdentity));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getDescription(Map<String, Object> params)
   {
      return "Queue: " + params.get("queueName");
   }

}
