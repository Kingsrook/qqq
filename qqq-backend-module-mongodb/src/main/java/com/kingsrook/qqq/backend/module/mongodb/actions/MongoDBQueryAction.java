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

package com.kingsrook.qqq.backend.module.mongodb.actions;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ActionTimeoutHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;


/*******************************************************************************
 **
 *******************************************************************************/
public class MongoDBQueryAction extends AbstractMongoDBAction implements QueryInterface
{
   private static final QLogger LOG = QLogger.getLogger(MongoDBQueryAction.class);

   private ActionTimeoutHelper actionTimeoutHelper;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      MongoClientContainer mongoClientContainer = null;

      Long       queryStartTime = System.currentTimeMillis();
      List<Bson> queryToLog     = new ArrayList<>();

      try
      {
         QueryOutput            queryOutput      = new QueryOutput(queryInput);
         QTableMetaData         table            = queryInput.getTable();
         String                 backendTableName = getBackendTableName(table);
         MongoDBBackendMetaData backend          = (MongoDBBackendMetaData) queryInput.getBackend();

         mongoClientContainer = openClient(backend, queryInput.getTransaction());
         MongoDatabase             database   = mongoClientContainer.getMongoClient().getDatabase(backend.getDatabaseName());
         MongoCollection<Document> collection = database.getCollection(backendTableName);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // set up & start an actionTimeoutHelper (note, internally it'll deal with the time being null or negative as meaning not to timeout) //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         actionTimeoutHelper = new ActionTimeoutHelper(queryInput.getTimeoutSeconds(), TimeUnit.SECONDS, new TimeoutCanceller(mongoClientContainer));
         actionTimeoutHelper.start();

         /////////////////////////
         // set up filter/query //
         /////////////////////////
         QQueryFilter filter      = queryInput.getFilter();
         Bson         searchQuery = makeSearchQueryDocument(table, filter);
         queryToLog.add(searchQuery);
         setQueryInQueryStat(searchQuery);

         ////////////////////////////////////////////////////////////
         // create cursor - further adjustments to it still follow //
         ////////////////////////////////////////////////////////////
         FindIterable<Document> cursor = collection.find(mongoClientContainer.getMongoSession(), searchQuery);

         ///////////////////////////////////////////////////////////////////////////////////////////////
         // if input specifies a set of field names to include, then add a 'projection' to the cursor //
         ///////////////////////////////////////////////////////////////////////////////////////////////
         if(queryInput.getFieldNamesToInclude() != null)
         {
            List<String> backendFieldNames = queryInput.getFieldNamesToInclude().stream().map(f -> getFieldBackendName(table.getField(f))).toList();
            cursor.projection(Projections.include(backendFieldNames));
         }

         ///////////////////////////////////
         // add a sort operator if needed //
         ///////////////////////////////////
         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            Document sortDocument = new Document();
            queryToLog.add(sortDocument);
            for(QFilterOrderBy orderBy : filter.getOrderBys())
            {
               String fieldBackendName = getFieldBackendName(table.getField(orderBy.getFieldName()));
               sortDocument.put(fieldBackendName, orderBy.getIsAscending() ? 1 : -1);
            }
            cursor.sort(sortDocument);
         }

         ////////////////////////
         // apply skip & limit //
         ////////////////////////
         if(filter != null)
         {
            if(filter.getSkip() != null)
            {
               cursor.skip(filter.getSkip());
            }

            if(filter.getLimit() != null)
            {
               cursor.limit(filter.getLimit());
            }
         }

         ////////////////////////////////////////////
         // iterate over results, building records //
         ////////////////////////////////////////////
         for(Document document : cursor)
         {
            /////////////////////////////////////////////////////////////////////////
            // once we've started getting results, go ahead and cancel the timeout //
            /////////////////////////////////////////////////////////////////////////
            actionTimeoutHelper.cancel();
            setQueryStatFirstResultTime();

            QRecord record = documentToRecord(queryInput, document);
            queryOutput.addRecord(record);

            if(queryInput.getAsyncJobCallback().wasCancelRequested())
            {
               LOG.info("Breaking query job, as requested.");
               break;
            }
         }

         return (queryOutput);
      }
      catch(Exception e)
      {
         if(actionTimeoutHelper != null && actionTimeoutHelper.getDidTimeout())
         {
            setQueryStatFirstResultTime();
            throw (new QUserFacingException("Query timed out."));
         }

         /*
         /////////////////////////////////////////////////////////////////////////////////////
         // this was copied from RDBMS - not sure where/how/if it's being used there though //
         /////////////////////////////////////////////////////////////////////////////////////
         if(isCancelled)
         {
            throw (new QUserFacingException("Query was cancelled."));
         }
         */

         LOG.warn("Error executing query", e);
         throw new QException("Error executing query", e);
      }
      finally
      {
         logQuery(getBackendTableName(queryInput.getTable()), "query", queryToLog, queryStartTime);

         if(mongoClientContainer != null)
         {
            mongoClientContainer.closeIfNeeded();
         }
      }
   }

}
