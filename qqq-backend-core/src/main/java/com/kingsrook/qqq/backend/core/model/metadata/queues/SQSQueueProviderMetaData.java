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

package com.kingsrook.qqq.backend.core.model.metadata.queues;


import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;


/*******************************************************************************
 ** Meta-data for an source of Amazon SQS queues (e.g, an aws account/credential
 ** set, with a common base URL).
 **
 ** Scheduled can be defined here, to apply to all queues in the provider - or
 ** each can supply their own schedule.
 *******************************************************************************/
public class SQSQueueProviderMetaData extends QQueueProviderMetaData
{
   private String accessKey;
   private String secretKey;
   private String region;
   private String baseURL;

   private QScheduleMetaData schedule;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData()
   {
      super();
      setType(QueueType.SQS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public SQSQueueProviderMetaData withName(String name)
   {
      super.withName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for accessKey
    **
    *******************************************************************************/
   public String getAccessKey()
   {
      return accessKey;
   }



   /*******************************************************************************
    ** Setter for accessKey
    **
    *******************************************************************************/
   public void setAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
   }



   /*******************************************************************************
    ** Fluent setter for accessKey
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData withAccessKey(String accessKey)
   {
      this.accessKey = accessKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for secretKey
    **
    *******************************************************************************/
   public String getSecretKey()
   {
      return secretKey;
   }



   /*******************************************************************************
    ** Setter for secretKey
    **
    *******************************************************************************/
   public void setSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
   }



   /*******************************************************************************
    ** Fluent setter for secretKey
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData withSecretKey(String secretKey)
   {
      this.secretKey = secretKey;
      return (this);
   }



   /*******************************************************************************
    ** Getter for region
    **
    *******************************************************************************/
   public String getRegion()
   {
      return region;
   }



   /*******************************************************************************
    ** Setter for region
    **
    *******************************************************************************/
   public void setRegion(String region)
   {
      this.region = region;
   }



   /*******************************************************************************
    ** Fluent setter for region
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData withRegion(String region)
   {
      this.region = region;
      return (this);
   }



   /*******************************************************************************
    ** Getter for baseURL
    **
    *******************************************************************************/
   public String getBaseURL()
   {
      return baseURL;
   }



   /*******************************************************************************
    ** Setter for baseURL
    **
    *******************************************************************************/
   public void setBaseURL(String baseURL)
   {
      this.baseURL = baseURL;
   }



   /*******************************************************************************
    ** Fluent setter for baseURL
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData withBaseURL(String baseURL)
   {
      this.baseURL = baseURL;
      return (this);
   }



   /*******************************************************************************
    ** Getter for schedule
    **
    *******************************************************************************/
   public QScheduleMetaData getSchedule()
   {
      return schedule;
   }



   /*******************************************************************************
    ** Setter for schedule
    **
    *******************************************************************************/
   public void setSchedule(QScheduleMetaData schedule)
   {
      this.schedule = schedule;
   }



   /*******************************************************************************
    ** Fluent setter for schedule
    **
    *******************************************************************************/
   public SQSQueueProviderMetaData withSchedule(QScheduleMetaData schedule)
   {
      this.schedule = schedule;
      return (this);
   }

}
