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
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.AggregateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByAggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableStatsStep implements BackendStep
{

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
         String filterJSON = runBackendStepInput.getValueString("filterJSON");

         QQueryFilter filter = null;
         if(StringUtils.hasContent(filterJSON))
         {
            filter = JsonUtils.toObject(filterJSON, QQueryFilter.class);
         }
         else
         {
            filter = new QQueryFilter();
         }

         QTableMetaData table = QContext.getQInstance().getTable(tableName);
         QFieldMetaData field = table.getField(fieldName);

         Aggregate aggregate = new Aggregate(fieldName, AggregateOperator.COUNT);
         GroupBy   groupBy   = new GroupBy(field.getType(), fieldName);

         Integer        limit          = 1000; // too big?
         AggregateInput aggregateInput = new AggregateInput();
         aggregateInput.withAggregate(aggregate);
         aggregateInput.withGroupBy(groupBy);
         aggregateInput.setTableName(tableName);
         aggregateInput.setFilter(filter.withOrderBy(new QFilterOrderByAggregate(aggregate, false)));
         aggregateInput.setLimit(limit);
         AggregateOutput aggregateOutput = new AggregateAction().execute(aggregateInput);

         ArrayList<QRecord> valueCounts = new ArrayList<>();
         for(AggregateResult result : aggregateOutput.getResults())
         {
            Serializable value = result.getGroupByValue(groupBy);
            Integer      count = ValueUtils.getValueAsInteger(result.getAggregateValue(aggregate));
            valueCounts.add(new QRecord().withValue("value", value).withValue("count", count));
         }
         runBackendStepOutput.addValue("valueCounts", valueCounts);

         if(valueCounts.size() < limit)
         {
            runBackendStepOutput.addValue("countDistinct", valueCounts.size());
         }
         else
         {
            Aggregate      countDistinctAggregate      = new Aggregate(fieldName, AggregateOperator.COUNT_DISTINCT);
            AggregateInput countDistinctAggregateInput = new AggregateInput();
            countDistinctAggregateInput.withAggregate(countDistinctAggregate);
            countDistinctAggregateInput.setTableName(tableName);
            countDistinctAggregateInput.setFilter(filter.withOrderBy(new QFilterOrderByAggregate(aggregate, false)));
            AggregateOutput countDistinctAggregateOutput = new AggregateAction().execute(countDistinctAggregateInput);
            AggregateResult countDistinctAggregateResult = countDistinctAggregateOutput.getResults().get(0);
            runBackendStepOutput.addValue("countDistinct", countDistinctAggregateResult.getAggregateValue(countDistinctAggregate));
         }
      }
      catch(Exception e)
      {
         throw new QException("Error calculating stats", e);
      }
   }

}
