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

package com.kingsrook.qqq.backend.module.mongodb.actions;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.module.mongodb.BaseTest;
import com.kingsrook.qqq.backend.module.mongodb.TestUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for MongoDBQueryAction 
 *******************************************************************************/
class MongoDBInsertActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin")
            .withValue("unmappedField", 1701)
            .withValue("unmappedList", new ArrayList<>(List.of("A", "B", "C")))
            .withValue("unmappedObject", new HashMap<>(Map.of("A", 1, "C", true))),
         new QRecord().withValue("firstName", "Tim"),
         new QRecord().withValue("firstName", "Tyler")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      /////////////////////////////////////////
      // make sure id got put on all records //
      /////////////////////////////////////////
      for(QRecord record : insertOutput.getRecords())
      {
         assertNotNull(record.getValueString("id"));
      }

      ///////////////////////////////////////////////////
      // directly query mongo for the inserted records //
      ///////////////////////////////////////////////////
      MongoDatabase             database   = getMongoClient().getDatabase(TestUtils.MONGO_DATABASE);
      MongoCollection<Document> collection = database.getCollection(TestUtils.TABLE_NAME_PERSON);
      assertEquals(3, collection.countDocuments());
      for(Document document : collection.find())
      {
         /////////////////////////////////////////////////////////////
         // make sure values got set - including some nested values //
         /////////////////////////////////////////////////////////////
         assertNotNull(document.get("firstName"));
         assertNotNull(document.get("metaData"));
         assertThat(document.get("metaData")).isInstanceOf(Document.class);
         assertNotNull(((Document) document.get("metaData")).get("createDate"));
      }

      Document document = collection.find(new Document("firstName", "Darin")).first();
      assertNotNull(document);
      assertEquals(1701, document.get("unmappedField"));
      assertEquals(List.of("A", "B", "C"), document.get("unmappedList"));
      assertEquals(Map.of("A", 1, "C", true), document.get("unmappedObject"));
   }

}