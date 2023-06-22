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

package com.kingsrook.qqq.backend.core.processes.implementations.savedfilters;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.savedfilters.SavedFilter;


/*******************************************************************************
 ** Process used by the saved filter dialog
 *******************************************************************************/
public class StoreSavedFilterProcess implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(StoreSavedFilterProcess.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QProcessMetaData getProcessMetaData()
   {
      return (new QProcessMetaData()
         .withName("storeSavedFilter")
         .withStepList(List.of(
            new QBackendStepMetaData()
               .withCode(new QCodeReference(StoreSavedFilterProcess.class))
               .withName("store")
         )));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ActionHelper.validateSession(runBackendStepInput);

      try
      {
         QRecord qRecord = new QRecord()
            .withValue("id", runBackendStepInput.getValueInteger("id"))
            .withValue("label", runBackendStepInput.getValueString("label"))
            .withValue("tableName", runBackendStepInput.getValueString("tableName"))
            .withValue("filterJson", runBackendStepInput.getValueString("filterJson"))
            .withValue("userId", runBackendStepInput.getSession().getUser().getIdReference());

         List<QRecord> savedFilterList = new ArrayList<>();
         if(qRecord.getValueInteger("id") == null)
         {
            InsertInput input = new InsertInput();
            input.setTableName(SavedFilter.TABLE_NAME);
            input.setRecords(List.of(qRecord));

            InsertOutput output = new InsertAction().execute(input);
            savedFilterList = output.getRecords();
         }
         else
         {
            UpdateInput input = new UpdateInput();
            input.setTableName(SavedFilter.TABLE_NAME);
            input.setRecords(List.of(qRecord));

            UpdateOutput output = new UpdateAction().execute(input);
            savedFilterList = output.getRecords();
         }

         runBackendStepOutput.addValue("savedFilterList", (Serializable) savedFilterList);
      }
      catch(Exception e)
      {
         LOG.warn("Error storing data saved filter", e);
         throw (e);
      }
   }
}
