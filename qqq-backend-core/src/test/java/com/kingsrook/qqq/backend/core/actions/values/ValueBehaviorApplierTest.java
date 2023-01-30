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


import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for ValueBehaviorApplier
 *******************************************************************************/
class ValueBehaviorApplierTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValueTooLongNormalCases()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);
      table.getField("firstName").withMaxLength(10).withBehavior(ValueTooLongBehavior.TRUNCATE);
      table.getField("lastName").withMaxLength(10).withBehavior(ValueTooLongBehavior.TRUNCATE_ELLIPSIS);
      table.getField("email").withMaxLength(20).withBehavior(ValueTooLongBehavior.ERROR);

      List<QRecord> recordList = List.of(
         new QRecord().withValue("id", 1).withValue("firstName", "First name too long").withValue("lastName", "Smith").withValue("email", "john@smith.com"),
         new QRecord().withValue("id", 2).withValue("firstName", "John").withValue("lastName", "Last name too long").withValue("email", "john@smith.com"),
         new QRecord().withValue("id", 3).withValue("firstName", "First name too long").withValue("lastName", "Smith").withValue("email", "john.smith@emaildomainwayytolongtofit.com")
      );
      ValueBehaviorApplier.applyFieldBehaviors(qInstance, table, recordList);

      assertEquals("First name", getRecordById(recordList, 1).getValueString("firstName"));
      assertEquals("Last na...", getRecordById(recordList, 2).getValueString("lastName"));
      assertEquals("john.smith@emaildomainwayytolongtofit.com", getRecordById(recordList, 3).getValueString("email"));
      assertFalse(getRecordById(recordList, 3).getErrors().isEmpty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValueTooLongEdgeCases()
   {
      QInstance      qInstance = QContext.getQInstance();
      QTableMetaData table     = qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // make sure PASS THROUGH actually does nothing, and that a maxLength w/ no behavior specified also does nothing (e.g., does PASS_THROUGH) //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      table.getField("firstName").withMaxLength(10).withBehavior(ValueTooLongBehavior.PASS_THROUGH);
      table.getField("lastName").withMaxLength(10);

      List<QRecord> recordList = List.of(
         ////////////////////////////////////////////////////////////////
         // make sure nulls and empty are okay, and don't get changed. //
         ////////////////////////////////////////////////////////////////
         new QRecord().withValue("id", 1).withValue("firstName", "First name too long").withValue("lastName", null).withValue("email", "john@smith.com"),
         new QRecord().withValue("id", 2).withValue("firstName", "").withValue("lastName", "Last name too long").withValue("email", "john@smith.com")
      );
      ValueBehaviorApplier.applyFieldBehaviors(qInstance, table, recordList);

      assertEquals("First name too long", getRecordById(recordList, 1).getValueString("firstName"));
      assertNull(getRecordById(recordList, 1).getValueString("lastName"));
      assertEquals("Last name too long", getRecordById(recordList, 2).getValueString("lastName"));
      assertEquals("", getRecordById(recordList, 2).getValueString("firstName"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord getRecordById(List<QRecord> recordList, Integer id)
   {
      Optional<QRecord> recordOpt = recordList.stream().filter(r -> r.getValueInteger("id").equals(id)).findFirst();
      if(recordOpt.isEmpty())
      {
         fail("Didn't find record with id=" + id);
      }
      return (recordOpt.get());
   }

}