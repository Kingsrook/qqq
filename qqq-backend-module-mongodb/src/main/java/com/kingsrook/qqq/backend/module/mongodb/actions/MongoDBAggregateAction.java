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
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ActionTimeoutHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByAggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByGroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import org.bson.Document;
import org.bson.conversions.Bson;


/*******************************************************************************
 **
 *******************************************************************************/
public class MongoDBAggregateAction extends AbstractMongoDBAction implements AggregateInterface
{
   private static final QLogger LOG = QLogger.getLogger(MongoDBAggregateAction.class);

   private ActionTimeoutHelper actionTimeoutHelper;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateOutput execute(AggregateInput aggregateInput) throws QException
   {
      MongoClientContainer mongoClientContainer = null;

      Long       queryStartTime = System.currentTimeMillis();
      List<Bson> queryToLog     = new ArrayList<>();

      try
      {
         AggregateOutput        aggregateOutput  = new AggregateOutput();
         QTableMetaData         table            = aggregateInput.getTable();
         String                 backendTableName = getBackendTableName(table);
         MongoDBBackendMetaData backend          = (MongoDBBackendMetaData) aggregateInput.getBackend();

         mongoClientContainer = openClient(backend, null); // todo - aggregate input has no transaction!?
         MongoDatabase             database   = mongoClientContainer.getMongoClient().getDatabase(backend.getDatabaseName());
         MongoCollection<Document> collection = database.getCollection(backendTableName);

         QQueryFilter filter      = aggregateInput.getFilter();
         Bson         searchQuery = makeSearchQueryDocument(table, filter);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // set up & start an actionTimeoutHelper (note, internally it'll deal with the time being null or negative as meaning not to timeout) //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         actionTimeoutHelper = new ActionTimeoutHelper(aggregateInput.getTimeoutSeconds(), TimeUnit.SECONDS, new TimeoutCanceller(mongoClientContainer));
         actionTimeoutHelper.start();

         /////////////////////////////////////////////////////////////////////////
         // we have to submit a list of BSON objects to the aggregate function. //
         // the first one is the search query                                   //
         // second is the group-by stuff, which we'll explain as we build it    //
         /////////////////////////////////////////////////////////////////////////
         List<Bson> bsonList = new ArrayList<>();
         bsonList.add(Aggregates.match(searchQuery));
         setQueryInQueryStat(searchQuery);
         queryToLog = bsonList;

         //////////////////////////////////////////////////////////////////////////////////////
         // if there are group-by fields, then we need to build a document with those fields //
         // not sure what the whole name, $name is, but, go go mongo                         //
         //////////////////////////////////////////////////////////////////////////////////////
         Document groupValueDocument = new Document();
         if(CollectionUtils.nullSafeHasContents(aggregateInput.getGroupBys()))
         {
            for(GroupBy groupBy : aggregateInput.getGroupBys())
            {
               String name = getFieldBackendName(table.getField(groupBy.getFieldName()));
               groupValueDocument.append(name, "$" + name);
            }
         }

         ////////////////////////////////////////////////////////////////////
         // next build a list of accumulator fields - for aggregate values //
         ////////////////////////////////////////////////////////////////////
         List<BsonField> bsonFields = new ArrayList<>();
         for(Aggregate aggregate : aggregateInput.getAggregates())
         {
            String fieldName  = aggregate.getFieldName() + "_" + aggregate.getOperator().toString().toLowerCase();
            String expression = "$" + getFieldBackendName(table.getField(aggregate.getFieldName()));

            bsonFields.add(switch(aggregate.getOperator())
            {
               case COUNT -> Accumulators.sum(fieldName, 1); // count... do a sum of 1's
               case COUNT_DISTINCT -> throw new QException("Count Distinct is not supported for MongoDB tables at this time.");
               case SUM -> Accumulators.sum(fieldName, expression);
               case MIN -> Accumulators.min(fieldName, expression);
               case MAX -> Accumulators.max(fieldName, expression);
               case AVG -> Accumulators.avg(fieldName, expression);
            });
         }

         ///////////////////////////////////////////////////////////////////////////////////
         // add the group-by fields and the aggregates in the group stage of the pipeline //
         ///////////////////////////////////////////////////////////////////////////////////
         bsonList.add(Aggregates.group(groupValueDocument, bsonFields));

         //////////////////////////////////////////////
         // if there are any order-bys, add them too //
         //////////////////////////////////////////////
         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            Document sortValue = new Document();
            for(QFilterOrderBy orderBy : filter.getOrderBys())
            {
               String fieldName;
               if(orderBy instanceof QFilterOrderByAggregate orderByAggregate)
               {
                  Aggregate aggregate = orderByAggregate.getAggregate();
                  fieldName = aggregate.getFieldName() + "_" + aggregate.getOperator().toString().toLowerCase();
               }
               else if(orderBy instanceof QFilterOrderByGroupBy orderByGroupBy)
               {
                  fieldName = "_id." + getFieldBackendName(table.getField(orderByGroupBy.getGroupBy().getFieldName()));
               }
               else
               {
                  ///////////////////////////////////////////////////
                  // does this happen?  should it be "_id." if so? //
                  ///////////////////////////////////////////////////
                  fieldName = getFieldBackendName(table.getField(orderBy.getFieldName()));
               }

               sortValue.append(fieldName, orderBy.getIsAscending() ? 1 : -1);
            }

            bsonList.add(new Document("$sort", sortValue));
         }

         ////////////////////////////////////////////////////////
         // todo - system property to control (like print-sql) //
         ////////////////////////////////////////////////////////
         // LOG.debug(bsonList.toString());

         ///////////////////////////
         // execute the aggregate //
         ///////////////////////////
         AggregateIterable<Document> aggregates = collection.aggregate(mongoClientContainer.getMongoSession(), bsonList);

         List<AggregateResult> results = new ArrayList<>();
         aggregateOutput.setResults(results);

         /////////////////////
         // process results //
         /////////////////////
         for(Document document : aggregates)
         {
            /////////////////////////////////////////////////////////////////////////
            // once we've started getting results, go ahead and cancel the timeout //
            /////////////////////////////////////////////////////////////////////////
            actionTimeoutHelper.cancel();
            setQueryStatFirstResultTime();

            AggregateResult result = new AggregateResult();
            results.add(result);

            ////////////////////////////////////////////////////////////////
            // get group by values (if there are any) out of the document //
            ////////////////////////////////////////////////////////////////
            for(GroupBy groupBy : CollectionUtils.nonNullList(aggregateInput.getGroupBys()))
            {
               Document idDocument = (Document) document.get("_id");
               Object   value      = idDocument.get(groupBy.getFieldName());
               result.withGroupByValue(groupBy, ValueUtils.getValueAsFieldType(groupBy.getType(), value));
            }

            //////////////////////////////////////////
            // get aggregate values out of document //
            //////////////////////////////////////////
            for(Aggregate aggregate : aggregateInput.getAggregates())
            {
               QFieldMetaData field     = table.getField(aggregate.getFieldName());
               QFieldType     fieldType = aggregate.getFieldType();
               if(fieldType == null)
               {
                  fieldType = field.getType();
               }
               if(fieldType.equals(QFieldType.INTEGER) && (aggregate.getOperator().equals(AggregateOperator.AVG)))
               {
                  fieldType = QFieldType.DECIMAL;
               }

               Object value = document.get(aggregate.getFieldName() + "_" + aggregate.getOperator().toString().toLowerCase());
               result.withAggregateValue(aggregate, ValueUtils.getValueAsFieldType(fieldType, value));
            }
         }

         return (aggregateOutput);
      }
      catch(Exception e)
      {
         if(actionTimeoutHelper != null && actionTimeoutHelper.getDidTimeout())
         {
            setQueryStatFirstResultTime();
            throw (new QUserFacingException("Aggregate timed out."));
         }

         /*
         /////////////////////////////////////////////////////////////////////////////////////
         // this was copied from RDBMS - not sure where/how/if it's being used there though //
         /////////////////////////////////////////////////////////////////////////////////////
         if(isCancelled)
         {
            throw (new QUserFacingException("Aggregate was cancelled."));
         }
         */

         LOG.warn("Error executing aggregate", e);
         throw new QException("Error executing aggregate", e);
      }
      finally
      {
         logQuery(getBackendTableName(aggregateInput.getTable()), "aggregate", queryToLog, queryStartTime);

         if(mongoClientContainer != null)
         {
            mongoClientContainer.closeIfNeeded();
         }
      }
   }

}
