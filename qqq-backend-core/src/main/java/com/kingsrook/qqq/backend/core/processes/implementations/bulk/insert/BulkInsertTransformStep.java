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
import java.util.Objects;
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
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaDataInterface;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.statusmessages.QErrorMessage;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping.AbstractBulkLoadRollableValueError;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping.BulkLoadRecordUtils;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping.BulkLoadTableStructureBuilder;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkLoadTableStructure;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.general.ProcessSummaryWarningsAndErrorsRollup;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Transform step for generic table bulk-insert ETL process
 *******************************************************************************/
public class BulkInsertTransformStep extends AbstractTransformStep
{
   ProcessSummaryLine okSummary = new ProcessSummaryLine(Status.OK);

   ProcessSummaryWarningsAndErrorsRollup processSummaryWarningsAndErrorsRollup = ProcessSummaryWarningsAndErrorsRollup.build("inserted")
      .withDoReplaceSingletonCountLinesWithSuffixOnly(false);

   private ListingHash<String, RowValue> errorToExampleRowValueMap = new ListingHash<>();
   private ListingHash<String, String>   errorToExampleRowsMap     = new ListingHash<>();

   private Map<UniqueKey, ProcessSummaryLineWithUKSampleValues> ukErrorSummaries              = new HashMap<>();
   private Map<String, ProcessSummaryLine>                      associationsToInsertSummaries = new HashMap<>();

   private QTableMetaData table;

   private Map<UniqueKey, Set<List<Serializable>>> keysInThisFile = new HashMap<>();

   private int rowsProcessed = 0;

   private static final int EXAMPLE_ROW_LIMIT = 10;



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

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // make sure that if a saved profile was selected on a review screen, that the result screen knows about it. //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      BulkInsertStepUtils.handleSavedBulkLoadProfileIdValue(runBackendStepInput, runBackendStepOutput);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up the validationReview widget to render preview records using the table layout, and including the associations //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      runBackendStepOutput.addValue("formatPreviewRecordUsingTableLayout", table.getName());

