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
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
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
import com.kingsrook.qqq.backend.core.model.metadata.processes.VariantRunStrategy;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 ** Utility methods used by various schedulers.
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
   public static void runProcess(QInstance qInstance, Supplier<QSession> sessionSupplier, QProcessMetaData process, Map<String, Serializable> backendVariantData, Map<String, Serializable> processInputValues)
   {
      String originalThreadName = Thread.currentThread().getName();

      try
      {
         QContext.init(qInstance, sessionSupplier.get());

         if(process.getVariantBackend() == null || VariantRunStrategy.PARALLEL.equals(process.getVariantRunStrategy()))
         {
            executeSingleProcess(process, backendVariantData, processInputValues);
         }
         else if(VariantRunStrategy.SERIAL.equals(process.getVariantRunStrategy()))
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // if this is "serial", which for example means we want to run each backend variant one after    //
            // the other in the same thread so loop over these here so that they run in same lambda function //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            for(QRecord qRecord : getBackendVariantFilteredRecords(process))
            {
               try
               {
                  QBackendMetaData  backendMetaData  = qInstance.getBackend(process.getVariantBackend());
                  Map<String, Serializable> thisVariantData = MapBuilder.of(backendMetaData.getVariantOptionsTableTypeValue(), qRecord.getValue(backendMetaData.getVariantOptionsTableIdField()));
                  executeSingleProcess(process, thisVariantData, processInputValues);
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
   private static void executeSingleProcess(QProcessMetaData process, Map<String, Serializable> backendVariantData, Map<String, Serializable> processInputValues) throws QException
   {
      if(backendVariantData != null)
      {
         QContext.getQSession().setBackendVariants(backendVariantData);
      }

      Thread.currentThread().setName("ScheduledProcess>" + process.getName());
      LOG.debug("Running Scheduled Process [" + process.getName() + "] with values [" + processInputValues + "]");

      RunProcessInput runProcessInput = new RunProcessInput();
      runProcessInput.setProcessName(process.getName());

      Serializable recordId = null;
      for(Map.Entry<String, Serializable> entry : CollectionUtils.nonNullMap(processInputValues).entrySet())
      {
         runProcessInput.withValue(entry.getKey(), entry.getValue());
         if(entry.getKey().equals("recordId"))
         {
            recordId = entry.getValue();
         }
      }

      runProcessInput.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if there was a "recordId" input value, and this table is for a process, then set up a callback to get the record //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(recordId != null && StringUtils.hasContent(process.getTableName()))
      {
         QTableMetaData table = QContext.getQInstance().getTable(process.getTableName());
         runProcessInput.setCallback(QProcessCallbackFactory.forPrimaryKey(table.getPrimaryKeyField(), recordId));
      }

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
         QBackendMetaData  backendMetaData  = QContext.getQInstance().getBackend(processMetaData.getVariantBackend());

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
