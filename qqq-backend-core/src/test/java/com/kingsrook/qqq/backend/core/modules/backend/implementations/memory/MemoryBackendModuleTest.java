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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.memory;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for MemoryBackendModule
 *******************************************************************************/
class MemoryBackendModuleTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      MemoryRecordStore.getInstance().reset();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFullCRUD() throws QException
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_SHAPE);
      QSession       session   = new QSession();

      /////////////////////////
      // do an initial count //
      /////////////////////////
      CountInput countInput = new CountInput(qInstance);
      countInput.setSession(session);
      countInput.setTableName(table.getName());
      assertEquals(0, new CountAction().execute(countInput).getCount());

      //////////////////
      // do an insert //
      //////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(session);
      insertInput.setTableName(table.getName());
      insertInput.setRecords(List.of(
         new QRecord()
            .withTableName(table.getName())
            .withValue("name", "My Triangle")
            .withValue("type", "triangle")
            .withValue("noOfSides", 3)
            .withValue("isPolygon", true),
         new QRecord()
            .withTableName(table.getName())
            .withValue("name", "Your Square")
            .withValue("type", "square")
            .withValue("noOfSides", 4)
            .withValue("isPolygon", true),
         new QRecord()
            .withTableName(table.getName())
            .withValue("name", "Some Circle")
            .withValue("type", "circle")
            .withValue("noOfSides", null)
            .withValue("isPolygon", false)
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(insertOutput.getRecords().size(), 3);
      assertTrue(insertOutput.getRecords().stream().allMatch(r -> r.getValue("id") != null));
      assertTrue(insertOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(insertOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(2)));
      assertTrue(insertOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));

      ////////////////
      // do a query //
      ////////////////
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(table.getName());
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(queryOutput.getRecords().size(), 3);
      assertTrue(queryOutput.getRecords().stream().allMatch(r -> r.getValue("id") != null));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(2)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("name").equals("My Triangle")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("name").equals("Your Square")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("name").equals("Some Circle")));

      assertEquals(3, new CountAction().execute(countInput).getCount());

      //////////////////
      // do an update //
      //////////////////
      UpdateInput updateInput = new UpdateInput(qInstance);
      updateInput.setSession(session);
      updateInput.setTableName(table.getName());
      updateInput.setRecords(List.of(
         new QRecord()
            .withTableName(table.getName())
            .withValue("id", 1)
            .withValue("name", "Not My Triangle any more"),
         new QRecord()
            .withTableName(table.getName())
            .withValue("id", 3)
            .withValue("type", "ellipse")
      ));
      UpdateOutput updateOutput = new UpdateAction().execute(updateInput);
      assertEquals(updateOutput.getRecords().size(), 2);

      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(queryOutput.getRecords().size(), 3);
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("name").equals("My Triangle")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("name").equals("Not My Triangle any more")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("type").equals("ellipse")));
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("type").equals("circle")));

      assertEquals(3, new CountAction().execute(countInput).getCount());

      /////////////////
      // do a delete //
      /////////////////
      DeleteInput deleteInput = new DeleteInput(qInstance);
      deleteInput.setSession(session);
      deleteInput.setTableName(table.getName());
      deleteInput.setPrimaryKeys(List.of(1, 2));
      DeleteOutput deleteOutput = new DeleteAction().execute(deleteInput);
      assertEquals(deleteOutput.getDeletedRecordCount(), 2);

      assertEquals(1, new CountAction().execute(countInput).getCount());

      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(queryOutput.getRecords().size(), 1);
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueInteger("id").equals(2)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));
   }

}