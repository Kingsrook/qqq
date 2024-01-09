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


import com.kingsrook.qqq.backend.core.actions.interfaces.DeleteInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.mongodb.MongoDBBackendModule;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class MongoDBDeleteAction extends AbstractMongoDBAction implements DeleteInterface
{
   private static final QLogger LOG = QLogger.getLogger(MongoDBBackendModule.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean supportsQueryFilterInput()
   {
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public DeleteOutput execute(DeleteInput deleteInput) throws QException
   {
      MongoClientContainer mongoClientContainer = null;

      try
      {
         DeleteOutput           deleteOutput     = new DeleteOutput();
         QTableMetaData         table            = deleteInput.getTable();
         String                 backendTableName = getBackendTableName(table);
         MongoDBBackendMetaData backend          = (MongoDBBackendMetaData) deleteInput.getBackend();

         mongoClientContainer = openClient(backend, deleteInput.getTransaction());
         MongoDatabase             database   = mongoClientContainer.getMongoClient().getDatabase(backend.getDatabaseName());
         MongoCollection<Document> collection = database.getCollection(backendTableName);

         QQueryFilter queryFilter = deleteInput.getQueryFilter();
         Bson         searchQuery;
         if(CollectionUtils.nullSafeHasContents(deleteInput.getPrimaryKeys()))
         {
            searchQuery = Filters.in("_id", deleteInput.getPrimaryKeys().stream().map(id -> new ObjectId(ValueUtils.getValueAsString(id))).toList());
         }
         else if(queryFilter != null && queryFilter.hasAnyCriteria())
         {
            QQueryFilter filter = queryFilter;
            searchQuery = makeSearchQueryDocument(table, filter);
         }
         else
         {
            LOG.info("Missing both primary keys and a search filter in delete request - exiting with noop", logPair("tableName", table.getName()));
            return (deleteOutput);
         }

         ////////////////////////////////////////////////////////
         // todo - system property to control (like print-sql) //
         ////////////////////////////////////////////////////////
         // LOG.debug(searchQuery);

         DeleteResult deleteResult = collection.deleteMany(mongoClientContainer.getMongoSession(), searchQuery);
         deleteOutput.setDeletedRecordCount((int) deleteResult.getDeletedCount());

         //////////////////////////////////////////////////////////////////////////
         // todo any way to get records with errors or warnings for deleteOutput //
         //////////////////////////////////////////////////////////////////////////

         return (deleteOutput);
      }
      catch(Exception e)
      {
         LOG.warn("Error executing delete", e);
         throw new QException("Error executing delete", e);
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
