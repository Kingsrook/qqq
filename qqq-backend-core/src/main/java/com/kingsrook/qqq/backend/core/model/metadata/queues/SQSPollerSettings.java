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

package com.kingsrook.qqq.backend.core.model.metadata.queues;


/*******************************************************************************
 ** settings that can be applied to either an SQSQueue or an SQSQueueProvider,
 ** to control what the SQSQueuePoller does when it receives from AWS.
 *******************************************************************************/
public class SQSPollerSettings
{
   private Integer maxNumberOfMessages;
   private Integer waitTimeSeconds;
   private Integer maxLoops;



   /*******************************************************************************
    ** Getter for maxNumberOfMessages
    *******************************************************************************/
   public Integer getMaxNumberOfMessages()
   {
      return (this.maxNumberOfMessages);
   }



   /*******************************************************************************
    ** Setter for maxNumberOfMessages
    *******************************************************************************/
   public void setMaxNumberOfMessages(Integer maxNumberOfMessages)
   {
      this.maxNumberOfMessages = maxNumberOfMessages;
   }



   /*******************************************************************************
    ** Fluent setter for maxNumberOfMessages
    *******************************************************************************/
   public SQSPollerSettings withMaxNumberOfMessages(Integer maxNumberOfMessages)
   {
      this.maxNumberOfMessages = maxNumberOfMessages;
      return (this);
   }



   /*******************************************************************************
    ** Getter for waitTimeSeconds
    *******************************************************************************/
   public Integer getWaitTimeSeconds()
   {
      return (this.waitTimeSeconds);
   }



   /*******************************************************************************
    ** Setter for waitTimeSeconds
    *******************************************************************************/
   public void setWaitTimeSeconds(Integer waitTimeSeconds)
   {
      this.waitTimeSeconds = waitTimeSeconds;
   }



   /*******************************************************************************
    ** Fluent setter for waitTimeSeconds
    *******************************************************************************/
   public SQSPollerSettings withWaitTimeSeconds(Integer waitTimeSeconds)
   {
      this.waitTimeSeconds = waitTimeSeconds;
      return (this);
   }



   /*******************************************************************************
    ** Getter for maxLoops
    *******************************************************************************/
   public Integer getMaxLoops()
   {
      return (this.maxLoops);
   }



   /*******************************************************************************
    ** Setter for maxLoops
    *******************************************************************************/
   public void setMaxLoops(Integer maxLoops)
   {
      this.maxLoops = maxLoops;
   }



   /*******************************************************************************
    ** Fluent setter for maxLoops
    *******************************************************************************/
   public SQSPollerSettings withMaxLoops(Integer maxLoops)
   {
      this.maxLoops = maxLoops;
      return (this);
   }

}
