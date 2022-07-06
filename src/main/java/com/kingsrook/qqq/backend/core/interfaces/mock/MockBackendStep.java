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

package com.kingsrook.qqq.backend.core.interfaces.mock;


import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;


/*******************************************************************************
 ** Mock implementation of a FunctionBody.
 **
 ** Basically just passes data from the request to the response.
 *******************************************************************************/
public class MockBackendStep implements BackendStep
{
   public final static String FIELD_GREETING_PREFIX = "greetingPrefix";
   public final static String FIELD_GREETING_SUFFIX = "greetingSuffix";

   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult)
   {
      runBackendStepResult.getRecords().forEach(r -> r.setValue("mockValue", "Ha ha!"));

      runBackendStepResult.setValues(runBackendStepRequest.getValues());
      runBackendStepResult.addValue("mockValue", "You so silly");

      /////////////////////////////////
      // mock the "greet" process... //
      /////////////////////////////////
      runBackendStepResult.addValue("outputMessage", runBackendStepRequest.getValueString(FIELD_GREETING_PREFIX) + " X " + runBackendStepRequest.getValueString(FIELD_GREETING_SUFFIX));
   }
}
