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

package com.kingsrook.qqq.backend.core.processes.implementations.scripts;


import java.io.Serializable;
import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryRecordLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.NoopTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Transform step run-record-script process.  extends no-op, but main purpose is
 ** to set FIELD_VALIDATION_SUMMARY in the output, with the name of the script
 ** you selected.
 *******************************************************************************/
public class RunRecordScriptTransformStep extends NoopTransformStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> processSummary = new ArrayList<>();

      try
      {
         Serializable scriptId = runBackendStepOutput.getValue("scriptId");
         GetInput     getInput = new GetInput();
         getInput.setTableName(Script.TABLE_NAME);
         getInput.setPrimaryKey(scriptId);
         GetOutput getOutput = new GetAction().execute(getInput);
         if(getOutput.getRecord() != null)
         {
            processSummary.add(new ProcessSummaryRecordLink(Status.OK, Script.TABLE_NAME, scriptId, getOutput.getRecord().getValueString("name"))
               .withLinkPreText(StringUtils.plural(runBackendStepOutput.getRecords(), "It", "They") + " will have the script ")
               .withLinkPostText(" ran against " + StringUtils.plural(runBackendStepOutput.getRecords(), "it.", "them.")));
         }
         else
         {
            processSummary.add(new ProcessSummaryLine(Status.ERROR, null, "The selected script could not be found."));
         }
      }
      catch(Exception e)
      {
         processSummary.add(new ProcessSummaryLine(Status.ERROR, null, "Error getting the script: " + e.getMessage()));
      }

      return (processSummary);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.runOnePage(runBackendStepInput, runBackendStepOutput);

      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY, doGetProcessSummary(runBackendStepOutput, false));
   }

}
