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
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.interfaces.AggregateInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ActionTimeoutHelper;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.AggregateResult;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSAggregateAction extends AbstractRDBMSAction implements AggregateInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSAggregateAction.class);

   private ActionTimeoutHelper actionTimeoutHelper;



   /*******************************************************************************
    **
    *******************************************************************************/
   public AggregateOutput execute(AggregateInput aggregateInput) throws QException
   {
      try
      {
         QTableMetaData table = aggregateInput.getTable();

         QQueryFilter filter       = clonedOrNewFilter(aggregateInput.getFilter());
         JoinsContext joinsContext = new JoinsContext(aggregateInput.getInstance(), table.getName(), aggregateInput.getQueryJoins(), filter);

         List<Serializable> params = new ArrayList<>();

         String       fromClause    = makeFromClause(aggregateInput.getInstance(), table.getName(), joinsContext, params);
         List<String> selectClauses = buildSelectClauses(aggregateInput, joinsContext);

         String sql = "SELECT " + StringUtils.join(", ", selectClauses)
            + " FROM " + fromClause
            + " WHERE " + makeWhereClause(joinsContext, filter, params);

         if(CollectionUtils.nullSafeHasContents(aggregateInput.getGroupBys()))
         {
            sql += " GROUP BY " + makeGroupByClause(aggregateInput, joinsContext);
         }

         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            sql += " ORDER BY " + makeOrderByClause(table, filter.getOrderBys(), joinsContext);
         }

         if(aggregateInput.getLimit() != null)
         {
            sql += " LIMIT " + aggregateInput.getLimit();
         }

         // todo sql customization - can edit sql and/or param list

         setSqlAndJoinsInQueryStat(sql, joinsContext);

         AggregateOutput       rs      = new AggregateOutput();
         List<AggregateResult> results = new ArrayList<>();
         rs.setResults(results);

         Long mark = System.currentTimeMillis();

         try(Connection connection = getConnection(aggregateInput))
         {
            statement = connection.prepareStatement(sql);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // set up & start an actionTimeoutHelper (note, internally it'll deal with the time being null or negative as meaning not to timeout) //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            actionTimeoutHelper = new ActionTimeoutHelper(aggregateInput.getTimeoutSeconds(), TimeUnit.SECONDS, new StatementTimeoutCanceller(statement, sql));
            actionTimeoutHelper.start();

            QueryManager.executeStatement(statement, sql, ((ResultSet resultSet) ->
            {
               /////////////////////////////////////////////////////////////////////////
               // once we've started getting results, go ahead and cancel the timeout //
               /////////////////////////////////////////////////////////////////////////
               actionTimeoutHelper.cancel();

               while(resultSet.next())
               {
                  setQueryStatFirstResultTime();

                  AggregateResult result = new AggregateResult();
                  results.add(result);

                  int selectionIndex = 1;
                  for(GroupBy groupBy : CollectionUtils.nonNullList(aggregateInput.getGroupBys()))
                  {
                     Serializable value = getFieldValueFromResultSet(groupBy.getType(), resultSet, selectionIndex++);
                     result.withGroupByValue(groupBy, value);
                  }

                  for(Aggregate aggregate : aggregateInput.getAggregates())
                  {
                     JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(aggregate.getFieldName());
                     QFieldMetaData                        field                    = fieldAndTableNameOrAlias.field();

                     QFieldType fieldType = aggregate.getFieldType();
                     if(fieldType == null)
                     {
                        if((field.getType().equals(QFieldType.INTEGER) || field.getType().equals(QFieldType.LONG)) && (aggregate.getOperator().equals(AggregateOperator.AVG)))
                        {
                           fieldType = QFieldType.DECIMAL;
                        }
                     }

                     if(fieldType != null)
                     {
                        field = new QFieldMetaData().withType(fieldType);
                     }

                     Serializable value = getFieldValueFromResultSet(field, resultSet, selectionIndex++);
                     result.withAggregateValue(aggregate, value);
                  }
               }

               /////////////////////////////////////////////////////////////////
               // in case there were no results, set the firstResultTime here //
               /////////////////////////////////////////////////////////////////
               setQueryStatFirstResultTime();

            }), params);
         }

         logSQL(sql, params, mark);

         return rs;
      }
      catch(Exception e)
      {
         if(actionTimeoutHelper != null && actionTimeoutHelper.getDidTimeout())
         {
            setQueryStatFirstResultTime();
            throw (new QUserFacingException("Aggregate query timed out."));
         }

         if(isCancelled)
         {
            throw (new QUserFacingException("Aggregate query was cancelled."));
         }

         LOG.warn("Error executing aggregate", e);
         throw new QException("Error executing aggregate", e);
      }
      finally
      {
         if(actionTimeoutHelper != null)
         {
            /////////////////////////////////////////
            // make sure the timeout got cancelled //
            /////////////////////////////////////////
            actionTimeoutHelper.cancel();
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<String> buildSelectClauses(AggregateInput aggregateInput, JoinsContext joinsContext)
   {
      List<String> rs = new ArrayList<>();

      for(GroupBy groupBy : CollectionUtils.nonNullList(aggregateInput.getGroupBys()))
      {
         rs.add(getSingleGroupByClause(groupBy, joinsContext));
      }

      for(Aggregate aggregate : aggregateInput.getAggregates())
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(aggregate.getFieldName());
         rs.add(aggregate.getOperator().getSqlPrefix() + escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(fieldAndTableNameOrAlias.field())) + ")");
      }
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeGroupByClause(AggregateInput aggregateInput, JoinsContext joinsContext)
   {
      List<String> columns = new ArrayList<>();
      for(GroupBy groupBy : CollectionUtils.nonNullList(aggregateInput.getGroupBys()))
      {
         columns.add(getSingleGroupByClause(groupBy, joinsContext));
      }

      return (StringUtils.join(",", columns));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void cancelAction()
   {
      doCancelQuery();
   }

}
