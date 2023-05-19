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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.UniqueKeyHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.general.ProcessSummaryWarningsAndErrorsRollup;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** Transform step for generic table bulk-insert ETL process
 *******************************************************************************/
public class BulkInsertTransformStep extends AbstractTransformStep
{
   private ProcessSummaryLine okSummary = new ProcessSummaryLine(Status.OK);

   private ProcessSummaryWarningsAndErrorsRollup processSummaryWarningsAndErrorsRollup = ProcessSummaryWarningsAndErrorsRollup.build("inserted");

   private Map<UniqueKey, ProcessSummaryLine> ukErrorSummaries = new HashMap<>();

   private QTableMetaData table;

   private Map<UniqueKey, Set<List<Serializable>>> keysInThisFile = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      this.table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getTableName());

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // since we're doing a unique key check in this class, we can tell the loadViaInsert step that it (rather, the InsertAction) doesn't need to re-do one. //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      runBackendStepOutput.addValue(LoadViaInsertStep.FIELD_SKIP_UNIQUE_KEY_CHECK, true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QTableMetaData table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getTableName());

      Map<UniqueKey, Set<List<Serializable>>> existingKeys = new HashMap<>();
      List<UniqueKey>                         uniqueKeys   = CollectionUtils.nonNullList(table.getUniqueKeys());
      for(UniqueKey uniqueKey : uniqueKeys)
      {
         existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(null, table, runBackendStepInput.getRecords(), uniqueKey).keySet());
         ukErrorSummaries.computeIfAbsent(uniqueKey, x -> new ProcessSummaryLine(Status.ERROR));
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // on the validate step, we haven't read the full file, so we don't know how many rows there are - thus        //
      // record count is null, and the ValidateStep won't be setting status counters - so - do it here in that case. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE))
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus("Processing row " + "%,d".formatted(okSummary.getCount()));
      }
      else if(runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE))
      {
         if(runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT) == null)
         {
            runBackendStepInput.getAsyncJobCallback().updateStatus("Inserting " + table.getLabel() + " record " + "%,d".formatted(okSummary.getCount()));
         }
         else
         {
            runBackendStepInput.getAsyncJobCallback().updateStatus("Inserting " + table.getLabel() + " records");
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // Note, we want to do our own UK checking here, even though InsertAction also tries to do it, because InsertAction //
      // will only be getting the records in pages, but in here, we'll track UK's across pages!!                          //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      ////////////////////////////////////////////////////
      // if there are no UK's, proceed with all records //
      ////////////////////////////////////////////////////
      List<QRecord> recordsWithoutUkErrors = new ArrayList<>();
      if(existingKeys.isEmpty())
      {
         recordsWithoutUkErrors.addAll(runBackendStepInput.getRecords());
      }
      else
      {
         /////////////////////////////////////////////////////////////
         // else, only proceed with records that don't violate a UK //
         /////////////////////////////////////////////////////////////
         for(UniqueKey uniqueKey : uniqueKeys)
         {
            keysInThisFile.computeIfAbsent(uniqueKey, x -> new HashSet<>());
         }

         ///////////////////////////////////////////////////////////////////////////
         // else, get each records keys and see if it already exists or not       //
         // also, build a set of keys we've seen (within this page (or overall?)) //
         ///////////////////////////////////////////////////////////////////////////
         for(QRecord record : runBackendStepInput.getRecords())
         {
            //////////////////////////////////////////////////////////
            // check if this record violates any of the unique keys //
            //////////////////////////////////////////////////////////
            boolean foundDupe = false;
            for(UniqueKey uniqueKey : uniqueKeys)
            {
               Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
               if(keyValues.isPresent() && (existingKeys.get(uniqueKey).contains(keyValues.get()) || keysInThisFile.get(uniqueKey).contains(keyValues.get())))
               {
                  ukErrorSummaries.get(uniqueKey).incrementCount();
                  foundDupe = true;
                  break;
               }
            }

            ///////////////////////////////////////////////////////////////////////////////
            // if this record doesn't violate any uk's, then we can add it to the output //
            ///////////////////////////////////////////////////////////////////////////////
            if(!foundDupe)
            {
               for(UniqueKey uniqueKey : uniqueKeys)
               {
                  Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
                  keyValues.ifPresent(kv -> keysInThisFile.get(uniqueKey).add(kv));
               }
               recordsWithoutUkErrors.add(record);
            }
         }
      }

      /////////////////////////////////////////////////////////////////////////////////
      // run all validation from the insert action - in Preview mode (boolean param) //
      /////////////////////////////////////////////////////////////////////////////////
      InsertAction insertAction = new InsertAction();
      InsertInput  insertInput  = new InsertInput();
      insertInput.setInputSource(QInputSource.USER);
      insertInput.setTableName(runBackendStepInput.getTableName());
      insertInput.setRecords(recordsWithoutUkErrors);
      insertInput.setSkipUniqueKeyCheck(true);
      insertAction.performValidations(insertInput, true);
      List<QRecord> validationResultRecords = insertInput.getRecords();

      /////////////////////////////////////////////////////////////////
      // look at validation results to build process summary results //
      /////////////////////////////////////////////////////////////////
      List<QRecord> outputRecords = new ArrayList<>();
      for(QRecord record : validationResultRecords)
      {
         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            String message = record.getErrors().get(0).getMessage();
            processSummaryWarningsAndErrorsRollup.addError(message, null);
         }
         else if(CollectionUtils.nullSafeHasContents(record.getWarnings()))
         {
            String message = record.getWarnings().get(0).getMessage();
            processSummaryWarningsAndErrorsRollup.addWarning(message, null);
            outputRecords.add(record);
         }
         else
         {
            okSummary.incrementCountAndAddPrimaryKey(null);
            outputRecords.add(record);
         }
      }

      runBackendStepOutput.setRecords(outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs         = new ArrayList<>();
      String                                 tableLabel = table == null ? "" : table.getLabel();

      String noWarningsSuffix = processSummaryWarningsAndErrorsRollup.countWarnings() == 0 ? "" : " with no warnings";
      okSummary.setSingularFutureMessage(tableLabel + " record will be inserted" + noWarningsSuffix + ".");
      okSummary.setPluralFutureMessage(tableLabel + " records will be inserted" + noWarningsSuffix + ".");
      okSummary.setSingularPastMessage(tableLabel + " record was inserted" + noWarningsSuffix + ".");
      okSummary.setPluralPastMessage(tableLabel + " records were inserted" + noWarningsSuffix + ".");
      okSummary.pickMessage(isForResultScreen);
      okSummary.addSelfToListIfAnyCount(rs);

      for(Map.Entry<UniqueKey, ProcessSummaryLine> entry : ukErrorSummaries.entrySet())
      {
         UniqueKey          uniqueKey      = entry.getKey();
         ProcessSummaryLine ukErrorSummary = entry.getValue();
         String             ukErrorSuffix  = " inserted, because of duplicate values in a unique key (" + uniqueKey.getDescription(table) + ")";

         ukErrorSummary
            .withSingularFutureMessage(tableLabel + " record will not be" + ukErrorSuffix)
            .withPluralFutureMessage(tableLabel + " records will not be" + ukErrorSuffix)
            .withSingularPastMessage(tableLabel + " record was not" + ukErrorSuffix)
            .withPluralPastMessage(tableLabel + " records were not" + ukErrorSuffix);

         ukErrorSummary.addSelfToListIfAnyCount(rs);
      }

      processSummaryWarningsAndErrorsRollup.addToList(rs);

      return (rs);
   }

}
