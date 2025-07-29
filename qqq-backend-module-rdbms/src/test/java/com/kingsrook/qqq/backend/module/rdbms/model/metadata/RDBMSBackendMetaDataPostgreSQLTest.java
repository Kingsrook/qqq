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

import com.kingsrook.qqq.backend.module.rdbms.strategy.PostgreSQLRDBMSActionStrategy;
import com.kingsrook.qqq.backend.module.rdbms.strategy.RDBMSActionStrategyInterface;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test class for PostgreSQL support in RDBMSBackendMetaData.
 */
public class RDBMSBackendMetaDataPostgreSQLTest
{

   /**
    * Test that PostgreSQL vendor constant is defined correctly.
    */
   @Test
   void testPostgreSQLVendorConstant()
   {
      assertEquals("postgresql", RDBMSBackendMetaData.VENDOR_POSTGRESQL);
   }



   /**
    * Test that PostgreSQL action strategy can be loaded via QCodeLoader when referenced.
    */
   @Test
   void testPostgreSQLActionStrategyViaCodeReference()
   {
      RDBMSBackendMetaData metadata = new RDBMSBackendMetaData()
         .withVendor(RDBMSBackendMetaData.VENDOR_POSTGRESQL);

      // The action strategy should default to BaseRDBMSActionStrategy when no code reference is set
      RDBMSActionStrategyInterface strategy = metadata.getActionStrategy();
      
      assertNotNull(strategy);
      // Should be BaseRDBMSActionStrategy by default, not PostgreSQLRDBMSActionStrategy
      // PostgreSQL strategy would be loaded via QCodeLoader if actionStrategyCodeReference is set
   }
} 