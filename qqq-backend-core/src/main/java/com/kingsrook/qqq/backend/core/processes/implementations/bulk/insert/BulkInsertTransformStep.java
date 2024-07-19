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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreInsertCustomizer.WhenToRun;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.UniqueKeyHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
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
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Transform step for generic table bulk-insert ETL process
 *******************************************************************************/
public class BulkInsertTransformStep extends AbstractTransformStep
{
   private ProcessSummaryLine okSummary = new ProcessSummaryLine(Status.OK);

   private ProcessSummaryWarningsAndErrorsRollup processSummaryWarningsAndErrorsRollup = ProcessSummaryWarningsAndErrorsRollup.build("inserted");

   private Map<UniqueKey, ProcessSummaryLineWithUKSampleValues> ukErrorSummaries = new HashMap<>();

   private QTableMetaData table;

   private Map<UniqueKey, Set<List<Serializable>>> keysInThisFile = new HashMap<>();

   private int rowsProcessed = 0;



   /*******************************************************************************
    ** extension of ProcessSummaryLine for lines where a UniqueKey was violated,
    ** where we'll collect a sample (or maybe all) of the values that broke the UK.
    *******************************************************************************/
   private static class ProcessSummaryLineWithUKSampleValues extends ProcessSummaryLine
   {
      private Set<String> sampleValues             = new LinkedHashSet<>();
      private boolean     areThereMoreSampleValues = false;



      /*******************************************************************************
       **
       *******************************************************************************/
      public ProcessSummaryLineWithUKSampleValues(Status status)
      {
         super(status);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      this.table = QContext.getQInstance().getTable(runBackendStepInput.getTableName());

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // since we're doing a unique key check in this class, we can tell the loadViaInsert step that it (rather, the InsertAction) doesn't need to re-do one. //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      runBackendStepOutput.addValue(LoadViaInsertStep.FIELD_SKIP_UNIQUE_KEY_CHECK, true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      int            rowsInThisPage = runBackendStepInput.getRecords().size();
      QTableMetaData table          = QContext.getQInstance().getTable(runBackendStepInput.getTableName());

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up an insert-input, which will be used as input to the pre-customizer as well as for additional validations //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setInputSource(QInputSource.USER);
      insertInput.setTableName(runBackendStepInput.getTableName());
      insertInput.setRecords(runBackendStepInput.getRecords());
      insertInput.setSkipUniqueKeyCheck(true);

      //////////////////////////////////////////////////////////////////////
      // load the pre-insert customizer and set it up, if there is one    //
      // then we'll run it based on its WhenToRun value                   //
      // we do this, in case it needs to, for example, adjust values that //
      // are part of a unique key                                         //
      //////////////////////////////////////////////////////////////////////
      Optional<TableCustomizerInterface> preInsertCustomizer = QCodeLoader.getTableCustomizer(table, TableCustomizers.PRE_INSERT_RECORD.getRole());
      if(preInsertCustomizer.isPresent())
      {
         AbstractPreInsertCustomizer.WhenToRun whenToRun = preInsertCustomizer.get().whenToRunPreInsert(insertInput, true);
         if(WhenToRun.BEFORE_ALL_VALIDATIONS.equals(whenToRun) || WhenToRun.BEFORE_UNIQUE_KEY_CHECKS.equals(whenToRun))
         {
            List<QRecord> recordsAfterCustomizer = preInsertCustomizer.get().preInsert(insertInput, runBackendStepInput.getRecords(), true);
            runBackendStepInput.setRecords(recordsAfterCustomizer);

            ///////////////////////////////////////////////////////////////////////////////////////
            // todo - do we care if the customizer runs both now, and in the validation below?   //
            // right now we'll let it run both times, but maybe that should be protected against //
            ///////////////////////////////////////////////////////////////////////////////////////
         }
      }

      Map<UniqueKey, Set<List<Serializable>>> existingKeys = new HashMap<>();
      List<UniqueKey>                         uniqueKeys   = CollectionUtils.nonNullList(table.getUniqueKeys());
      for(UniqueKey uniqueKey : uniqueKeys)
      {
         existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(null, table, runBackendStepInput.getRecords(), uniqueKey).keySet());
         ukErrorSummaries.computeIfAbsent(uniqueKey, x -> new ProcessSummaryLineWithUKSampleValues(Status.ERROR));
      }

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // on the validate step, we haven't read the full file, so we don't know how many rows there are - thus        //
      // record count is null, and the ValidateStep won't be setting status counters - so - do it here in that case. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE))
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus("Processing row " + "%,d".formatted(rowsProcessed + 1));
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
      List<QRecord> recordsWithoutUkErrors = getRecordsWithoutUniqueKeyErrors(runBackendStepInput, existingKeys, uniqueKeys, table);

