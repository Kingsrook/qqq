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

package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertOrUpdateStep
 *******************************************************************************/
class LoadViaInsertOrUpdateStepTest extends BaseTest
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
   void test() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      List<QRecord> existingRecordList = List.of(
         new QRecord().withValue("id", 47).withValue("firstName", "Tom")
      );
      TestUtils.insertRecords(qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY), existingRecordList);

      List<QRecord> inputRecordList = List.of(
         new QRecord().withValue("id", 47).withValue("firstName", "Tim"),
         new QRecord().withValue("firstName", "John")
      );
      RunBackendStepInput input = new RunBackendStepInput();
      input.setRecords(inputRecordList);
      input.addValue(LoadViaInsertOrUpdateStep.FIELD_DESTINATION_TABLE, TestUtils.TABLE_NAME_PERSON_MEMORY);
      RunBackendStepOutput output = new RunBackendStepOutput();
      new LoadViaInsertOrUpdateStep().runOnePage(input, output);

      List<QRecord> qRecords = TestUtils.queryTable(qInstance, TestUtils.TABLE_NAME_PERSON_MEMORY);
      assertEquals(2, qRecords.size());
   }

}