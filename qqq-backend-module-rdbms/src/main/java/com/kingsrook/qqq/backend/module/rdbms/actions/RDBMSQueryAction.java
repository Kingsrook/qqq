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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ActionTimeoutHelper;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.QueryHint;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSQueryAction extends AbstractRDBMSAction implements QueryInterface
{
   private static final QLogger LOG = QLogger.getLogger(RDBMSQueryAction.class);

   private ActionTimeoutHelper actionTimeoutHelper;

   private static boolean mysqlResultSetOptimizationEnabled = false;

   static
   {
      try
      {
         mysqlResultSetOptimizationEnabled = new QMetaDataVariableInterpreter().getBooleanFromPropertyOrEnvironment("qqq.rdbms.mysql.resultSetOptimizationEnabled", "QQQ_RDBMS_MYSQL_RESULT_SET_OPTIMIZATION_ENABLED", false);
      }
      catch(Exception e)
      {
         LOG.warn("Error reading property/env for mysqlResultSetOptimizationEnabled", e);
      }
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      try
      {
         QTableMetaData table     = queryInput.getTable();
         String         tableName = queryInput.getTableName();

         Selection     selection = makeSelection(queryInput);
         StringBuilder sql = new StringBuilder(selection.selectClause());

         QQueryFilter filter       = clonedOrNewFilter(queryInput.getFilter());
         JoinsContext joinsContext = new JoinsContext(queryInput.getInstance(), tableName, queryInput.getQueryJoins(), filter);

         List<Serializable> params = new ArrayList<>();
         sql.append(" FROM ").append(makeFromClause(queryInput.getInstance(), tableName, joinsContext, params));
         sql.append(" WHERE ").append(makeWhereClause(joinsContext, filter, params));

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

         setSqlAndJoinsInQueryStat(sql, joinsContext);

         Connection connection;
         boolean    needToCloseConnection = false;
         if(queryInput.getTransaction() != null && queryInput.getTransaction() instanceof RDBMSTransaction rdbmsTransaction)
         {
            connection = rdbmsTransaction.getConnection();
         }
         else
         {
            connection = getConnection(queryInput);
            needToCloseConnection = true;
         }

         Long mark = System.currentTimeMillis();

         try
         {
            /////////////////////////////////////
            // create a statement from the SQL //
            /////////////////////////////////////
            statement = createStatement(connection, sql.toString(), queryInput);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // set up & start an actionTimeoutHelper (note, internally it'll deal with the time being null or negative as meaning not to timeout) //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            actionTimeoutHelper = new ActionTimeoutHelper(queryInput.getTimeoutSeconds(), TimeUnit.SECONDS, new StatementTimeoutCanceller(statement, sql));
            actionTimeoutHelper.start();

            //////////////////////////////////////////////
            // execute the query - iterate over results //
            //////////////////////////////////////////////
            QueryOutput queryOutput = new QueryOutput(queryInput);

            QueryManager.executeStatement(statement, sql, ((ResultSet resultSet) ->
            {
               /////////////////////////////////////////////////////////////////////////
               // once we've started getting results, go ahead and cancel the timeout //
               /////////////////////////////////////////////////////////////////////////
               actionTimeoutHelper.cancel();

               ResultSetMetaData metaData = resultSet.getMetaData();
               while(resultSet.next())
               {
                  setQueryStatFirstResultTime();

                  QRecord record = new QRecord();
                  record.setTableName(table.getName());
                  LinkedHashMap<String, Serializable> values = new LinkedHashMap<>();
                  record.setValues(values);

                  for(int i = 1; i <= metaData.getColumnCount(); i++)
                  {
                     QFieldMetaData field = selection.fields().get(i - 1);

                     if(!queryInput.getShouldFetchHeavyFields() && field.getIsHeavy())
                     {
                        ///////////////////////////////////////////////////////////////////////////////////
                        // if this is a non-fetched heavy field (e.g., we just fetched its length), then //
                        // get the value here as an INTEGER, not a BLOB or whatever the field would be   //
                        ///////////////////////////////////////////////////////////////////////////////////
                        Serializable fieldLength = getFieldValueFromResultSet(QFieldType.INTEGER, resultSet, i);
                        setHeavyFieldLengthInRecordBackendDetails(record, field, fieldLength);
                     }
                     else
                     {
                        Serializable value = getFieldValueFromResultSet(field, resultSet, i);
                        values.put(field.getName(), value);
                     }
                  }

                  queryOutput.addRecord(record);

                  if(queryInput.getAsyncJobCallback().wasCancelRequested())
                  {
                     LOG.info("Breaking query job, as requested.");
                     break;
                  }
               }

               /////////////////////////////////////////////////////////////////
               // in case there were no results, set the firstResultTime here //
               /////////////////////////////////////////////////////////////////
               setQueryStatFirstResultTime();

            }), params);

            return queryOutput;
         }
         finally
         {
            logSQL(sql, params, mark);

            if(actionTimeoutHelper != null)
            {
               /////////////////////////////////////////
               // make sure the timeout got cancelled //
               /////////////////////////////////////////
               actionTimeoutHelper.cancel();
            }

            if(needToCloseConnection)
            {
               connection.close();
            }
         }
      }
      catch(Exception e)
      {
         if(actionTimeoutHelper != null && actionTimeoutHelper.getDidTimeout())
         {
            setQueryStatFirstResultTime();
            throw (new QUserFacingException("Query timed out."));
         }

         if(isCancelled)
         {
            throw (new QUserFacingException("Query was cancelled."));
         }

         LOG.warn("Error executing query", e, logPair("tableName", queryInput.getTableName()), logPair("filter", queryInput.getFilter()));
         throw new QException("Error executing query", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   private static void setHeavyFieldLengthInRecordBackendDetails(QRecord record, QFieldMetaData field, Serializable fieldLength)
   {
      if(record.getBackendDetails() == null)
      {
         record.setBackendDetails(new HashMap<>());
      }

      if(record.getBackendDetail(QRecord.BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS) == null)
      {
         record.addBackendDetail(QRecord.BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS, new HashMap<>());
      }

      ((Map<String, Serializable>) record.getBackendDetail(QRecord.BACKEND_DETAILS_TYPE_HEAVY_FIELD_LENGTHS)).put(field.getName(), fieldLength);
   }



   /***************************************************************************
    ** output wrapper for makeSelection method.
    ** - selectClause is everything from SELECT up to (but not including) FROM
    ** - fields are those being selected, in the same order, and with mutated
    ** names for join fields.
    ***************************************************************************/
   private record Selection(String selectClause, List<QFieldMetaData> fields)
   {

   }



   /*******************************************************************************
    ** For a given queryInput, determine what fields are being selected - returning
    ** a record containing the SELECT clause, as well as a List of QFieldMetaData
    ** representing those fields - where - note - the names for fields from join
    ** tables will be prefixed by the join table nameOrAlias.
    *******************************************************************************/
   private Selection makeSelection(QueryInput queryInput) throws QException
   {
      QInstance       instance   = QContext.getQInstance();
      String          tableName  = queryInput.getTableName();
      List<QueryJoin> queryJoins = queryInput.getQueryJoins();
      QTableMetaData  table      = instance.getTable(tableName);

      Set<String> fieldNamesToInclude = queryInput.getFieldNamesToInclude();

      ///////////////////////////////////////////////////////////////////////////////////////////////
      // start with the main table's fields, optionally filtered by the set of fieldNamesToInclude //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      List<QFieldMetaData> fieldList = table.getFields().values()
         .stream().filter(field -> fieldNamesToInclude == null || fieldNamesToInclude.contains(field.getName()))
         .toList();

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // map those field names to columns, joined with ", ".                                                              //
      // if a field is heavy, and heavy fields aren't being selected, then replace that field name with a LENGTH function //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      String columns = fieldList.stream()
         .map(field -> Pair.of(field, escapeIdentifier(tableName) + "." + escapeIdentifier(getColumnName(field))))
         .map(pair -> wrapHeavyFieldsWithLengthFunctionIfNeeded(pair, queryInput.getShouldFetchHeavyFields()))
         .collect(Collectors.joining(", "));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // figure out if distinct is being used.  then start building the select clause with the table's columns //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      boolean              requiresDistinct   = queryInput.getSelectDistinct() || doesSelectClauseRequireDistinct(table);
      StringBuilder        selectClause       = new StringBuilder((requiresDistinct) ? "SELECT DISTINCT " : "SELECT ").append(columns);
      List<QFieldMetaData> selectionFieldList = new ArrayList<>(fieldList);

      ///////////////////////////////////
      // add any 'selected' queryJoins //
      ///////////////////////////////////
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

            ///////////////////////////////////
            // filter by fieldNamesToInclude //
            ///////////////////////////////////
            List<QFieldMetaData> joinFieldList = joinTable.getFields().values()
               .stream().filter(field -> fieldNamesToInclude == null || fieldNamesToInclude.contains(tableNameOrAlias + "." + field.getName()))
               .toList();

            /////////////////////////////////////////////////////
            // map to columns, wrapping heavy fields as needed //
            /////////////////////////////////////////////////////
            String joinColumns = joinFieldList.stream()
               .map(field -> Pair.of(field, escapeIdentifier(tableNameOrAlias) + "." + escapeIdentifier(getColumnName(field))))
               .map(pair -> wrapHeavyFieldsWithLengthFunctionIfNeeded(pair, queryInput.getShouldFetchHeavyFields()))
               .collect(Collectors.joining(", "));

            ////////////////////////////////////////////////////////////////////////////////////////////////
            // append to output objects.                                                                  //
            // note that fields are cloned, since we are changing their names to have table/alias prefix. //
            ////////////////////////////////////////////////////////////////////////////////////////////////
            selectClause.append(", ").append(joinColumns);
            selectionFieldList.addAll(joinFieldList.stream().map(field -> field.clone().withName(tableNameOrAlias + "." + field.getName())).toList());
         }
      }

      return (new Selection(selectClause.toString(), selectionFieldList));
   }



   /*******************************************************************************
    ** if we're not fetching heavy fields, instead just get their length.  this
    ** method wraps the field 'sql name' (e.g., column_name or table_name.column_name)
    ** with the LENGTH() function, if needed.
    *******************************************************************************/
   private String wrapHeavyFieldsWithLengthFunctionIfNeeded(Pair<QFieldMetaData, String> fieldAndSqlName, boolean shouldFetchHeavyFields)
   {
      QFieldMetaData field   = fieldAndSqlName.getA();
      String         sqlName = fieldAndSqlName.getB();
      if(!shouldFetchHeavyFields && field.getIsHeavy())
      {
         return ("LENGTH(" + sqlName + ")");
      }
      return (sqlName);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private PreparedStatement createStatement(Connection connection, String sql, QueryInput queryInput) throws SQLException
   {
      /////////////////////////////////////////////////////////////////////////
      // if we're allowed to use the mysqlResultSetOptimization, and we have //
      // the query hint of "potentially large no of results", then check if  //
      // our backend is indeed mysql, and if so, then apply those settings.  //
      /////////////////////////////////////////////////////////////////////////
      if(mysqlResultSetOptimizationEnabled && queryInput.hasQueryHint(QueryHint.POTENTIALLY_LARGE_NUMBER_OF_RESULTS))
      {
         RDBMSBackendMetaData rdbmsBackendMetaData = (RDBMSBackendMetaData) queryInput.getBackend();

         if(RDBMSBackendMetaData.VENDOR_MYSQL.equals(rdbmsBackendMetaData.getVendor()) || RDBMSBackendMetaData.VENDOR_AURORA_MYSQL.equals(rdbmsBackendMetaData.getVendor()))
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            // mysql "optimization", presumably here - from Result Set section of                               //
            // https://dev.mysql.com/doc/connector-j/en/connector-j-reference-implementation-notes.html without //
            // this change, we saw ~10 seconds of "wait" time, before results would start to stream out of a    //
            // large query (e.g., > 1,000,000 rows).                                                            //
            // with this change, we start to get results immediately, and the total runtime also seems lower... //
            // perhaps more importantly, without this change, the whole result set goes into memory - but with  //
            // this change, it is streamed.                                                                     //
            //////////////////////////////////////////////////////////////////////////////////////////////////////
            PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(Integer.MIN_VALUE);
            return (statement);
         }
      }

      return (connection.prepareStatement(sql));
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
