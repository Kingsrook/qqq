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

package com.kingsrook.qqq.backend.core.actions.tables.helpers.querystats;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
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
   public void start(Supplier<QSession> sessionSupplier)
   {
      qInstance = QContext.getQInstance();
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
         synchronized(this)
         {
            if(queryStat.getFirstResultTimestamp() == null)
            {
               ////////////////////////////////////////////////
               // in case it didn't get set in the interface //
               ////////////////////////////////////////////////
               queryStat.setFirstResultTimestamp(Instant.now());
            }

            ///////////////////////////////////////////////
            // compute the millis (so you don't have to) //
            ///////////////////////////////////////////////
            if(queryStat.getStartTimestamp() != null && queryStat.getFirstResultTimestamp() != null && queryStat.getFirstResultMillis() == null)
            {
               long millis = queryStat.getFirstResultTimestamp().toEpochMilli() - queryStat.getStartTimestamp().toEpochMilli();
               queryStat.setFirstResultMillis((int) millis);
            }

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

            try
            {
               InsertInput insertInput = new InsertInput();
               insertInput.setTableName(QueryStat.TABLE_NAME);
               insertInput.setRecords(list.stream().map(qs -> qs.toQRecord()).toList());
               new InsertAction().execute(insertInput);
            }
            catch(Exception e)
            {
               LOG.error("Error inserting query stats", e);
            }
         }
         finally
         {
            QContext.clear();
         }
      }
   }

}
