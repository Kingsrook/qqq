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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditSingleInput;
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
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** This class is for transforming records from a Source table to a Destination table.
 **
 ** The Source table has a (unique/primary) key field:  sourceTableKeyField,
 ** Which is matched against the Destination table's foreign-key:  destinationTableForeignKeyField
 *******************************************************************************/
public abstract class AbstractTableSyncTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(AbstractTableSyncTransformStep.class);

   private ProcessSummaryLine okToInsert           = StandardProcessSummaryLineProducer.getOkToInsertLine();
   private ProcessSummaryLine okToUpdate           = StandardProcessSummaryLineProducer.getOkToUpdateLine();
   private ProcessSummaryLine willNotInsert        = new ProcessSummaryLine(Status.INFO)
      .withMessageSuffix("because of this process' configuration.")
      .withSingularFutureMessage("will not be inserted ")
      .withPluralFutureMessage("will not be inserted ")
      .withSingularPastMessage("was not inserted ")
      .withPluralPastMessage("were not inserted ");
   private ProcessSummaryLine willNotUpdate        = new ProcessSummaryLine(Status.INFO)
      .withMessageSuffix("because of this process' configuration.")
      .withSingularFutureMessage("will not be updated ")
      .withPluralFutureMessage("will not be updated ")
      .withSingularPastMessage("was not updated ")
      .withPluralPastMessage("were not updated ");
   private ProcessSummaryLine errorMissingKeyField = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix("missing a value for the key field.")
      .withSingularFutureMessage("will not be synced, because it is ")
      .withPluralFutureMessage("will not be synced, because they are ")
      .withSingularPastMessage("was not synced, because it is ")
      .withPluralPastMessage("were not synced, because they are ");

   protected RunBackendStepInput  runBackendStepInput  = null;
   protected RunBackendStepOutput runBackendStepOutput = null;
   protected RecordLookupHelper   recordLookupHelper   = null;

   private QPossibleValueTranslator possibleValueTranslator;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> processSummaryLineList = StandardProcessSummaryLineProducer.toArrayList(okToInsert, okToUpdate, errorMissingKeyField);
      if(willNotInsert.getCount() > 0)
      {
         processSummaryLineList.add(willNotInsert);
      }
      if(willNotUpdate.getCount() > 0)
      {
         processSummaryLineList.add(willNotUpdate);
      }
      return (processSummaryLineList);
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
   public record SyncProcessConfig(String sourceTable, String sourceTableKeyField, String destinationTable, String destinationTableForeignKey, boolean performInserts, boolean performUpdates)
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
      this.runBackendStepOutput = runBackendStepOutput;

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

      if(this.recordLookupHelper == null)
      {
         initializeRecordLookupHelper(runBackendStepInput, runBackendStepInput.getRecords());
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // query to see if we already have those records in the destination (to determine insert/update) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      Map<Pair<String, Serializable>, QRecord> existingRecordsByForeignKey = getExistingRecordsByForeignKey(runBackendStepInput, destinationTableForeignKeyField, destinationTableName, sourceKeyList);

      /////////////////////////////////////////////////////////////////
      // foreach source record, build the record we'll insert/update //
      /////////////////////////////////////////////////////////////////
      QFieldMetaData    destinationForeignKeyField = runBackendStepInput.getInstance().getTable(destinationTableName).getField(destinationTableForeignKeyField);
      Set<Serializable> processedSourceKeys        = new HashSet<>();
      for(QRecord sourceRecord : runBackendStepInput.getRecords())
      {
         Serializable sourceKeyValue = sourceRecord.getValue(sourceTableKeyField);
         if(processedSourceKeys.contains(sourceKeyValue))
         {
            LOG.info("Skipping duplicated source-key within page", logPair("key", sourceKeyValue));
            continue;
         }
         processedSourceKeys.add(sourceKeyValue);

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

         //////////////////////////////////////////////////////////////
         // look for the existing record, to determine insert/update //
         //////////////////////////////////////////////////////////////
         QRecord existingRecord = getExistingRecord(existingRecordsByForeignKey, destinationForeignKeyField, sourceKeyValue);

         QRecord recordToStore;
         if(existingRecord != null && config.performUpdates)
         {
            recordToStore = existingRecord;
            okToUpdate.incrementCount();
         }
         else if(existingRecord == null && config.performInserts)
         {
            recordToStore = new QRecord();
            okToInsert.incrementCount();
         }
         else
         {
            if(existingRecord != null)
            {
               LOG.info("Skipping storing existing record because this sync process is set to not perform updates");
               willNotInsert.incrementCount();
            }
            else
            {
               LOG.info("Skipping storing new record because this sync process is set to not perform inserts");
               willNotUpdate.incrementCount();
            }
            continue;
         }

         ////////////////////////////////////////////////////////////////
         // if we received a record to store add to the output records //
         ////////////////////////////////////////////////////////////////
         recordToStore = populateRecordToStore(runBackendStepInput, recordToStore, sourceRecord);
         if(recordToStore != null)
         {
            runBackendStepOutput.addRecord(recordToStore);
         }
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
    **
    *******************************************************************************/
   protected QRecord getExistingRecord(Map<Pair<String, Serializable>, QRecord> existingRecordsByForeignKey, QFieldMetaData destinationForeignKeyField, Serializable sourceKeyValue)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////
      // note - we may need to type-convert here, the sourceKey value from the source table to        //
      // the destinationKey.  e.g., if source table had an integer, and the destination has a string. //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      Serializable sourceKeyValueInTargetFieldType = ValueUtils.getValueAsFieldType(destinationForeignKeyField.getType(), sourceKeyValue);
      return (existingRecordsByForeignKey.get(Pair.of(destinationForeignKeyField.getName(), sourceKeyValueInTargetFieldType)));
   }



   /*******************************************************************************
    ** Run the existingRecordQueryFilter - to look in the destinationTable for
    ** any records that may need an update (rather than an insert).
    **
    ** Generally returns a Map, keyed by a Pair of the destinationTableForeignKeyField
    ** and the value in that field.  But, for more complex use-cases, one can override
    ** the buildExistingRecordsMap method, to make different keys (e.g., if there are
    ** two possible destinationTableForeignKeyFields).
    *******************************************************************************/
   protected Map<Pair<String, Serializable>, QRecord> getExistingRecordsByForeignKey(RunBackendStepInput runBackendStepInput, String destinationTableForeignKeyField, String destinationTableName, List<Serializable> sourceKeyList) throws QException
   {
      if(sourceKeyList.isEmpty())
      {
         return (Collections.emptyMap());
      }

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(destinationTableName);
      getTransaction().ifPresent(queryInput::setTransaction);
      QQueryFilter filter = getExistingRecordQueryFilter(runBackendStepInput, sourceKeyList);
      queryInput.setFilter(filter);

      Collection<String> associationNamesToInclude = getAssociationNamesToInclude();
      if(CollectionUtils.nullSafeHasContents(associationNamesToInclude))
      {
         queryInput.setIncludeAssociations(true);
         queryInput.setAssociationNamesToInclude(associationNamesToInclude);
      }

      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (buildExistingRecordsMap(destinationTableForeignKeyField, queryOutput.getRecords()));
   }



   /*******************************************************************************
    ** Overridable point where you can, for example, keys in the existingRecordsMap
    ** with different fieldNames from the destinationTable.
    **
    ** Note, if you're overriding this method, you'll likely also want & need to
    ** override getExistingRecord.
    *******************************************************************************/
   protected Map<Pair<String, Serializable>, QRecord> buildExistingRecordsMap(String destinationTableForeignKeyField, List<QRecord> existingRecordList)
   {
      Map<Pair<String, Serializable>, QRecord> existingRecordsByForeignKey = new HashMap<>();
      for(QRecord record : existingRecordList)
      {
         existingRecordsByForeignKey.put(Pair.of(destinationTableForeignKeyField, record.getValue(destinationTableForeignKeyField)), record);
      }
      return (existingRecordsByForeignKey);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Collection<String> getAssociationNamesToInclude()
   {
      return null;
   }



   /*******************************************************************************
    ** If needed, init a record lookup helper for this process.
    *******************************************************************************/
   protected void initializeRecordLookupHelper(RunBackendStepInput runBackendStepInput, List<QRecord> sourceRecordList) throws QException
   {
      this.recordLookupHelper = new RecordLookupHelper();

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



   /*******************************************************************************
    ** Let the subclass "easily" add an audit to be inserted on the Execute step.
    *******************************************************************************/
   protected void addAuditForExecuteStep(AuditSingleInput auditSingleInput)
   {
      if(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE.equals(this.runBackendStepInput.getStepName()))
      {
         this.runBackendStepOutput.addAuditSingleInput(auditSingleInput);
      }
   }

}
