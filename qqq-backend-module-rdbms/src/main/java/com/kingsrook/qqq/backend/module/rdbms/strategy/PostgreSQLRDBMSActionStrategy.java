/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.strategy;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/**
 * PostgreSQL-specific implementation of the RDBMS action strategy.
 * Provides PostgreSQL-specific identifier escaping and other database-specific behaviors.
 *
 * <p>This strategy handles PostgreSQL-specific requirements including:</p>
 * <ul>
 *   <li>Double-quote identifier escaping (instead of backticks)</li>
 *   <li>Strict type checking for IN clauses with null values</li>
 *   <li>Explicit timestamp type specification for Instant objects</li>
 *   <li>RETURNING clause for retrieving generated keys</li>
 * </ul>
 *
 * <p>PostgreSQL has stricter type checking than other databases, particularly for:</p>
 * <ul>
 *   <li>IN clauses that contain null values</li>
 *   <li>Timestamp/Instant parameter binding</li>
 *   <li>Identifier quoting syntax</li>
 * </ul>
 */
public class PostgreSQLRDBMSActionStrategy extends BaseRDBMSActionStrategy
{

   /**
    * Escapes an identifier according to PostgreSQL-specific rules.
    *
    * <p>PostgreSQL uses double quotes for identifier quoting, unlike MySQL which uses backticks.
    * This method ensures that all identifiers are properly escaped for PostgreSQL syntax.</p>
    *
    * <p>Examples:</p>
    * <ul>
    *   <li>{@code escapeIdentifier("table_name")} returns {@code "\"table_name\""}</li>
    *   <li>{@code escapeIdentifier("user")} returns {@code "\"user\""}</li>
    *   <li>{@code escapeIdentifier("select")} returns {@code "\"select\""}</li>
    * </ul>
    *
    * @param id
    *    The identifier to escape (table name, column name, etc.)
    *
    * @return The escaped identifier wrapped in double quotes
    *
    * @throws NullPointerException
    *    if the identifier is null
    */
   @Override
   public String escapeIdentifier(final String id)
   {
      if(id == null)
      {
         throw new NullPointerException("Identifier cannot be null");
      }
      return ("\"" + id + "\"");
   }



   /**
    * Appends a criterion to a WHERE clause with PostgreSQL-specific handling for null values in IN clauses.
    *
    * <p>PostgreSQL is strict about type checking in IN clauses. When an IN clause contains null values,
    * PostgreSQL requires special handling to avoid type mismatch errors. This method:</p>
    * <ul>
    *   <li>Separates null and non-null values in IN clauses</li>
    *   <li>Generates PostgreSQL-compliant SQL: {@code column IN (values) OR column IS NULL}</li>
    *   <li>Handles the case where all values are null by generating {@code column IS NULL}</li>
    *   <li>Properly manages the parameter count for prepared statements</li>
    * </ul>
    *
    * <p>For non-IN clauses or IN clauses without null values, delegates to the base implementation.</p>
    *
    * @param criterion
    *    The filter criterion to process
    * @param clause
    *    The StringBuilder to append the SQL clause to
    * @param column
    *    The column name (already escaped)
    * @param values
    *    The list of values for the criterion (may be modified by this method)
    * @param field
    *    The field metadata for the column
    *
    * @return The number of parameters that will be bound to the prepared statement
    */
   @Override
   public Integer appendCriterionToWhereClause(final QFilterCriteria criterion, final StringBuilder clause, final String column, final List<Serializable> values, final QFieldMetaData field)
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // Handle the case where IN clause has null values - PostgreSQL is strict about types //
      ////////////////////////////////////////////////////////////////////////////////////////
      if(criterion.getOperator() == QCriteriaOperator.IN && values != null && values.contains(null))
      {
         ///////////////////////////////////////////////////////
         // Filter out null values and handle them separately //
         ///////////////////////////////////////////////////////
         List<Serializable> nonNullValues = values.stream()
            .filter(v -> v != null)
            .collect(Collectors.toList());

         if(nonNullValues.isEmpty())
         {
            /////////////////////////////////////////////////
            // If all values are null, just check for null //
            /////////////////////////////////////////////////
            clause.append(column).append(" IS NULL");

            ////////////////////////////////////////////////////////////////
            // Clear the values list since we're not using any parameters //
            ////////////////////////////////////////////////////////////////
            values.clear();
            return 0;
         }
         else
         {
            /////////////////////////////////////////////////
            // Build: column IN (values) OR column IS NULL //
            /////////////////////////////////////////////////
            clause.append("(").append(column).append(" IN (").append(nonNullValues.stream().map(x -> "?").collect(Collectors.joining(","))).append(") OR ").append(column).append(" IS NULL)");

            ///////////////////////////////////////////////////////
            // Replace the values list with only non-null values //
            ///////////////////////////////////////////////////////
            values.clear();
            values.addAll(nonNullValues);
            return nonNullValues.size();
         }
      }

