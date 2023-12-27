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

package com.kingsrook.qqq.backend.core.scheduler;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.scheduleing.QScheduleMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 **
 *******************************************************************************/
public class SchedulerUtils
{
   private static final QLogger LOG = QLogger.getLogger(SchedulerUtils.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static boolean allowedToStart(String name)
   {
      String propertyName  = "qqq.scheduleManager.onlyStartNamesMatching";
      String propertyValue = System.getProperty(propertyName, "");
      if(propertyValue.equals(""))
      {
         return (true);
      }

      return (name.matches(propertyValue));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void runProcess(QInstance qInstance, Supplier<QSession> sessionSupplier, QProcessMetaData process, Map<String, Serializable> backendVariantData)
   {
      String originalThreadName = Thread.currentThread().getName();

      try
      {
         QContext.init(qInstance, sessionSupplier.get());

         if(process.getSchedule().getVariantBackend() == null || QScheduleMetaData.RunStrategy.PARALLEL.equals(process.getSchedule().getVariantRunStrategy()))
         {
            SchedulerUtils.executeSingleProcess(process, backendVariantData);
         }
         else if(QScheduleMetaData.RunStrategy.SERIAL.equals(process.getSchedule().getVariantRunStrategy()))
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // if this is "serial", which for example means we want to run each backend variant one after    //
            // the other in the same thread so loop over these here so that they run in same lambda function //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            for(QRecord qRecord : getBackendVariantFilteredRecords(process))
            {
               try
               {
                  QScheduleMetaData scheduleMetaData = process.getSchedule();
                  QBackendMetaData  backendMetaData  = qInstance.getBackend(scheduleMetaData.getVariantBackend());
                  executeSingleProcess(process, MapBuilder.of(backendMetaData.getVariantOptionsTableTypeValue(), qRecord.getValue(backendMetaData.getVariantOptionsTableIdField())));
               }
               catch(Exception e)
               {
                  LOG.error("An error starting process [" + process.getLabel() + "], with backend variant data.", e, new LogPair("variantQRecord", qRecord));
               }
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Exception thrown running scheduled process [" + process.getName() + "]", e);
      }
      finally
      {
         Thread.currentThread().setName(originalThreadName);
         QContext.clear();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void executeSingleProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData) throws QException
   {
      if(backendVariantData != null)
      {
         QContext.getQSession().setBackendVariants(backendVariantData);
      }

      Thread.currentThread().setName("ScheduledProcess>" + process.getName());
      LOG.debug("Running Scheduled Process [" + process.getName() + "]");

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(process.getName());
      runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      QContext.pushAction(runProcessInput);

      RunProcessAction runProcessAction = new RunProcessAction();
      runProcessAction.execute(runProcessInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecord> getBackendVariantFilteredRecords(QProcessMetaData processMetaData)
   {
      List<QRecord> records = null;
      try
      {
         QScheduleMetaData scheduleMetaData = processMetaData.getSchedule();
         QBackendMetaData  backendMetaData  = QContext.getQInstance().getBackend(scheduleMetaData.getVariantBackend());

         QueryInput queryInput = new QueryInput();
         queryInput.setTableName(backendMetaData.getVariantOptionsTableName());
         queryInput.setFilter(new QQueryFilter(new QFilterCriteria(backendMetaData.getVariantOptionsTableTypeField(), QCriteriaOperator.EQUALS, backendMetaData.getVariantOptionsTableTypeValue())));

         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         records = queryOutput.getRecords();
      }
      catch(Exception e)
      {
         LOG.error("An error fetching variant data for process [" + processMetaData.getLabel() + "]", e);
      }

      return (records);
   }

}
