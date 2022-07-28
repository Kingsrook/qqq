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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for BulkEditStoreRecordsStep
 *******************************************************************************/
class BulkEditStoreRecordsStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      RunBackendStepInput stepInput = new RunBackendStepInput(TestUtils.defineInstance());
      stepInput.setSession(TestUtils.getMockSession());
      stepInput.setTableName(TestUtils.defineTablePerson().getName());
      stepInput.addValue(BulkEditReceiveValuesStep.FIELD_ENABLED_FIELDS, "firstName,email,birthDate");
      stepInput.addValue("firstName", "Johnny");
      stepInput.addValue("email", null);
      stepInput.addValue("birthDate", "1909-01-09");
      List<QRecord> records = TestUtils.queryTable(TestUtils.defineTablePerson().getName());
      stepInput.setRecords(records);

      RunBackendStepOutput stepOutput = new RunBackendStepOutput();
      new BulkEditStoreRecordsStep().run(stepInput, stepOutput);

      assertRecordValues(stepOutput.getRecords());

      // re-fetch the records, make sure they are updated.
      // but since Mock backend doesn't actually update them, we can't do this..
      // todo - implement an in-memory backend, that would do this.
      // List<QRecord> updatedRecords = TestUtils.queryTable(TestUtils.defineTablePerson().getName());
      // assertRecordValues(updatedRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertRecordValues(List<QRecord> records)
   {
      for(QRecord record : records)
      {
         assertEquals("Johnny", record.getValueString("firstName"));
         assertNull(record.getValue("email"));
         // todo value utils needed in getValueDate... assertEquals(LocalDate.of(1909, 1, 9), record.getValueDate("birthDate"));
      }
   }

}