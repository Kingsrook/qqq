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
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 ** Backend step to store the records from a bulk insert file
 *******************************************************************************/
public class BulkEditStoreRecordsStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String         enabledFieldsString = runBackendStepInput.getValueString(BulkEditReceiveValuesStep.FIELD_ENABLED_FIELDS);
      String[]       enabledFields       = enabledFieldsString.split(",");
      QTableMetaData table               = runBackendStepInput.getTable();
      List<QRecord>  recordsToUpdate     = new ArrayList<>();

      runBackendStepInput.getAsyncJobCallback().updateStatus("Updating values in records...");
      int i = 1;
      for(QRecord record : runBackendStepInput.getRecords())
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus(i++, runBackendStepInput.getRecords().size());
         QRecord recordToUpdate = new QRecord();
         recordsToUpdate.add(recordToUpdate);

         recordToUpdate.setValue(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
         for(String fieldName : enabledFields)
         {
            Serializable value = runBackendStepInput.getValue(fieldName);
            recordToUpdate.setValue(fieldName, value);
         }
      }

      runBackendStepInput.getAsyncJobCallback().updateStatus("Updating database...");
      runBackendStepInput.getAsyncJobCallback().clearCurrentAndTotal();

      UpdateInput updateInput = new UpdateInput(runBackendStepInput.getInstance());
      updateInput.setSession(runBackendStepInput.getSession());
      updateInput.setAsyncJobCallback(runBackendStepInput.getAsyncJobCallback());
      updateInput.setAreAllValuesBeingUpdatedTheSame(true);
      updateInput.setTableName(runBackendStepInput.getTableName());
      updateInput.setRecords(recordsToUpdate);

      UpdateAction updateAction = new UpdateAction();
      UpdateOutput updateOutput = updateAction.execute(updateInput);

      runBackendStepOutput.setRecords(updateOutput.getRecords());
   }

}
