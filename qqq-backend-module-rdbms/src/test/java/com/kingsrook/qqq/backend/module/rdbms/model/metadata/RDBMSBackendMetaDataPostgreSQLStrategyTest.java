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

package com.kingsrook.qqq.backend.module.rdbms.model.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import com.kingsrook.qqq.backend.module.rdbms.strategy.BaseRDBMSActionStrategy;
import com.kingsrook.qqq.backend.module.rdbms.strategy.PostgreSQLRDBMSActionStrategy;
import com.kingsrook.qqq.backend.module.rdbms.strategy.RDBMSActionStrategyInterface;

/**
 * Test for RDBMSBackendMetaData PostgreSQL strategy selection.
 * 
 * <p>This test verifies that the RDBMSBackendMetaData correctly selects the appropriate
 * action strategy based on the vendor configuration. This is critical for ensuring
 * that PostgreSQL-specific functionality is used when the vendor is set to "postgresql".</p>
 * 
 * <p>This test serves as a template for testing other vendor strategy selections in the future.</p>
 */
public class RDBMSBackendMetaDataPostgreSQLStrategyTest
{

   /**
    * Test that PostgreSQL strategy is selected when vendor is set to postgresql.
    */
   @Test
   void testPostgreSQLStrategySelection()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_POSTGRESQL);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      assertNotNull(actionStrategy);
      assertTrue(actionStrategy instanceof PostgreSQLRDBMSActionStrategy);
      assertTrue(actionStrategy instanceof BaseRDBMSActionStrategy);
   }

   /**
    * Test that base strategy is selected when vendor is not postgresql.
    */
   @Test
   void testNonPostgreSQLStrategySelection()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_MYSQL);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      assertNotNull(actionStrategy);
      assertTrue(actionStrategy instanceof BaseRDBMSActionStrategy);
      assertTrue(!(actionStrategy instanceof PostgreSQLRDBMSActionStrategy));
   }

   /**
    * Test that base strategy is selected when vendor is null.
    */
   @Test
   void testNullVendorStrategySelection()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(null);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      assertNotNull(actionStrategy);
      assertTrue(actionStrategy instanceof BaseRDBMSActionStrategy);
      assertTrue(!(actionStrategy instanceof PostgreSQLRDBMSActionStrategy));
   }

   /**
    * Test that base strategy is selected when vendor is empty string.
    */
   @Test
   void testEmptyVendorStrategySelection()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor("");

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      assertNotNull(actionStrategy);
      assertTrue(actionStrategy instanceof BaseRDBMSActionStrategy);
      assertTrue(!(actionStrategy instanceof PostgreSQLRDBMSActionStrategy));
   }

   /**
    * Test that PostgreSQL strategy uses double quotes for identifier escaping.
    */
   @Test
   void testPostgreSQLIdentifierEscaping()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_POSTGRESQL);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      // PostgreSQL should use double quotes
      assertEquals("\"table_name\"", actionStrategy.escapeIdentifier("table_name"));
      assertEquals("\"column_name\"", actionStrategy.escapeIdentifier("column_name"));
      assertEquals("\"select\"", actionStrategy.escapeIdentifier("select"));
   }

   /**
    * Test that base strategy uses backticks for identifier escaping.
    */
   @Test
   void testBaseStrategyIdentifierEscaping()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_MYSQL);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      // Base strategy should use backticks
      assertEquals("`table_name`", actionStrategy.escapeIdentifier("table_name"));
      assertEquals("`column_name`", actionStrategy.escapeIdentifier("column_name"));
      assertEquals("`select`", actionStrategy.escapeIdentifier("select"));
   }

   /**
    * Test that strategy selection is consistent across multiple calls.
    */
   @Test
   void testStrategySelectionConsistency()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_POSTGRESQL);

      RDBMSActionStrategyInterface firstStrategy = backendMetaData.getActionStrategy();
      RDBMSActionStrategyInterface secondStrategy = backendMetaData.getActionStrategy();

      // Should return the same instance (cached)
      assertTrue(firstStrategy == secondStrategy);
      assertTrue(firstStrategy instanceof PostgreSQLRDBMSActionStrategy);
   }

   /**
    * Test that vendor constants are correctly defined.
    */
   @Test
   void testVendorConstants()
   {
      assertEquals("postgresql", RDBMSBackendMetaData.VENDOR_POSTGRESQL);
      assertEquals("mysql", RDBMSBackendMetaData.VENDOR_MYSQL);
      assertEquals("h2", RDBMSBackendMetaData.VENDOR_H2);
      assertEquals("aurora-mysql", RDBMSBackendMetaData.VENDOR_AURORA_MYSQL);
   }

   /**
    * Test that the strategy can be used for actual database operations.
    */
   @Test
   void testStrategyFunctionality()
   {
      RDBMSBackendMetaData backendMetaData = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_POSTGRESQL);

      RDBMSActionStrategyInterface actionStrategy = backendMetaData.getActionStrategy();

      // Test that the strategy has all required functionality
      assertNotNull(actionStrategy.escapeIdentifier("test"));
      assertNotNull(actionStrategy.getPageSize(null));
      
      // Test that it's a valid PostgreSQL strategy
      assertTrue(actionStrategy instanceof PostgreSQLRDBMSActionStrategy);
      PostgreSQLRDBMSActionStrategy postgresStrategy = (PostgreSQLRDBMSActionStrategy) actionStrategy;
      
      // Verify PostgreSQL-specific behavior
      assertEquals("\"test\"", postgresStrategy.escapeIdentifier("test"));
   }
} 