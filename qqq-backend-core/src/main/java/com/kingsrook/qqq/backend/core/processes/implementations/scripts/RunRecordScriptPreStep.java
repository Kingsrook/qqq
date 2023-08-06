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


import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableAccessor;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamed.StreamedETLProcess;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** pre-step for the run-record-script process.  Help deal with this being
 ** a generic process (e.g., no table name defined in the meta data).
 *******************************************************************************/
public class RunRecordScriptPreStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // this is a generic (e.g., not table-specific) process - so we must be sure to set the tableName field in the expected slot. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      String tableName = runBackendStepInput.getValueString("tableName");
      if(!StringUtils.hasContent(tableName))
      {
         throw (new QException("Table name was not specified as input value"));
      }

      runBackendStepInput.addValue(StreamedETLProcess.FIELD_SOURCE_TABLE, tableName);

      /////////////////////////////////////////////////////////////////
      // set this value, for the select-script possible-value filter //
      /////////////////////////////////////////////////////////////////
      runBackendStepInput.addValue("tableId", QQQTableAccessor.getTableId(tableName));
   }

}
