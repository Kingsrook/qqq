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

package com.kingsrook.qqq.backend.core.model.metadata.scheduleing.quartz;


import java.util.Properties;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QSchedulerMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.QSchedulerInterface;
import com.kingsrook.qqq.backend.core.scheduler.quartz.QuartzScheduler;


/*******************************************************************************
 **
 *******************************************************************************/
public class QuartzSchedulerMetaData extends QSchedulerMetaData
{
   private static final QLogger LOG = QLogger.getLogger(QuartzSchedulerMetaData.class);

   public static final String TYPE = "quartz";

   private Properties properties;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QuartzSchedulerMetaData()
   {
      setType(TYPE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean supportsCronSchedules()
   {
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSchedulerInterface initSchedulerInstance(QInstance qInstance, Supplier<QSession> systemSessionSupplier) throws QException
   {
      try
      {
         QuartzScheduler quartzScheduler = QuartzScheduler.initInstance(qInstance, getName(), getProperties(), systemSessionSupplier);
         return (quartzScheduler);
      }
      catch(Exception e)
      {
         LOG.error("Error initializing quartz scheduler", e);
         throw (new QException("Error initializing quartz scheduler", e));
      }
   }



   /*******************************************************************************
    ** Getter for properties
    *******************************************************************************/
   public Properties getProperties()
   {
      return (this.properties);
   }



   /*******************************************************************************
    ** Setter for properties
    *******************************************************************************/
   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }



   /*******************************************************************************
    ** Fluent setter for properties
    *******************************************************************************/
   public QuartzSchedulerMetaData withProperties(Properties properties)
   {
      this.properties = properties;
      return (this);
   }

}
