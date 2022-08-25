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
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
   @BeforeEach
   @AfterEach
   void beforeAndAfter()
   {
      MemoryRecordStore.getInstance().reset();
      MemoryRecordStore.resetStatistics();
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
      insertInput.setRecords(getTestRecords(table));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(3, insertOutput.getRecords().size());
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
      assertEquals(3, queryOutput.getRecords().size());
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
      assertEquals(2, updateOutput.getRecords().size());

      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size());
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("name").equals("My Triangle")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("name").equals("Not My Triangle any more")));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueString("type").equals("ellipse")));
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueString("type").equals("circle")));

      assertEquals(3, new CountAction().execute(countInput).getCount());

      /////////////////////////
      // do a filtered query //
      /////////////////////////
      queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(table.getName());
      queryInput.setFilter(new QQueryFilter().withCriteria(new QFilterCriteria("id", QCriteriaOperator.IN, List.of(1, 3))));
      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(2, queryOutput.getRecords().size());
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));

      /////////////////////////
      // do a filtered count //
      /////////////////////////
      countInput.setFilter(queryInput.getFilter());
      assertEquals(2, new CountAction().execute(countInput).getCount());

      /////////////////
      // do a delete //
      /////////////////
      DeleteInput deleteInput = new DeleteInput(qInstance);
      deleteInput.setSession(session);
      deleteInput.setTableName(table.getName());
      deleteInput.setPrimaryKeys(List.of(1, 2));
      DeleteOutput deleteOutput = new DeleteAction().execute(deleteInput);
      assertEquals(2, deleteOutput.getDeletedRecordCount());

      assertEquals(1, new CountAction().execute(countInput).getCount());

      queryOutput = new QueryAction().execute(queryInput);
      assertEquals(1, queryOutput.getRecords().size());
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(queryOutput.getRecords().stream().noneMatch(r -> r.getValueInteger("id").equals(2)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSerials() throws QException
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_SHAPE);
      QSession       session   = new QSession();

      //////////////////
      // do an insert //
      //////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(session);
      insertInput.setTableName(table.getName());
      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("name", "Shape 1")));
      new InsertAction().execute(insertInput);

      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("name", "Shape 2")));
      new InsertAction().execute(insertInput);

      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("name", "Shape 3")));
      new InsertAction().execute(insertInput);

      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(new QSession());
      queryInput.setTableName(table.getName());
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(1)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(2)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3)));

      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("id", 4).withValue("name", "Shape 4")));
      new InsertAction().execute(insertInput);
      queryOutput = new QueryAction().execute(queryInput);
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(4)));

      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("id", 6).withValue("name", "Shape 6")));
      new InsertAction().execute(insertInput);
      queryOutput = new QueryAction().execute(queryInput);
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(6)));

      insertInput.setRecords(List.of(new QRecord().withTableName(table.getName()).withValue("name", "Shape 7")));
      new InsertAction().execute(insertInput);
      queryOutput = new QueryAction().execute(queryInput);
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(7)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> getTestRecords(QTableMetaData table)
   {
      return List.of(
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
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCustomizer() throws QException
   {
      QInstance      qInstance = TestUtils.defineInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_SHAPE);
      QSession       session   = new QSession();

      ///////////////////////////////////
      // add a customizer to the table //
      ///////////////////////////////////
      table.withCustomizer(TableCustomizers.POST_QUERY_RECORD.getRole(), new QCodeReference(ShapeTestCustomizer.class, QCodeUsage.CUSTOMIZER));

      //////////////////
      // do an insert //
      //////////////////
      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(session);
      insertInput.setTableName(table.getName());
      insertInput.setRecords(getTestRecords(table));
      new InsertAction().execute(insertInput);

      ///////////////////////////////////////////////////////
      // do a query - assert that the customizer did stuff //
      ///////////////////////////////////////////////////////
      ShapeTestCustomizer.executionCount = 0;
      QueryInput queryInput = new QueryInput(qInstance);
      queryInput.setSession(session);
      queryInput.setTableName(table.getName());
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertEquals(3, queryOutput.getRecords().size());
      assertEquals(3, ShapeTestCustomizer.executionCount);
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(1) && r.getValueInteger("tenTimesId").equals(10)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(2) && r.getValueInteger("tenTimesId").equals(20)));
      assertTrue(queryOutput.getRecords().stream().anyMatch(r -> r.getValueInteger("id").equals(3) && r.getValueInteger("tenTimesId").equals(30)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class ShapeTestCustomizer implements Function<QRecord, QRecord>
   {
      static int executionCount = 0;



      @Override
      public QRecord apply(QRecord record)
      {
         executionCount++;
         record.setValue("tenTimesId", record.getValueInteger("id") * 10);
         return (record);
      }
   }
}