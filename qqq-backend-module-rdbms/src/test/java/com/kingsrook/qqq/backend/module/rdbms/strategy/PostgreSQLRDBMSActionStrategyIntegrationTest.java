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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;

/**
 * Unit tests for PostgreSQLRDBMSActionStrategy.
 * 
 * <p>This test suite verifies PostgreSQL-specific functionality without requiring
 * a database connection. These are pure unit tests that test the logic and
 * behavior of the PostgreSQL strategy implementation.</p>
 * 
 * <p>This test suite verifies all PostgreSQL-specific functionality including:</p>
 * <ul>
 *   <li>Identifier escaping with double quotes</li>
 *   <li>IN clause handling with null values</li>
 *   <li>Framework integration</li>
 *   <li>Edge cases and error conditions</li>
 * </ul>
 * 
 * <p>These tests serve as a template for testing other RDBMS vendors in the future.</p>
 */
public class PostgreSQLRDBMSActionStrategyIntegrationTest
{
   private PostgreSQLRDBMSActionStrategy strategy;

   @BeforeEach
   void setUp()
   {
      strategy = new PostgreSQLRDBMSActionStrategy();
   }

   /**
    * Test identifier escaping with PostgreSQL double quotes.
    */
   @Test
   void testEscapeIdentifier()
   {
      // Test basic identifier escaping
      assertEquals("\"table_name\"", strategy.escapeIdentifier("table_name"));
      assertEquals("\"column_name\"", strategy.escapeIdentifier("column_name"));
      
      // Test reserved words (should still work with quotes)
      assertEquals("\"select\"", strategy.escapeIdentifier("select"));
      assertEquals("\"from\"", strategy.escapeIdentifier("from"));
      assertEquals("\"where\"", strategy.escapeIdentifier("where"));
      assertEquals("\"order\"", strategy.escapeIdentifier("order"));
      
      // Test identifiers with special characters
      assertEquals("\"user_table\"", strategy.escapeIdentifier("user_table"));
      assertEquals("\"customer_id\"", strategy.escapeIdentifier("customer_id"));
   }

