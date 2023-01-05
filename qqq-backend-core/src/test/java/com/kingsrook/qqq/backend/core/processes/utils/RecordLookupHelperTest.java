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


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for RecordLookupHelper
 *******************************************************************************/
class RecordLookupHelperTest
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
      MemoryRecordStore.resetStatistics();
      MemoryRecordStore.setCollectStatistics(true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutPreload() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      TestUtils.insertDefaultShapes(qInstance);
      RecordLookupHelper recordLookupHelper = new RecordLookupHelper(new AbstractActionInput(qInstance, new QSession()));

      MemoryRecordStore.setCollectStatistics(true);
      assertEquals(2, recordLookupHelper.getRecordId(TestUtils.TABLE_NAME_SHAPE, "name", "Square"));
      assertEquals(2, recordLookupHelper.getRecordId(TestUtils.TABLE_NAME_SHAPE, "name", "Square", Integer.class));
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      assertEquals("Circle", recordLookupHelper.getRecordValue(TestUtils.TABLE_NAME_SHAPE, "name", "id", 3));
      assertEquals("Circle", recordLookupHelper.getRecordValue(TestUtils.TABLE_NAME_SHAPE, "name", "id", 3, String.class));
      assertEquals(2, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      assertNull(recordLookupHelper.getRecordId(TestUtils.TABLE_NAME_SHAPE, "name", "notAShape"));
      assertNull(recordLookupHelper.getRecordId(TestUtils.TABLE_NAME_SHAPE, "name", "notAShape", Integer.class));
      assertEquals(3, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithPreload() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      TestUtils.insertDefaultShapes(qInstance);

      RecordLookupHelper recordLookupHelper = new RecordLookupHelper(new AbstractActionInput(qInstance, new QSession()));
      recordLookupHelper.preloadRecords(TestUtils.TABLE_NAME_SHAPE, "name");
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      assertNotNull(recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle"));
      assertEquals(1, recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle").getValueInteger("id"));
      assertEquals("Triangle", recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle").getValueString("name"));
      assertEquals(2, recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Square").getValueInteger("id"));
      assertEquals("Square", recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Square").getValueString("name"));
      assertEquals(3, recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Circle").getValueInteger("id"));
      assertEquals("Circle", recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Circle").getValueString("name"));

      /////////////////////////////////////////////////////
      // all those gets should run no additional queries //
      /////////////////////////////////////////////////////
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      ////////////////////////////////////////////////////////////////////
      // make sure we don't re-do the query in a second call to preload //
      ////////////////////////////////////////////////////////////////////
      recordLookupHelper.preloadRecords(TestUtils.TABLE_NAME_SHAPE, "name");
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      ///////////////////////////////////////////////////
      // make sure we can preload by a different field //
      ///////////////////////////////////////////////////
      recordLookupHelper.preloadRecords(TestUtils.TABLE_NAME_SHAPE, "id");
      assertEquals(2, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithPreloadInListToCacheMisses() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      TestUtils.insertDefaultShapes(qInstance);

      RecordLookupHelper recordLookupHelper = new RecordLookupHelper(new AbstractActionInput(qInstance, new QSession()));
      recordLookupHelper.preloadRecords(TestUtils.TABLE_NAME_SHAPE, "name", List.of("Triangle", "Square", "Circle", "Hexagon"));
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));

      //////////////////////////////////////////////
      // assert we cached a record that was found //
      //////////////////////////////////////////////
      assertNotNull(recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle"));
      assertEquals(1, recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle").getValueInteger("id"));
      assertEquals("Triangle", recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Triangle").getValueString("name"));

      //////////////////////////////////////////////////
      // assert we cached a null for a name not found //
      //////////////////////////////////////////////////
      assertNull(recordLookupHelper.getRecordByKey(TestUtils.TABLE_NAME_SHAPE, "name", "Hexagon"));

      /////////////////////////////////////////////////////
      // all those gets should run no additional queries //
      /////////////////////////////////////////////////////
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));
   }

}