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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.general.StandardProcessSummaryLineProducer;
import com.kingsrook.qqq.backend.core.processes.utils.RecordLookupHelper;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


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

   protected RunBackendStepInput runBackendStepInput = null;
   protected RecordLookupHelper  recordLookupHelper  = null;

   private QPossibleValueTranslator possibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      return (StandardProcessSummaryLineProducer.toArrayList(okToInsert, okToUpdate, errorMissingKeyField));
   }



   /*******************************************************************************
    ** Map a record from the source table to the destination table.   e.g., put
    ** values into the destinationRecord, from the sourceRecord.
    **
    ** The destinationRecord will already be constructed, and will actually already
    ** be the record being updated, in the case of an update.  It'll be empty (newly
    ** constructed) for an insert.
    *******************************************************************************/
   public abstract QRecord populateRecordToStore(RunBackendStepInput runBackendStepInput, QRecord destinationRecord, QRecord sourceRecord) throws QException;



   /*******************************************************************************
    ** Specify a list of tableName/keyColumnName pairs to run through
    ** the preloadRecords method of the recordLookupHelper.
    *******************************************************************************/
   protected List<Pair<String, String>> getLookupsToPreLoad()
   {
      return (null);
   }



   /*******************************************************************************
    ** Define the query filter to find existing records.  e.g., for determining
    ** insert vs. update.  Subclasses may override this to customize the behavior,
    ** e.g., in case an additional field is needed in the query.
    *******************************************************************************/
   protected QQueryFilter getExistingRecordQueryFilter(RunBackendStepInput runBackendStepInput, List<Serializable> sourceKeyList)
   {
      String destinationTableForeignKeyField = getSyncProcessConfig().destinationTableForeignKey;
      return new QQueryFilter().withCriteria(new QFilterCriteria(destinationTableForeignKeyField, QCriteriaOperator.IN, sourceKeyList));
   }



   /*******************************************************************************
    ** Define the config for this process - e.g., what fields & tables are used.
    *******************************************************************************/
   protected abstract SyncProcessConfig getSyncProcessConfig();



   /*******************************************************************************
    ** Record to store the config for this process - e.g., what fields & tables are used.
    *******************************************************************************/
   public record SyncProcessConfig(String sourceTable, String sourceTableKeyField, String destinationTable, String destinationTableForeignKey)
   {
      /*******************************************************************************
       ** artificial method, here to make jacoco see that this class is indeed
       ** included in test coverage...
       *******************************************************************************/
      void noop()
      {
         System.out.println("noop");
      }
   }



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

      if(this.recordLookupHelper == null)
      {
         initializeRecordLookupHelper(runBackendStepInput);
      }

      SyncProcessConfig config = getSyncProcessConfig();

      String sourceTableKeyField             = config.sourceTableKeyField;
      String destinationTableForeignKeyField = config.destinationTableForeignKey;
      String destinationTableName            = config.destinationTable;
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_DESTINATION_TABLE, destinationTableName);

      if(!StringUtils.hasContent(sourceTableKeyField))
      {
         throw (new IllegalStateException("Missing sourceTableKeyField in config for " + getClass().getSimpleName()));
      }

      if(!StringUtils.hasContent(destinationTableForeignKeyField))
      {
         throw (new IllegalStateException("Missing destinationTableForeignKey in config for " + getClass().getSimpleName()));
      }

      if(!StringUtils.hasContent(destinationTableName))
      {
         throw (new IllegalStateException("Missing destinationTable in config for " + getClass().getSimpleName()));
      }

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
         QQueryFilter filter = getExistingRecordQueryFilter(runBackendStepInput, sourceKeyList);
         queryInput.setFilter(filter);
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         existingRecordsByForeignKey = CollectionUtils.recordsToMap(queryOutput.getRecords(), destinationTableForeignKeyField);
      }

      /////////////////////////////////////////////////////////////////
      // foreach source record, build the record we'll insert/update //
      /////////////////////////////////////////////////////////////////
      QFieldMetaData destinationForeignKeyField = runBackendStepInput.getInstance().getTable(destinationTableName).getField(destinationTableForeignKeyField);
      for(QRecord sourceRecord : runBackendStepInput.getRecords())
      {
         Serializable sourceKeyValue = sourceRecord.getValue(sourceTableKeyField);

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

         /////////////////////////////////////////////////////////////////////////////////////////////////
         // look for the existing record - note - we may need to type-convert here, the sourceKey value //
         // from the source table to the destinationKey.  e.g., if source table had an integer, and the //
         // destination has a string.                                                                   //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         Serializable sourceKeyValueInTargetFieldType = ValueUtils.getValueAsFieldType(destinationForeignKeyField.getType(), sourceKeyValue);
         QRecord      existingRecord                  = existingRecordsByForeignKey.get(sourceKeyValueInTargetFieldType);

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



   /*******************************************************************************
    ** If needed, init a record lookup helper for this process.
    *******************************************************************************/
   protected void initializeRecordLookupHelper(RunBackendStepInput runBackendStepInput) throws QException
   {
      this.recordLookupHelper = new RecordLookupHelper(runBackendStepInput);

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if there's only 1 record, don't bother preloading all records - just do the single lookup by the single needed key. //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(runBackendStepInput.getRecords().size() > 1)
      {
         for(Pair<String, String> pair : CollectionUtils.nonNullList(getLookupsToPreLoad()))
         {
            recordLookupHelper.preloadRecords(pair.getA(), pair.getB());
         }
      }
   }

}
