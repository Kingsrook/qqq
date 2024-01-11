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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.CountInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.mongodb.MongoDBBackendModule;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;
import org.bson.conversions.Bson;


/*******************************************************************************
 **
 *******************************************************************************/
public class MongoDBCountAction extends AbstractMongoDBAction implements CountInterface
{
   private static final QLogger LOG = QLogger.getLogger(MongoDBBackendModule.class);

   // todo? private ActionTimeoutHelper actionTimeoutHelper;



   /*******************************************************************************
    **
    *******************************************************************************/
   public CountOutput execute(CountInput countInput) throws QException
   {
      MongoClientContainer mongoClientContainer = null;

      try
      {
         CountOutput            countOutput      = new CountOutput();
         QTableMetaData         table            = countInput.getTable();
         String                 backendTableName = getBackendTableName(table);
         MongoDBBackendMetaData backend          = (MongoDBBackendMetaData) countInput.getBackend();

         mongoClientContainer = openClient(backend, null); // todo - count input has no transaction!?
         MongoDatabase             database   = mongoClientContainer.getMongoClient().getDatabase(backend.getDatabaseName());
         MongoCollection<Document> collection = database.getCollection(backendTableName);

         QQueryFilter filter      = countInput.getFilter();
         Bson         searchQuery = makeSearchQueryDocument(table, filter);

         List<Bson> bsonList = List.of(
            Aggregates.match(searchQuery),
            Aggregates.group("_id", Accumulators.sum("count", 1)));

         ////////////////////////////////////////////////////////
         // todo - system property to control (like print-sql) //
         ////////////////////////////////////////////////////////
         // LOG.debug(bsonList.toString());

         AggregateIterable<Document> aggregate = collection.aggregate(mongoClientContainer.getMongoSession(), bsonList);

         Document document = aggregate.first();
         countOutput.setCount(document == null ? 0 : document.get("count", Integer.class));

         return (countOutput);
      }
      catch(Exception e)
      {
         /*
         if(actionTimeoutHelper != null && actionTimeoutHelper.getDidTimeout())
         {
            setCountStatFirstResultTime();
            throw (new QUserFacingException("Count timed out."));
         }

         if(isCancelled)
         {
            throw (new QUserFacingException("Count was cancelled."));
         }
         */

         LOG.warn("Error executing count", e);
         throw new QException("Error executing count", e);
      }
      finally
      {
         if(mongoClientContainer != null)
         {
            mongoClientContainer.closeIfNeeded();
         }
      }
   }

}