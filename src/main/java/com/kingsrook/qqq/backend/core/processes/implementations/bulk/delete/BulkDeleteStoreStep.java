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


import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Backend step to do a bulk delete.
 *******************************************************************************/
public class BulkDeleteStoreStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      runBackendStepInput.getAsyncJobCallback().updateStatus("Deleting records in database...");
      runBackendStepInput.getAsyncJobCallback().clearCurrentAndTotal();

      DeleteInput deleteInput = new DeleteInput(runBackendStepInput.getInstance());
      deleteInput.setSession(runBackendStepInput.getSession());
      deleteInput.setAsyncJobCallback(runBackendStepInput.getAsyncJobCallback());
      deleteInput.setTableName(runBackendStepInput.getTableName());

      String queryFilterJSON = runBackendStepInput.getValueString("queryFilterJSON");
      if(StringUtils.hasContent(queryFilterJSON))
      {
         try
         {
            deleteInput.setQueryFilter(JsonUtils.toObject(queryFilterJSON, QQueryFilter.class));
         }
         catch(IOException e)
         {
            throw (new QException("Error loading record query filter from process", e));
         }
      }
      else if(CollectionUtils.nullSafeHasContents(runBackendStepInput.getRecords()))
      {
         String primaryKeyField = runBackendStepInput.getTable().getPrimaryKeyField();
         List<Serializable> primaryKeyList = runBackendStepInput.getRecords().stream()
            .map(r -> r.getValue(primaryKeyField))
            .toList();
         deleteInput.setPrimaryKeys(primaryKeyList);
      }

      DeleteAction deleteAction = new DeleteAction();
      DeleteOutput deleteOutput = deleteAction.execute(deleteInput);

      // todo - something with the output!!
      deleteOutput.getRecordsWithErrors();

      runBackendStepOutput.setRecords(runBackendStepInput.getRecords());
   }

}
