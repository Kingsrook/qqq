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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.actions.ActionHelper;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.interfaces.QActionInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.Aggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.GroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByAggregate;
import com.kingsrook.qqq.backend.core.model.actions.tables.aggregate.QFilterOrderByGroupBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.JoinsContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Base class for all core actions in the RDBMS module.
 *******************************************************************************/
public abstract class AbstractRDBMSAction implements QActionInterface
{
   private static final QLogger LOG = QLogger.getLogger(AbstractRDBMSAction.class);



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
         if(type.equals(QFieldType.INTEGER) || type.equals(QFieldType.DECIMAL) || type.equals(QFieldType.DATE) || type.equals(QFieldType.DATE_TIME) || type.equals(QFieldType.BOOLEAN))
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
      else if(field.getType().equals(QFieldType.DATE_TIME) && value instanceof String)
      {
         value = ValueUtils.getValueAsInstant(value);
      }
      else if(field.getType().equals(QFieldType.DECIMAL) && value instanceof String)
      {
         value = ValueUtils.getValueAsBigDecimal(value);
      }
      else if(field.getType().equals(QFieldType.BOOLEAN) && value instanceof String)
      {
         value = ValueUtils.getValueAsBoolean(value);
      }

      return (value);
   }



   /*******************************************************************************
    ** If the table has a field with the given name, then set the given value in the
    ** given record.
    *******************************************************************************/
   protected void setValueIfTableHasField(QRecord record, QTableMetaData table, String fieldName, Serializable value)
   {
      try
      {
         QFieldMetaData field = table.getField(fieldName);
         if(field != null)
         {
            record.setValue(fieldName, value);
         }
      }
      catch(Exception e)
      {
         /////////////////////////////////////////////////
         // this means field doesn't exist, so, ignore. //
         /////////////////////////////////////////////////
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String makeFromClause(QInstance instance, String tableName, JoinsContext joinsContext) throws QException
   {
      StringBuilder rs = new StringBuilder(escapeIdentifier(getTableName(instance.getTable(tableName))) + " AS " + escapeIdentifier(tableName));

      for(QueryJoin queryJoin : joinsContext.getQueryJoins())
      {
         QTableMetaData joinTable        = instance.getTable(queryJoin.getJoinTable());
         String         tableNameOrAlias = queryJoin.getJoinTableOrItsAlias();

         rs.append(" ").append(queryJoin.getType()).append(" JOIN ")
            .append(escapeIdentifier(getTableName(joinTable)))
            .append(" AS ").append(escapeIdentifier(tableNameOrAlias));

         ////////////////////////////////////////////////////////////
         // find the join in the instance, to set the 'on' clause  //
         ////////////////////////////////////////////////////////////
         List<String> joinClauseList = new ArrayList<>();
         String       baseTableName  = joinsContext.resolveTableNameOrAliasToTableName(queryJoin.getBaseTableOrAlias());
         QJoinMetaData joinMetaData = Objects.requireNonNullElseGet(queryJoin.getJoinMetaData(), () ->
         {
            QJoinMetaData found = findJoinMetaData(instance, joinsContext, baseTableName, queryJoin.getJoinTable());
            if(found == null)
            {
               throw (new RuntimeException("Could not find a join between tables [" + baseTableName + "][" + queryJoin.getJoinTable() + "]"));
            }
            return (found);
         });

         for(JoinOn joinOn : joinMetaData.getJoinOns())
         {
            QTableMetaData leftTable  = instance.getTable(joinMetaData.getLeftTable());
            QTableMetaData rightTable = instance.getTable(joinMetaData.getRightTable());

            String baseTableOrAlias = queryJoin.getBaseTableOrAlias();
            if(baseTableOrAlias == null)
            {
               baseTableOrAlias = leftTable.getName();
               if(!joinsContext.hasAliasOrTable(baseTableOrAlias))
               {
                  throw (new RuntimeException("Could not find a table or alias [" + baseTableOrAlias + "] in query.  May need to be more specific setting up QueryJoins."));
               }
            }

            String joinTableOrAlias = queryJoin.getJoinTableOrItsAlias();

            joinClauseList.add(escapeIdentifier(baseTableOrAlias)
               + "." + escapeIdentifier(getColumnName(leftTable.getField(joinOn.getLeftField())))
               + " = " + escapeIdentifier(joinTableOrAlias)
               + "." + escapeIdentifier(getColumnName((rightTable.getField(joinOn.getRightField())))));
         }
         rs.append(" ON ").append(StringUtils.join(" AND ", joinClauseList));
      }

      return (rs.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private QJoinMetaData findJoinMetaData(QInstance instance, JoinsContext joinsContext, String baseTableName, String joinTableName)
   {
      List<QJoinMetaData> matches = new ArrayList<>();
      if(baseTableName != null)
      {
         ///////////////////////////////////////////////////////////////////////////
         // if query specified a left-table, look for a join between left & right //
         ///////////////////////////////////////////////////////////////////////////
         for(QJoinMetaData join : instance.getJoins().values())
         {
            if(join.getLeftTable().equals(baseTableName) && join.getRightTable().equals(joinTableName))
            {
               matches.add(join);
            }

            //////////////////////////////
            // look in both directions! //
            //////////////////////////////
            if(join.getRightTable().equals(baseTableName) && join.getLeftTable().equals(joinTableName))
            {
               matches.add(join.flip());
            }
         }
      }
      else
      {
         /////////////////////////////////////////////////////////////////////////////////////
         // if query didn't specify a left-table, then look for any join to the right table //
         /////////////////////////////////////////////////////////////////////////////////////
         for(QJoinMetaData join : instance.getJoins().values())
         {
            if(join.getRightTable().equals(joinTableName) && joinsContext.hasTable(join.getLeftTable()))
            {
               matches.add(join);
            }

            //////////////////////////////
            // look in both directions! //
            //////////////////////////////
            if(join.getLeftTable().equals(joinTableName) && joinsContext.hasTable(join.getRightTable()))
            {
               matches.add(join.flip());
            }
         }
      }

      if(matches.size() == 1)
      {
         return (matches.get(0));
      }
      else if(matches.size() > 1)
      {
         throw (new RuntimeException("More than 1 join was found between [" + baseTableName + "] and [" + joinTableName + "].  Specify which one in your QueryJoin."));
      }

      return (null);
   }



   /*******************************************************************************
    ** method that sub-classes should call to make a full WHERE clause, including
    ** security clauses.
    *******************************************************************************/
   protected String makeWhereClause(QInstance instance, QSession session, QTableMetaData table, JoinsContext joinsContext, QQueryFilter filter, List<Serializable> params) throws IllegalArgumentException, QException
   {
      String       whereClauseWithoutSecurity = makeWhereClauseWithoutSecurity(instance, table, joinsContext, filter, params);
      QQueryFilter securityFilter             = getSecurityFilter(instance, session, table, joinsContext);
      if(!securityFilter.hasAnyCriteria())
      {
         return (whereClauseWithoutSecurity);
      }
      String securityWhereClause = makeWhereClauseWithoutSecurity(instance, table, joinsContext, securityFilter, params);
      return ("(" + whereClauseWithoutSecurity + ") AND (" + securityWhereClause + ")");
   }



   /*******************************************************************************
    ** private method for making the part of a where clause that gets AND'ed to the
    ** security clause.  Recursively handles sub-clauses.
    *******************************************************************************/
   private String makeWhereClauseWithoutSecurity(QInstance instance, QTableMetaData table, JoinsContext joinsContext, QQueryFilter filter, List<Serializable> params) throws IllegalArgumentException, QException
   {
      if(filter == null || !filter.hasAnyCriteria())
      {
         return ("1 = 1");
      }

      String clause = getSqlWhereStringAndPopulateParamsListFromNonNestedFilter(instance, table, joinsContext, filter.getCriteria(), filter.getBooleanOperator(), params);
      if(!CollectionUtils.nullSafeHasContents(filter.getSubFilters()))
      {
         ///////////////////////////////////////////////////////////////
         // if there are no sub-clauses, then just return this clause //
         ///////////////////////////////////////////////////////////////
         return (clause);
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // else, build a list of clauses - recursively expanding the sub-filters into clauses, then return them joined with our operator //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<String> clauses = new ArrayList<>();
      if(StringUtils.hasContent(clause))
      {
         clauses.add("(" + clause + ")");
      }
      for(QQueryFilter subFilter : filter.getSubFilters())
      {
         String subClause = makeWhereClauseWithoutSecurity(instance, table, joinsContext, subFilter, params);
         if(StringUtils.hasContent(subClause))
         {
            clauses.add("(" + subClause + ")");
         }
      }
      return (String.join(" " + filter.getBooleanOperator().toString() + " ", clauses));
   }



   /*******************************************************************************
    ** Build a QQueryFilter to apply record-level security to the query.
    ** Note, it may be empty, if there are no lock fields, or all are all-access.
    *******************************************************************************/
   private QQueryFilter getSecurityFilter(QInstance instance, QSession session, QTableMetaData table, JoinsContext joinsContext)
   {
      QQueryFilter securityFilter = new QQueryFilter();
      securityFilter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);

      for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(table.getRecordSecurityLocks()))
      {
         // todo - uh, if it's a RIGHT (or FULL) join, then, this should be isOuter = true, right?
         boolean isOuter = false;
         addSubFilterForRecordSecurityLock(instance, session, table, securityFilter, recordSecurityLock, joinsContext, table.getName(), isOuter);
      }

      for(QueryJoin queryJoin : CollectionUtils.nonNullList(joinsContext.getQueryJoins()))
      {
         QTableMetaData joinTable = instance.getTable(queryJoin.getJoinTable());
         for(RecordSecurityLock recordSecurityLock : CollectionUtils.nonNullList(joinTable.getRecordSecurityLocks()))
         {
            boolean isOuter = queryJoin.getType().equals(QueryJoin.Type.LEFT); // todo full?
            addSubFilterForRecordSecurityLock(instance, session, joinTable, securityFilter, recordSecurityLock, joinsContext, queryJoin.getJoinTableOrItsAlias(), isOuter);
         }
      }

      return (securityFilter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void addSubFilterForRecordSecurityLock(QInstance instance, QSession session, QTableMetaData table, QQueryFilter securityFilter, RecordSecurityLock recordSecurityLock, JoinsContext joinsContext, String tableNameOrAlias, boolean isOuter)
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check if the key type has an all-access key, and if so, if it's set to true for the current user/session //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QSecurityKeyType securityKeyType = instance.getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
      if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()))
      {
         if(session.hasSecurityKeyValue(securityKeyType.getAllAccessKeyName(), true, QFieldType.BOOLEAN))
         {
            ///////////////////////////////////////////////////////////////////////////////
            // if we have all-access on this key, then we don't need a criterion for it. //
            ///////////////////////////////////////////////////////////////////////////////
            return;
         }
      }

      String fieldName                   = tableNameOrAlias + "." + recordSecurityLock.getFieldName();
      String fieldNameWithoutTablePrefix = recordSecurityLock.getFieldName().replaceFirst(".*\\.", "");
      String fieldNameTablePrefix        = recordSecurityLock.getFieldName().replaceFirst("\\..*", "");
      if(CollectionUtils.nullSafeHasContents(recordSecurityLock.getJoinNameChain()))
      {
         for(String joinName : recordSecurityLock.getJoinNameChain())
         {
            QJoinMetaData joinMetaData = instance.getJoin(joinName);

            /*
            for(QueryJoin queryJoin : joinsContext.getQueryJoins())
            {
               if(queryJoin.getJoinMetaData().getName().equals(joinName))
               {
                  joinMetaData = queryJoin.getJoinMetaData();
                  break;
               }
            }
            */

            if(joinMetaData == null)
            {
               throw (new RuntimeException("Could not find joinMetaData for recordSecurityLock with joinChain member [" + joinName + "]"));
            }

            if(fieldNameTablePrefix.equals(joinMetaData.getLeftTable()))
            {
               table = instance.getTable(joinMetaData.getLeftTable());
            }
            else
            {
               table = instance.getTable(joinMetaData.getRightTable());
            }

            tableNameOrAlias = table.getName();
            fieldName = tableNameOrAlias + "." + fieldNameWithoutTablePrefix;
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////
      // else - get the key values from the session and decide what kind of criterion to build //
      ///////////////////////////////////////////////////////////////////////////////////////////
      QQueryFilter          lockFilter   = new QQueryFilter();
      List<QFilterCriteria> lockCriteria = new ArrayList<>();
      lockFilter.setCriteria(lockCriteria);
      List<Serializable> securityKeyValues = session.getSecurityKeyValues(recordSecurityLock.getSecurityKeyType(), table.getField(fieldNameWithoutTablePrefix).getType());
      if(CollectionUtils.nullSafeIsEmpty(securityKeyValues))
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // handle user with no values -- they can only see null values, and only iff the lock's null-value behavior is ALLOW //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(recordSecurityLock.getNullValueBehavior()))
         {
            lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IS_BLANK));
         }
         else
         {
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // else, if no user/session values, and null-value behavior is deny, then setup a FALSE condition, to allow no rows.           //
            // todo - make some explicit contradiction here - maybe even avoid running the whole query - as you're not allowed ANY records //
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IN, Collections.emptyList()));
         }
      }
      else
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // else, if user/session has some values, build an IN rule -                                                //
         // noting that if the lock's null-value behavior is ALLOW, then we actually want IS_NULL_OR_IN, not just IN //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(RecordSecurityLock.NullValueBehavior.ALLOW.equals(recordSecurityLock.getNullValueBehavior()))
         {
            lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IS_NULL_OR_IN, securityKeyValues));
         }
         else
         {
            lockCriteria.add(new QFilterCriteria(fieldName, QCriteriaOperator.IN, securityKeyValues));
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if this field is on the outer side of an outer join, then if we do a straight filter on it, then we're basically      //
      // nullifying the outer join... so for an outer join use-case, OR the security field criteria with a primary-key IS NULL //
      // which will make missing rows from the join be found.                                                                  //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(isOuter)
      {
         lockFilter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
         lockFilter.addCriteria(new QFilterCriteria(tableNameOrAlias + "." + table.getPrimaryKeyField(), QCriteriaOperator.IS_BLANK));
      }

      securityFilter.addSubFilter(lockFilter);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getSqlWhereStringAndPopulateParamsListFromNonNestedFilter(QInstance instance, QTableMetaData table, JoinsContext joinsContext, List<QFilterCriteria> criteria, QQueryFilter.BooleanOperator booleanOperator, List<Serializable> params) throws IllegalArgumentException
   {
      List<String> clauses = new ArrayList<>();
      for(QFilterCriteria criterion : criteria)
      {
         JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(criterion.getFieldName());

         List<Serializable> values             = criterion.getValues() == null ? new ArrayList<>() : new ArrayList<>(criterion.getValues());
         QFieldMetaData     field              = fieldAndTableNameOrAlias.field();
         String             column             = escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(field));
         String             clause             = column;
         Integer            expectedNoOfParams = null;
         switch(criterion.getOperator())
         {
            case EQUALS:
            {
               clause += " = ?";
               expectedNoOfParams = 1;
               break;
            }
            case NOT_EQUALS:
            {
               clause += " != ?";
               expectedNoOfParams = 1;
               break;
            }
            case IN:
            {
               if(values.isEmpty())
               {
                  ///////////////////////////////////////////////////////
                  // if there are no values, then we want a false here //
                  ///////////////////////////////////////////////////////
                  clause = " 0 = 1 ";
               }
               else
               {
                  clause += " IN (" + values.stream().map(x -> "?").collect(Collectors.joining(",")) + ")";
               }
               break;
            }
            case IS_NULL_OR_IN:
            {
               clause += " IS NULL ";

               if(!values.isEmpty())
               {
                  clause += " OR " + column + " IN (" + values.stream().map(x -> "?").collect(Collectors.joining(",")) + ")";
               }
               break;
            }
            case NOT_IN:
            {
               if(values.isEmpty())
               {
                  //////////////////////////////////////////////////////
                  // if there are no values, then we want a true here //
                  //////////////////////////////////////////////////////
                  clause = " 1 = 1 ";
               }
               else
               {
                  clause += " NOT IN (" + values.stream().map(x -> "?").collect(Collectors.joining(",")) + ")";
               }
               break;
            }
            case STARTS_WITH:
            {
               clause += " LIKE ?";
               ActionHelper.editFirstValue(values, (s -> s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case ENDS_WITH:
            {
               clause += " LIKE ?";
               ActionHelper.editFirstValue(values, (s -> "%" + s));
               expectedNoOfParams = 1;
               break;
            }
            case CONTAINS:
            {
               clause += " LIKE ?";
               ActionHelper.editFirstValue(values, (s -> "%" + s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_STARTS_WITH:
            {
               clause += " NOT LIKE ?";
               ActionHelper.editFirstValue(values, (s -> s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_ENDS_WITH:
            {
               clause += " NOT LIKE ?";
               ActionHelper.editFirstValue(values, (s -> "%" + s));
               expectedNoOfParams = 1;
               break;
            }
            case NOT_CONTAINS:
            {
               clause += " NOT LIKE ?";
               ActionHelper.editFirstValue(values, (s -> "%" + s + "%"));
               expectedNoOfParams = 1;
               break;
            }
            case LESS_THAN:
            {
               clause += " < ?";
               expectedNoOfParams = 1;
               break;
            }
            case LESS_THAN_OR_EQUALS:
            {
               clause += " <= ?";
               expectedNoOfParams = 1;
               break;
            }
            case GREATER_THAN:
            {
               clause += " > ?";
               expectedNoOfParams = 1;
               break;
            }
            case GREATER_THAN_OR_EQUALS:
            {
               clause += " >= ?";
               expectedNoOfParams = 1;
               break;
            }
            case IS_BLANK:
            {
               clause += " IS NULL";
               if(field.getType().isStringLike())
               {
                  clause += " OR " + column + " = ''";
               }
               expectedNoOfParams = 0;
               break;
            }
            case IS_NOT_BLANK:
            {
               clause += " IS NOT NULL";
               if(field.getType().isStringLike())
               {
                  clause += " AND " + column + " != ''";
               }
               expectedNoOfParams = 0;
               break;
            }
            case BETWEEN:
            {
               clause += " BETWEEN ? AND ?";
               expectedNoOfParams = 2;
               break;
            }
            case NOT_BETWEEN:
            {
               clause += " NOT BETWEEN ? AND ?";
               expectedNoOfParams = 2;
               break;
            }
            default:
            {
               throw new IllegalArgumentException("Unexpected operator: " + criterion.getOperator());
            }
         }

         if(expectedNoOfParams != null)
         {
            if(expectedNoOfParams.equals(1) && StringUtils.hasContent(criterion.getOtherFieldName()))
            {
               JoinsContext.FieldAndTableNameOrAlias otherFieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(criterion.getOtherFieldName());

               String otherColumn = escapeIdentifier(otherFieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(otherFieldAndTableNameOrAlias.field()));
               clause = clause.replace("?", otherColumn);

               /////////////////////////////////////////////////////////////////////
               // make sure we don't add any values in this case, just in case... //
               /////////////////////////////////////////////////////////////////////
               values = Collections.emptyList();
            }
            else if(!expectedNoOfParams.equals(values.size()))
            {
               throw new IllegalArgumentException("Incorrect number of values given for criteria [" + field.getName() + "]");
            }
         }

         clauses.add("(" + clause + ")");

         params.addAll(values);
      }

      return (String.join(" " + booleanOperator.toString() + " ", clauses));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QBackendTransaction openTransaction(AbstractTableActionInput input) throws QException
   {
      try
      {
         LOG.debug("Opening transaction");
         Connection connection = getConnection(input);

         return (new RDBMSTransaction(connection));
      }
      catch(Exception e)
      {
         throw new QException("Error opening transaction: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String escapeIdentifier(String id)
   {
      return ("`" + id + "`");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Serializable getFieldValueFromResultSet(QFieldType type, ResultSet resultSet, int i) throws SQLException
   {
      switch(type)
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
            throw new IllegalStateException("Unexpected field type: " + type);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Serializable getFieldValueFromResultSet(QFieldMetaData qFieldMetaData, ResultSet resultSet, int i) throws SQLException
   {
      return (getFieldValueFromResultSet(qFieldMetaData.getType(), resultSet, i));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String makeOrderByClause(QTableMetaData table, List<QFilterOrderBy> orderBys, JoinsContext joinsContext)
   {
      List<String> clauses = new ArrayList<>();

      for(QFilterOrderBy orderBy : orderBys)
      {
         String ascOrDesc = orderBy.getIsAscending() ? "ASC" : "DESC";
         if(orderBy instanceof QFilterOrderByAggregate orderByAggregate)
         {
            Aggregate aggregate = orderByAggregate.getAggregate();
            String    clause    = (aggregate.getOperator() + "(" + escapeIdentifier(getColumnName(table.getField(aggregate.getFieldName()))) + ")");
            clauses.add(clause + " " + ascOrDesc);
         }
         else if(orderBy instanceof QFilterOrderByGroupBy orderByGroupBy)
         {
            clauses.add(getSingleGroupByClause(orderByGroupBy.getGroupBy(), joinsContext) + " " + ascOrDesc);
         }
         else
         {
            JoinsContext.FieldAndTableNameOrAlias otherFieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(orderBy.getFieldName());

            QFieldMetaData field  = otherFieldAndTableNameOrAlias.field();
            String         column = getColumnName(field);
            clauses.add(escapeIdentifier(otherFieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(column) + " " + ascOrDesc);
         }
      }
      return (String.join(", ", clauses));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getSingleGroupByClause(GroupBy groupBy, JoinsContext joinsContext)
   {
      JoinsContext.FieldAndTableNameOrAlias fieldAndTableNameOrAlias = joinsContext.getFieldAndTableNameOrAlias(groupBy.getFieldName());
      String                                fullFieldName            = escapeIdentifier(fieldAndTableNameOrAlias.tableNameOrAlias()) + "." + escapeIdentifier(getColumnName(fieldAndTableNameOrAlias.field()));
      if(groupBy.getFormatString() == null)
      {
         return (fullFieldName);
      }
      else
      {
         return (String.format(groupBy.getFormatString(), fullFieldName));
      }
   }
}
