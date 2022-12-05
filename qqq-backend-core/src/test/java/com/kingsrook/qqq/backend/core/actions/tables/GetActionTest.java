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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
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
 ** Unit test for GetAction
 **
 *******************************************************************************/
class GetActionTest
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
   }



   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      GetInput request = new GetInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setTableName("person");
      request.setPrimaryKey(1);
      request.setShouldGenerateDisplayValues(true);
      request.setShouldTranslatePossibleValues(true);
      GetOutput result = new GetAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getRecord());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeyCache() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      /////////////////////////////////////
      // insert rows in the source table //
      /////////////////////////////////////
      TestUtils.insertRecords(qInstance, qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "George").withValue("lastName", "Washington").withValue("noOfShoes", 5),
         new QRecord().withValue("id", 2).withValue("firstName", "John").withValue("lastName", "Adams"),
         new QRecord().withValue("id", 3).withValue("firstName", "Thomas").withValue("lastName", "Jefferson")
      ));

      /////////////////////////////////////////////////////////////////////////////
      // get from the table which caches it - confirm they are (magically) found //
      /////////////////////////////////////////////////////////////////////////////
      {
         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNotNull(getOutput.getRecord());
         assertNotNull(getOutput.getRecord().getValue("cachedDate"));
         assertEquals(5, getOutput.getRecord().getValue("noOfShoes"));
      }

      ///////////////////////////////////////////////////////////////////////////
      // request a row that doesn't exist in cache or source, should miss both //
      ///////////////////////////////////////////////////////////////////////////
      {
         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "John", "lastName", "McCain"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNull(getOutput.getRecord());
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // update the record in the source table - then re-get from cache table - shouldn't see new value. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      {
         UpdateInput updateInput = new UpdateInput(qInstance);
         updateInput.setSession(new QSession());
         updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("noOfShoes", 6)));
         new UpdateAction().execute(updateInput);

         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNotNull(getOutput.getRecord());
         assertNotNull(getOutput.getRecord().getValue("cachedDate"));
         assertEquals(5, getOutput.getRecord().getValue("noOfShoes"));
      }

      ///////////////////////////////////////////////////////////////////////////
      // delete the cached record; re-get, and we should see the updated value //
      ///////////////////////////////////////////////////////////////////////////
      {
         DeleteInput deleteInput = new DeleteInput(qInstance);
         deleteInput.setSession(new QSession());
         deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         deleteInput.setQueryFilter(new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.EQUALS, "George")));
         new DeleteAction().execute(deleteInput);

         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNotNull(getOutput.getRecord());
         assertNotNull(getOutput.getRecord().getValue("cachedDate"));
         assertEquals(6, getOutput.getRecord().getValue("noOfShoes"));
      }

      ///////////////////////////////////////////////////////////////////
      // update the source record; see that it isn't updated in cache. //
      ///////////////////////////////////////////////////////////////////
      {
         UpdateInput updateInput = new UpdateInput(qInstance);
         updateInput.setSession(new QSession());
         updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         updateInput.setRecords(List.of(new QRecord().withValue("id", 1).withValue("noOfShoes", 7)));
         new UpdateAction().execute(updateInput);

         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNotNull(getOutput.getRecord());
         assertNotNull(getOutput.getRecord().getValue("cachedDate"));
         assertEquals(6, getOutput.getRecord().getValue("noOfShoes"));

         ///////////////////////////////////////////////////////////////////////
         // then artificially move back the cachedDate in the cache table.    //
         // then re-get from cache table, and we should see the updated value //
         ///////////////////////////////////////////////////////////////////////
         updateInput = new UpdateInput(qInstance);
         updateInput.setSession(new QSession());
         updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         updateInput.setRecords(List.of(getOutput.getRecord().withValue("cachedDate", Instant.parse("2001-01-01T00:00:00Z"))));
         new UpdateAction().execute(updateInput);

         getOutput = new GetAction().execute(getInput);
         assertEquals(7, getOutput.getRecord().getValue("noOfShoes"));
      }

      /////////////////////////////////////////////////
      // should only be 1 cache record at this point //
      /////////////////////////////////////////////////
      assertEquals(1, TestUtils.queryTable(TestUtils.defineInstance(), TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE).size());

      //////////////////////////////////////////////////////////////////////
      // delete the source record - it will still be in the cache though. //
      //////////////////////////////////////////////////////////////////////
      {
         DeleteInput deleteInput = new DeleteInput(qInstance);
         deleteInput.setSession(new QSession());
         deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
         deleteInput.setPrimaryKeys(List.of(1));
         new DeleteAction().execute(deleteInput);

         GetInput getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         GetOutput getOutput = new GetAction().execute(getInput);
         assertNotNull(getOutput.getRecord());

         ////////////////////////////////////////////////////////////////////
         // then artificially move back the cachedDate in the cache table. //
         // then re-get from cache table, and now it should go away        //
         ////////////////////////////////////////////////////////////////////
         UpdateInput updateInput = new UpdateInput(qInstance);
         updateInput.setSession(new QSession());
         updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         updateInput.setRecords(List.of(getOutput.getRecord().withValue("cachedDate", Instant.parse("2001-01-01T00:00:00Z"))));
         new UpdateAction().execute(updateInput);

         getInput = new GetInput(qInstance);
         getInput.setSession(new QSession());
         getInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY_CACHE);
         getInput.setUniqueKey(Map.of("firstName", "George", "lastName", "Washington"));
         getOutput = new GetAction().execute(getInput);
         assertNull(getOutput.getRecord());
      }
   }

}
