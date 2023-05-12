/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RecordCustomizerUtilityInterface 
 *******************************************************************************/
class RecordCustomizerUtilityInterfaceTest extends BaseTest implements RecordCustomizerUtilityInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetChanges()
   {
      Map<String, Change> changes = getChanges(TestUtils.TABLE_NAME_PERSON_MEMORY,
         new QRecord().withValue("id", 1).withValue("firstName", "Homer"),
         new QRecord().withValue("id", 2).withValue("firstName", "Homer"));

      assertEquals(1, changes.size());
      assertEquals(new Change(1, 2), changes.get("id"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testErrorIfNoValue()
   {
      {
         QRecord record = new QRecord();
         errorIfNoValue(null, record, "no value");
         assertEquals(1, record.getErrors().size());
         assertEquals("no value", record.getErrors().get(0).getMessage());
      }
      {
         QRecord record = new QRecord();
         errorIfNoValue("", record, "no value");
         assertEquals(1, record.getErrors().size());
         assertEquals("no value", record.getErrors().get(0).getMessage());
      }
      {
         QRecord record = new QRecord();
         errorIfNoValue("hi", record, "no value");
         assertEquals(0, record.getErrors().size());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testErrorIfAnyValue()
   {
      {
         QRecord record = new QRecord();
         errorIfAnyValue(null, record, "any value");
         assertEquals(0, record.getErrors().size());
      }
      {
         QRecord record = new QRecord();
         errorIfAnyValue("", record, "any value");
         assertEquals(0, record.getErrors().size());
      }
      {
         QRecord record = new QRecord();
         errorIfAnyValue("hi", record, "any value");
         assertEquals(1, record.getErrors().size());
         assertEquals("any value", record.getErrors().get(0).getMessage());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testErrorIfEditedValue()
   {
      {
         QRecord oldRecord = new QRecord().withValue("id", 1).withValue("firstName", "Homer");
         QRecord newRecord = new QRecord().withValue("id", 1).withValue("firstName", "Marge");
         errorIfEditedValue(oldRecord, newRecord, "firstName", "changed firstName");
         assertEquals(1, newRecord.getErrors().size());
         assertEquals("changed firstName", newRecord.getErrors().get(0).getMessage());
      }
      {
         QRecord oldRecord = new QRecord().withValue("id", 1).withValue("firstName", "Homer");
         QRecord newRecord = new QRecord().withValue("id", 1).withValue("firstName", "Homer");
         errorIfEditedValue(oldRecord, newRecord, "firstName", "changed firstName");
         assertEquals(0, newRecord.getErrors().size());
      }
      {
         QRecord oldRecord = new QRecord().withValue("id", 1).withValue("firstName", "Homer");
         QRecord newRecord = new QRecord().withValue("id", 1);
         errorIfEditedValue(oldRecord, newRecord, "firstName", "changed firstName");
         assertEquals(0, newRecord.getErrors().size());
      }
   }

}