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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for InsertAction
 **
 *******************************************************************************/
class InsertActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   @AfterEach
   void beforeAndAfterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      InsertInput request = new InsertInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setTableName("person");
      List<QRecord> records = new ArrayList<>();
      QRecord       record  = new QRecord();
      record.setValue("firstName", "James");
      records.add(record);
      request.setRecords(records);
      InsertOutput result = new InsertAction().execute(request);
      assertNotNull(result);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeysPreExisting() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());

      ///////////////////////////////////////////////////////
      // try to insert that person again - shouldn't work. //
      ///////////////////////////////////////////////////////
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      insertOutput = new InsertAction().execute(insertInput);
      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertNull(insertOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(0).getErrors().size());
      assertThat(insertOutput.getRecords().get(0).getErrors().get(0)).contains("Another record already exists with this First Name and Last Name");

      //////////////////////////////////////////////////////////////////////////////////////////
      // try to insert that person again, with 2 others - the 2 should work, but the one fail //
      //////////////////////////////////////////////////////////////////////////////////////////
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Smith"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Trevor").withValue("lastName", "Kelkhoff")
      ));
      insertOutput = new InsertAction().execute(insertInput);
      assertEquals(3, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertNotNull(insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertNotNull(insertOutput.getRecords().get(2).getValueInteger("id"));
      assertEquals(0, insertOutput.getRecords().get(0).getErrors().size());
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertEquals(0, insertOutput.getRecords().get(2).getErrors().size());

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testUniqueKeysWithinBatch() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertThat(insertOutput.getRecords().get(1).getErrors().get(0)).contains("Another record already exists with this First Name and Last Name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSingleColumnUniqueKey() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      qInstance.getTable(TestUtils.TABLE_NAME_SHAPE)
         .withUniqueKey(new UniqueKey("name"));

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_SHAPE);
      insertInput.setRecords(List.of(
         new QRecord().withValue("name", "Circle"),
         new QRecord().withValue("name", "Circle")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(1, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertNull(insertOutput.getRecords().get(1).getValueInteger("id"));
      assertEquals(1, insertOutput.getRecords().get(1).getErrors().size());
      assertThat(insertOutput.getRecords().get(1).getErrors().get(0)).contains("Another record already exists with this Name");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSkippingUniqueKeys() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setSkipUniqueKeyCheck(true);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff"),
         new QRecord().withValue("firstName", "Darin").withValue("lastName", "Kelkhoff")
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);

      assertEquals(2, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY).size());
      assertEquals(1, insertOutput.getRecords().get(0).getValueInteger("id"));
      assertEquals(2, insertOutput.getRecords().get(1).getValueInteger("id"));
      assertTrue(CollectionUtils.nullSafeIsEmpty(insertOutput.getRecords().get(1).getErrors()));
   }

}
