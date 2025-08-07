/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.util.concurrent.atomic.AtomicBoolean;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ExtractViaQueryStep 
 *******************************************************************************/
class ExtractViaQueryStepTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCustomizeInputPreQuery() throws QException
   {
      AtomicBoolean called = new AtomicBoolean(false);

      RunBackendStepInput input = new RunBackendStepInput();
      input.addValue(StreamedETLWithFrontendProcess.FIELD_SOURCE_TABLE, TestUtils.TABLE_NAME_PERSON_MEMORY);
      input.addValue(StreamedETLWithFrontendProcess.FIELD_DEFAULT_QUERY_FILTER, "{}");
      input.setCallback(new QProcessCallback()
      {
         /***************************************************************************
          *
          ***************************************************************************/
         @Override
         public void customizeInputPreQuery(RunBackendStepInput runBackendStepInput, QueryInput queryInput)
         {
            called.set(true);
         }
      });

      RunBackendStepOutput output = new RunBackendStepOutput();
      ExtractViaQueryStep  extractViaQueryStep = new ExtractViaQueryStep();
      extractViaQueryStep.preRun(input, output);
      extractViaQueryStep.run(input, output);

      assertTrue(called.get());
   }
}