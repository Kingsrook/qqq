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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for ChildInserterPostInsertCustomizer
 **
 ** We'll use person & shape tables here, w/ the favoriteShapeId foreign key,
 ** so a rule of "every time we insert a person, if they aren't already pointed at
 ** a favoriteShape, insert a new shape for them".
 *******************************************************************************/
class ChildInserterPostInsertCustomizerTest extends BaseTest
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
    **
    *******************************************************************************/
   @Test
   void testEmptyCases() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      addPostInsertActionToTable(qInstance);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of());

      ////////////////////////////////////////
      // just looking for no exception here //
      ////////////////////////////////////////
      new InsertAction().execute(insertInput);
      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // now insert one person, but they shouldn't need a favoriteShape to be inserted - again, make sure we don't blow up and that no shapes get inserted. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      insertInput.setRecords(List.of(new QRecord().withValue("firstName", "James").withValue("favoriteShapeId", -1)));
      new InsertAction().execute(insertInput);
      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void addPostInsertActionToTable(QInstance qInstance)
   {
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY)
         .withCustomizer(TableCustomizers.POST_INSERT_RECORD.getTableCustomizer(), new QCodeReference(PersonPostInsertAddFavoriteShapeCustomizer.class, QCodeUsage.CUSTOMIZER));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleCase() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      addPostInsertActionToTable(qInstance);

      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin")
      ));
      InsertOutput insertOutput    = new InsertAction().execute(insertInput);
      Serializable favoriteShapeId = insertOutput.getRecords().get(0).getValue("favoriteShapeId");
      assertNotNull(favoriteShapeId);

      List<QRecord> shapeRecords = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE);
      assertEquals(1, shapeRecords.size());
      assertEquals(favoriteShapeId, shapeRecords.get(0).getValue("id"));
      assertEquals("Darin's favorite shape!", shapeRecords.get(0).getValue("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testComplexCase() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      addPostInsertActionToTable(qInstance);

      assertEquals(0, TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE).size());

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("firstName", "Darin"),
         new QRecord().withValue("firstName", "James").withValue("favoriteShapeId", -1),
         new QRecord().withValue("firstName", "Tim"),
         new QRecord().withValue("firstName", "Garret").withValue("favoriteShapeId", -2)
      ));
      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      assertEquals(1, insertOutput.getRecords().get(0).getValue("favoriteShapeId"));
      assertEquals(-1, insertOutput.getRecords().get(1).getValue("favoriteShapeId"));
      assertEquals(2, insertOutput.getRecords().get(2).getValue("favoriteShapeId"));
      assertEquals(-2, insertOutput.getRecords().get(3).getValue("favoriteShapeId"));

      List<QRecord> shapeRecords = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_SHAPE);
      assertEquals(2, shapeRecords.size());
      assertEquals(1, shapeRecords.get(0).getValue("id"));
      assertEquals(2, shapeRecords.get(1).getValue("id"));
      assertEquals("Darin's favorite shape!", shapeRecords.get(0).getValue("name"));
      assertEquals("Tim's favorite shape!", shapeRecords.get(1).getValue("name"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PersonPostInsertAddFavoriteShapeCustomizer extends ChildInserterPostInsertCustomizer
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public QRecord buildChildForRecord(QRecord parentRecord) throws QException
      {
         return (new QRecord().withValue("name", parentRecord.getValue("firstName") + "'s favorite shape!"));
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String getChildTableName()
      {
         return (TestUtils.TABLE_NAME_SHAPE);
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String getForeignKeyFieldName()
      {
         return ("favoriteShapeId");
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public RelationshipType getRelationshipType()
      {
         return (RelationshipType.PARENT_POINTS_AT_CHILD);
      }
   }

}