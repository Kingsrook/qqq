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

package com.kingsrook.qqq.backend.core.model.data;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.data.QRecord.BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS;
import static com.kingsrook.qqq.backend.core.model.data.QRecord.BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;


/*******************************************************************************
 ** Unit test for QRecord 
 *******************************************************************************/
class QRecordTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCopyConstructor()
   {
      String jsonValue = """
         {"key": [1,2]}
         """;
      Map<String, Integer> fieldLengths = MapBuilder.of("a", 1, "b", 2);

      QRecord original = new QRecord()
         .withTableName("myTable")
         .withRecordLabel("My Record")
         .withValue("one", 1)
         .withValue("two", "two")
         .withValue("three", new BigDecimal("3"))
         .withValue("false", false)
         .withValue("empty", null)
         .withDisplayValue("three", "3.00")
         .withBackendDetail(BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT, jsonValue)
         .withBackendDetail(BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS, new HashMap<>(fieldLengths))
         .withError(new BadInputStatusMessage("Bad Input"))
         .withAssociatedRecord("child", new QRecord().withValue("id", "child1"))
         .withAssociatedRecord("child", new QRecord().withValue("id", "child2"))
         .withAssociatedRecord("nephew", new QRecord().withValue("id", "nephew1"));

      QRecord clone = new QRecord(original);

      //////////////////////////////////////////////////////////////
      // assert equality on all the members values in the records //
      //////////////////////////////////////////////////////////////
      assertEquals("myTable", clone.getTableName());
      assertEquals("My Record", clone.getRecordLabel());
      assertEquals(1, clone.getValue("one"));
      assertEquals("two", clone.getValue("two"));
      assertEquals(new BigDecimal("3"), clone.getValue("three"));
      assertEquals(false, clone.getValue("false"));
      assertNull(clone.getValue("empty"));
      assertEquals("3.00", clone.getDisplayValue("three"));
      assertEquals(jsonValue, clone.getBackendDetail(BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT));
      assertEquals(fieldLengths, clone.getBackendDetail(BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS));
      assertEquals(1, clone.getErrors().size());
      assertEquals(BadInputStatusMessage.class, clone.getErrors().get(0).getClass());
      assertEquals("Bad Input", clone.getErrors().get(0).getMessage());
      assertEquals(0, clone.getWarnings().size());
      assertEquals(2, clone.getAssociatedRecords().size());
      assertEquals(2, clone.getAssociatedRecords().get("child").size());
      assertEquals("child1", clone.getAssociatedRecords().get("child").get(0).getValue("id"));
      assertEquals("child2", clone.getAssociatedRecords().get("child").get(1).getValue("id"));
      assertEquals(1, clone.getAssociatedRecords().get("nephew").size());
      assertEquals("nephew1", clone.getAssociatedRecords().get("nephew").get(0).getValue("id"));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // make sure the associated record data structures are not the same (e.g., not the same map & lists) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      assertNotSame(clone.getAssociatedRecords(), original.getAssociatedRecords());
      assertNotSame(clone.getAssociatedRecords().get("child"), original.getAssociatedRecords().get("child"));

      /////////////////////////////////////////////////////////////////////////////////////
      // but we'll be okay with the same records inside the associated records structure //
      /////////////////////////////////////////////////////////////////////////////////////
      assertSame(clone.getAssociatedRecords().get("child").get(0), original.getAssociatedRecords().get("child").get(0));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCopyConstructorEdgeCases()
   {
      QRecord nullValuesRecord = new QRecord();
      nullValuesRecord.setValues(null);
      assertNull(new QRecord(nullValuesRecord).getValues());

      QRecord nullDisplayValuesRecord = new QRecord();
      nullDisplayValuesRecord.setDisplayValues(null);
      assertNull(new QRecord(nullDisplayValuesRecord).getDisplayValues());

      QRecord nullBackendDetailsRecord = new QRecord();
      nullBackendDetailsRecord.setBackendDetails(null);
      assertNull(new QRecord(nullBackendDetailsRecord).getBackendDetails());

      QRecord nullAssociations = new QRecord();
      nullAssociations.setAssociatedRecords(null);
      assertNull(new QRecord(nullAssociations).getAssociatedRecords());

      QRecord nullErrors = new QRecord();
      nullErrors.setErrors(null);
      assertNull(new QRecord(nullErrors).getErrors());

      QRecord nullWarnings = new QRecord();
      nullWarnings.setWarnings(null);
      assertNull(new QRecord(nullWarnings).getWarnings());

      QRecord emptyRecord = new QRecord();
      QRecord emptyClone  = new QRecord(emptyRecord);
      assertNull(emptyClone.getTableName());
      assertNull(emptyClone.getRecordLabel());
      assertEquals(0, emptyClone.getValues().size());
      assertEquals(0, emptyClone.getDisplayValues().size());
      assertEquals(0, emptyClone.getBackendDetails().size());
      assertEquals(0, emptyClone.getErrors().size());
      assertEquals(0, emptyClone.getWarnings().size());
      assertEquals(0, emptyClone.getAssociatedRecords().size());
   }

}