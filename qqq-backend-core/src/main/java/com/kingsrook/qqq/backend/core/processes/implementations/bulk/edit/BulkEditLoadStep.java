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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.InputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaUpdateStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.general.ProcessSummaryWarningsAndErrorsRollup;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import static com.kingsrook.qqq.backend.core.processes.implementations.bulk.edit.BulkEditTransformStep.buildInfoSummaryLines;


/*******************************************************************************
 ** Load step for generic table bulk-edit ETL process
 *******************************************************************************/
public class BulkEditLoadStep extends LoadViaUpdateStep implements ProcessSummaryProviderInterface
{
   public static final String FIELD_ENABLED_FIELDS = "bulkEditEnabledFields";

   private ProcessSummaryLine       okSummary     = new ProcessSummaryLine(Status.OK);
   private List<ProcessSummaryLine> infoSummaries = new ArrayList<>();

   private ProcessSummaryWarningsAndErrorsRollup processSummaryWarningsAndErrorsRollup = ProcessSummaryWarningsAndErrorsRollup.build("edited");

   private String tableLabel;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   protected InputSource getInputSource()
   {
      return (QInputSource.USER);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();

      String noWarningsSuffix = processSummaryWarningsAndErrorsRollup.countWarnings() == 0 ? "" : " with no warnings";

      okSummary.setSingularPastMessage(tableLabel + " record was edited" + noWarningsSuffix + ".");
      okSummary.setPluralPastMessage(tableLabel + " records were edited" + noWarningsSuffix + ".");
      okSummary.pickMessage(isForResultScreen);
      okSummary.addSelfToListIfAnyCount(rs);

      processSummaryWarningsAndErrorsRollup.addToList(rs);
      rs.addAll(infoSummaries);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.preRun(runBackendStepInput, runBackendStepOutput);

      QTableMetaData table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getTableName());
      if(table != null)
      {
         tableLabel = table.getLabel();
      }

      buildInfoSummaryLines(runBackendStepInput, table, infoSummaries, true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ////////////////////////////
      // have base class update //
      ////////////////////////////
      super.run(runBackendStepInput, runBackendStepOutput);

      ////////////////////////////////////////////////////////
      // roll up results based on output from update action //
      ////////////////////////////////////////////////////////
      QTableMetaData table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getTableName());
      for(QRecord record : runBackendStepOutput.getRecords())
      {
         Serializable recordPrimaryKey = record.getValue(table.getPrimaryKeyField());
         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            String message = record.getErrors().get(0).getMessage();
            processSummaryWarningsAndErrorsRollup.addError(message, recordPrimaryKey);
         }
         else if(CollectionUtils.nullSafeHasContents(record.getWarnings()))
         {
            String message = record.getWarnings().get(0).getMessage();
            processSummaryWarningsAndErrorsRollup.addWarning(message, recordPrimaryKey);
         }
         else
         {
            okSummary.incrementCountAndAddPrimaryKey(recordPrimaryKey);
         }
      }
   }

}
