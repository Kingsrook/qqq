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


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Implementation of a TransformStep - it does nothing other than take input records
 ** and sets them in the output
 *******************************************************************************/
public class NoopTransformStep extends AbstractTransformStep
{
   private final String             okSummarySuffix = " successfully processed.";
   private final ProcessSummaryLine okSummary       = new ProcessSummaryLine(Status.OK)
      .withSingularFutureMessage("can be" + okSummarySuffix)
      .withPluralFutureMessage("can be" + okSummarySuffix)
      .withSingularPastMessage("has been" + okSummarySuffix)
      .withPluralPastMessage("have been" + okSummarySuffix);



   /*******************************************************************************
    ** getProcessSummary
    *
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okSummary.addSelfToListIfAnyCount(rs);
      return (rs);
   }



   /*******************************************************************************
    ** run
    *
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////////////
      // return if no input records //
      ////////////////////////////////
      if(CollectionUtils.nullSafeIsEmpty(runBackendStepInput.getRecords()))
      {
         return;
      }

      for(QRecord qRecord : runBackendStepInput.getRecords())
      {
         okSummary.incrementCountAndAddPrimaryKey(qRecord.getValue(runBackendStepInput.getTable().getPrimaryKeyField()));
         runBackendStepOutput.getRecords().add(qRecord);
      }
   }

}
