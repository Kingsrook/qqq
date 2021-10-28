package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.InsertResult;
import com.kingsrook.qqq.backend.core.model.actions.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithStatus;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.InsertInterface;
import com.kingsrook.qqq.backend.module.rdbms.RDBSMBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSInsertAction extends AbstractRDBMSAction implements InsertInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertResult execute(InsertRequest insertRequest) throws QException
   {
      try
      {
         InsertResult rs = new InsertResult();
         QTableMetaData table = insertRequest.getTable();

         List<QFieldMetaData> insertableFields = table.getFields().stream()
            .filter(field -> !field.getName().equals("id")) // todo - intent here is to avoid non-insertable fields.
            .toList();

         String columns = insertableFields.stream()
            .map(this::getColumnName)
            .collect(Collectors.joining(", "));
         String questionMarks = insertableFields.stream()
            .map(x -> "?")
            .collect(Collectors.joining(", "));

         String tableName = table.getName();
         StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append("(").append(columns).append(") VALUES");
         List<Object> params = new ArrayList<>();

         int recordIndex = 0;
         for(QRecord record : insertRequest.getRecords())
         {
            if(recordIndex++ > 0)
            {
               sql.append(",");
            }
            sql.append("(").append(questionMarks).append(")");
            for(QFieldMetaData field : insertableFields)
            {
               params.add(record.getValue(field.getName()));
            }
         }

         // todo sql customization - can edit sql and/or param list

         ConnectionManager connectionManager = new ConnectionManager();
         Connection connection = connectionManager.getConnection(new RDBSMBackendMetaData(insertRequest.getBackend()));

         // QueryResult rs = new QueryResult();
         // List<QRecord> records = new ArrayList<>();
         // rs.setRecords(records);

         // todo - non-serial-id style tables
         // todo - other generated values, e.g., createDate...  maybe need to re-select?
         List<Integer> idList = QueryManager.executeInsertForGeneratedIds(connection, sql.toString(), params);
         List<QRecordWithStatus> recordsWithStatus = new ArrayList<>();
         rs.setRecords(recordsWithStatus);
         int index = 0;
         for(QRecord record : insertRequest.getRecords())
         {
            Integer id = idList.get(index);
            QRecordWithStatus recordWithStatus = new QRecordWithStatus(record);
            recordWithStatus.setPrimaryKey(id);
            recordWithStatus.setValue(table.getPrimaryKeyField(), id);
            recordsWithStatus.add(recordWithStatus);
         }

         return rs;
      }
      catch(Exception e)
      {
         throw new QException("Error executing insert: " + e.getMessage(), e);
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
