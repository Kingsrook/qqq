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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.QInputSource;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.general.ProcessSummaryWarningsAndErrorsRollup;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Transform step for generic table bulk-edit ETL process
 *******************************************************************************/
public class BulkEditTransformStep extends AbstractTransformStep
{
   public static final String FIELD_ENABLED_FIELDS = "bulkEditEnabledFields";

   private ProcessSummaryLine       okSummary     = new ProcessSummaryLine(Status.OK);
   private List<ProcessSummaryLine> infoSummaries = new ArrayList<>();

   private ProcessSummaryWarningsAndErrorsRollup processSummaryWarningsAndErrorsRollup = ProcessSummaryWarningsAndErrorsRollup.build("edited");

   private QTableMetaData table;
   private String         tableLabel;
   private String[]       enabledFields;

   private boolean isValidateStep;
   private boolean isExecuteStep;
   private boolean haveRecordCount;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void preRun(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ///////////////////////////////////////////////////////
      // capture the table label - for the process summary //
      ///////////////////////////////////////////////////////
      table = runBackendStepInput.getInstance().getTable(runBackendStepInput.getTableName());
      if(table != null)
      {
         tableLabel = table.getLabel();
      }

      isValidateStep = runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE);
      isExecuteStep = runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE);
      haveRecordCount = runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT) != null;

      String enabledFieldsString = runBackendStepInput.getValueString(FIELD_ENABLED_FIELDS);
      enabledFields = enabledFieldsString.split(",");

      buildInfoSummaryLines(runBackendStepInput, table, infoSummaries, isExecuteStep);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // on the validate step, we haven't read the full file, so we don't know how many rows there are - thus        //
      // record count is null, and the ValidateStep won't be setting status counters - so - do it here in that case. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(isValidateStep)
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus("Processing " + tableLabel + " records");
         if(!haveRecordCount)
         {
            runBackendStepInput.getAsyncJobCallback().updateStatus("Processing record " + "%,d".formatted(okSummary.getCount()));
         }
      }
      else if(isExecuteStep)
      {
         runBackendStepInput.getAsyncJobCallback().updateStatus("Editing " + tableLabel + " records");
         if(!haveRecordCount)
         {
            runBackendStepInput.getAsyncJobCallback().updateStatus("Editing " + tableLabel + " record " + "%,d".formatted(okSummary.getCount()));
         }
      }

      List<QRecord> outputRecords = new ArrayList<>();
      if(isExecuteStep)
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // for the execute step - create new record objects, just with the primary key, and the fields being updated. //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         for(QRecord record : runBackendStepInput.getRecords())
         {
            QRecord recordToUpdate = new QRecord();
            recordToUpdate.setValue(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
            outputRecords.add(recordToUpdate);
            setUpdatedFieldsInRecord(runBackendStepInput, enabledFields, recordToUpdate);
         }
      }
      else
      {
         /////////////////////////////////////////////////////////////////////////////////////////
         // Build records-to-update for passing into the validation method of the Update action //
         /////////////////////////////////////////////////////////////////////////////////////////
         List<QRecord>              recordsForValidation = new ArrayList<>();
         Map<Serializable, QRecord> pkeyToFullRecordMap  = new HashMap<>();
         for(QRecord record : runBackendStepInput.getRecords())
         {
            QRecord recordToUpdate = new QRecord();
            recordToUpdate.setValue(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
            setUpdatedFieldsInRecord(runBackendStepInput, enabledFields, recordToUpdate);
            recordsForValidation.add(recordToUpdate);

            /////////////////////////////////////////////////////////////
            // put the full record (with updated values) in the output //
            /////////////////////////////////////////////////////////////
            setUpdatedFieldsInRecord(runBackendStepInput, enabledFields, record);
            pkeyToFullRecordMap.put(record.getValue(table.getPrimaryKeyField()), record);
         }

         ///////////////////////////////////////////////////////////////////////
         // run the validation - critically - in preview mode (boolean param) //
         ///////////////////////////////////////////////////////////////////////
         UpdateInput updateInput = new UpdateInput();
         updateInput.setInputSource(QInputSource.USER);
         updateInput.setTableName(table.getName());
         updateInput.setRecords(recordsForValidation);
         new UpdateAction().performValidations(updateInput, Optional.of(runBackendStepInput.getRecords()), true);

         /////////////////////////////////////////////////////////////
         // look at the update input to build process summary lines //
         /////////////////////////////////////////////////////////////
         for(QRecord record : updateInput.getRecords())
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
               outputRecords.add(pkeyToFullRecordMap.get(recordPrimaryKey));
            }
            else
            {
               okSummary.incrementCountAndAddPrimaryKey(recordPrimaryKey);
               outputRecords.add(pkeyToFullRecordMap.get(recordPrimaryKey));
            }
         }
      }
      runBackendStepOutput.setRecords(outputRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void buildInfoSummaryLines(RunBackendStepInput runBackendStepInput, QTableMetaData table, List<ProcessSummaryLine> infoSummaries, boolean isExecuteStep)
   {
      String   enabledFieldsString = runBackendStepInput.getValueString(FIELD_ENABLED_FIELDS);
      String[] enabledFields       = enabledFieldsString.split(",");

      for(String fieldName : enabledFields)
      {
         QFieldMetaData field = table.getField(fieldName);
         String         label = field.getLabel();
         Serializable   value = runBackendStepInput.getValue(fieldName);

         ProcessSummaryLine summaryLine = new ProcessSummaryLine(Status.INFO);
         summaryLine.setCount(null);
         infoSummaries.add(summaryLine);

         String verb = isExecuteStep ? "was" : "will be";
         if(StringUtils.hasContent(ValueUtils.getValueAsString(value)))
         {
            String formattedValue = QValueFormatter.formatValue(field, value);

            if(field.getPossibleValueSourceName() != null)
            {
               QPossibleValueTranslator qPossibleValueTranslator = new QPossibleValueTranslator(runBackendStepInput.getInstance(), runBackendStepInput.getSession());
               String                   translatedValue          = qPossibleValueTranslator.translatePossibleValue(field, value);
               if(StringUtils.hasContent(translatedValue))
               {
                  formattedValue = translatedValue;
               }
            }

            summaryLine.setMessage(label + " " + verb + " set to: " + formattedValue);
         }
         else
         {
            summaryLine.setMessage(label + " " + verb + " cleared out");
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void setUpdatedFieldsInRecord(RunBackendStepInput runBackendStepInput, String[] enabledFields, QRecord record)
   {
      for(String fieldName : enabledFields)
      {
         Serializable value = runBackendStepInput.getValue(fieldName);
         record.setValue(fieldName, value);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();

      String noWarningsSuffix = processSummaryWarningsAndErrorsRollup.countWarnings() == 0 ? "" : " with no warnings";

      okSummary.setSingularFutureMessage(tableLabel + " record will be edited" + noWarningsSuffix + ".");
      okSummary.setPluralFutureMessage(tableLabel + " records will be edited" + noWarningsSuffix + ".");
      okSummary.pickMessage(isForResultScreen);
      okSummary.addSelfToListIfAnyCount(rs);

      processSummaryWarningsAndErrorsRollup.addToList(rs);

      rs.addAll(infoSummaries);
      return (rs);
   }

}
