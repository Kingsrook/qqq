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


import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.module.mongodb.BaseTest;
import com.kingsrook.qqq.backend.module.mongodb.TestUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MongoDBQueryAction 
 *******************************************************************************/
class MongoDBQueryActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach()
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      ////////////////////////////////////////
      // directly insert some mongo records //
      ////////////////////////////////////////
      MongoDatabase             database   = getMongoClient().getDatabase(TestUtils.MONGO_DATABASE);
      MongoCollection<Document> collection = database.getCollection(TestUtils.TEST_COLLECTION);
      collection.insertMany(List.of(
         Document.parse("""
            {  "metaData": {"createDate": "2023-01-09T01:01:01.123Z", "modifyDate": "2023-01-09T02:02:02.123Z", "oops": "All Crunchberries"},
               "firstName": "Darin",
               "lastName": "Kelkhoff",
               "unmappedField": 1701,
               "unmappedList": [1,2,3],
               "unmappedObject": {
                  "A": "B",
                  "One": 2,
                  "subSub": {
                     "so": true
                  }
               }
            }"""),
         Document.parse("""
            {"metaData": {"createDate": "2023-01-09T03:03:03.123Z", "modifyDate": "2023-01-09T04:04:04.123Z"}, "firstName": "Tylers", "lastName": "Sample"}""")
      ));

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      assertEquals(2, queryOutput.getRecords().size());

      QRecord record = queryOutput.getRecords().get(0);
      assertEquals(Instant.parse("2023-01-09T01:01:01.123Z"), record.getValueInstant("createDate"));
      assertEquals(Instant.parse("2023-01-09T02:02:02.123Z"), record.getValueInstant("modifyDate"));
      assertThat(record.getValue("id")).isInstanceOf(String.class);
      assertEquals("Darin", record.getValueString("firstName"));
      assertEquals("Kelkhoff", record.getValueString("lastName"));

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // test that un-mapped (or un-structured) fields come through, with their shape as they exist in the mongo record //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals(1701, record.getValueInteger("unmappedField"));
      assertEquals(List.of(1, 2, 3), record.getValue("unmappedList"));
      assertEquals(Map.of("A", "B", "One", 2, "subSub", Map.of("so", true)), record.getValue("unmappedObject"));
      assertEquals(Map.of("oops", "All Crunchberries"), record.getValue("metaData"));

      record = queryOutput.getRecords().get(1);
      assertEquals(Instant.parse("2023-01-09T03:03:03.123Z"), record.getValueInstant("createDate"));
      assertEquals(Instant.parse("2023-01-09T04:04:04.123Z"), record.getValueInstant("modifyDate"));
      assertEquals("Tylers", record.getValueString("firstName"));
      assertEquals("Sample", record.getValueString("lastName"));
   }

}