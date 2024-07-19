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

package com.kingsrook.qqq.backend.core.actions.metadata;


import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Action to fetch meta-data for a process.
 **
 *******************************************************************************/
public class ProcessMetaDataAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public ProcessMetaDataOutput execute(ProcessMetaDataInput processMetaDataInput) throws QException
   {
      ActionHelper.validateSession(processMetaDataInput);

      // todo pre-customization - just get to modify the request?
      ProcessMetaDataOutput processMetaDataOutput = new ProcessMetaDataOutput();

      QProcessMetaData process = QContext.getQInstance().getProcess(processMetaDataInput.getProcessName());
      if(process == null)
      {
         throw (new QNotFoundException("Process [" + processMetaDataInput.getProcessName() + "] was not found."));
      }
      processMetaDataOutput.setProcess(new QFrontendProcessMetaData(processMetaDataInput, process, true));

      // todo post-customization - can do whatever w/ the result if you want

      return processMetaDataOutput;
   }
}