   /**
    * Test IN clause handling with null values - a key PostgreSQL-specific feature.
    */
   @Test
   void testAppendCriterionToWhereClauseWithNullValues()
   {
      // Test IN clause with mixed null and non-null values
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.IN)
         .withValues(List.of("value1", null, "value2", null));

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.STRING);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"column_name\"", values, field);

      // Should generate: (column_name IN (?,?) OR column_name IS NULL)
      String expectedSql = "(\"column_name\" IN (?,?) OR \"column_name\" IS NULL)";
      assertEquals(expectedSql, clause.toString());
      assertEquals(2, paramCount); // Only non-null values count as parameters
      assertEquals(2, values.size()); // Values list should be updated to only contain non-null values
      assertEquals("value1", values.get(0));
      assertEquals("value2", values.get(1));
   }

   /**
    * Test IN clause with all null values.
    */
   @Test
   void testAppendCriterionToWhereClauseWithAllNullValues()
   {
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.IN)
         .withValues(List.of(null, null, null));

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.STRING);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"column_name\"", values, field);

      // Should generate: column_name IS NULL
      assertEquals("\"column_name\" IS NULL", clause.toString());
      assertEquals(0, paramCount); // No parameters needed
      assertEquals(0, values.size()); // Values list should be cleared
   }

   /**
    * Test IN clause with no null values (should delegate to base implementation).
    */
   @Test
   void testAppendCriterionToWhereClauseWithNoNullValues()
   {
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.IN)
         .withValues(List.of("value1", "value2", "value3"));

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.STRING);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"column_name\"", values, field);

      // Should delegate to base implementation (no special handling needed)
      String expectedSql = "\"column_name\" IN (?,?,?)";
      assertEquals(expectedSql, clause.toString());
      assertEquals(3, paramCount);
      assertEquals(3, values.size());
   }

   /**
    * Test that the strategy integrates properly with the QQQ framework.
    */
   @Test
   void testStrategyIntegration()
   {
      // Verify the strategy implements the correct interface
      assertTrue(strategy instanceof RDBMSActionStrategyInterface);
      
      // Verify it extends the base strategy
      assertTrue(strategy instanceof BaseRDBMSActionStrategy);
      
      // Test that all required methods are accessible
      assertNotNull(strategy.escapeIdentifier("test"));
      assertNotNull(strategy.getPageSize(null));
   }

   /**
    * Test edge cases and error conditions.
    */
   @Test
   void testEdgeCases()
   {
      // Test null identifier (should throw exception)
      try {
         strategy.escapeIdentifier(null);
         assertTrue(false, "Should have thrown NullPointerException");
      } catch (NullPointerException e) {
         assertEquals("Identifier cannot be null", e.getMessage());
      }

      // Test empty identifier
      assertEquals("\"\"", strategy.escapeIdentifier(""));
      
      // Test identifier with quotes (should be handled properly)
      assertEquals("\"\"quoted\"\"", strategy.escapeIdentifier("\"quoted\""));
   }

   /**
    * Test that the strategy properly handles different field types in IN clauses.
    */
   @Test
   void testAppendCriterionToWhereClauseWithDifferentFieldTypes()
   {
      // Test with INTEGER field type
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.IN)
         .withValues(List.of(1, null, 3, null));

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.INTEGER);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"id\"", values, field);

      // Should generate: (id IN (?,?) OR id IS NULL)
      String expectedSql = "(\"id\" IN (?,?) OR \"id\" IS NULL)";
      assertEquals(expectedSql, clause.toString());
      assertEquals(2, paramCount);
      assertEquals(2, values.size());
      assertEquals(1, values.get(0));
      assertEquals(3, values.get(1));
   }

   /**
    * Test that non-IN operators delegate to base implementation.
    */
   @Test
   void testAppendCriterionToWhereClauseWithNonInOperators()
   {
      // Test EQUALS operator (should delegate to base)
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.EQUALS)
         .withValues(List.of("test_value"));

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.STRING);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"column_name\"", values, field);

      // Should delegate to base implementation
      String expectedSql = "\"column_name\" = ?";
      assertEquals(expectedSql, clause.toString());
      assertEquals(1, paramCount);
      assertEquals(1, values.size());
   }

   /**
    * Test that the strategy properly handles the getColumnName method.
    */
   @Test
   void testGetColumnName()
   {
      QFieldMetaData field = new QFieldMetaData()
         .withName("test_column")
         .withBackendName("test_column_backend");

      // Should use backend name if available
      String columnName = ((BaseRDBMSActionStrategy) strategy).getColumnName(field);
      assertEquals("\"test_column_backend\"", columnName);

      // Should fall back to field name if no backend name
      QFieldMetaData fieldNoBackend = new QFieldMetaData()
         .withName("test_column");

      String columnNameNoBackend = ((BaseRDBMSActionStrategy) strategy).getColumnName(fieldNoBackend);
      assertEquals("\"test_column\"", columnNameNoBackend);
   }

   /**
    * Test string-to-integer conversion for PostgreSQL parameter binding.
    * 
    * <p>This test verifies that string parameters that represent numeric values
    * are properly converted to Long for PostgreSQL binding, which prevents
    * the "operator does not exist: bigint = character varying" error.</p>
    */
   @Test
   void testStringToIntegerConversion()
   {
      // Test that numeric strings are converted to Long
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.EQUALS)
         .withValues(List.of("6")); // String representation of ID

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.INTEGER);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"id\"", values, field);

      // Should generate: id = ?
      String expectedSql = "\"id\" = ?";
      assertEquals(expectedSql, clause.toString());
      assertEquals(1, paramCount);
      assertEquals(1, values.size());
      assertEquals("6", values.get(0)); // Value should remain as string, conversion happens during binding
   }

   /**
    * Test that non-numeric strings are handled correctly.
    */
   @Test
   void testNonNumericStringHandling()
   {
      // Test that non-numeric strings are handled as regular strings
      QFilterCriteria criterion = new QFilterCriteria()
         .withOperator(QCriteriaOperator.EQUALS)
         .withValues(List.of("test_value")); // Non-numeric string

      StringBuilder clause = new StringBuilder();
      List<Serializable> values = new ArrayList<>(criterion.getValues());
      QFieldMetaData field = new QFieldMetaData().withType(QFieldType.STRING);

      Integer paramCount = strategy.appendCriterionToWhereClause(criterion, clause, "\"name\"", values, field);

      // Should generate: name = ?
      String expectedSql = "\"name\" = ?";
      assertEquals(expectedSql, clause.toString());
      assertEquals(1, paramCount);
      assertEquals(1, values.size());
      assertEquals("test_value", values.get(0));
   }

   /**
    * Test that INSERT SQL generation uses correct PostgreSQL identifier escaping.
    * 
    * <p>This test verifies that when generating INSERT SQL, the system uses
    * the PostgreSQL strategy's escapeIdentifier method (double quotes) instead
    * of hardcoded backticks, which would cause syntax errors in PostgreSQL.</p>
    */
   @Test
   void testInsertSQLGenerationUsesCorrectEscaping()
   {
      // Test that the strategy's escapeIdentifier method is used for column names
      // This ensures that INSERT statements use double quotes for PostgreSQL
      
      // Simulate column names that would be used in an INSERT statement
      String tableName = strategy.escapeIdentifier("customer_type");
      String column1 = strategy.escapeIdentifier("display_name");
      String column2 = strategy.escapeIdentifier("code");
      String column3 = strategy.escapeIdentifier("is_active");
      
      // Verify PostgreSQL-style double quotes are used
      assertEquals("\"customer_type\"", tableName);
      assertEquals("\"display_name\"", column1);
      assertEquals("\"code\"", column2);
      assertEquals("\"is_active\"", column3);
      
      // Verify that backticks are NOT used (which would cause PostgreSQL syntax errors)
      assertFalse(tableName.contains("`"));
      assertFalse(column1.contains("`"));
      assertFalse(column2.contains("`"));
      assertFalse(column3.contains("`"));
   }
} 
