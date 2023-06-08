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


import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;


/*******************************************************************************
 ** Meta-data to define scheduled actions within QQQ.
 **
 ** Initially, only supports repeating jobs, either on a given # of seconds or millis.
 ** Can also specify an initialDelay - e.g., to avoid all jobs starting up at the
 ** same moment.
 **
 ** In the future we most likely would want to allow cron strings to be added here.
 *******************************************************************************/
public class QScheduleMetaData
{
   public enum RunStrategy
   {PARALLEL, SERIAL}



   private Integer repeatSeconds;
   private Integer repeatMillis;
   private Integer initialDelaySeconds;
   private Integer initialDelayMillis;

   private RunStrategy  variantRunStrategy;
   private String       backendVariant;
   private String       variantTableName;
   private QQueryFilter variantFilter;
   private String       variantFieldName;



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
    ** Getter for backendVariant
    *******************************************************************************/
   public String getBackendVariant()
   {
      return (this.backendVariant);
   }



   /*******************************************************************************
    ** Setter for backendVariant
    *******************************************************************************/
   public void setBackendVariant(String backendVariant)
   {
      this.backendVariant = backendVariant;
   }



   /*******************************************************************************
    ** Fluent setter for backendVariant
    *******************************************************************************/
   public QScheduleMetaData withBackendVariant(String backendVariant)
   {
      this.backendVariant = backendVariant;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variantTableName
    *******************************************************************************/
   public String getVariantTableName()
   {
      return (this.variantTableName);
   }



   /*******************************************************************************
    ** Setter for variantTableName
    *******************************************************************************/
   public void setVariantTableName(String variantTableName)
   {
      this.variantTableName = variantTableName;
   }



   /*******************************************************************************
    ** Fluent setter for variantTableName
    *******************************************************************************/
   public QScheduleMetaData withVariantTableName(String variantTableName)
   {
      this.variantTableName = variantTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variantFilter
    *******************************************************************************/
   public QQueryFilter getVariantFilter()
   {
      return (this.variantFilter);
   }



   /*******************************************************************************
    ** Setter for variantFilter
    *******************************************************************************/
   public void setVariantFilter(QQueryFilter variantFilter)
   {
      this.variantFilter = variantFilter;
   }



   /*******************************************************************************
    ** Fluent setter for variantFilter
    *******************************************************************************/
   public QScheduleMetaData withVariantFilter(QQueryFilter variantFilter)
   {
      this.variantFilter = variantFilter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variantFieldName
    *******************************************************************************/
   public String getVariantFieldName()
   {
      return (this.variantFieldName);
   }



   /*******************************************************************************
    ** Setter for variantFieldName
    *******************************************************************************/
   public void setVariantFieldName(String variantFieldName)
   {
      this.variantFieldName = variantFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for variantFieldName
    *******************************************************************************/
   public QScheduleMetaData withVariantFieldName(String variantFieldName)
   {
      this.variantFieldName = variantFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variantRunStrategy
    *******************************************************************************/
   public RunStrategy getVariantRunStrategy()
   {
      return (this.variantRunStrategy);
   }



   /*******************************************************************************
    ** Setter for variantRunStrategy
    *******************************************************************************/
   public void setVariantRunStrategy(RunStrategy variantRunStrategy)
   {
      this.variantRunStrategy = variantRunStrategy;
   }



   /*******************************************************************************
    ** Fluent setter for variantRunStrategy
    *******************************************************************************/
   public QScheduleMetaData withVariantRunStrategy(RunStrategy variantRunStrategy)
   {
      this.variantRunStrategy = variantRunStrategy;
      return (this);
   }

}
