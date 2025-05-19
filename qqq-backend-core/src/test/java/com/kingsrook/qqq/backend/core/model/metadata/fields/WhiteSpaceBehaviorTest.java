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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for WhiteSpaceBehavior
 *******************************************************************************/
class WhiteSpaceBehaviorTest extends BaseTest
{
   public static final String FIELD = "firstName";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNone()
   {
      assertNull(applyToRecord(WhiteSpaceBehavior.NONE, new QRecord(), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertNull(applyToRecord(WhiteSpaceBehavior.NONE, new QRecord().withValue(FIELD, null), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertEquals("John", applyToRecord(WhiteSpaceBehavior.NONE, new QRecord().withValue(FIELD, "John"), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));

      assertEquals(ListBuilder.of("J.   ohn", null, "Jane\n"), applyToRecords(WhiteSpaceBehavior.NONE, List.of(
            new QRecord().withValue(FIELD, "J.   ohn"),
            new QRecord(),
            new QRecord().withValue(FIELD, "Jane\n")),
         ValueBehaviorApplier.Action.INSERT).stream().map(r -> r.getValueString(FIELD)).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRemoveWhiteSpace()
   {
      assertNull(applyToRecord(WhiteSpaceBehavior.REMOVE_ALL_WHITESPACE, new QRecord(), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertNull(applyToRecord(WhiteSpaceBehavior.REMOVE_ALL_WHITESPACE, new QRecord().withValue(FIELD, null), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertEquals("doobeedoobeedoo", applyToRecord(WhiteSpaceBehavior.REMOVE_ALL_WHITESPACE, new QRecord().withValue(FIELD, "doo bee doo\n bee doo"), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));

      assertEquals(ListBuilder.of("thisistheway", null, "thatwastheway"), applyToRecords(WhiteSpaceBehavior.REMOVE_ALL_WHITESPACE, List.of(
            new QRecord().withValue(FIELD, "this is\rthe way   \t"),
            new QRecord(),
            new QRecord().withValue(FIELD, "that was the way\n")),
         ValueBehaviorApplier.Action.INSERT).stream().map(r -> r.getValueString(FIELD)).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTrimWhiteSpace()
   {
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM, new QRecord(), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM, new QRecord().withValue(FIELD, null), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertEquals("doo bee doo\n bee doo", applyToRecord(WhiteSpaceBehavior.TRIM, new QRecord().withValue(FIELD, "    doo bee doo\n bee doo\r \n\n"), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));

      assertEquals(ListBuilder.of("this is\rthe way", null, "that was the way"), applyToRecords(WhiteSpaceBehavior.TRIM, List.of(
            new QRecord().withValue(FIELD, "      this is\rthe way   \t"),
            new QRecord(),
            new QRecord().withValue(FIELD, "that was the way\n")),
         ValueBehaviorApplier.Action.INSERT).stream().map(r -> r.getValueString(FIELD)).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTrimLeftWhiteSpace()
   {
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM_LEFT, new QRecord(), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM_LEFT, new QRecord().withValue(FIELD, null), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertEquals("doo bee doo\n bee doo\r \n\n", applyToRecord(WhiteSpaceBehavior.TRIM_LEFT, new QRecord().withValue(FIELD, "    doo bee doo\n bee doo\r \n\n"), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));

      assertEquals(ListBuilder.of("this is\rthe way   \t", null, "that was the way\n"), applyToRecords(WhiteSpaceBehavior.TRIM_LEFT, List.of(
            new QRecord().withValue(FIELD, "      this is\rthe way   \t"),
            new QRecord(),
            new QRecord().withValue(FIELD, "  \n that was the way\n")),
         ValueBehaviorApplier.Action.INSERT).stream().map(r -> r.getValueString(FIELD)).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testTrimRightWhiteSpace()
   {
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM_RIGHT, new QRecord(), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertNull(applyToRecord(WhiteSpaceBehavior.TRIM_RIGHT, new QRecord().withValue(FIELD, null), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));
      assertEquals("    doo bee doo\n bee doo", applyToRecord(WhiteSpaceBehavior.TRIM_RIGHT, new QRecord().withValue(FIELD, "    doo bee doo\n bee doo\r \n\n"), ValueBehaviorApplier.Action.INSERT).getValue(FIELD));

      assertEquals(ListBuilder.of("      this is\rthe way", null, "  \n that was the way"), applyToRecords(WhiteSpaceBehavior.TRIM_RIGHT, List.of(
            new QRecord().withValue(FIELD, "      this is\rthe way   \t"),
            new QRecord(),
            new QRecord().withValue(FIELD, "  \n that was the way\n")),
         ValueBehaviorApplier.Action.INSERT).stream().map(r -> r.getValueString(FIELD)).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QRecord applyToRecord(WhiteSpaceBehavior behavior, QRecord record, ValueBehaviorApplier.Action action)
   {
      return (applyToRecords(behavior, List.of(record), action).get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> applyToRecords(WhiteSpaceBehavior behavior, List<QRecord> records, ValueBehaviorApplier.Action action)
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      behavior.apply(action, records, QContext.getQInstance(), table, table.getField(FIELD));
      return (records);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidation()
   {
      QTableMetaData table = QContext.getQInstance().getTable(TestUtils.TABLE_NAME_SHAPE);

      ///////////////////////////////////////////
      // should be no errors on a string field //
      ///////////////////////////////////////////
      assertTrue(WhiteSpaceBehavior.TRIM.validateBehaviorConfiguration(table, table.getField("name")).isEmpty());

      //////////////////////////////////////////
      // should be an error on a number field //
      //////////////////////////////////////////
      assertEquals(1, WhiteSpaceBehavior.REMOVE_ALL_WHITESPACE.validateBehaviorConfiguration(table, table.getField("id")).size());

      /////////////////////////////////////////
      // NONE should be allowed on any field //
      /////////////////////////////////////////
      assertTrue(WhiteSpaceBehavior.NONE.validateBehaviorConfiguration(table, table.getField("id")).isEmpty());
   }

}