      BulkLoadTableStructure tableStructure = BulkLoadTableStructureBuilder.buildTableStructure(table.getName());
      if(CollectionUtils.nullSafeHasContents(tableStructure.getAssociations()))
      {
         ArrayList<String> previewRecordAssociatedTableNames  = new ArrayList<>();
         ArrayList<String> previewRecordAssociatedWidgetNames = new ArrayList<>();
         ArrayList<String> previewRecordAssociationNames      = new ArrayList<>();

         ////////////////////////////////////////////////////////////
         // note - not recursively processing associations here... //
         ////////////////////////////////////////////////////////////
         for(BulkLoadTableStructure associatedStructure : tableStructure.getAssociations())
         {
            String                associationName = associatedStructure.getAssociationPath();
            Optional<Association> association     = table.getAssociations().stream().filter(a -> a.getName().equals(associationName)).findFirst();
            if(association.isPresent())
            {
               for(QFieldSection section : table.getSections())
               {
                  QWidgetMetaDataInterface widget = QContext.getQInstance().getWidget(section.getWidgetName());
                  if(widget != null && WidgetType.CHILD_RECORD_LIST.getType().equals(widget.getType()))
                  {
                     Serializable widgetJoinName = widget.getDefaultValues().get("joinName");
                     if(Objects.equals(widgetJoinName, association.get().getJoinName()))
                     {
                        previewRecordAssociatedTableNames.add(association.get().getAssociatedTableName());
                        previewRecordAssociatedWidgetNames.add(widget.getName());
                        previewRecordAssociationNames.add(association.get().getName());
                     }
                  }
               }
            }
         }
         runBackendStepOutput.addValue("previewRecordAssociatedTableNames", previewRecordAssociatedTableNames);
         runBackendStepOutput.addValue("previewRecordAssociatedWidgetNames", previewRecordAssociatedWidgetNames);
         runBackendStepOutput.addValue("previewRecordAssociationNames", previewRecordAssociationNames);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      QTableMetaData table   = QContext.getQInstance().getTable(runBackendStepInput.getTableName());
      List<QRecord>  records = runBackendStepInput.getRecords();

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up an insert-input, which will be used as input to the pre-customizer as well as for additional validations //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      InsertInput insertInput = new InsertInput();
      insertInput.setInputSource(QInputSource.USER);
      insertInput.setTableName(runBackendStepInput.getTableName());
      insertInput.setRecords(records);
      insertInput.setSkipUniqueKeyCheck(true);

      //////////////////////////////////////////////////////////////////////
      // load the pre-insert customizer and set it up, if there is one    //
      // then we'll run it based on its WhenToRun value                   //
      // we do this, in case it needs to, for example, adjust values that //
      // are part of a unique key                                         //
      //////////////////////////////////////////////////////////////////////
      boolean                            didAlreadyRunCustomizer = false;
      Optional<TableCustomizerInterface> preInsertCustomizer     = QCodeLoader.getTableCustomizer(table, TableCustomizers.PRE_INSERT_RECORD.getRole());
      if(preInsertCustomizer.isPresent())
      {
         AbstractPreInsertCustomizer.WhenToRun whenToRun = preInsertCustomizer.get().whenToRunPreInsert(insertInput, true);
         if(WhenToRun.BEFORE_ALL_VALIDATIONS.equals(whenToRun) || WhenToRun.BEFORE_UNIQUE_KEY_CHECKS.equals(whenToRun))
         {
            List<QRecord> recordsAfterCustomizer = preInsertCustomizer.get().preInsert(insertInput, records, true);
            runBackendStepInput.setRecords(recordsAfterCustomizer);

            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // so we used to have a comment here asking "do we care if the customizer runs both now, and in the validation below?" //
            // when implementing Bulk Load V2, we were seeing that some customizers were adding errors to records, both now, and   //
            // when they ran below.  so, at that time, we added this boolean, to track and avoid the double-run...                 //
            // we could also imagine this being a setting on the pre-insert customizer, similar to its whenToRun attribute...      //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            didAlreadyRunCustomizer = true;
         }
      }

      ///////////////////////////////////////////////////////////////////////////////
      // If the table has unique keys - then capture all values on these records   //
      // for each key and set up a processSummaryLine for each of the table's UK's //
      ///////////////////////////////////////////////////////////////////////////////
      Map<UniqueKey, Set<List<Serializable>>> existingKeys = new HashMap<>();
      List<UniqueKey>                         uniqueKeys   = CollectionUtils.nonNullList(table.getUniqueKeys());
      for(UniqueKey uniqueKey : uniqueKeys)
      {
         existingKeys.put(uniqueKey, UniqueKeyHelper.getExistingKeys(null, table, records, uniqueKey).keySet());
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
      List<QRecord> recordsWithoutUkErrors = getRecordsWithoutUniqueKeyErrors(records, existingKeys, uniqueKeys, table);

      /////////////////////////////////////////////////////////////////////////////////
      // run all validation from the insert action - in Preview mode (boolean param) //
      /////////////////////////////////////////////////////////////////////////////////
      insertInput.setRecords(recordsWithoutUkErrors);
      InsertAction insertAction = new InsertAction();
      insertAction.performValidations(insertInput, true, didAlreadyRunCustomizer);
      List<QRecord> validationResultRecords = insertInput.getRecords();

      /////////////////////////////////////////////////////////////////
      // look at validation results to build process summary results //
      /////////////////////////////////////////////////////////////////
      List<QRecord> outputRecords = new ArrayList<>();
      for(QRecord record : validationResultRecords)
      {
         List<QErrorMessage> errorsFromAssociations = getErrorsFromAssociations(record);
         if(CollectionUtils.nullSafeHasContents(errorsFromAssociations))
         {
            List<QErrorMessage> recordErrors = Objects.requireNonNullElseGet(record.getErrors(), () -> new ArrayList<>());
            recordErrors.addAll(errorsFromAssociations);
            record.setErrors(recordErrors);
         }

         if(CollectionUtils.nullSafeHasContents(record.getErrors()))
         {
            for(QErrorMessage error : record.getErrors())
            {
               if(error instanceof AbstractBulkLoadRollableValueError rollableValueError)
               {
                  processSummaryWarningsAndErrorsRollup.addError(rollableValueError.getMessageToUseAsProcessSummaryRollupKey(), null);
                  addToErrorToExampleRowValueMap(rollableValueError, record);
               }
               else
               {
                  processSummaryWarningsAndErrorsRollup.addError(error.getMessage(), null);
                  addToErrorToExampleRowMap(error.getMessage(), record);
               }
            }
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

            for(Map.Entry<String, List<QRecord>> entry : CollectionUtils.nonNullMap(record.getAssociatedRecords()).entrySet())
            {
               String             associationName         = entry.getKey();
               ProcessSummaryLine associationToInsertLine = associationsToInsertSummaries.computeIfAbsent(associationName, x -> new ProcessSummaryLine(Status.OK));
               associationToInsertLine.incrementCount(CollectionUtils.nonNullList(entry.getValue()).size());
            }
         }
      }

      runBackendStepOutput.setRecords(outputRecords);
      this.rowsProcessed += records.size();
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<QErrorMessage> getErrorsFromAssociations(QRecord record)
   {
      List<QErrorMessage> rs = null;
      for(Map.Entry<String, List<QRecord>> entry : CollectionUtils.nonNullMap(record.getAssociatedRecords()).entrySet())
      {
         for(QRecord associatedRecord : CollectionUtils.nonNullList(entry.getValue()))
         {
            if(CollectionUtils.nullSafeHasContents(associatedRecord.getErrors()))
            {
               rs = Objects.requireNonNullElseGet(rs, () -> new ArrayList<>());
               rs.addAll(associatedRecord.getErrors());

               List<QErrorMessage> childErrors = getErrorsFromAssociations(associatedRecord);
               if(CollectionUtils.nullSafeHasContents(childErrors))
               {
                  rs.addAll(childErrors);
               }
            }
         }
      }
      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void addToErrorToExampleRowValueMap(AbstractBulkLoadRollableValueError bulkLoadRollableValueError, QRecord record)
   {
      String         message   = bulkLoadRollableValueError.getMessageToUseAsProcessSummaryRollupKey();
      List<RowValue> rowValues = errorToExampleRowValueMap.computeIfAbsent(message, k -> new ArrayList<>());

      if(rowValues.size() < EXAMPLE_ROW_LIMIT)
      {
         rowValues.add(new RowValue(bulkLoadRollableValueError, record));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void addToErrorToExampleRowMap(String message, QRecord record)
   {
      List<String> rowNos = errorToExampleRowsMap.computeIfAbsent(message, k -> new ArrayList<>());

      if(rowNos.size() < EXAMPLE_ROW_LIMIT)
      {
         rowNos.add(BulkLoadRecordUtils.getRowNosString(record));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<QRecord> getRecordsWithoutUniqueKeyErrors(List<QRecord> records, Map<UniqueKey, Set<List<Serializable>>> existingKeys, List<UniqueKey> uniqueKeys, QTableMetaData table)
   {
      ////////////////////////////////////////////////////
      // if there are no UK's, proceed with all records //
      ////////////////////////////////////////////////////
      List<QRecord> recordsWithoutUkErrors = new ArrayList<>();
      if(existingKeys.isEmpty())
      {
         recordsWithoutUkErrors.addAll(records);
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
         for(QRecord record : records)
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

      ProcessSummaryLine recordsProcessedLine = new ProcessSummaryLine(Status.INFO);
      recordsProcessedLine.setCount(rowsProcessed);
      rs.add(recordsProcessedLine);
      recordsProcessedLine.withMessageSuffix(" processed from the file.");
      recordsProcessedLine.withSingularFutureMessage("record was");
      recordsProcessedLine.withSingularPastMessage("record was");
      recordsProcessedLine.withPluralFutureMessage("records were");
      recordsProcessedLine.withPluralPastMessage("records were");

      String noWarningsSuffix = processSummaryWarningsAndErrorsRollup.countWarnings() == 0 ? "" : " with no warnings";
      okSummary.setSingularFutureMessage(tableLabel + " record will be inserted" + noWarningsSuffix + ".");
      okSummary.setPluralFutureMessage(tableLabel + " records will be inserted" + noWarningsSuffix + ".");
      okSummary.setSingularPastMessage(tableLabel + " record was inserted" + noWarningsSuffix + ".");
      okSummary.setPluralPastMessage(tableLabel + " records were inserted" + noWarningsSuffix + ".");
      okSummary.pickMessage(isForResultScreen);
      okSummary.addSelfToListIfAnyCount(rs);

      for(Map.Entry<String, ProcessSummaryLine> entry : associationsToInsertSummaries.entrySet())
      {
         Optional<Association> association = table.getAssociations().stream().filter(a -> a.getName().equals(entry.getKey())).findFirst();
         if(association.isPresent())
         {
            QTableMetaData associationTable = QContext.getQInstance().getTable(association.get().getAssociatedTableName());
            String         associationLabel = associationTable.getLabel();

            ProcessSummaryLine line = entry.getValue();
            line.setSingularFutureMessage(associationLabel + " record will be inserted.");
            line.setPluralFutureMessage(associationLabel + " records will be inserted.");
            line.setSingularPastMessage(associationLabel + " record was inserted.");
            line.setPluralPastMessage(associationLabel + " records were inserted.");
            line.pickMessage(isForResultScreen);
            line.addSelfToListIfAnyCount(rs);
         }
      }

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

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // for process summary lines that exist in the error-to-example-row-value map, add those example values to the lines. //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(Map.Entry<String, ProcessSummaryLine> entry : processSummaryWarningsAndErrorsRollup.getErrorSummaries().entrySet())
      {
         String message = entry.getKey();
         if(errorToExampleRowValueMap.containsKey(message))
         {
            ProcessSummaryLine line          = entry.getValue();
            List<RowValue>     rowValues     = errorToExampleRowValueMap.get(message);
            String             exampleOrFull = rowValues.size() < line.getCount() ? "Example " : "";
            line.setMessageSuffix(line.getMessageSuffix() + periodIfNeeded(line.getMessageSuffix()) + "  " + exampleOrFull + "Values:");
            line.setBulletsOfText(new ArrayList<>(rowValues.stream().map(String::valueOf).toList()));
         }
         else if(errorToExampleRowsMap.containsKey(message))
         {
            ProcessSummaryLine line            = entry.getValue();
            List<String>       rowDescriptions = errorToExampleRowsMap.get(message);
            String             exampleOrFull   = rowDescriptions.size() < line.getCount() ? "Example " : "";
            line.setMessageSuffix(line.getMessageSuffix() + periodIfNeeded(line.getMessageSuffix()) + "  " + exampleOrFull + "Records:");
            line.setBulletsOfText(new ArrayList<>(rowDescriptions.stream().map(String::valueOf).toList()));
         }
      }

      processSummaryWarningsAndErrorsRollup.addToList(rs);

      return (rs);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private String periodIfNeeded(String input)
   {
      if(input != null && input.matches(".*\\. *$"))
      {
         return ("");
      }

      return (".");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private record RowValue(String row, String value)
   {

      /***************************************************************************
       **
       ***************************************************************************/
      public RowValue(AbstractBulkLoadRollableValueError bulkLoadRollableValueError, QRecord record)
      {
         this(BulkLoadRecordUtils.getRowNosString(record), ValueUtils.getValueAsString(bulkLoadRollableValueError.getValue()));
      }



      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public String toString()
      {
         return row + " [" + value + "]";
      }
   }

}
