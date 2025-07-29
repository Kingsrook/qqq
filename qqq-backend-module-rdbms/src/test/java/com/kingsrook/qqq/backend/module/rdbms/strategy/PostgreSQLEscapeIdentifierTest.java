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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for PostgreSQL-specific escapeIdentifier functionality.
 * Tests that PostgreSQL uses double quotes instead of backticks.
 */
class PostgreSQLEscapeIdentifierTest
{

   /**
    * Test basic PostgreSQL identifier escaping functionality.
    * Verifies that PostgreSQL uses double quotes for identifier escaping.
    */
   @Test
   void testPostgreSQLRDBMSActionStrategyEscapeIdentifier()
   {
      PostgreSQLRDBMSActionStrategy strategy = new PostgreSQLRDBMSActionStrategy();
      
      // Test basic identifier escaping with double quotes
      assertEquals("\"table_name\"", strategy.escapeIdentifier("table_name"));
      assertEquals("\"column_name\"", strategy.escapeIdentifier("column_name"));
      assertEquals("\"user\"", strategy.escapeIdentifier("user"));
      
      // Test identifiers with special characters
      assertEquals("\"table-name\"", strategy.escapeIdentifier("table-name"));
      assertEquals("\"table_name_123\"", strategy.escapeIdentifier("table_name_123"));
      assertEquals("\"table name\"", strategy.escapeIdentifier("table name"));
      
      // Test edge cases
      assertEquals("\"\"", strategy.escapeIdentifier(""));
      assertEquals("\"null\"", strategy.escapeIdentifier("null"));
   }

   /**
    * Test that PostgreSQL escapeIdentifier handles null input correctly.
    * Should throw NullPointerException when null is passed.
    */
   @Test
   void testPostgreSQLEscapeIdentifierWithNullInput()
   {
      PostgreSQLRDBMSActionStrategy strategy = new PostgreSQLRDBMSActionStrategy();
      
      // Should handle null gracefully
      assertThrows(NullPointerException.class, () ->
      {
         strategy.escapeIdentifier(null);
      });
   }

   /**
    * Test PostgreSQL escapeIdentifier with SQL reserved words.
    * Verifies that reserved words are properly escaped with double quotes.
    */
   @Test
   void testPostgreSQLEscapeIdentifierWithReservedWords()
   {
      PostgreSQLRDBMSActionStrategy strategy = new PostgreSQLRDBMSActionStrategy();
      
      // Test with SQL reserved words
      assertEquals("\"select\"", strategy.escapeIdentifier("select"));
      assertEquals("\"from\"", strategy.escapeIdentifier("from"));
      assertEquals("\"where\"", strategy.escapeIdentifier("where"));
      assertEquals("\"order\"", strategy.escapeIdentifier("order"));
      assertEquals("\"group\"", strategy.escapeIdentifier("group"));
   }

   /**
    * Test PostgreSQL escapeIdentifier with mixed case identifiers.
    * Verifies that case is preserved when escaping identifiers.
    */
   @Test
   void testPostgreSQLEscapeIdentifierWithMixedCase()
   {
      PostgreSQLRDBMSActionStrategy strategy = new PostgreSQLRDBMSActionStrategy();
      
      // Test with mixed case identifiers
      assertEquals("\"TableName\"", strategy.escapeIdentifier("TableName"));
      assertEquals("\"TABLE_NAME\"", strategy.escapeIdentifier("TABLE_NAME"));
      assertEquals("\"tableName\"", strategy.escapeIdentifier("tableName"));
   }

   /**
    * Test that PostgreSQL strategy differs from base strategy.
    * Verifies that PostgreSQL uses double quotes while base strategy uses backticks.
    */
   @Test
   void testPostgreSQLVsBaseStrategyDifference()
   {
      BaseRDBMSActionStrategy baseStrategy = new BaseRDBMSActionStrategy();
      PostgreSQLRDBMSActionStrategy postgresStrategy = new PostgreSQLRDBMSActionStrategy();
      
      String identifier = "table_name";
      
      // PostgreSQL should use double quotes, base should use backticks
      assertEquals("`table_name`", baseStrategy.escapeIdentifier(identifier));
      assertEquals("\"table_name\"", postgresStrategy.escapeIdentifier(identifier));
      
      // They should be different
      assertNotEquals(
         baseStrategy.escapeIdentifier(identifier),
         postgresStrategy.escapeIdentifier(identifier)
      );
   }
} 