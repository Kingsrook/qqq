/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSQueryAction extends AbstractRDBMSAction implements QueryInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryResult execute(QueryRequest queryRequest) throws QException
   {
      try
      {
         QTableMetaData table = queryRequest.getTable();
         String tableName = table.getName();

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

         ConnectionManager connectionManager = new ConnectionManager();
         Connection connection = connectionManager.getConnection(new RDBMSBackendMetaData(queryRequest.getBackend()));

         QueryResult rs = new QueryResult();
         List<QRecord> records = new ArrayList<>();
         rs.setRecords(records);

         QueryManager.executeStatement(connection, sql, ((ResultSet resultSet) ->
         {
            ResultSetMetaData metaData = resultSet.getMetaData();
            while(resultSet.next())
            {
               // todo - should refactor this for view etc to use too.
               QRecord record = new QRecord();
               records.add(record);
               record.setTableName(table.getName());
               LinkedHashMap<String, Serializable> values = new LinkedHashMap<>();
               record.setValues(values);

               for(int i = 1; i <= metaData.getColumnCount(); i++)
               {
                  QFieldMetaData qFieldMetaData = fieldList.get(i - 1);
                  Serializable value = getValue(qFieldMetaData, resultSet, i);
                  values.put(qFieldMetaData.getName(), value);
                  if(qFieldMetaData.getName().equals(table.getPrimaryKeyField()))
                  {
                     record.setPrimaryKey(value);
                  }
               }
            }

         }), params);

         return rs;
      }
      catch(Exception e)
      {
         e.printStackTrace();
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
   private String makeWhereClause(QTableMetaData table, List<QFilterCriteria> criteria, List<Serializable> params) throws IllegalArgumentException
   {
      List<String> clauses = new ArrayList<>();
      for(QFilterCriteria criterion : criteria)
      {
         QFieldMetaData field = table.getField(criterion.getFieldName());
         List<Serializable> values = criterion.getValues() == null ? new ArrayList<>() : new ArrayList<>(criterion.getValues());
         String column = getColumnName(field);
         String clause = column;
         Integer expectedNoOfParams = null;
         switch(criterion.getOperator())
         {
            case EQUALS:
            {
               clause += " = ? ";
               expectedNoOfParams = 1;
               break;
            }
            case NOT_EQUALS:
            {
               clause += " != ? ";
               expectedNoOfParams = 1;
               break;
            }
            case IN:
            {
               clause += " IN (" + values.stream().map(x -> "?").collect(Collectors.joining(",")) + ") ";
               break;
            }
            case NOT_IN:
            {
               clause += " NOT IN (" + values.stream().map(x -> "?").collect(Collectors.joining(",")) + ") ";
               break;
            }
            case STARTS_WITH:
            {
               clause += " LIKE ? ";
               editFirstValue(values, (s -> s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case ENDS_WITH:
            {
               clause += " LIKE ? ";
               editFirstValue(values, (s -> "%" + s));
               expectedNoOfParams = 1;
               break;
            }
            case CONTAINS:
            {
               clause += " LIKE ? ";
               editFirstValue(values, (s -> "%" + s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_STARTS_WITH:
            {
               clause += " NOT LIKE ? ";
               editFirstValue(values, (s -> s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_ENDS_WITH:
            {
               clause += " NOT LIKE ? ";
               editFirstValue(values, (s -> "%" + s));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_CONTAINS:
            {
               clause += " NOT LIKE ? ";
               editFirstValue(values, (s -> "%" + s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case LESS_THAN:
            {
               clause += " < ? ";
               expectedNoOfParams = 1;
               break;
            }
            case LESS_THAN_OR_EQUALS:
            {
               clause += " <= ? ";
               expectedNoOfParams = 1;
               break;
            }
            case GREATER_THAN:
            {
               clause += " > ? ";
               expectedNoOfParams = 1;
               break;
            }
            case GREATER_THAN_OR_EQUALS:
            {
               clause += " >= ? ";
               expectedNoOfParams = 1;
               break;
            }
            case IS_BLANK:
            {
               clause += " IS NULL ";
               if(isString(field.getType()))
               {
                  clause += " OR " + column + " = '' ";
               }
               expectedNoOfParams = 0;
               break;
            }
            case IS_NOT_BLANK:
            {
               clause += " IS NOT NULL ";
               if(isString(field.getType()))
               {
                  clause += " AND " + column + " !+ '' ";
               }
               expectedNoOfParams = 0;
               break;
            }
            case BETWEEN:
            {
               clause += " BETWEEN ? AND ? ";
               expectedNoOfParams = 2;
               break;
            }
            case NOT_BETWEEN:
            {
               clause += " NOT BETWEEN ? AND ? ";
               expectedNoOfParams = 2;
               break;
            }
            default:
            {
               throw new IllegalArgumentException("Unexpected operator: " + criterion.getOperator());
            }
         }
         clauses.add("(" + clause + ")");
         if(expectedNoOfParams != null)
         {
            if(!expectedNoOfParams.equals(values.size()))
            {
               throw new IllegalArgumentException("Incorrect number of values given for criteria [" + field.getName() + "]");
            }
         }

         params.addAll(values);
      }

      return (String.join(" AND ", clauses));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void editFirstValue(List<Serializable> values, Function<String, String> editFunction)
   {
      if(values.size() > 0)
      {
         values.set(0, editFunction.apply(String.valueOf(values.get(0))));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private boolean isString(QFieldType fieldType)
   {
      return fieldType == QFieldType.STRING || fieldType == QFieldType.TEXT || fieldType == QFieldType.HTML || fieldType == QFieldType.PASSWORD;
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
