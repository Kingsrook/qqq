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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSQueryAction extends AbstractRDBMSAction implements QueryInterface
{
   private static final Logger LOG = LogManager.getLogger(RDBMSQueryAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput execute(QueryInput queryInput) throws QException
   {
      try
      {
         QTableMetaData table     = queryInput.getTable();
         String         tableName = getTableName(table);

         List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values());
         String columns = fieldList.stream()
            .map(this::getColumnName)
            .collect(Collectors.joining(", "));

         String sql = "SELECT " + columns + " FROM " + tableName;

         QQueryFilter       filter = queryInput.getFilter();
         List<Serializable> params = new ArrayList<>();
         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getCriteria()))
         {
            sql += " WHERE " + makeWhereClause(table, filter.getCriteria(), params);
         }

         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            sql += " ORDER BY " + makeOrderByClause(table, filter.getOrderBys());
         }

         if(queryInput.getLimit() != null)
         {
            sql += " LIMIT " + queryInput.getLimit();

            if(queryInput.getSkip() != null)
            {
               // todo - other sql grammars?
               sql += " OFFSET " + queryInput.getSkip();
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

         try
         {
            QueryOutput       queryOutput = new QueryOutput(queryInput);
            PreparedStatement statement   = createStatement(connection, sql, queryInput);
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
                     Serializable   value          = getValue(qFieldMetaData, resultSet, i);
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

            return queryOutput;
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
    **
    *******************************************************************************/
   private Serializable getValue(QFieldMetaData qFieldMetaData, ResultSet resultSet, int i) throws SQLException
   {
      switch(qFieldMetaData.getType())
      {
         case STRING:
         case TEXT:
         case HTML:
         case PASSWORD:
         {
            return (QueryManager.getString(resultSet, i));
         }
         case INTEGER:
         {
            return (QueryManager.getInteger(resultSet, i));
         }
         case DECIMAL:
         {
            return (QueryManager.getBigDecimal(resultSet, i));
         }
         case DATE:
         {
            // todo - queryManager.getLocalDate?
            return (QueryManager.getDate(resultSet, i));
         }
         case TIME:
         {
            return (QueryManager.getLocalTime(resultSet, i));
         }
         case DATE_TIME:
         {
            return (QueryManager.getInstant(resultSet, i));
         }
         case BOOLEAN:
         {
            return (QueryManager.getBoolean(resultSet, i));
         }
         default:
         {
            throw new IllegalStateException("Unexpected field type: " + qFieldMetaData.getType());
         }
      }

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String makeOrderByClause(QTableMetaData table, List<QFilterOrderBy> orderBys)
   {
      List<String> clauses = new ArrayList<>();

      for(QFilterOrderBy orderBy : orderBys)
      {
         QFieldMetaData field  = table.getField(orderBy.getFieldName());
         String         column = getColumnName(field);
         clauses.add(column + " " + (orderBy.getIsAscending() ? "ASC" : "DESC"));
      }
      return (String.join(", ", clauses));
   }

}
