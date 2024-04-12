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

package com.kingsrook.qqq.backend.core.processes.implementations.general;


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QRecordListMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;


/*******************************************************************************
 ** Function body to take care of loading the initial records to be used by a
 ** process.
 **
 *******************************************************************************/
public class LoadInitialRecordsStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      // basically this is a no-op... sometimes we just need a backendStep to be the first step in a process. //
      // While we're here, go ahead and put the query filter in the payload as a value - this is needed for   //
      // processes that have a screen before their first backend step (why is this needed?  not sure, but is) //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////
      runBackendStepInput.getAsyncJobCallback().updateStatus("Loading records");
      if(runBackendStepInput.getCallback() != null)
      {
         QQueryFilter queryFilter = runBackendStepInput.getCallback().getQueryFilter();
         runBackendStepOutput.addValue("queryFilterJson", JsonUtils.toJson(queryFilter));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendStepMetaData defineMetaData(String tableName)
   {
      return (new QBackendStepMetaData()
         .withName("loadInitialRecords")
         .withCode(new QCodeReference()
            .withName(LoadInitialRecordsStep.class.getName())
            .withCodeType(QCodeType.JAVA))
         .withInputData(new QFunctionInputMetaData()
            .withRecordListMetaData(new QRecordListMetaData()
               .withTableName(tableName))));

   }

}
