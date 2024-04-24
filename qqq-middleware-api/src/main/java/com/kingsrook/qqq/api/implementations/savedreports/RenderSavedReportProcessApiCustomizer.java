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

package com.kingsrook.qqq.api.implementations.savedreports;


import com.kingsrook.qqq.api.model.metadata.processes.PreRunApiProcessCustomizer;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallbackFactory;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.savedreports.SavedReport;


/*******************************************************************************
 ** API-Customizer for the RenderSavedReport process
 *******************************************************************************/
public class RenderSavedReportProcessApiCustomizer implements PreRunApiProcessCustomizer
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preApiRun(RunProcessInput runProcessInput) throws QException
   {
      Integer reportId = runProcessInput.getValueInteger("reportId");
      if(reportId != null)
      {
         QRecord record = new GetAction().executeForRecord(new GetInput(SavedReport.TABLE_NAME).withPrimaryKey(reportId));
         if(record == null)
         {
            throw (new QNotFoundException("Report Id " + reportId + " was not found."));
         }

         runProcessInput.setCallback(QProcessCallbackFactory.forPrimaryKey("id", reportId));
      }
   }

}
