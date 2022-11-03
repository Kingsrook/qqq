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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.reporting.QReportMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Version of PrepareReportStep for a report that runs off a single record.
 *******************************************************************************/
public class PrepareReportForRecordStep extends PrepareReportStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.run(runBackendStepInput, runBackendStepOutput);

      //////////////////////////////////////////////////////////////////////////////////
      // look for the recordId having been posted to the process - error if not found //
      //////////////////////////////////////////////////////////////////////////////////
      Serializable recordId = null;
      if("recordIds".equals(runBackendStepInput.getValueString("recordsParam")))
      {
         String   recordIdsString = runBackendStepInput.getValueString("recordIds");
         String[] recordIdsArray  = recordIdsString.split(",");
         if(recordIdsArray.length != 1)
         {
            throw (new QUserFacingException("Exactly 1 record must be selected as input to this report."));
         }

         recordId = recordIdsArray[0];
      }
      else
      {
         throw (new QUserFacingException("No record was selected as input to this report."));
      }

      /////////////////////////////////////////////////////////////////////////////////////////////
      // look for the recordI input field on the process - put the input recordId in that field. //
      // then remove that input field from the process's inputFieldList                          //
      /////////////////////////////////////////////////////////////////////////////////////////////
      @SuppressWarnings("unchecked")
      ArrayList<QFieldMetaData> inputFieldList = (ArrayList<QFieldMetaData>) runBackendStepOutput.getValue("inputFieldList");
      if(CollectionUtils.nullSafeHasContents(inputFieldList))
      {
         Iterator<QFieldMetaData> inputFieldListIterator = inputFieldList.iterator();
         while(inputFieldListIterator.hasNext())
         {
            QFieldMetaData fieldMetaData = inputFieldListIterator.next();
            if(fieldMetaData.getName().equals(RunReportForRecordProcess.FIELD_RECORD_ID))
            {
               runBackendStepOutput.addValue(RunReportForRecordProcess.FIELD_RECORD_ID, recordId);
               inputFieldListIterator.remove();
               runBackendStepOutput.addValue("inputFieldList", inputFieldList);
               break;
            }
         }
      }

      GetInput getInput = new GetInput(runBackendStepInput.getInstance());
      getInput.setSession(runBackendStepInput.getSession());
      getInput.setTableName(runBackendStepInput.getTableName());
      getInput.setPrimaryKey(recordId);
      getInput.setShouldGenerateDisplayValues(true);
      GetOutput getOutput = new GetAction().execute(getInput);
      QRecord   record    = getOutput.getRecord();
      if(record == null)
      {
         throw (new QUserFacingException("The selected record for the report was not found."));
      }

      String          reportName = runBackendStepInput.getValueString("reportName");
      QReportMetaData report     = runBackendStepInput.getInstance().getReport(reportName);
      // runBackendStepOutput.addValue("downloadFileBaseName", runBackendStepInput.getTable().getLabel() + " " + record.getRecordLabel());
      runBackendStepOutput.addValue("downloadFileBaseName", report.getLabel() + " - " + record.getRecordLabel());

      /////////////////////////////////////////////////////////////////////////////////////
      // if there are no more input fields, then remove the INPUT step from the process. //
      /////////////////////////////////////////////////////////////////////////////////////
      inputFieldList = (ArrayList<QFieldMetaData>) runBackendStepOutput.getValue("inputFieldList");
      if(!CollectionUtils.nullSafeHasContents(inputFieldList))
      {
         removeInputStepFromProcess(runBackendStepOutput);
      }
   }

}
