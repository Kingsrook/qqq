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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for UniqueKeyHelper 
 *******************************************************************************/
class UniqueKeyHelperTest extends BaseTest
{
   private static Integer originalPageSize;

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeAll
   static void beforeAll()
   {
      originalPageSize = UniqueKeyHelper.getPageSize();
      UniqueKeyHelper.setPageSize(5);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterAll
   static void afterAll()
   {
      UniqueKeyHelper.setPageSize(originalPageSize);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.fullReset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKey() throws QException
   {
      List<QRecord> recordsWithKey1Equals1AndKey2In1Through10 = List.of(
         new QRecord().withValue("key1", 1).withValue("key2", 1),
         new QRecord().withValue("key1", 1).withValue("key2", 2),
         new QRecord().withValue("key1", 1).withValue("key2", 3),
         new QRecord().withValue("key1", 1).withValue("key2", 4),
         new QRecord().withValue("key1", 1).withValue("key2", 5),
         new QRecord().withValue("key1", 1).withValue("key2", 6),
         new QRecord().withValue("key1", 1).withValue("key2", 7),
         new QRecord().withValue("key1", 1).withValue("key2", 8),
         new QRecord().withValue("key1", 1).withValue("key2", 9),
         new QRecord().withValue("key1", 1).withValue("key2", 10)
      );

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_TWO_KEYS);
      insertInput.setRecords(recordsWithKey1Equals1AndKey2In1Through10);
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      MemoryRecordStore.resetStatistics();
      MemoryRecordStore.setCollectStatistics(true);

      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_TWO_KEYS);
      Map<List<Serializable>, Serializable> existingKeys = UniqueKeyHelper.getExistingKeys(null, table, recordsWithKey1Equals1AndKey2In1Through10, table.getUniqueKeys().get(0), false);
      assertEquals(recordsWithKey1Equals1AndKey2In1Through10.size(), existingKeys.size());

      assertEquals(2, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCacheSourceTableNotQueried() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      String sourceTableName = TestUtils.TABLE_NAME_SHAPE;
      String cacheTableName  = TestUtils.TABLE_NAME_SHAPE_CACHE;

      ///////////////////////////////////////////////////////////////////
      // insert rows in the source table - but none in the cache table //
      ///////////////////////////////////////////////////////////////////
      TestUtils.insertRecords(qInstance.getTable(sourceTableName), List.of(
         new QRecord().withValue("id", 1).withValue("name", "Triangle").withValue("noOfSides", 3)
      ));

      //////////////////////////////////////////////////////////////////////////////
      // look for existing keys in the cache table - there should be none found.  //
      // (before CacheActionFlags.DO_NOT_QUERY_SOURCE_TABLE was added, this query //
      // would have passed through to the source table, which isn't what we want) //
      //////////////////////////////////////////////////////////////////////////////
      Map<List<Serializable>, Serializable> existingKeys = UniqueKeyHelper.getExistingKeys(null, qInstance.getTable(cacheTableName), List.of(new QRecord().withValue("name", "Triangle")), new UniqueKey("name"));
      assertEquals(0, existingKeys.size());
   }

}