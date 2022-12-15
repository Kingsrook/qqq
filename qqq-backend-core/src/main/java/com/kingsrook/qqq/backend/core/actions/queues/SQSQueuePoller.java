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


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
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
import com.kingsrook.qqq.backend.core.scheduler.StandardScheduledExecutor;
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
      String originalThreadName = Thread.currentThread().getName();
      Thread.currentThread().setName("SQSPoller>" + queueMetaData.getName() + StandardScheduledExecutor.newThreadNameRandomSuffix());
      LOG.debug("Running " + this.getClass().getSimpleName() + "[" + queueMetaData.getName() + "]");

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
            ///////////////////////////////
            // fetch a batch of messages //
            ///////////////////////////////
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
            receiveMessageRequest.setQueueUrl(queueUrl);
            receiveMessageRequest.setMaxNumberOfMessages(10);
            receiveMessageRequest.setWaitTimeSeconds(20); // help urge SQS to query multiple servers and find more messages
            ReceiveMessageResult receiveMessageResult = sqs.receiveMessage(receiveMessageRequest);
            if(receiveMessageResult.getMessages().isEmpty())
            {
               LOG.debug("0 messages received.  Breaking.");
               break;
            }
            LOG.debug(receiveMessageResult.getMessages().size() + " messages received.  Processing.");

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // extract data from the messages into list of bodies to pass into process, and list of delete-batch-inputs //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            List<DeleteMessageBatchRequestEntry> deleteRequestEntries = new ArrayList<>();
            ArrayList<String>                    bodies               = new ArrayList<>();
            int                                  i                    = 0;
            for(Message message : receiveMessageResult.getMessages())
            {
               bodies.add(message.getBody());
               deleteRequestEntries.add(new DeleteMessageBatchRequestEntry(String.valueOf(i++), message.getReceiptHandle()));
            }

            /////////////////////////////////////////////////////////////////////////////////////
            // run the process, in a try-catch, so even if it fails, our loop keeps going.     //
            // the messages in a failed process will get re-delivered, to try-again, up to the //
            // number of times configured in AWS                                               //
            /////////////////////////////////////////////////////////////////////////////////////
            try
            {
               RunProcessInput runProcessInput = new RunProcessInput(qInstance);
               runProcessInput.setSession(sessionSupplier.get());
               runProcessInput.setProcessName(queueMetaData.getProcessName());
               runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
               runProcessInput.addValue("bodies", bodies);

               RunProcessAction runProcessAction = new RunProcessAction();
               RunProcessOutput runProcessOutput = runProcessAction.execute(runProcessInput);

               ////////////////////////////////////////////////////////////////////////////////////////////
               // if there was an exception returned by the process (e.g., thrown in backend step), then //
               // warn and leave the messages for re-processing.                                         //
               ////////////////////////////////////////////////////////////////////////////////////////////
               if(runProcessOutput.getException().isPresent())
               {
                  LOG.warn("Exception returned by process when handling SQS Messages.  They will not be deleted from the queue.", runProcessOutput.getException().get());
               }
               else
               {
                  ///////////////////////////////////////////////
                  // else, if no exception, do a batch delete. //
                  ///////////////////////////////////////////////
                  sqs.deleteMessageBatch(new DeleteMessageBatchRequest()
                     .withQueueUrl(queueUrl)
                     .withEntries(deleteRequestEntries));
               }
            }
            catch(Exception e)
            {
               LOG.warn("Error receiving SQS Messages.", e);
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error running SQS Queue Poller", e);
      }
      finally
      {
         Thread.currentThread().setName(originalThreadName);
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
