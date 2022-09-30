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
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
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

      String enabledFieldsString = runBackendStepInput.getValueString(FIELD_ENABLED_FIELDS);
      enabledFields = enabledFieldsString.split(",");

      isValidateStep = runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_VALIDATE);
      isExecuteStep = runBackendStepInput.getStepName().equals(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE);
      haveRecordCount = runBackendStepInput.getValue(StreamedETLWithFrontendProcess.FIELD_RECORD_COUNT) != null;

      buildInfoSummaryLines(runBackendStepInput, enabledFields);
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
         ////////////////////////////////////////////////////////////////////////////////////////////
         // put the value in all the records (note, this is just for display on the review screen, //
         // and/or if we wanted to do some validation - this is NOT what will be store, as the     //
         // Update action only wants fields that are being changed.                                //
         ////////////////////////////////////////////////////////////////////////////////////////////
         for(QRecord record : runBackendStepInput.getRecords())
         {
            outputRecords.add(record);
            setUpdatedFieldsInRecord(runBackendStepInput, enabledFields, record);
         }
      }
      runBackendStepOutput.setRecords(outputRecords);
      okSummary.incrementCount(runBackendStepInput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void buildInfoSummaryLines(RunBackendStepInput runBackendStepInput, String[] enabledFields)
   {
      QValueFormatter qValueFormatter = new QValueFormatter();
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
            String formattedValue = qValueFormatter.formatValue(field, value); // todo - PVS!
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
      if(isForResultScreen)
      {
         okSummary.setMessage(tableLabel + " records were edited.");
      }
      else
      {
         okSummary.setMessage(tableLabel + " records will be edited.");
      }

      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      rs.add(okSummary);
      rs.addAll(infoSummaries);
      return (rs);
   }
}
