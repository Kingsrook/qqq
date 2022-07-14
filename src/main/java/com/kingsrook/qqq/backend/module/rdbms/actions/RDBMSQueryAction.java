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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
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
   public QueryResult execute(QueryRequest queryRequest) throws QException
   {
      try
      {
         QTableMetaData table = queryRequest.getTable();
         String tableName = getTableName(table);

         List<QFieldMetaData> fieldList = new ArrayList<>(table.getFields().values());
         String columns = fieldList.stream()
            .map(this::getColumnName)
            .collect(Collectors.joining(", "));

         String sql = "SELECT " + columns + " FROM " + tableName;

         QQueryFilter filter = queryRequest.getFilter();
         List<Serializable> params = new ArrayList<>();
         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getCriteria()))
         {
            sql += " WHERE " + makeWhereClause(table, filter.getCriteria(), params);
         }

         if(filter != null && CollectionUtils.nullSafeHasContents(filter.getOrderBys()))
         {
            sql += " ORDER BY " + makeOrderByClause(table, filter.getOrderBys());
         }

         if(queryRequest.getLimit() != null)
         {
            sql += " LIMIT " + queryRequest.getLimit();

            if(queryRequest.getSkip() != null)
            {
               // todo - other sql grammars?
               sql += " OFFSET " + queryRequest.getSkip();
            }
         }

         // todo sql customization - can edit sql and/or param list

         QueryResult rs = new QueryResult();
         List<QRecord> records = new ArrayList<>();
         rs.setRecords(records);

         try(Connection connection = getConnection(queryRequest))
         {
            QueryManager.executeStatement(connection, sql, ((ResultSet resultSet) ->
            {
               ResultSetMetaData metaData = resultSet.getMetaData();
               while(resultSet.next())
               {
                  // todo - should refactor this for view etc to use too.
                  // todo - Add display values (String labels for possibleValues, formatted #'s, etc)
                  QRecord record = new QRecord();
                  records.add(record);
                  record.setTableName(table.getName());
                  LinkedHashMap<String, Serializable> values = new LinkedHashMap<>();
                  record.setValues(values);

                  for(int i = 1; i <= metaData.getColumnCount(); i++)
                  {
                     QFieldMetaData qFieldMetaData = fieldList.get(i - 1);
                     Serializable   value          = getValue(qFieldMetaData, resultSet, i);
                     values.put(qFieldMetaData.getName(), value);
                  }
               }

            }), params);
         }

         return rs;
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
            return (QueryManager.getDate(resultSet, i));
         }
         case DATE_TIME:
         {
            return (QueryManager.getLocalDateTime(resultSet, i));
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
         QFieldMetaData field = table.getField(orderBy.getFieldName());
         String column = getColumnName(field);
         clauses.add(column + " " + (orderBy.getIsAscending() ? "ASC" : "DESC"));
      }
      return (String.join(", ", clauses));
   }

}
