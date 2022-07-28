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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.delete;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for BulkDeleteStoreStep
 *******************************************************************************/
class BulkDeleteStoreStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutFilter() throws QException
   {
      RunBackendStepInput stepInput = new RunBackendStepInput(TestUtils.defineInstance());
      stepInput.setSession(TestUtils.getMockSession());
      stepInput.setTableName(TestUtils.defineTablePerson().getName());
      stepInput.setRecords(TestUtils.queryTable(TestUtils.defineTablePerson().getName()));

      RunBackendStepOutput stepOutput = new RunBackendStepOutput();
      new BulkDeleteStoreStep().run(stepInput, stepOutput);
      assertEquals(0, stepOutput.getValueInteger(BulkDeleteStoreStep.ERROR_COUNT));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithFilter() throws QException
   {
      RunBackendStepInput stepInput = new RunBackendStepInput(TestUtils.defineInstance());
      stepInput.setSession(TestUtils.getMockSession());
      stepInput.setTableName(TestUtils.defineTablePerson().getName());
      stepInput.addValue("queryFilterJSON", JsonUtils.toJson(new QQueryFilter()));

      RunBackendStepOutput stepOutput = new RunBackendStepOutput();
      new BulkDeleteStoreStep().run(stepInput, stepOutput);
      assertEquals(0, stepOutput.getValueInteger(BulkDeleteStoreStep.ERROR_COUNT));
   }

}