      /////////////////////////////////////////////////////////////////////////////////
      // run all validation from the insert action - in Preview mode (boolean param) //
      /////////////////////////////////////////////////////////////////////////////////
      insertInput.setRecords(recordsWithoutUkErrors);
      InsertAction insertAction = new InsertAction();
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

      this.rowsProcessed += rowsInThisPage;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> getRecordsWithoutUniqueKeyErrors(RunBackendStepInput runBackendStepInput, Map<UniqueKey, Set<List<Serializable>>> existingKeys, List<UniqueKey> uniqueKeys, QTableMetaData table)
   {
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
            if(CollectionUtils.nullSafeHasContents(record.getErrors()))
            {
               ///////////////////////////////////////////////////
               // skip any records that may already be in error //
               ///////////////////////////////////////////////////
               recordsWithoutUkErrors.add(record);
               continue;
            }

            //////////////////////////////////////////////////////////
            // check if this record violates any of the unique keys //
            //////////////////////////////////////////////////////////
            boolean foundDupe = false;
            for(UniqueKey uniqueKey : uniqueKeys)
            {
               Optional<List<Serializable>> keyValues = UniqueKeyHelper.getKeyValues(table, uniqueKey, record);
               if(keyValues.isPresent() && (existingKeys.get(uniqueKey).contains(keyValues.get()) || keysInThisFile.get(uniqueKey).contains(keyValues.get())))
               {
                  ProcessSummaryLineWithUKSampleValues processSummaryLineWithUKSampleValues = ukErrorSummaries.get(uniqueKey);
                  processSummaryLineWithUKSampleValues.incrementCount();
                  if(processSummaryLineWithUKSampleValues.sampleValues.size() < 3)
                  {
                     processSummaryLineWithUKSampleValues.sampleValues.add(keyValues.get().toString());
                  }
                  else
                  {
                     processSummaryLineWithUKSampleValues.areThereMoreSampleValues = true;
                  }
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
      return recordsWithoutUkErrors;
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

      for(Map.Entry<UniqueKey, ProcessSummaryLineWithUKSampleValues> entry : ukErrorSummaries.entrySet())
      {
         UniqueKey                            uniqueKey      = entry.getKey();
         ProcessSummaryLineWithUKSampleValues ukErrorSummary = entry.getValue();

         ukErrorSummary
            .withMessageSuffix(" inserted, because of duplicate values in a unique key on the fields (" + uniqueKey.getDescription(table) + "), with values"
               + (ukErrorSummary.areThereMoreSampleValues ? " such as: " : ": ")
               + StringUtils.joinWithCommasAndAnd(new ArrayList<>(ukErrorSummary.sampleValues)))

            .withSingularFutureMessage(" record will not be")
            .withPluralFutureMessage(" records will not be")
            .withSingularPastMessage(" record was not")
            .withPluralPastMessage(" records were not");

         ukErrorSummary.addSelfToListIfAnyCount(rs);
      }

      processSummaryWarningsAndErrorsRollup.addToList(rs);

      return (rs);
   }

}
