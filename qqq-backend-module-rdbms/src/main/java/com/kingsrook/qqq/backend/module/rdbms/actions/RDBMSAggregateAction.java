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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSAggregateAction extends AbstractRDBMSAction implements AggregateInterface
{
   private static final Logger LOG = LogManager.getLogger(RDBMSAggregateAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateOutput execute(AggregateInput aggregateInput) throws QException
   {
      try
      {
         QTableMetaData table = aggregateInput.getTable();

         JoinsContext joinsContext  = new JoinsContext(aggregateInput.getInstance(), table.getName(), aggregateInput.getQueryJoins());
         String       fromClause    = makeFromClause(aggregateInput.getInstance(), table.getName(), joinsContext);
         List<String> selectClauses = buildSelectClauses(aggregateInput, joinsContext);

         String sql = "SELECT " + StringUtils.join(", ", selectClauses)
            + " FROM " + fromClause;

         QQueryFilter       filter = aggregateInput.getFilter();
         List<Serializable> params = new ArrayList<>();
         if(filter != null && filter.hasAnyCriteria())
         {
            sql += " WHERE " + makeWhereClause(aggregateInput.getInstance(), table, joinsContext, filter, params);
         }

         if(CollectionUtils.nullSafeHasContents(aggregateInput.getGroupByFieldNames()))
         {
            sql += " GROUP BY " + makeGroupByClause(aggregateInput, joinsContext);
         }

         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            sql += " ORDER BY " + makeOrderByClause(table, filter.getOrderBys(), joinsContext);
         }

         // todo sql customization - can edit sql and/or param list
         LOG.debug(sql); // todo not commit - downgrade to trace

         AggregateOutput       rs      = new AggregateOutput();
         List<AggregateResult> results = new ArrayList<>();
         rs.setResults(results);

         try(Connection connection = getConnection(aggregateInput))
         {
            QueryManager.executeStatement(connection, sql, ((ResultSet resultSet) ->
            {
               while(resultSet.next())
               {
                  AggregateResult result = new AggregateResult();
                  results.add(result);

                  int selectionIndex = 1;
                  for(String groupByFieldName : CollectionUtils.nonNullList(aggregateInput.getGroupByFieldNames()))
                  {
                     JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(groupByFieldName);
                     Serializable                          value                    = getFieldValueFromResultSet(fieldAndTableNameOrAlias.field(), resultSet, selectionIndex++);
                     result.withGroupByValue(groupByFieldName, value);
                  }

                  for(Aggregate aggregate : aggregateInput.getAggregates())
                  {
                     JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(aggregate.getFieldName());
                     QFieldMetaData                        field                    = fieldAndTableNameOrAlias.field();

                     if(field.getType().equals(QFieldType.INTEGER) && aggregate.getOperator().equals(AggregateOperator.AVG))
                     {
                        field = new QFieldMetaData().withType(QFieldType.DECIMAL);
                     }

                     Serializable value = getFieldValueFromResultSet(field, resultSet, selectionIndex++);
                     result.withAggregateValue(aggregate, value);
                  }
               }

            }), params);
         }

         return rs;
      }
      catch(Exception e)
      {
         LOG.warn("Error executing aggregate", e);
         throw new QException("Error executing aggregate", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<String> buildSelectClauses(AggregateInput aggregateInput, JoinsContext joinsContext)
   {
      List<String> rs = new ArrayList<>();

      for(String groupByFieldName : CollectionUtils.nonNullList(aggregateInput.getGroupByFieldNames()))
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(groupByFieldName);
         rs.add(escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(fieldAndTableNameOrAlias.field())));
      }

      for(Aggregate aggregate : aggregateInput.getAggregates())
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(aggregate.getFieldName());
         rs.add(aggregate.getOperator() + "(" + escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(fieldAndTableNameOrAlias.field())) + ")");
      }
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeGroupByClause(AggregateInput aggregateInput, JoinsContext joinsContext)
   {
      List<String> columns = new ArrayList<>();
      for(String groupByFieldName : aggregateInput.getGroupByFieldNames())
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(groupByFieldName);
         columns.add(escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(fieldAndTableNameOrAlias.field())));
      }

      return (StringUtils.join(",", columns));
   }

}
