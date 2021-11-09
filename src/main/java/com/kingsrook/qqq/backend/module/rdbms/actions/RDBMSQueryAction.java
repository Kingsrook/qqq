package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QueryInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.RDBSMBackendMetaData;
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
         Connection connection = connectionManager.getConnection(new RDBSMBackendMetaData(queryRequest.getBackend()));

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
                  String value = QueryManager.getString(resultSet, i); // todo - types!
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
   private String makeWhereClause(QTableMetaData table, List<QFilterCriteria> criteria, List<Serializable> params) throws IllegalArgumentException
   {
      List<String> clauses = new ArrayList<>();
      for(QFilterCriteria criterion : criteria)
      {
         QFieldMetaData field = table.getField(criterion.getFieldName());
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
               clause += " IN (" + criterion.getValues().stream().map(x -> "?").collect(Collectors.joining(",")) + ") ";
               break;
            }
            default:
            {
               throw new IllegalArgumentException("Unexpected operator: " + criterion.getOperator());
            }
         }
         clauses.add(clause);
         if(expectedNoOfParams != null && criterion.getValues().size() != expectedNoOfParams)
         {
            throw new IllegalArgumentException("Incorrect number of values given for criteria [" + field.getName() + "]");
         }
         params.addAll(criterion.getValues());
      }

      return (String.join(" AND ", clauses));
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
