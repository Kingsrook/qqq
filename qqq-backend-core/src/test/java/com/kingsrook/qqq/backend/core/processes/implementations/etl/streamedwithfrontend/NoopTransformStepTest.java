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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for NoopTransformStep
 *******************************************************************************/
class NoopTransformStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      RunBackendStepInput input = new RunBackendStepInput();
      input.setTableName(TestUtils.TABLE_NAME_PERSON);
      input.setRecords(List.of(new QRecord().withValue("id", 47)));

      RunBackendStepOutput output = new RunBackendStepOutput();

      NoopTransformStep noopTransformStep = new NoopTransformStep();
      noopTransformStep.runOnePage(input, output);

      assertEquals(1, output.getRecords().size());
      assertEquals(47, output.getRecords().get(0).getValueInteger("id"));

      ArrayList<ProcessSummaryLineInterface> processSummary = noopTransformStep.getProcessSummary(output, false);
      assertEquals(1, processSummary.size());
      ProcessSummaryLineInterface processSummaryLineInterface = processSummary.get(0);
      assertEquals(Status.OK, processSummaryLineInterface.getStatus());
      if(processSummaryLineInterface instanceof ProcessSummaryLine processSummaryLine)
      {
         assertEquals(1, processSummaryLine.getCount());
         assertEquals(1, processSummaryLine.getPrimaryKeys().size());
         assertEquals(47, processSummaryLine.getPrimaryKeys().get(0));
      }
   }

}