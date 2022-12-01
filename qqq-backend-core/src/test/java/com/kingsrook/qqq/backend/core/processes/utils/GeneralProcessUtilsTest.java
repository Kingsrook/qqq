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

package com.kingsrook.qqq.backend.core.processes.utils;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for GeneralProcessUtils
 *******************************************************************************/
class GeneralProcessUtilsTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   @BeforeEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetForeignRecordMap() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("favoriteShapeId", 3),
         new QRecord().withValue("favoriteShapeId", 1)
      ));
      TestUtils.insertDefaultShapes(instance);

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      Map<Serializable, QRecord> foreignRecordMap = GeneralProcessUtils.getForeignRecordMap(queryInput, queryOutput.getRecords(), "favoriteShapeId", TestUtils.TABLE_NAME_SHAPE, "id");

      assertEquals(2, foreignRecordMap.size());
      assertEquals(1, foreignRecordMap.get(1).getValueInteger("id"));
      assertEquals(3, foreignRecordMap.get(3).getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetForeignRecordListingHashMap() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 2).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 3).withValue("favoriteShapeId", 1)
      ));
      TestUtils.insertDefaultShapes(instance);

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      ListingHash<Serializable, QRecord> foreignRecordListingHashMap = GeneralProcessUtils.getForeignRecordListingHashMap(queryInput, queryOutput.getRecords(), "id", TestUtils.TABLE_NAME_PERSON_MEMORY, "favoriteShapeId");

      assertEquals(2, foreignRecordListingHashMap.size());

      assertEquals(1, foreignRecordListingHashMap.get(1).size());
      assertEquals(Set.of(3), foreignRecordListingHashMap.get(1).stream().map(r -> r.getValueInteger("id")).collect(Collectors.toSet()));

      assertEquals(2, foreignRecordListingHashMap.get(3).size());
      assertEquals(Set.of(1, 2), foreignRecordListingHashMap.get(3).stream().map(r -> r.getValueInteger("id")).collect(Collectors.toSet()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAddForeignRecordsToRecordList() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("favoriteShapeId", 3),
         new QRecord().withValue("favoriteShapeId", 1)
      ));
      TestUtils.insertDefaultShapes(instance);

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      GeneralProcessUtils.addForeignRecordsToRecordList(queryInput, queryOutput.getRecords(), "favoriteShapeId", TestUtils.TABLE_NAME_SHAPE, "id");

      for(QRecord record : queryOutput.getRecords())
      {
         assertEquals(record.getValue("favoriteShapeId"), ((QRecord) record.getValue(TestUtils.TABLE_NAME_SHAPE)).getValue("id"));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAddForeignRecordsListToRecordList() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 2).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 3).withValue("favoriteShapeId", 1)
      ));
      TestUtils.insertDefaultShapes(instance);

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
      QueryOutput queryOutput = new QueryAction().execute(queryInput);

      GeneralProcessUtils.addForeignRecordsListToRecordList(queryInput, queryOutput.getRecords(), "id", TestUtils.TABLE_NAME_PERSON_MEMORY, "favoriteShapeId");

      for(QRecord record : queryOutput.getRecords())
      {
         @SuppressWarnings("unchecked")
         List<QRecord> foreignRecordList = (List<QRecord>) record.getValue(TestUtils.TABLE_NAME_PERSON_MEMORY);

         if(record.getValueInteger("id").equals(3))
         {
            assertEquals(2, foreignRecordList.size());
         }
         else if(record.getValueInteger("id").equals(2))
         {
            assertNull(foreignRecordList);
            continue;
         }

         for(QRecord foreignRecord : foreignRecordList)
         {
            assertEquals(record.getValue("id"), foreignRecord.getValue("favoriteShapeId"));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetRecordListByField() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 2).withValue("favoriteShapeId", 3),
         new QRecord().withValue("id", 3).withValue("favoriteShapeId", 1)
      ));

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      List<QRecord> records = GeneralProcessUtils.getRecordListByField(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "favoriteShapeId", 3);
      assertEquals(2, records.size());
      assertTrue(records.stream().anyMatch(r -> r.getValue("id").equals(1)));
      assertTrue(records.stream().anyMatch(r -> r.getValue("id").equals(2)));
      assertTrue(records.stream().noneMatch(r -> r.getValue("id").equals(3)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetRecordById() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin"),
         new QRecord().withValue("id", 2).withValue("firstName", "James"),
         new QRecord().withValue("id", 3).withValue("firstName", "Tim")
      ));

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      Optional<QRecord> record = GeneralProcessUtils.getRecordByField(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "firstName", "James");
      assertTrue(record.isPresent());
      assertEquals(2, record.get().getValueInteger("id"));

      record = GeneralProcessUtils.getRecordByField(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "firstName", "Bobby");
      assertFalse(record.isPresent());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLoadTable() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin"),
         new QRecord().withValue("id", 2).withValue("firstName", "James"),
         new QRecord().withValue("id", 3).withValue("firstName", "Tim")
      ));

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      List<QRecord> records = GeneralProcessUtils.loadTable(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertEquals(3, records.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLoadTableToMap() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin"),
         new QRecord().withValue("id", 2).withValue("firstName", "James"),
         new QRecord().withValue("id", 3).withValue("firstName", "Tim")
      ));

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      Map<Serializable, QRecord> recordMapById = GeneralProcessUtils.loadTableToMap(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "id");
      assertEquals(3, recordMapById.size());
      assertEquals("Darin", recordMapById.get(1).getValueString("firstName"));
      assertEquals("James", recordMapById.get(2).getValueString("firstName"));

      Map<Serializable, QRecord> recordMapByFirstName = GeneralProcessUtils.loadTableToMap(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "firstName");
      assertEquals(3, recordMapByFirstName.size());
      assertEquals(1, recordMapByFirstName.get("Darin").getValueInteger("id"));
      assertEquals(3, recordMapByFirstName.get("Tim").getValueInteger("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLoadTableToListingHash() throws QException
   {
      QInstance instance = TestUtils.defineInstance();

      TestUtils.insertRecords(instance, instance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("id", 2).withValue("firstName", "James").withValue("lastName", "Maes"),
         new QRecord().withValue("id", 3).withValue("firstName", "James").withValue("lastName", "Brown")
      ));

      QueryInput queryInput = new QueryInput(instance);
      queryInput.setSession(new QSession());
      ListingHash<Serializable, QRecord> map = GeneralProcessUtils.loadTableToListingHash(queryInput, TestUtils.TABLE_NAME_PERSON_MEMORY, "firstName");
      assertEquals(2, map.size());
      assertEquals(1, map.get("Darin").size());
      assertEquals(2, map.get("James").size());
   }
}