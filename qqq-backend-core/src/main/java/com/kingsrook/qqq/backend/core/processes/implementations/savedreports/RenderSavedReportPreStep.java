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

package com.kingsrook.qqq.backend.core.processes.implementations.savedreports;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class RenderSavedReportPreStep implements BackendStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String storageTableName = runBackendStepInput.getValueString(RenderSavedReportMetaDataProducer.FIELD_NAME_STORAGE_TABLE_NAME);
      if(!StringUtils.hasContent(storageTableName))
      {
         throw (new QUserFacingException("Process configuration error:  Missing value for storageTableName."));
      }

      if(QContext.getQInstance().getTable(storageTableName) == null)
      {
         throw (new QUserFacingException("Process configuration error:  Unrecognized value for storageTableName - no table named [" + storageTableName + "] was found in the instance."));
      }

      List<QRecord> records = runBackendStepInput.getRecords();
      if(!CollectionUtils.nullSafeHasContents(records))
      {
         throw (new QUserFacingException("No report was selected or found to be rendered."));
      }

      if(records.size() > 1)
      {
         throw (new QUserFacingException("You may only render 1 report at a time."));
      }

      SavedReport savedReport = new SavedReport(records.get(0));

      // todo - check for inputs - set up the input screen...
   }

}
