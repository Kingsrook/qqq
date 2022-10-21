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

package com.kingsrook.qqq.backend.core.actions.queues;


import java.util.function.Supplier;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.queues.QQueueMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.queues.SQSQueueProviderMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Class to poll an SQS queue, and run process code for each message found.
 *******************************************************************************/
public class SQSQueuePoller implements Runnable
{
   private static final Logger LOG = LogManager.getLogger(SQSQueuePoller.class);

   ///////////////////////////////////////////////
   // todo - move these 2 to a "QBaseRunnable"? //
   ///////////////////////////////////////////////
   private QInstance          qInstance;
   private Supplier<QSession> sessionSupplier;

   private SQSQueueProviderMetaData queueProviderMetaData;
   private QQueueMetaData           queueMetaData;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      try
      {
         BasicAWSCredentials credentials = new BasicAWSCredentials(queueProviderMetaData.getAccessKey(), queueProviderMetaData.getSecretKey());
         final AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            .withRegion(queueProviderMetaData.getRegion())
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

         String queueUrl = queueProviderMetaData.getBaseURL();
         if(!queueUrl.endsWith("/"))
         {
            queueUrl += "/";
         }
         queueUrl += queueMetaData.getQueueName();

         while(true)
         {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
            receiveMessageRequest.setQueueUrl(queueUrl);
            ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(receiveMessageRequest);
            if(receiveMessageResult.getMessages().isEmpty())
            {
               LOG.debug("0 messages received.  Breaking.");
               break;
            }
            LOG.debug(receiveMessageResult.getMessages().size() + " messages received.  Processing.");

            for(Message message : receiveMessageResult.getMessages())
            {
               String body = message.getBody();

               RunProcessInput runProcessInput = new RunProcessInput(qInstance);
               runProcessInput.setSession(sessionSupplier.get());
               runProcessInput.setProcessName(queueMetaData.getProcessName());
               runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
               runProcessInput.addValue("body", body);

               RunProcessAction runProcessAction = new RunProcessAction();
               RunProcessOutput runProcessOutput = runProcessAction.execute(runProcessInput);

               /////////////////////////////////
               // todo - what of exceptions?? //
               /////////////////////////////////

               String receiptHandle = message.getReceiptHandle();
               sqs.deleteMessage(new DeleteMessageRequest(queueUrl, receiptHandle));
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error receiving SQS Message", e);
      }
   }



   /*******************************************************************************
    ** Setter for queueProviderMetaData
    **
    *******************************************************************************/
   public void setQueueProviderMetaData(SQSQueueProviderMetaData queueProviderMetaData)
   {
      this.queueProviderMetaData = queueProviderMetaData;
   }



   /*******************************************************************************
    ** Setter for queueMetaData
    **
    *******************************************************************************/
   public void setQueueMetaData(QQueueMetaData queueMetaData)
   {
      this.queueMetaData = queueMetaData;
   }



   /*******************************************************************************
    ** Setter for qInstance
    **
    *******************************************************************************/
   public void setQInstance(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Setter for sessionSupplier
    **
    *******************************************************************************/
   public void setSessionSupplier(Supplier<QSession> sessionSupplier)
   {
      this.sessionSupplier = sessionSupplier;
   }
}
