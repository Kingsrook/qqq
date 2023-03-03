/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.processes.implementations.tablestats;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.actions.values.QPossibleValueTranslator;
import com.kingsrook.qqq.backend.core.actions.values.QValueFormatter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByAggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableStatsStep implements BackendStep
{
   private static final QLogger LOG = QLogger.getLogger(TableStatsStep.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      try
      {
         String tableName  = runBackendStepInput.getValueString("tableName");
         String fieldName  = runBackendStepInput.getValueString("fieldName");
         String orderBy    = runBackendStepInput.getValueString("orderBy");
         String filterJSON = runBackendStepInput.getValueString("filterJSON");

         /////////////////////////////////////////
         // make sure user may query this table //
         /////////////////////////////////////////
         PermissionsHelper.checkTablePermissionThrowing(new QueryInput().withTableName(tableName), TablePermissionSubType.READ);

         QQueryFilter filter = null;
         if(StringUtils.hasContent(filterJSON))
         {
            filter = JsonUtils.toObject(filterJSON, QQueryFilter.class);

            ///////////////////////////////////////////////////////////////
            // ... remove any order-by that may have been in that filter //
            ///////////////////////////////////////////////////////////////
            filter.setOrderBys(new ArrayList<>());
         }
         else
         {
            filter = new QQueryFilter();
         }

         QTableMetaData table = QContext.getQInstance().getTable(tableName);
         QFieldMetaData field = table.getField(fieldName);

         ////////////////////////////////////////////
         // do a count query grouped by this field //
         ////////////////////////////////////////////
         Aggregate aggregate = new Aggregate(table.getPrimaryKeyField(), AggregateOperator.COUNT);
         GroupBy   groupBy   = new GroupBy(field.getType(), fieldName);

         if(StringUtils.hasContent(orderBy))
         {
            if(orderBy.equalsIgnoreCase("count.asc"))
            {
               filter.withOrderBy(new QFilterOrderByAggregate(aggregate, true));
            }
            else if(orderBy.equalsIgnoreCase("count.desc"))
            {
               filter.withOrderBy(new QFilterOrderByAggregate(aggregate, false));
            }
            else if(orderBy.equalsIgnoreCase(fieldName + ".asc"))
            {
               filter.withOrderBy(new QFilterOrderBy(fieldName, true));
            }
            else if(orderBy.equalsIgnoreCase(fieldName + ".desc"))
            {
               filter.withOrderBy(new QFilterOrderBy(fieldName, false));
            }
            else
            {
               LOG.info("Unrecognized orderBy: " + orderBy);
            }
         }

         ///////////////////////////////////////////////////////////////////////////////////////////////////
         // always add order by to break ties.  these will be the default too, if input didn't supply one //
         ///////////////////////////////////////////////////////////////////////////////////////////////////
         filter.withOrderBy(new QFilterOrderByAggregate(aggregate, false));
         filter.withOrderBy(new QFilterOrderBy(fieldName));

         Integer        limit          = 1000; // too big?
         AggregateInput aggregateInput = new AggregateInput();
         aggregateInput.withAggregate(aggregate);
         aggregateInput.withGroupBy(groupBy);
         aggregateInput.setTableName(tableName);
         aggregateInput.setFilter(filter);
         aggregateInput.setLimit(limit);
         AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

         ArrayList<QRecord> valueCounts = new ArrayList<>();
         for(AggregateResult result : aggregateOutput.getResults())
         {
            Serializable value = result.getGroupByValue(groupBy);
            Integer      count = ValueUtils.getValueAsInteger(result.getAggregateValue(aggregate));
            valueCounts.add(new QRecord().withValue(fieldName, value).withValue("count", count));
         }
         QFieldMetaData countField = new QFieldMetaData("count", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS).withLabel("Count");

         QPossibleValueTranslator qPossibleValueTranslator = new QPossibleValueTranslator();
         qPossibleValueTranslator.translatePossibleValuesInRecords(table, valueCounts, null, null);
         QValueFormatter.setDisplayValuesInRecords(List.of(table.getField(fieldName), countField), valueCounts);

         runBackendStepOutput.addValue("valueCounts", valueCounts);

         /////////////////////////////////////////////////////
         // now do individual statistics as a pseudo-record //
         /////////////////////////////////////////////////////
         QFieldMetaData countNonNullField  = new QFieldMetaData("count", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS);
         QFieldMetaData countDistinctField = new QFieldMetaData("countDistinct", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS);
         QFieldMetaData sumField           = new QFieldMetaData("sum", QFieldType.DECIMAL).withDisplayFormat(field.getDisplayFormat());
         QFieldMetaData avgField           = new QFieldMetaData("average", QFieldType.DECIMAL).withDisplayFormat(field.getDisplayFormat());
         QFieldMetaData minField           = new QFieldMetaData("min", field.getType()).withDisplayFormat(field.getDisplayFormat());
         QFieldMetaData maxField           = new QFieldMetaData("max", field.getType()).withDisplayFormat(field.getDisplayFormat());

         boolean doCountDistinct = true;
         boolean doSum           = true;
         boolean doAvg           = true;
         boolean doMin           = true;
         boolean doMax           = true;
         if(field.getType().isStringLike())
         {
            doSum = false;
            doAvg = false;
         }
         if(field.getType().equals(QFieldType.BOOLEAN))
         {
            doSum = false;
            doAvg = false;
            doMin = false;
            doMax = false;
         }
         if(field.getType().equals(QFieldType.DATE) || field.getType().equals(QFieldType.DATE_TIME))
         {
            doSum = false;
            doAvg = false; // could this be done?
         }
         if(StringUtils.hasContent(field.getPossibleValueSourceName()))
         {
            doSum = false;
            doAvg = false;
            doMin = false;
            doMax = false;
         }

         ArrayList<QFieldMetaData> fields = new ArrayList<>();
         fields.add(countNonNullField);
         fields.add(countDistinctField);
         if(doSum)
         {
            fields.add(sumField);
         }
         if(doAvg)
         {
            fields.add(avgField);
         }
         if(doMin)
         {
            fields.add(minField);
         }
         if(doMax)
         {
            fields.add(maxField);
         }

         QRecord statsRecord = new QRecord();

         if(valueCounts.size() < limit)
         {
            statsRecord.setValue(countDistinctField.getName(), valueCounts.size());
            doCountDistinct = false;
         }

         Aggregate      countNonNullAggregate  = new Aggregate(fieldName, AggregateOperator.COUNT);
         Aggregate      countDistinctAggregate = new Aggregate(fieldName, AggregateOperator.COUNT_DISTINCT);
         Aggregate      sumAggregate           = new Aggregate(fieldName, AggregateOperator.SUM);
         Aggregate      avgAggregate           = new Aggregate(fieldName, AggregateOperator.AVG);
         Aggregate      minAggregate           = new Aggregate(fieldName, AggregateOperator.MIN);
         Aggregate      maxAggregate           = new Aggregate(fieldName, AggregateOperator.MAX);
         AggregateInput statsAggregateInput    = new AggregateInput();
         statsAggregateInput.withAggregate(countNonNullAggregate);
         if(doCountDistinct)
         {
            statsAggregateInput.withAggregate(countDistinctAggregate);
         }
         if(doSum)
         {
            statsAggregateInput.withAggregate(sumAggregate);
         }
         if(doAvg)
         {
            statsAggregateInput.withAggregate(avgAggregate);
         }
         if(doMin)
         {
            statsAggregateInput.withAggregate(minAggregate);
         }
         if(doMax)
         {
            statsAggregateInput.withAggregate(maxAggregate);
         }

         if(CollectionUtils.nullSafeHasContents(statsAggregateInput.getAggregates()))
         {
            statsAggregateInput.setTableName(tableName);
            filter.setOrderBys(new ArrayList<>());
            statsAggregateInput.setFilter(filter);
            AggregateOutput statsAggregateOutput = new AggregateAction().execute(statsAggregateInput);
            AggregateResult statsAggregateResult = statsAggregateOutput.getResults().get(0);

            statsRecord.setValue(countNonNullField.getName(), statsAggregateResult.getAggregateValue(countNonNullAggregate));
            if(doCountDistinct)
            {
               statsRecord.setValue(countDistinctField.getName(), statsAggregateResult.getAggregateValue(countDistinctAggregate));
            }
            if(doSum)
            {
               statsRecord.setValue(sumField.getName(), statsAggregateResult.getAggregateValue(sumAggregate));
            }
            if(doAvg)
            {
               statsRecord.setValue(avgField.getName(), statsAggregateResult.getAggregateValue(avgAggregate));
            }
            if(doMin)
            {
               statsRecord.setValue(minField.getName(), statsAggregateResult.getAggregateValue(minAggregate));
            }
            if(doMax)
            {
               statsRecord.setValue(maxField.getName(), statsAggregateResult.getAggregateValue(maxAggregate));
            }
         }

         QInstanceEnricher qInstanceEnricher = new QInstanceEnricher(null);
         fields.forEach(qInstanceEnricher::enrichField);

         QValueFormatter.setDisplayValuesInRecord(fields, statsRecord);

         runBackendStepOutput.addValue("statsFields", fields);
         runBackendStepOutput.addValue("statsRecord", statsRecord);
      }
      catch(Exception e)
      {
         throw new QException("Error calculating stats", e);
      }
   }

}
