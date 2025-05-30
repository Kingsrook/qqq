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

package com.kingsrook.qqq.backend.core.model.metadata.scheduleing;


import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Meta-data to define scheduled actions within QQQ.
 **
 ** Supports repeating jobs, either on a given # of seconds or millis, or cron
 ** expressions (though cron may not be supported by all schedulers!)
 **
 ** Can also specify an initialDelay - e.g., to avoid all jobs starting up at the
 ** same moment.
 **
 *******************************************************************************/
public class QScheduleMetaData implements QMetaDataObject
{
   private String schedulerName;
   private String description;

   private Integer repeatSeconds;
   private Integer repeatMillis;
   private Integer initialDelaySeconds;
   private Integer initialDelayMillis;

   private String cronExpression;
   private String cronTimeZoneId;



   /*******************************************************************************
    **
    *******************************************************************************/
   public boolean isCron()
   {
      return StringUtils.hasContent(cronExpression);
   }



   /*******************************************************************************
    ** Getter for repeatSeconds
    **
    *******************************************************************************/
   public Integer getRepeatSeconds()
   {
      return repeatSeconds;
   }



   /*******************************************************************************
    ** Setter for repeatSeconds
    **
    *******************************************************************************/
   public void setRepeatSeconds(Integer repeatSeconds)
   {
      this.repeatSeconds = repeatSeconds;
   }



   /*******************************************************************************
    ** Fluent setter for repeatSeconds
    **
    *******************************************************************************/
   public QScheduleMetaData withRepeatSeconds(Integer repeatSeconds)
   {
      this.repeatSeconds = repeatSeconds;
      return (this);
   }



   /*******************************************************************************
    ** Getter for initialDelaySeconds
    **
    *******************************************************************************/
   public Integer getInitialDelaySeconds()
   {
      return initialDelaySeconds;
   }



   /*******************************************************************************
    ** Setter for initialDelaySeconds
    **
    *******************************************************************************/
   public void setInitialDelaySeconds(Integer initialDelaySeconds)
   {
      this.initialDelaySeconds = initialDelaySeconds;
   }



   /*******************************************************************************
    ** Fluent setter for initialDelaySeconds
    **
    *******************************************************************************/
   public QScheduleMetaData withInitialDelaySeconds(Integer initialDelaySeconds)
   {
      this.initialDelaySeconds = initialDelaySeconds;
      return (this);
   }



   /*******************************************************************************
    ** Getter for repeatMillis
    **
    *******************************************************************************/
   public Integer getRepeatMillis()
   {
      return repeatMillis;
   }



   /*******************************************************************************
    ** Setter for repeatMillis
    **
    *******************************************************************************/
   public void setRepeatMillis(Integer repeatMillis)
   {
      this.repeatMillis = repeatMillis;
   }



   /*******************************************************************************
    ** Fluent setter for repeatMillis
    **
    *******************************************************************************/
   public QScheduleMetaData withRepeatMillis(Integer repeatMillis)
   {
      this.repeatMillis = repeatMillis;
      return (this);
   }



   /*******************************************************************************
    ** Getter for initialDelayMillis
    **
    *******************************************************************************/
   public Integer getInitialDelayMillis()
   {
      return initialDelayMillis;
   }



   /*******************************************************************************
    ** Setter for initialDelayMillis
    **
    *******************************************************************************/
   public void setInitialDelayMillis(Integer initialDelayMillis)
   {
      this.initialDelayMillis = initialDelayMillis;
   }



   /*******************************************************************************
    ** Fluent setter for initialDelayMillis
    **
    *******************************************************************************/
   public QScheduleMetaData withInitialDelayMillis(Integer initialDelayMillis)
   {
      this.initialDelayMillis = initialDelayMillis;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cronExpression
    *******************************************************************************/
   public String getCronExpression()
   {
      return (this.cronExpression);
   }



   /*******************************************************************************
    ** Setter for cronExpression
    *******************************************************************************/
   public void setCronExpression(String cronExpression)
   {
      this.cronExpression = cronExpression;
   }



   /*******************************************************************************
    ** Fluent setter for cronExpression
    *******************************************************************************/
   public QScheduleMetaData withCronExpression(String cronExpression)
   {
      this.cronExpression = cronExpression;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cronTimeZoneId
    *******************************************************************************/
   public String getCronTimeZoneId()
   {
      return (this.cronTimeZoneId);
   }



   /*******************************************************************************
    ** Setter for cronTimeZoneId
    *******************************************************************************/
   public void setCronTimeZoneId(String cronTimeZoneId)
   {
      this.cronTimeZoneId = cronTimeZoneId;
   }



   /*******************************************************************************
    ** Fluent setter for cronTimeZoneId
    *******************************************************************************/
   public QScheduleMetaData withCronTimeZoneId(String cronTimeZoneId)
   {
      this.cronTimeZoneId = cronTimeZoneId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for schedulerName
    *******************************************************************************/
   public String getSchedulerName()
   {
      return (this.schedulerName);
   }



   /*******************************************************************************
    ** Setter for schedulerName
    *******************************************************************************/
   public void setSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
   }



   /*******************************************************************************
    ** Fluent setter for schedulerName
    *******************************************************************************/
   public QScheduleMetaData withSchedulerName(String schedulerName)
   {
      this.schedulerName = schedulerName;
      return (this);
   }


   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public QScheduleMetaData withDescription(String description)
   {
      this.description = description;
      return (this);
   }


}
