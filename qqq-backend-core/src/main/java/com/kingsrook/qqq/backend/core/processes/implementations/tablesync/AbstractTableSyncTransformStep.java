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

package com.kingsrook.qqq.backend.core.processes.implementations.tablesync;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.general.StandardProcessSummaryLineProducer;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** This class is for transforming records from a Source table to a Destination table.
 **
 ** The Source table has a (unique/primary) key field:  sourceTableKeyField,
 ** Which is matched against the Destination table's foreign-key:  destinationTableForeignKeyField
 *******************************************************************************/
public abstract class AbstractTableSyncTransformStep extends AbstractTransformStep
{
   private ProcessSummaryLine okToInsert           = StandardProcessSummaryLineProducer.getOkToInsertLine();
   private ProcessSummaryLine okToUpdate           = StandardProcessSummaryLineProducer.getOkToUpdateLine();
   private ProcessSummaryLine errorMissingKeyField = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix("missing a value for the key field.")
      .withSingularFutureMessage("will not be synced, because it is ")
      .withPluralFutureMessage("will not be synced, because they are ")
      .withSingularPastMessage("was not synced, because it is ")
      .withPluralPastMessage("were not synced, because they are ");

   private RunBackendStepInput runBackendStepInput = null;

   private QPossibleValueTranslator possibleValueTranslator;

   private Map<String, Map<Serializable, QRecord>> tableMaps = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QRecord getRecord(String tableName, String fieldName, Serializable value) throws QException
   {
      if(!tableMaps.containsKey(tableName))
      {
         Map<Serializable, QRecord> recordMap = GeneralProcessUtils.loadTableToMap(runBackendStepInput, tableName, fieldName);
         tableMaps.put(tableName, recordMap);
      }

      return (tableMaps.get(tableName).get(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Serializable getRecordField(String tableName, String fieldName, Serializable value, String outputField) throws QException
   {
      QRecord record = getRecord(tableName, fieldName, value);
      if(record == null)
      {
         return (null);
      }

      return (record.getValue(outputField));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      return (StandardProcessSummaryLineProducer.toArrayList(okToInsert, okToUpdate, errorMissingKeyField));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public abstract QRecord populateRecordToStore(RunBackendStepInput runBackendStepInput, QRecord destinationRecord, QRecord sourceRecord) throws QException;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(runBackendStepInput.getRecords()))
      {
         return;
      }

      this.runBackendStepInput = runBackendStepInput;
      String sourceTableKeyField             = runBackendStepInput.getValueString(TableSyncProcess.FIELD_SOURCE_TABLE_KEY_FIELD);
      String destinationTableForeignKeyField = runBackendStepInput.getValueString(TableSyncProcess.FIELD_DESTINATION_TABLE_FOREIGN_KEY);
      String destinationTableName            = runBackendStepInput.getValueString(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE);

      //////////////////////////////////////
      // extract keys from source records //
      //////////////////////////////////////
      List<Serializable> sourceKeyList = runBackendStepInput.getRecords().stream()
         .map(r -> r.getValueString(sourceTableKeyField))
         .filter(Objects::nonNull)
         .filter(v -> !"".equals(v))
         .collect(Collectors.toList());

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // query to see if we already have those records in the destination (to determine insert/update) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      Map<Serializable, QRecord> existingRecordsByForeignKey = Collections.emptyMap();
      if(!sourceKeyList.isEmpty())
      {
         QueryInput queryInput = new QueryInput(runBackendStepInput.getInstance());
         queryInput.setSession(runBackendStepInput.getSession());
         queryInput.setTableName(destinationTableName);
         queryInput.setFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria(destinationTableForeignKeyField, QCriteriaOperator.IN, sourceKeyList))
         );
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         existingRecordsByForeignKey = CollectionUtils.recordsToMap(queryOutput.getRecords(), destinationTableForeignKeyField);
      }

      /////////////////////////////////////////////////////////////////
      // foreach source record, build the record we'll insert/update //
      /////////////////////////////////////////////////////////////////
      for(QRecord sourceRecord : runBackendStepInput.getRecords())
      {
         Serializable sourceKeyValue = sourceRecord.getValue(sourceTableKeyField);
         QRecord      existingRecord = existingRecordsByForeignKey.get(sourceKeyValue);

         if(sourceKeyValue == null || "".equals(sourceKeyValue))
         {
            errorMissingKeyField.incrementCount();

            try
            {
               errorMissingKeyField.setMessageSuffix("missing a value for the field " + runBackendStepInput.getTable().getField(sourceTableKeyField).getLabel());
            }
            catch(Exception e)
            {
               /////////////////////////////////////////
               // just leave the default error suffix //
               /////////////////////////////////////////
            }

            continue;
         }

         QRecord recordToStore;
         if(existingRecord != null)
         {
            recordToStore = existingRecord;
            okToUpdate.incrementCount();
         }
         else
         {
            recordToStore = new QRecord();
            okToInsert.incrementCount();
         }

         recordToStore = populateRecordToStore(runBackendStepInput, recordToStore, sourceRecord);
         runBackendStepOutput.addRecord(recordToStore);
      }

      ////////////////////////////////////////////////
      // populate possible-values for review screen //
      ////////////////////////////////////////////////
      if(RunProcessInput.FrontendStepBehavior.BREAK.equals(runBackendStepInput.getFrontendStepBehavior()))
      {
         if(CollectionUtils.nullSafeHasContents(runBackendStepOutput.getRecords()))
         {
            if(possibleValueTranslator == null)
            {
               possibleValueTranslator = new QPossibleValueTranslator(runBackendStepInput.getInstance(), runBackendStepInput.getSession());
            }

            possibleValueTranslator.translatePossibleValuesInRecords(runBackendStepInput.getInstance().getTable(destinationTableName), runBackendStepOutput.getRecords());
         }
      }
   }

}
