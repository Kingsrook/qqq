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

package com.kingsrook.qqq.backend.core.processes.implementations.reports;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Process step to prepare for running a report.
 **
 ** Checks for input fields - if there are any, it puts them in process value output
 ** as inputFieldList (QFieldMetaData objects).
 ** If there aren't any input fields, re-routes the process to skip the input screen.
 *******************************************************************************/
public class PrepareReportStep implements BackendStep
{
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String reportName = runBackendStepInput.getValueString("reportName");
      if(!StringUtils.hasContent(reportName))
      {
         throw (new QException("Process value [reportName] was not given."));
      }

      QReportMetaData report = runBackendStepInput.getInstance().getReport(reportName);
      if(report == null)
      {
         throw (new QException("Process named [" + reportName + "] was not found in this instance."));
      }

      /////////////////////////////////////////////////////////////////
      // if there are input fields, communicate them to the frontend //
      /////////////////////////////////////////////////////////////////
      if(CollectionUtils.nullSafeHasContents(report.getInputFields()))
      {
         ArrayList<QFieldMetaData> inputFieldList = new ArrayList<>(report.getInputFields());
         runBackendStepOutput.addValue("inputFieldList", inputFieldList);
      }
      else
      {
         //////////////////////////////////////////////////////////////
         // no input?  re-route the process to skip the input screen //
         //////////////////////////////////////////////////////////////
         List<String> stepList = new ArrayList<>(runBackendStepOutput.getProcessState().getStepList());
         stepList.removeIf(s -> s.equals(BasicRunReportProcess.STEP_NAME_INPUT));
         runBackendStepOutput.getProcessState().setStepList(stepList);
      }
   }
}
