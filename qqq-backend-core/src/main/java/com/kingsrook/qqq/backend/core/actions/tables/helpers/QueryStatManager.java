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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStat;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStatCriteriaField;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStatJoinTable;
import com.kingsrook.qqq.backend.core.model.querystats.QueryStatOrderByField;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;
import com.kingsrook.qqq.backend.core.model.tables.QQQTablesMetaDataProvider;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueryStatManager
{
   private static QueryStatManager queryStatManager = null;

   // todo - support multiple qInstances?
   private QInstance          qInstance;
   private Supplier<QSession> sessionSupplier;

   private boolean         active     = false;
   private List<QueryStat> queryStats = new ArrayList<>();

   private ScheduledExecutorService executorService;



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private QueryStatManager()
   {

   }



   /*******************************************************************************
    ** Singleton accessor
    *******************************************************************************/
   public static QueryStatManager getInstance()
   {
      if(queryStatManager == null)
      {
         queryStatManager = new QueryStatManager();
      }
      return (queryStatManager);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void start(QInstance qInstance, Supplier<QSession> sessionSupplier)
   {
      this.qInstance = qInstance;
      this.sessionSupplier = sessionSupplier;

      active = true;
      queryStats = new ArrayList<>();

      executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.scheduleAtFixedRate(new QueryStatManagerInsertJob(), 60, 60, TimeUnit.SECONDS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void stop()
   {
      active = false;
      queryStats.clear();

      if(executorService != null)
      {
         executorService.shutdown();
         executorService = null;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void add(QueryStat queryStat)
   {
      if(active)
      {
         ////////////////////////////////////////////////////////////////////////////////////////
         // set fields that we need to capture now (rather than when the thread to store runs) //
         ////////////////////////////////////////////////////////////////////////////////////////
         if(queryStat.getFirstResultTimestamp() == null)
         {
            queryStat.setFirstResultTimestamp(Instant.now());
         }

         if(queryStat.getSessionId() == null && QContext.getQSession() != null)
         {
            queryStat.setSessionId(QContext.getQSession().getUuid());
         }

         if(queryStat.getAction() == null)
         {
            if(!QContext.getActionStack().isEmpty())
            {
               queryStat.setAction(QContext.getActionStack().peek().getActionIdentity());
            }
            else
            {
               boolean   expected = false;
               Exception e        = new Exception("Unexpected empty action stack");
               for(StackTraceElement stackTraceElement : e.getStackTrace())
               {
                  String className = stackTraceElement.getClassName();
                  if(className.contains(QueryStatManagerInsertJob.class.getName()))
                  {
                     expected = true;
                  }
               }

               if(!expected)
               {
                  e.printStackTrace();
               }
            }
         }

         synchronized(this)
         {
            queryStats.add(queryStat);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QueryStat> getListAndReset()
   {
      if(queryStats.isEmpty())
      {
         return Collections.emptyList();
      }

      synchronized(this)
      {
         List<QueryStat> returnList = queryStats;
         queryStats = new ArrayList<>();
         return (returnList);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void storeStatsNow()
   {
      new QueryStatManagerInsertJob().run();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static class QueryStatManagerInsertJob implements Runnable
   {
      private static final QLogger LOG = QLogger.getLogger(QueryStatManagerInsertJob.class);



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public void run()
      {
         try
         {
            QContext.init(getInstance().qInstance, getInstance().sessionSupplier.get());

            List<QueryStat> list = getInstance().getListAndReset();
            LOG.info(logPair("queryStatListSize", list.size()));

            if(list.isEmpty())
            {
               return;
            }

            ////////////////////////////////////
            // prime the entities for storing //
            ////////////////////////////////////
            List<QRecord> queryStatQRecordsToInsert = new ArrayList<>();
            for(QueryStat queryStat : list)
            {
               try
               {
                  ///////////////////////////////////////////////
                  // compute the millis (so you don't have to) //
                  ///////////////////////////////////////////////
                  if(queryStat.getStartTimestamp() != null && queryStat.getFirstResultTimestamp() != null && queryStat.getFirstResultMillis() == null)
                  {
                     long millis = queryStat.getFirstResultTimestamp().toEpochMilli() - queryStat.getStartTimestamp().toEpochMilli();
                     queryStat.setFirstResultMillis((int) millis);
                  }

                  //////////////////////
                  // set the table id //
                  //////////////////////
                  Integer qqqTableId = getQQQTableId(queryStat.getTableName());
                  queryStat.setQqqTableId(qqqTableId);

                  //////////////////////////////
                  // build join-table records //
                  //////////////////////////////
                  if(CollectionUtils.nullSafeHasContents(queryStat.getJoinTableNames()))
                  {
                     List<QueryStatJoinTable> queryStatJoinTableList = new ArrayList<>();
                     for(String joinTableName : queryStat.getJoinTableNames())
                     {
                        queryStatJoinTableList.add(new QueryStatJoinTable().withQqqTableId(getQQQTableId(joinTableName)));
                     }
                     queryStat.setQueryStatJoinTableList(queryStatJoinTableList);
                  }

                  ////////////////////////////
                  // build criteria records //
                  ////////////////////////////
                  if(queryStat.getQueryFilter() != null && queryStat.getQueryFilter().hasAnyCriteria())
                  {
                     List<QueryStatCriteriaField> queryStatCriteriaFieldList = new ArrayList<>();
                     processCriteriaFromFilter(qqqTableId, queryStatCriteriaFieldList, queryStat.getQueryFilter());
                     queryStat.setQueryStatCriteriaFieldList(queryStatCriteriaFieldList);
                  }

                  if(CollectionUtils.nullSafeHasContents(queryStat.getQueryFilter().getOrderBys()))
                  {
                     List<QueryStatOrderByField> queryStatOrderByFieldList = new ArrayList<>();
                     processOrderByFromFilter(qqqTableId, queryStatOrderByFieldList, queryStat.getQueryFilter());
                     queryStat.setQueryStatOrderByFieldList(queryStatOrderByFieldList);
                  }

                  queryStatQRecordsToInsert.add(queryStat.toQRecord());
               }
               catch(Exception e)
               {
                  //////////////////////
                  // skip this record //
                  //////////////////////
                  LOG.warn("Error priming a query stat for storing", e);
               }
            }

            try
            {
               InsertInput insertInput = new InsertInput();
               insertInput.setTableName(QueryStat.TABLE_NAME);
               insertInput.setRecords(queryStatQRecordsToInsert);
               new InsertAction().execute(insertInput);
            }
            catch(Exception e)
            {
               LOG.error("Error inserting query stats", e);
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error storing query stats", e);
         }
         finally
         {
            QContext.clear();
         }
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      private static void processCriteriaFromFilter(Integer qqqTableId, List<QueryStatCriteriaField> queryStatCriteriaFieldList, QQueryFilter queryFilter) throws QException
      {
         for(QFilterCriteria criteria : CollectionUtils.nonNullList(queryFilter.getCriteria()))
         {
            String                 fieldName              = criteria.getFieldName();
            QueryStatCriteriaField queryStatCriteriaField = new QueryStatCriteriaField();
            queryStatCriteriaField.setOperator(String.valueOf(criteria.getOperator()));

            if(criteria.getValues() != null)
            {
               queryStatCriteriaField.setValues(StringUtils.join(",", criteria.getValues()));
            }

            if(fieldName.contains("."))
            {
               String[] parts = fieldName.split("\\.");
               if(parts.length > 1)
               {
                  queryStatCriteriaField.setQqqTableId(getQQQTableId(parts[0]));
                  queryStatCriteriaField.setName(parts[1]);
               }
            }
            else
            {
               queryStatCriteriaField.setQqqTableId(qqqTableId);
               queryStatCriteriaField.setName(fieldName);
            }

            queryStatCriteriaFieldList.add(queryStatCriteriaField);
         }

         for(QQueryFilter subFilter : CollectionUtils.nonNullList(queryFilter.getSubFilters()))
         {
            processCriteriaFromFilter(qqqTableId, queryStatCriteriaFieldList, subFilter);
         }
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      private static void processOrderByFromFilter(Integer qqqTableId, List<QueryStatOrderByField> queryStatOrderByFieldList, QQueryFilter queryFilter) throws QException
      {
         for(QFilterOrderBy orderBy : CollectionUtils.nonNullList(queryFilter.getOrderBys()))
         {
            String                fieldName             = orderBy.getFieldName();
            QueryStatOrderByField queryStatOrderByField = new QueryStatOrderByField();

            if(fieldName.contains("."))
            {
               String[] parts = fieldName.split("\\.");
               if(parts.length > 1)
               {
                  queryStatOrderByField.setQqqTableId(getQQQTableId(parts[0]));
                  queryStatOrderByField.setName(parts[1]);
               }
            }
            else
            {
               queryStatOrderByField.setQqqTableId(qqqTableId);
               queryStatOrderByField.setName(fieldName);
            }

            queryStatOrderByFieldList.add(queryStatOrderByField);
         }
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      private static Integer getQQQTableId(String tableName) throws QException
      {
         /////////////////////////////
         // look in the cache table //
         /////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName(QQQTablesMetaDataProvider.QQQ_TABLE_CACHE_TABLE_NAME);
         getInput.setUniqueKey(MapBuilder.of("name", tableName));
         GetOutput getOutput = new GetAction().execute(getInput);

         ////////////////////////
         // upon cache miss... //
         ////////////////////////
         if(getOutput.getRecord() == null)
         {
            ///////////////////////////////////////////////////////
            // insert the record (into the table, not the cache) //
            ///////////////////////////////////////////////////////
            QTableMetaData tableMetaData = getInstance().qInstance.getTable(tableName);
            InsertInput    insertInput   = new InsertInput();
            insertInput.setTableName(QQQTable.TABLE_NAME);
            insertInput.setRecords(List.of(new QRecord().withValue("name", tableName).withValue("label", tableMetaData.getLabel())));
            InsertOutput insertOutput = new InsertAction().execute(insertInput);

            ///////////////////////////////////
            // repeat the get from the cache //
            ///////////////////////////////////
            getOutput = new GetAction().execute(getInput);
         }

         return getOutput.getRecord().getValueInteger("id");
      }
   }

}
