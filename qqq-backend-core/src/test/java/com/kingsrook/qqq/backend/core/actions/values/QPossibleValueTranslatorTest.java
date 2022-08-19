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

package com.kingsrook.qqq.backend.core.actions.values;


import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class QPossibleValueTranslatorTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueEnum()
   {
      QInstance                qInstance               = TestUtils.defineInstance();
      QPossibleValueTranslator possibleValueTranslator = new QPossibleValueTranslator(qInstance, new QSession());
      QFieldMetaData           stateField              = qInstance.getTable("person").getField("homeStateId");
      QPossibleValueSource     possibleValueSource     = qInstance.getPossibleValueSource(stateField.getPossibleValueSourceName());

      //////////////////////////////////////////////////////////////////////////
      // assert the default formatting for a not-found value is a null string //
      //////////////////////////////////////////////////////////////////////////
      assertNull(possibleValueTranslator.translatePossibleValue(stateField, null));
      assertNull(possibleValueTranslator.translatePossibleValue(stateField, -1));

      //////////////////////////////////////////////////////////////////////
      // let the not-found value be a simple string (no formatted values) //
      //////////////////////////////////////////////////////////////////////
      possibleValueSource.setValueFormatIfNotFound("?");
      assertEquals("?", possibleValueTranslator.translatePossibleValue(stateField, null));
      assertEquals("?", possibleValueTranslator.translatePossibleValue(stateField, -1));

      /////////////////////////////////////////////////////////////
      // let the not-found value be a string w/ formatted values //
      /////////////////////////////////////////////////////////////
      possibleValueSource.setValueFormatIfNotFound("? (%s)");
      possibleValueSource.setValueFieldsIfNotFound(List.of("id"));
      assertEquals("? ()", possibleValueTranslator.translatePossibleValue(stateField, null));
      assertEquals("? (-1)", possibleValueTranslator.translatePossibleValue(stateField, -1));

      /////////////////////////////////////////////////////
      // assert the default formatting is just the label //
      /////////////////////////////////////////////////////
      assertEquals("MO", possibleValueTranslator.translatePossibleValue(stateField, 2));
      assertEquals("IL", possibleValueTranslator.translatePossibleValue(stateField, 1));

      /////////////////////////////////////////////////////////////////
      // assert the LABEL_ONLY format (when called out specifically) //
      /////////////////////////////////////////////////////////////////
      possibleValueSource.setValueFormat(QPossibleValueSource.ValueFormat.LABEL_ONLY);
      possibleValueSource.setValueFields(QPossibleValueSource.ValueFields.LABEL_ONLY);
      assertEquals("IL", possibleValueTranslator.translatePossibleValue(stateField, 1));

      ///////////////////////////////////////
      // assert the LABEL_PARAMS_ID format //
      ///////////////////////////////////////
      possibleValueSource.setValueFormat(QPossibleValueSource.ValueFormat.LABEL_PARENS_ID);
      possibleValueSource.setValueFields(QPossibleValueSource.ValueFields.LABEL_PARENS_ID);
      assertEquals("IL (1)", possibleValueTranslator.translatePossibleValue(stateField, 1));

      //////////////////////////////////////
      // assert the ID_COLON_LABEL format //
      //////////////////////////////////////
      possibleValueSource.setValueFormat(QPossibleValueSource.ValueFormat.ID_COLON_LABEL);
      possibleValueSource.setValueFields(QPossibleValueSource.ValueFields.ID_COLON_LABEL);
      assertEquals("1: IL", possibleValueTranslator.translatePossibleValue(stateField, 1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPossibleValueTable() throws QException
   {
      QInstance                qInstance               = TestUtils.defineInstance();
      QPossibleValueTranslator possibleValueTranslator = new QPossibleValueTranslator(qInstance, new QSession());
      QTableMetaData           shapeTable              = qInstance.getTable(TestUtils.TABLE_NAME_SHAPE);
      QFieldMetaData           shapeField              = qInstance.getTable(TestUtils.TABLE_NAME_PERSON).getField("favoriteShapeId");
      QPossibleValueSource     possibleValueSource     = qInstance.getPossibleValueSource(shapeField.getPossibleValueSourceName());

      List<QRecord> shapeRecords = List.of(
         new QRecord().withTableName(shapeTable.getName()).withValue("id", 1).withValue("name", "Triangle"),
         new QRecord().withTableName(shapeTable.getName()).withValue("id", 2).withValue("name", "Square"),
         new QRecord().withTableName(shapeTable.getName()).withValue("id", 3).withValue("name", "Circle"));

      InsertInput insertInput = new InsertInput(qInstance);
      insertInput.setSession(new QSession());
      insertInput.setTableName(shapeTable.getName());
      insertInput.setRecords(shapeRecords);
      new InsertAction().execute(insertInput);

      //////////////////////////////////////////////////////////////////////////
      // assert the default formatting for a not-found value is a null string //
      //////////////////////////////////////////////////////////////////////////
      assertNull(possibleValueTranslator.translatePossibleValue(shapeField, null));
      assertNull(possibleValueTranslator.translatePossibleValue(shapeField, -1));

      //////////////////////////////////////////////////////////////////////
      // let the not-found value be a simple string (no formatted values) //
      //////////////////////////////////////////////////////////////////////
      possibleValueSource.setValueFormatIfNotFound("?");
      assertEquals("?", possibleValueTranslator.translatePossibleValue(shapeField, null));
      assertEquals("?", possibleValueTranslator.translatePossibleValue(shapeField, -1));

      /////////////////////////////////////////////////////
      // assert the default formatting is just the label //
      /////////////////////////////////////////////////////
      assertEquals("Square", possibleValueTranslator.translatePossibleValue(shapeField, 2));
      assertEquals("Triangle", possibleValueTranslator.translatePossibleValue(shapeField, 1));

      ///////////////////////////////////////
      // assert the LABEL_PARAMS_ID format //
      ///////////////////////////////////////
      possibleValueSource.setValueFormat(QPossibleValueSource.ValueFormat.LABEL_PARENS_ID);
      possibleValueSource.setValueFields(QPossibleValueSource.ValueFields.LABEL_PARENS_ID);
      assertEquals("Circle (3)", possibleValueTranslator.translatePossibleValue(shapeField, 3));

      ///////////////////////////////////////////////////////////
      // assert that we don't re-run queries for cached values //
      ///////////////////////////////////////////////////////////
      possibleValueTranslator = new QPossibleValueTranslator(qInstance, new QSession());
      MemoryRecordStore.setCollectStatistics(true);
      possibleValueTranslator.translatePossibleValue(shapeField, 1);
      possibleValueTranslator.translatePossibleValue(shapeField, 2);
      assertEquals(2, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN), "Should have ran 2 queries so far");
      possibleValueTranslator.translatePossibleValue(shapeField, 2);
      possibleValueTranslator.translatePossibleValue(shapeField, 3);
      possibleValueTranslator.translatePossibleValue(shapeField, 3);
      assertEquals(3, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN), "Should have ran 3 queries in total");

      ///////////////////////////////////////////////////////////////
      // assert that if we prime the cache, we can do just 1 query //
      ///////////////////////////////////////////////////////////////
      possibleValueTranslator = new QPossibleValueTranslator(qInstance, new QSession());
      List<QRecord> personRecords = List.of(
         new QRecord().withTableName(TestUtils.TABLE_NAME_PERSON).withValue("favoriteShapeId", 1),
         new QRecord().withTableName(TestUtils.TABLE_NAME_PERSON).withValue("favoriteShapeId", 1),
         new QRecord().withTableName(TestUtils.TABLE_NAME_PERSON).withValue("favoriteShapeId", 2),
         new QRecord().withTableName(TestUtils.TABLE_NAME_PERSON).withValue("favoriteShapeId", 2),
         new QRecord().withTableName(TestUtils.TABLE_NAME_PERSON).withValue("favoriteShapeId", 3)
      );
      QTableMetaData personTable = qInstance.getTable(TestUtils.TABLE_NAME_PERSON);
      MemoryRecordStore.resetStatistics();
      possibleValueTranslator.primePvsCache(personTable, personRecords);
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN), "Should only run 1 query");
      possibleValueTranslator.translatePossibleValue(shapeField, 1);
      possibleValueTranslator.translatePossibleValue(shapeField, 2);
      assertEquals(1, MemoryRecordStore.getStatistics().get(MemoryRecordStore.STAT_QUERIES_RAN), "Should only run 1 query");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSetDisplayValuesInRecords()
   {
      QTableMetaData table = new QTableMetaData()
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("firstName", "lastName")
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("price", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("homeStateId", QFieldType.INTEGER).withPossibleValueSourceName(TestUtils.POSSIBLE_VALUE_SOURCE_STATE));

      /////////////////////////////////////////////////////////////////
      // first, make sure it doesn't crash with null or empty inputs //
      /////////////////////////////////////////////////////////////////
      QPossibleValueTranslator possibleValueTranslator = new QPossibleValueTranslator(TestUtils.defineInstance(), new QSession());
      possibleValueTranslator.translatePossibleValuesInRecords(table, null);
      possibleValueTranslator.translatePossibleValuesInRecords(table, Collections.emptyList());

      List<QRecord> records = List.of(
         new QRecord()
            .withValue("firstName", "Tim")
            .withValue("lastName", "Chamberlain")
            .withValue("price", new BigDecimal("3.50"))
            .withValue("homeStateId", 1),
         new QRecord()
            .withValue("firstName", "Tyler")
            .withValue("lastName", "Samples")
            .withValue("price", new BigDecimal("174999.99"))
            .withValue("homeStateId", 2)
      );

      possibleValueTranslator.translatePossibleValuesInRecords(table, records);

      assertNull(records.get(0).getRecordLabel()); // regular display stuff NOT done by PVS translator
      assertNull(records.get(0).getDisplayValue("price"));

      assertEquals("IL", records.get(0).getDisplayValue("homeStateId"));
      assertEquals("MO", records.get(1).getDisplayValue("homeStateId"));
   }

}
