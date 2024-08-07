/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.garbagecollector;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class GenericGarbageCollectorExecuteStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(GenericGarbageCollectorExecuteStep.class);

   private ProcessSummaryLine partitionsLine = new ProcessSummaryLine(Status.INFO)
      .withSingularPastMessage("partition was processed")
      .withPluralPastMessage("partitions were processed");

   private ProcessSummaryLine deletedLine = new ProcessSummaryLine(Status.OK)
      .withSingularPastMessage("record was deleted")
      .withPluralPastMessage("records were deleted");

   private ProcessSummaryLine warningLine = new ProcessSummaryLine(Status.WARNING, "had an warning");
   private ProcessSummaryLine errorLine   = new ProcessSummaryLine(Status.ERROR, "had an error");



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      String         tableName = runBackendStepInput.getValueString("table");
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);
      if(table == null)
      {
         throw new QUserFacingException("Unrecognized table: " + tableName);
      }

      String         fieldName = runBackendStepInput.getValueString("field");
      QFieldMetaData field     = table.getFields().get(fieldName);
      if(field == null)
      {
         throw new QUserFacingException("Unrecognized field: " + fieldName);
      }

      if(!QFieldType.DATE_TIME.equals(field.getType()) && !QFieldType.DATE.equals(field.getType()))
      {
         throw new QUserFacingException("Field " + field + " is not a date-time or date type field.");
      }

      Integer daysBack = runBackendStepInput.getValueInteger("daysBack");
      if(daysBack == null || daysBack < 0)
      {
         throw new QUserFacingException("Illegal value for daysBack: " + daysBack + "; Must be positive.");
      }

      Integer maxPageSize = runBackendStepInput.getValueInteger("maxPageSize");
      if(maxPageSize == null || maxPageSize < 0)
      {
         throw new QUserFacingException("Illegal value for maxPageSize: " + maxPageSize + "; Must be positive.");
      }

      execute(table, field, daysBack, maxPageSize);

      deletedLine.prepareForFrontend(true);
      partitionsLine.prepareForFrontend(true);

      ArrayList<ProcessSummaryLineInterface> processSummary = new ArrayList<>();
      processSummary.add(partitionsLine);
      processSummary.add(deletedLine);
      warningLine.addSelfToListIfAnyCount(processSummary);
      errorLine.addSelfToListIfAnyCount(processSummary);
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, processSummary);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void execute(QTableMetaData table, QFieldMetaData field, Integer daysBack, Integer maxPageSize) throws QException
   {
      Instant maxDate = Instant.now().minusSeconds(daysBack * 60 * 60 * 24);
      Instant minDate = findMinDateInTable(table, field);

      processDateRange(table, field, maxPageSize, minDate, maxDate);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void processDateRange(QTableMetaData table, QFieldMetaData field, Integer maxPageSize, Instant minDate, Instant maxDate) throws QException
   {
      partitionsLine.incrementCount();

      LOG.info("Counting", logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));
      Integer count = count(table, field, minDate, maxDate);
      LOG.info("Count", logPair("count", count), logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));

      if(count == 0)
      {
         LOG.info("0 rows in this partition - nothing to delete", logPair("count", count), logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));
      }
      else if(count <= maxPageSize)
      {
         LOG.info("Deleting", logPair("count", count), logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));
         delete(table, field, minDate, maxDate);
      }
      else
      {
         LOG.info("Too many rows", logPair("count", count), logPair("maxPageSize", maxPageSize), logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));
         partition(table, field, minDate, maxDate, count, maxPageSize);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void partition(QTableMetaData table, QFieldMetaData field, Instant minDate, Instant maxDate, Integer count, Integer maxPageSize) throws QException
   {
      int  noOfPartitions    = (int) Math.ceil((float) count / (float) maxPageSize);
      long milliDiff         = maxDate.toEpochMilli() - minDate.toEpochMilli();
      long milliPerPartition = milliDiff / noOfPartitions;

      if(milliPerPartition < 1000)
      {
         throw (new QUserFacingException("To find a maxPageSize under " + String.format("%,d", maxPageSize) + ", the partition size would become smaller than 1 second (between " + minDate + " and " + maxDate + " there are " + String.format("%,d", count) + " rows) - you must use a larger maxPageSize to continue."));
      }

      LOG.info("Partitioning", logPair("count", count), logPair("noOfPartitions", noOfPartitions), logPair("milliDiff", milliDiff), logPair("milliPerPartition", milliPerPartition), logPair("table", table.getName()), logPair("field", field.getName()), logPair("minDate", minDate), logPair("maxDate", maxDate));
      for(int i = 0; i < noOfPartitions; i++)
      {
         maxDate = minDate.plusMillis(milliPerPartition);
         processDateRange(table, field, maxPageSize, minDate, maxDate);
         minDate = maxDate;
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void delete(QTableMetaData table, QFieldMetaData field, Instant minDate, Instant maxDate) throws QException
   {
      DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(table.getName())
         .withQueryFilter(new QQueryFilter(new QFilterCriteria(field.getName(), QCriteriaOperator.BETWEEN, minDate, maxDate))));

      deletedLine.incrementCount(deleteOutput.getDeletedRecordCount());

      if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
      {
         warningLine.incrementCount(deleteOutput.getRecordsWithWarnings().size());
      }

      if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
      {
         errorLine.incrementCount(deleteOutput.getRecordsWithErrors().size());
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Integer count(QTableMetaData table, QFieldMetaData field, Instant minDate, Instant maxDate) throws QException
   {
      Aggregate      countDateField = new Aggregate(field.getName(), AggregateOperator.COUNT).withFieldType(QFieldType.INTEGER);
      AggregateInput aggregateInput = new AggregateInput();
      aggregateInput.setTableName(table.getName());
      aggregateInput.withFilter(new QQueryFilter(new QFilterCriteria(field.getName(), QCriteriaOperator.BETWEEN, minDate, maxDate)));
      aggregateInput.withAggregate(countDateField);

      AggregateOutput       aggregateOutput = new AggregateAction().execute(aggregateInput);
      List<AggregateResult> results         = aggregateOutput.getResults();
      if(CollectionUtils.nullSafeIsEmpty(results))
      {
         throw (new QUserFacingException("Could not count rows table (null or empty aggregate result)."));
      }

      return (ValueUtils.getValueAsInteger(results.get(0).getAggregateValue(countDateField)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Instant findMinDateInTable(QTableMetaData table, QFieldMetaData field) throws QException
   {
      Aggregate      minDate        = new Aggregate(field.getName(), AggregateOperator.MIN);
      AggregateInput aggregateInput = new AggregateInput();
      aggregateInput.setTableName(table.getName());
      aggregateInput.withAggregate(minDate);

      AggregateOutput       aggregateOutput = new AggregateAction().execute(aggregateInput);
      List<AggregateResult> results         = aggregateOutput.getResults();
      if(CollectionUtils.nullSafeIsEmpty(results))
      {
         throw (new QUserFacingException("Could not find min date value in table (null or empty aggregate result)."));
      }

      return (ValueUtils.getValueAsInstant(results.get(0).getAggregateValue(minDate)));
   }

}
