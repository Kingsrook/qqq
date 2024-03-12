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

package com.kingsrook.qqq.backend.core.model.metadata.scheduleing.simple;


import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QSchedulerMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.scheduler.QSchedulerInterface;
import com.kingsrook.qqq.backend.core.scheduler.simple.SimpleScheduler;


/*******************************************************************************
 **
 *******************************************************************************/
public class SimpleSchedulerMetaData extends QSchedulerMetaData
{
   public static final String TYPE = "simple";



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SimpleSchedulerMetaData()
   {
      setType(TYPE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean supportsCronSchedules()
   {
      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSchedulerInterface initSchedulerInstance(QInstance qInstance, Supplier<QSession> systemSessionSupplier)
   {
      SimpleScheduler simpleScheduler = SimpleScheduler.getInstance(qInstance);
      simpleScheduler.setSessionSupplier(systemSessionSupplier);
      simpleScheduler.setSchedulerName(getName());
      return simpleScheduler;
   }

}
