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

package com.kingsrook.qqq.backend.core.actions.queues;


import java.util.List;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetQueueSize
{
   private static final QLogger LOG = QLogger.getLogger(GetQueueSize.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer getQueueSize(QQueueProviderMetaData queueProviderMetaData, QQueueMetaData queueMetaData) throws QException
   {
      try
      {
         //////////////////////////////////////////////////////////////////
         // todo - handle other queue provider types, somewhere, somehow //
         //////////////////////////////////////////////////////////////////
         SQSQueueProviderMetaData queueProvider = (SQSQueueProviderMetaData) queueProviderMetaData;

         BasicAWSCredentials credentials = new BasicAWSCredentials(queueProvider.getAccessKey(), queueProvider.getSecretKey());
         final AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withRegion(queueProvider.getRegion())
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

         String queueUrl = queueProvider.getBaseURL();
         if(!queueUrl.endsWith("/"))
         {
            queueUrl += "/";
         }
         queueUrl += queueMetaData.getQueueName();

         GetQueueAttributesResult queueAttributes             = sqs.getQueueAttributes(queueUrl, List.of("ApproximateNumberOfMessages"));
         String                   approximateNumberOfMessages = queueAttributes.getAttributes().get("ApproximateNumberOfMessages");
         return (Integer.parseInt(approximateNumberOfMessages));
      }
      catch(Exception e)
      {
         LOG.warn("Error getting queue size", e, logPair("queueName", queueMetaData == null ? "null" : queueMetaData.getName()));
         throw (new QException("Error getting queue size", e));
      }
   }

}
