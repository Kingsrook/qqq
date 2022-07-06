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

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.process.ProcessMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.process.ProcessMetaDataResult;
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
   public ProcessMetaDataResult execute(ProcessMetaDataRequest processMetaDataRequest) throws QException
   {
      ActionHelper.validateSession(processMetaDataRequest);

      // todo pre-customization - just get to modify the request?
      ProcessMetaDataResult processMetaDataResult = new ProcessMetaDataResult();

      QProcessMetaData process = processMetaDataRequest.getInstance().getProcess(processMetaDataRequest.getProcessName());
      if(process == null)
      {
         throw (new QNotFoundException("Process [" + processMetaDataRequest.getProcessName() + "] was not found."));
      }
      processMetaDataResult.setProcess(new QFrontendProcessMetaData(process, true));

      // todo post-customization - can do whatever w/ the result if you want

      return processMetaDataResult;
   }
}
