/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.executors;


import com.kingsrook.qqq.backend.core.actions.metadata.ProcessMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.middleware.javalin.executors.io.ProcessMetaDataInput;
import com.kingsrook.qqq.middleware.javalin.executors.io.ProcessMetaDataOutputInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessMetaDataExecutor extends AbstractMiddlewareExecutor<ProcessMetaDataInput, ProcessMetaDataOutputInterface>
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void execute(ProcessMetaDataInput input, ProcessMetaDataOutputInterface output) throws QException
   {
      com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput processMetaDataInput = new com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput();

      String           processName = input.getProcessName();
      QProcessMetaData process     = QContext.getQInstance().getProcess(processName);
      if(process == null)
      {
         throw (new QNotFoundException("Process [" + processName + "] was not found."));
      }
      PermissionsHelper.checkProcessPermissionThrowing(processMetaDataInput, processName);

      processMetaDataInput.setProcessName(processName);
      ProcessMetaDataAction processMetaDataAction = new ProcessMetaDataAction();
      ProcessMetaDataOutput processMetaDataOutput = processMetaDataAction.execute(processMetaDataInput);

      output.setProcessMetaData(processMetaDataOutput.getProcess());
   }

}