      /////////////////////////////////////////////////////
      // Use the base implementation for all other cases //
      /////////////////////////////////////////////////////
      return super.appendCriterionToWhereClause(criterion, clause, column, values, field);
   }



   /**
    * Binds a parameter object to a prepared statement with PostgreSQL-specific type handling.
    *
    * <p>PostgreSQL requires explicit type specification for certain Java types, particularly
    * {@code java.time.Instant} objects when binding to TIMESTAMP columns. This method also
    * handles string-to-integer conversions that are common when parameters come from HTTP
    * requests as strings but need to be bound to integer columns.</p>
    *
    * <p>This method:</p>
    * <ul>
    *   <li>Converts {@code Instant} objects to {@code java.sql.Timestamp}</li>
    *   <li>Uses explicit {@code Types.TIMESTAMP} for PostgreSQL compatibility</li>
    *   <li>Handles string-to-integer conversions for ID fields</li>
    *   <li>Delegates to the base implementation for all other types</li>
    * </ul>
    *
    * <p>This prevents common PostgreSQL errors like:
    * <ul>
    *   <li>"Can't infer the SQL type to use for an instance of java.time.Instant"</li>
    *   <li>"operator does not exist: bigint = character varying"</li>
    * </ul></p>
    *
    * @param statement
    *    The prepared statement to bind the parameter to
    * @param index
    *    The parameter index (1-based)
    * @param value
    *    The value to bind
    *
    * @return The number of parameters bound (always 1 for this implementation)
    *
    * @throws SQLException
    *    if an error occurs during parameter binding
    */
   @Override
   protected int bindParamObject(final PreparedStatement statement, final int index, final Object value) throws SQLException
   {
      /////////////////////////////////////////////////////////////////////////
      // PostgreSQL requires explicit type specification for Instant objects //
      /////////////////////////////////////////////////////////////////////////
      if(value instanceof Instant)
      {
         ///////////////////////////////////////////////////////////////
         // Convert Instant to Timestamp for PostgreSQL compatibility //
         ///////////////////////////////////////////////////////////////
         Timestamp timestamp = Timestamp.from((Instant) value);
         statement.setObject(index, timestamp, java.sql.Types.TIMESTAMP);
         return 1;
      }

      ///////////////////////////////////////////////////////////////////////////////
      // Handle string-to-integer conversions for ID fields and numeric parameters //
      ///////////////////////////////////////////////////////////////////////////////
      if(value instanceof String)
      {
         String stringValue = (String) value;

         // Try to convert string to Long for ID fields
         try
         {
            Long longValue = Long.parseLong(stringValue);
            statement.setLong(index, longValue);
            return 1;
         }
         catch(NumberFormatException e)
         {
            // If it's not a number, treat it as a regular string
            statement.setString(index, stringValue);
            return 1;
         }
      }

      /////////////////////////////////////////////////////
      // Use the base implementation for all other types //
      /////////////////////////////////////////////////////
      return super.bindParamObject(statement, index, value);
   }



   /**
    * Executes an INSERT statement and retrieves generated keys using PostgreSQL's RETURNING clause.
    *
    * <p>PostgreSQL uses the {@code RETURNING} clause instead of the standard JDBC
    * {@code getGeneratedKeys()} method for retrieving auto-generated primary keys.
    * This method:</p>
    * <ul>
    *   <li>Appends {@code RETURNING column_name} to the INSERT SQL</li>
    *   <li>Uses {@code execute()} instead of {@code executeUpdate()}</li>
    *   <li>Retrieves generated keys from the {@code ResultSet}</li>
    *   <li>Properly handles the primary key field type conversion</li>
    * </ul>
    *
    * <p>This approach is more reliable than {@code getGeneratedKeys()} for PostgreSQL
    * and ensures that auto-generated IDs are properly retrieved for insert operations.</p>
    *
    * @param connection
    *    The database connection to use
    * @param sql
    *    The INSERT SQL statement (without RETURNING clause)
    * @param params
    *    The parameters to bind to the prepared statement
    * @param primaryKeyField
    *    The metadata for the primary key field
    *
    * @return A list of generated primary key values
    *
    * @throws SQLException
    *    if an error occurs during statement execution
    */
   @Override
   public List<Serializable> executeInsertForGeneratedIds(final Connection connection, final String sql, final List<Object> params, final QFieldMetaData primaryKeyField) throws SQLException
   {
      ///////////////////////////////////////////////////////////////////////////////
      // PostgreSQL uses RETURNING clause instead of getGeneratedKeys() method //
      ///////////////////////////////////////////////////////////////////////////////
      String sqlWithReturning = sql + " RETURNING " + getColumnName(primaryKeyField);

      try(PreparedStatement statement = connection.prepareStatement(sqlWithReturning))
      {
         bindParams(params.toArray(), statement);
         incrementStatistic(STAT_QUERIES_RAN);
         statement.execute();

         ResultSet          generatedKeys = statement.getResultSet();
         List<Serializable> rs            = new ArrayList<>();
         while(generatedKeys.next())
         {
            rs.add(getFieldValueFromResultSet(primaryKeyField.getType(), generatedKeys, 1));
         }
         return rs;
      }
   }
} 
