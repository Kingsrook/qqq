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
import com.kingsrook.qqq.backend.core.actions.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;


/*******************************************************************************
 **
 *******************************************************************************/
public class MongoDBInsertAction extends AbstractMongoDBAction implements InsertInterface
{
   private static final QLogger LOG = QLogger.getLogger(MongoDBInsertAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertOutput execute(InsertInput insertInput) throws QException
   {
      MongoClientContainer mongoClientContainer = null;
      InsertOutput         rs                   = new InsertOutput();
      List<QRecord>        outputRecords        = new ArrayList<>();
      rs.setRecords(outputRecords);

      Long       queryStartTime = System.currentTimeMillis();
      List<Bson> queryToLog     = new ArrayList<>();

      try
      {
         QTableMetaData         table            = insertInput.getTable();
         String                 backendTableName = getBackendTableName(table);
         MongoDBBackendMetaData backend          = (MongoDBBackendMetaData) insertInput.getBackend();

         mongoClientContainer = openClient(backend, insertInput.getTransaction());
         MongoDatabase             database   = mongoClientContainer.getMongoClient().getDatabase(backend.getDatabaseName());
         MongoCollection<Document> collection = database.getCollection(backendTableName);

         ///////////////////////////////////////////////////////////////////////////
         // page over input record list (assuming some size of batch is too big?) //
         ///////////////////////////////////////////////////////////////////////////
         for(List<QRecord> page : CollectionUtils.getPages(insertInput.getRecords(), getPageSize()))
         {
            //////////////////////////////////////////////////////////////////
            // build list of documents from records w/o errors in this page //
            //////////////////////////////////////////////////////////////////
            List<Document> documentList = new ArrayList<>();
            for(QRecord record : page)
            {
               if(CollectionUtils.nullSafeHasContents(record.getErrors()))
               {
                  continue;
               }

               Document document = recordToDocument(table, record);
               documentList.add(document);
               queryToLog.add(document);
            }

            /////////////////////////////////////
            // skip pages that were all errors //
            /////////////////////////////////////
            if(documentList.isEmpty())
            {
               continue;
            }

            ///////////////////////////////////////////////
            // actually do the insert                    //
            // todo - how are errors returned by mongo?? //
            ///////////////////////////////////////////////
            InsertManyResult insertManyResult = collection.insertMany(mongoClientContainer.getMongoSession(), documentList);

            /////////////////////////////////
            // put ids on inserted records //
            /////////////////////////////////
            int index = 0;
            for(QRecord record : page)
            {
               QRecord outputRecord = new QRecord(record);
               rs.addRecord(outputRecord);

               if(CollectionUtils.nullSafeIsEmpty(record.getErrors()))
               {
                  BsonValue insertedId = insertManyResult.getInsertedIds().get(index++);
                  String    idString   = insertedId.asObjectId().getValue().toString();
                  outputRecord.setValue(table.getPrimaryKeyField(), idString);
               }
            }
         }
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }
      finally
      {
         logQuery(getBackendTableName(insertInput.getTable()), "insert", queryToLog, queryStartTime);

         if(mongoClientContainer != null)
         {
            mongoClientContainer.closeIfNeeded();
         }
      }

      return (rs);
   }

}
