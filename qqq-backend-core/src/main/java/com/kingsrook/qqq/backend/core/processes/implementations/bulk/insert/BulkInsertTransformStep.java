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
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Transform step for generic table bulk-insert ETL process
 *******************************************************************************/
public class BulkInsertTransformStep extends AbstractTransformStep
{
   private ProcessSummaryLine                 okSummary        = new ProcessSummaryLine(Status.OK);
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
         existingKeys.put(uniqueKey, getExistingKeys(runBackendStepInput, uniqueKey));
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

      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // no transformation needs to be done - just pass records through from input to output, if they don't violate any UK's //
      /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      ///////////////////////////////////////////////////
      // if there are no UK's, just output all records //
      ///////////////////////////////////////////////////
      if(existingKeys.isEmpty())
      {
         runBackendStepOutput.setRecords(runBackendStepInput.getRecords());
         okSummary.incrementCount(runBackendStepInput.getRecords().size());
      }
      else
      {
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
               List<Serializable> keyValues = getKeyValues(uniqueKey, record);
               if(existingKeys.get(uniqueKey).contains(keyValues) || keysInThisFile.get(uniqueKey).contains(keyValues))
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
                  List<Serializable> keyValues = getKeyValues(uniqueKey, record);
                  keysInThisFile.get(uniqueKey).add(keyValues);
               }
               okSummary.incrementCount();
               runBackendStepOutput.addRecord(record);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Set<List<Serializable>> getExistingKeys(RunBackendStepInput runBackendStepInput, UniqueKey uniqueKey) throws QException
   {
      return (getExistingKeys(runBackendStepInput, uniqueKey.getFieldNames()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Set<List<Serializable>> getExistingKeys(RunBackendStepInput runBackendStepInput, List<String> ukFieldNames) throws QException
   {
      Set<List<Serializable>> existingRecords = new HashSet<>();
      if(ukFieldNames != null)
      {
         QueryInput queryInput = new QueryInput(runBackendStepInput.getInstance());
         queryInput.setSession(runBackendStepInput.getSession());
         queryInput.setTableName(runBackendStepInput.getTableName());
         getTransaction().ifPresent(queryInput::setTransaction);

         QQueryFilter filter = new QQueryFilter();
         if(ukFieldNames.size() == 1)
         {
            List<Serializable> values = runBackendStepInput.getRecords().stream()
               .map(r -> r.getValue(ukFieldNames.get(0)))
               .collect(Collectors.toList());
            filter.addCriteria(new QFilterCriteria(ukFieldNames.get(0), QCriteriaOperator.IN, values));
         }
         else
         {
            filter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
            for(QRecord record : runBackendStepInput.getRecords())
            {
               QQueryFilter subFilter = new QQueryFilter();
               filter.addSubFilter(subFilter);
               for(String fieldName : ukFieldNames)
               {
                  subFilter.addCriteria(new QFilterCriteria(fieldName, QCriteriaOperator.EQUALS, List.of(record.getValue(fieldName))));
               }
            }
         }

         queryInput.setFilter(filter);
         QueryOutput queryOutput = new QueryAction().execute(queryInput);
         for(QRecord record : queryOutput.getRecords())
         {
            List<Serializable> keyValues = getKeyValues(ukFieldNames, record);
            existingRecords.add(keyValues);
         }
      }

      return (existingRecords);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<Serializable> getKeyValues(UniqueKey uniqueKey, QRecord record)
   {
      return (getKeyValues(uniqueKey.getFieldNames(), record));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<Serializable> getKeyValues(List<String> fieldNames, QRecord record)
   {
      List<Serializable> keyValues = new ArrayList<>();
      for(String fieldName : fieldNames)
      {
         keyValues.add(record.getValue(fieldName));
      }
      return keyValues;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      String tableLabel = table == null ? "" : table.getLabel();

      okSummary
         .withSingularFutureMessage(tableLabel + " record will be inserted")
         .withPluralFutureMessage(tableLabel + " records will be inserted")
         .withSingularPastMessage(tableLabel + " record was inserted")
         .withPluralPastMessage(tableLabel + " records were inserted");

      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okSummary.addSelfToListIfAnyCount(rs);

      for(Map.Entry<UniqueKey, ProcessSummaryLine> entry : ukErrorSummaries.entrySet())
      {
         UniqueKey          uniqueKey      = entry.getKey();
         ProcessSummaryLine ukErrorSummary = entry.getValue();
         String             ukErrorSuffix  = " inserted, because they contain a duplicate key (" + getUkDescription(uniqueKey.getFieldNames()) + ")";

         ukErrorSummary
            .withSingularFutureMessage(tableLabel + " record will not be" + ukErrorSuffix)
            .withPluralFutureMessage(tableLabel + " records will not be" + ukErrorSuffix)
            .withSingularPastMessage(tableLabel + " record was not" + ukErrorSuffix)
            .withPluralPastMessage(tableLabel + " records were not" + ukErrorSuffix);

         ukErrorSummary.addSelfToListIfAnyCount(rs);
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getUkDescription(List<String> ukFieldNames)
   {
      List<String> fieldLabels = new ArrayList<>();

      for(String fieldName : ukFieldNames)
      {
         fieldLabels.add(table.getField(fieldName).getLabel());
      }

      return (StringUtils.joinWithCommasAndAnd(fieldLabels));
   }

}
