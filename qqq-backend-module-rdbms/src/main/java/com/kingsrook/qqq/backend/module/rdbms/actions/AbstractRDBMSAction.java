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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Base class for all core actions in the RDBMS module.
 *******************************************************************************/
public abstract class AbstractRDBMSAction
{

   /*******************************************************************************
    ** Get the table name to use in the RDBMS from a QTableMetaData.
    **
    ** That is, table.backendDetails.tableName if set -- else, table.name
    *******************************************************************************/
   protected String getTableName(QTableMetaData table)
   {
      if(table.getBackendDetails() instanceof RDBMSTableBackendDetails details)
      {
         if(StringUtils.hasContent(details.getTableName()))
         {
            return (details.getTableName());
         }
      }
      return (table.getName());
   }



   /*******************************************************************************
    ** Get the column name to use for a field in the RDBMS, from the fieldMetaData.
    **
    ** That is, field.backendName if set -- else, field.name
    *******************************************************************************/
   protected String getColumnName(QFieldMetaData field)
   {
      if(field.getBackendName() != null)
      {
         return (field.getBackendName());
      }
      return (field.getName());
   }



   /*******************************************************************************
    ** Get a database connection, per the backend in the request.
    *******************************************************************************/
   protected Connection getConnection(AbstractTableActionInput qTableRequest) throws SQLException
   {
      ConnectionManager connectionManager = new ConnectionManager();
      return connectionManager.getConnection((RDBMSBackendMetaData) qTableRequest.getBackend());
   }



   /*******************************************************************************
    ** Handle obvious problems with values - like empty string for integer should be null,
    ** and type conversions that we can do "better" than jdbc...
    **
    *******************************************************************************/
   protected Serializable scrubValue(QFieldMetaData field, Serializable value, boolean isInsert)
   {
      if("".equals(value))
      {
         QFieldType type = field.getType();
         if(type.equals(QFieldType.INTEGER) || type.equals(QFieldType.DECIMAL) || type.equals(QFieldType.DATE) || type.equals(QFieldType.DATE_TIME))
         {
            value = null;
         }
      }

      //////////////////////////////////////////////////////////////////////////////
      // value utils is good at making values from strings - jdbc, not as much... //
      //////////////////////////////////////////////////////////////////////////////
      if(field.getType().equals(QFieldType.DATE) && value instanceof String)
      {
         value = ValueUtils.getValueAsLocalDate(value);
      }
      else if(field.getType().equals(QFieldType.DECIMAL) && value instanceof String)
      {
         value = ValueUtils.getValueAsBigDecimal(value);
      }

      return (value);
   }



   /*******************************************************************************
    ** If the table has a field with the given name, then set the given value in the
    ** given record.
    *******************************************************************************/
   protected void setValueIfTableHasField(QRecord record, QTableMetaData table, String fieldName, Serializable value)
   {
      QFieldMetaData field = table.getField(fieldName);
      if(field != null)
      {
         record.setValue(fieldName, value);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String makeWhereClause(QTableMetaData table, List<QFilterCriteria> criteria, List<Serializable> params) throws IllegalArgumentException
   {
      List<String> clauses = new ArrayList<>();
      for(QFilterCriteria criterion : criteria)
      {
         QFieldMetaData     field              = table.getField(criterion.getFieldName());
         List<Serializable> values             = criterion.getValues() == null ? new ArrayList<>() : new ArrayList<>(criterion.getValues());
         String             column             = getColumnName(field);
         String             clause             = column;
         Integer            expectedNoOfParams = null;
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
   private static void editFirstValue(List<Serializable> values, Function<String, String> editFunction)
   {
      if(values.size() > 0)
      {
         values.set(0, editFunction.apply(String.valueOf(values.get(0))));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isString(QFieldType fieldType)
   {
      return fieldType == QFieldType.STRING || fieldType == QFieldType.TEXT || fieldType == QFieldType.HTML || fieldType == QFieldType.PASSWORD;
   }
}
