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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Backend step to receive values for a bulk edit.
 *******************************************************************************/
public class BulkEditReceiveValuesStep implements BackendStep
{
   public static final String FIELD_ENABLED_FIELDS       = "bulkEditEnabledFields";
   public static final String FIELD_VALUES_BEING_UPDATED = "valuesBeingUpdated";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String   enabledFieldsString = runBackendStepInput.getValueString(FIELD_ENABLED_FIELDS);
      String[] enabledFields       = enabledFieldsString.split(",");

      ////////////////////////////////////////////////////////////////////////////////////////////
      // put the value in all the records (note, this is just for display on the review screen, //
      // and/or if we wanted to do some validation - this is NOT what will be store, as the     //
      // Update action only wants fields that are being changed.                                //
      ////////////////////////////////////////////////////////////////////////////////////////////
      for(QRecord record : runBackendStepInput.getRecords())
      {
         for(String fieldName : enabledFields)
         {
            Serializable value = runBackendStepInput.getValue(fieldName);
            record.setValue(fieldName, value);
         }
      }

      /////////////////////////////////////////////////////////////////////
      // build the string to show the user what fields are being changed //
      /////////////////////////////////////////////////////////////////////
      List<String>   valuesBeingUpdated = new ArrayList<>();
      QTableMetaData table              = runBackendStepInput.getTable();
      for(String fieldName : enabledFields)
      {
         String       label = table.getField(fieldName).getLabel();
         Serializable value = runBackendStepInput.getValue(fieldName);

         if(StringUtils.hasContent(ValueUtils.getValueAsString(value)))
         {
            valuesBeingUpdated.add(label + " will be set to: " + value);
         }
         else
         {
            valuesBeingUpdated.add(label + " will be cleared out.");
         }
      }
      runBackendStepOutput.addValue(FIELD_VALUES_BEING_UPDATED, String.join("\n", valuesBeingUpdated));

      runBackendStepOutput.setRecords(runBackendStepInput.getRecords());
   }
}
