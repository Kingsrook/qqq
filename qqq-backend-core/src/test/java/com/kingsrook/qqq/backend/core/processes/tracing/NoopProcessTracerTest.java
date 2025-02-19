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

package com.kingsrook.qqq.backend.core.processes.tracing;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for NoopProcessTracer ... kinda a BS test, but here to prevent
 ** a missing-class for code coverage...
 *******************************************************************************/
class NoopProcessTracerTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      //////////////////////////////////////////////////////////
      // activate the noop tracer for this run of the process //
      //////////////////////////////////////////////////////////
      RunProcessInput input = new RunProcessInput();
      input.addValue(RunProcessAction.PROCESS_TRACER_CODE_REFERENCE_FIELD, new QCodeReference(NoopProcessTracer.class));

      input.setProcessName(TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.BREAK);
      input.setCallback(QProcessCallbackFactory.forRecord(new QRecord().withValue("id", 1)));
      new RunProcessAction().execute(input);
   }

}