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


import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.InputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;


/*******************************************************************************
 ** Generic implementation of a LoadStep - that runs an Insert action for a
 ** specified table.
 *******************************************************************************/
public class LoadViaInsertStep extends AbstractLoadStep
{
   public static final String FIELD_DESTINATION_TABLE     = "destinationTable";
   public static final String FIELD_SKIP_UNIQUE_KEY_CHECK = "skipUniqueKeyCheck";



   /*******************************************************************************
    **
    *******************************************************************************/
   protected InputSource getInputSource()
   {
      return (QInputSource.SYSTEM);
   }



   /*******************************************************************************
    ** Execute the backend step - using the request as input, and the result as output.
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setInputSource(getInputSource());
      insertInput.setTableName(runBackendStepInput.getValueString(FIELD_DESTINATION_TABLE));
      insertInput.setRecords(runBackendStepInput.getRecords());
      getTransaction().ifPresent(insertInput::setTransaction);
      insertInput.setAsyncJobCallback(runBackendStepInput.getAsyncJobCallback());

      if(runBackendStepInput.getValuePrimitiveBoolean(FIELD_SKIP_UNIQUE_KEY_CHECK))
      {
         insertInput.setSkipUniqueKeyCheck(true);
      }

      InsertOutput insertOutput = new InsertAction().execute(insertInput);
      runBackendStepOutput.getRecords().addAll(insertOutput.getRecords());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Optional<QBackendTransaction> openTransaction(RunBackendStepInput runBackendStepInput) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(runBackendStepInput.getValueString(FIELD_DESTINATION_TABLE));
      return (Optional.of(QBackendTransaction.openFor(insertInput)));
   }
}
