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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.querystats.QueryStat;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSQueryAction extends AbstractRDBMSAction implements QueryInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSQueryAction.class);

   private QueryStat queryStat;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      try
      {
         QTableMetaData table     = queryInput.getTable();
         String         tableName = queryInput.getTableName();

         StringBuilder sql = new StringBuilder(makeSelectClause(queryInput));

         JoinsContext joinsContext = new JoinsContext(queryInput.getInstance(), tableName, queryInput.getQueryJoins(), queryInput.getFilter());
         sql.append(" FROM ").append(makeFromClause(queryInput.getInstance(), tableName, joinsContext));

         QQueryFilter       filter = queryInput.getFilter();
         List<Serializable> params = new ArrayList<>();

         sql.append(" WHERE ").append(makeWhereClause(queryInput.getInstance(), queryInput.getSession(), table, joinsContext, filter, params));

         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            sql.append(" ORDER BY ").append(makeOrderByClause(table, filter.getOrderBys(), joinsContext));
         }

         if(filter != null && filter.getLimit() != null)
         {
            sql.append(" LIMIT ").append(filter.getLimit());

            if(filter.getSkip() != null)
            {
               // todo - other sql grammars?
               sql.append(" OFFSET ").append(filter.getSkip());
            }
         }

         // todo sql customization - can edit sql and/or param list

         Connection connection;
         boolean    needToCloseConnection = false;
         if(queryInput.getTransaction() != null && queryInput.getTransaction() instanceof RDBMSTransaction rdbmsTransaction)
         {
            LOG.debug("Using connection from queryInput [" + rdbmsTransaction.getConnection() + "]");
            connection = rdbmsTransaction.getConnection();
         }
         else
         {
            connection = getConnection(queryInput);
            needToCloseConnection = true;
         }

         ////////////////////////////////////////////////////////////////////////////
         // build the list of fields that will be processed in the result-set loop //
         ////////////////////////////////////////////////////////////////////////////
         List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values().stream()
            .filter(field -> filterOutHeavyFieldsIfNeeded(field, queryInput.getShouldFetchHeavyFields()))
            .toList());
         for(QueryJoin queryJoin : CollectionUtils.nonNullList(queryInput.getQueryJoins()))
         {
            if(queryJoin.getSelect())
            {
               QTableMetaData joinTable        = queryInput.getInstance().getTable(queryJoin.getJoinTable());
               String         tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();
               for(QFieldMetaData joinField : joinTable.getFields().values())
               {
                  if(filterOutHeavyFieldsIfNeeded(joinField, queryInput.getShouldFetchHeavyFields()))
                  {
                     fieldList.add(joinField.clone().withName(tableNameOrAlias + "." + joinField.getName()));
                  }
               }
            }
         }

         Long mark = System.currentTimeMillis();

         try
         {
            //////////////////////////////////////////////
            // execute the query - iterate over results //
            //////////////////////////////////////////////
            QueryOutput queryOutput = new QueryOutput(queryInput);

            PreparedStatement statement = createStatement(connection, sql.toString(), queryInput);
            QueryManager.executeStatement(statement, ((ResultSet resultSet) ->
            {
               ResultSetMetaData metaData = resultSet.getMetaData();
               while(resultSet.next())
               {
                  QRecord record = new QRecord();
                  record.setTableName(table.getName());
                  LinkedHashMap<String, Serializable> values = new LinkedHashMap<>();
                  record.setValues(values);

                  for(int i = 1; i <= metaData.getColumnCount(); i++)
                  {
                     QFieldMetaData qFieldMetaData = fieldList.get(i - 1);
                     Serializable   value          = getFieldValueFromResultSet(qFieldMetaData, resultSet, i);
                     values.put(qFieldMetaData.getName(), value);
                  }

                  queryOutput.addRecord(record);

                  if(queryInput.getAsyncJobCallback().wasCancelRequested())
                  {
                     LOG.info("Breaking query job, as requested.");
                     break;
                  }
               }

            }), params);

            logSQL(sql, params, mark);

            return queryOutput;
         }
         catch(Exception e)
         {
            logSQL(sql, params, mark);
            throw (e);
         }
         finally
         {
            if(needToCloseConnection)
            {
               connection.close();
            }
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error executing query", e);
         throw new QException("Error executing query", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeSelectClause(QueryInput queryInput) throws QException
   {
      QInstance       instance   = queryInput.getInstance();
      String          tableName  = queryInput.getTableName();
      List<QueryJoin> queryJoins = queryInput.getQueryJoins();
      QTableMetaData  table      = instance.getTable(tableName);

      boolean requiresDistinct = queryInput.getSelectDistinct() || doesSelectClauseRequireDistinct(table);
      String  clausePrefix     = (requiresDistinct) ? "SELECT DISTINCT " : "SELECT ";

      List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values());
      String columns = fieldList.stream()
         .filter(field -> filterOutHeavyFieldsIfNeeded(field, queryInput.getShouldFetchHeavyFields()))
         .map(field -> escapeIdentifier(tableName) + "." + escapeIdentifier(getColumnName(field)))
         .collect(Collectors.joining(", "));
      StringBuilder rs = new StringBuilder(clausePrefix).append(columns);

      for(QueryJoin queryJoin : CollectionUtils.nonNullList(queryJoins))
      {
         if(queryJoin.getSelect())
         {
            QTableMetaData joinTable        = instance.getTable(queryJoin.getJoinTable());
            String         tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();
            if(joinTable == null)
            {
               throw new QException("Requested join table [" + queryJoin.getJoinTable() + "] is not a defined table.");
            }

            List<QFieldMetaData> joinFieldList = new ArrayList<>(joinTable.getFields().values());
            String joinColumns = joinFieldList.stream()
               .filter(field -> filterOutHeavyFieldsIfNeeded(field, queryInput.getShouldFetchHeavyFields()))
               .map(field -> escapeIdentifier(tableNameOrAlias) + "." + escapeIdentifier(getColumnName(field)))
               .collect(Collectors.joining(", "));
            rs.append(", ").append(joinColumns);
         }
      }

      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean filterOutHeavyFieldsIfNeeded(QFieldMetaData field, boolean shouldFetchHeavyFields)
   {
      if(!shouldFetchHeavyFields && field.getIsHeavy())
      {
         return (false);
      }
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private PreparedStatement createStatement(Connection connection, String sql, QueryInput queryInput) throws SQLException
   {
      RDBMSBackendMetaData backend = (RDBMSBackendMetaData) queryInput.getBackend();
      PreparedStatement    statement;
      if("mysql".equals(backend.getVendor()))
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // mysql "optimization", presumably here - from Result Set section of https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-implementation-notes.html //
         // without this change, we saw ~10 seconds of "wait" time, before results would start to stream out of a large query (e.g., > 1,000,000 rows).                     //
         // with this change, we start to get results immediately, and the total runtime also seems lower...                                                                //
         /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
         statement.setFetchSize(Integer.MIN_VALUE);
      }
      else
      {
         statement = connection.prepareStatement(sql);
      }
      return (statement);
   }



   /*******************************************************************************
    ** Getter for queryStat
    *******************************************************************************/
   @Override
   public QueryStat getQueryStat()
   {
      return (this.queryStat);
   }



   /*******************************************************************************
    ** Setter for queryStat
    *******************************************************************************/
   @Override
   public void setQueryStat(QueryStat queryStat)
   {
      this.queryStat = queryStat;
   }

}
