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


import java.sql.Connection;
import com.kingsrook.qqq.backend.module.rdbms.BaseTest;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RDBMSTransaction
 *******************************************************************************/
class RDBMSTransactionTest extends BaseTest
{
   private final String testToken = getClass().getName();



   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   protected void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase("prime-test-database.sql");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCommit() throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection        connection        = connectionManager.getConnection(TestUtils.defineBackend());
      Integer           preCount          = QueryManager.executeStatementForSingleValue(connection, Integer.class, "SELECT COUNT(*) FROM person");

      Connection       connectionForTransaction = connectionManager.getConnection(TestUtils.defineBackend());
      RDBMSTransaction transaction              = new RDBMSTransaction(connectionForTransaction);

      QueryManager.executeUpdate(transaction.getConnection(), "INSERT INTO person (first_name, last_name, email) VALUES (?, ?, ?)", testToken, testToken, testToken);
      transaction.commit();

      Integer postCount = QueryManager.executeStatementForSingleValue(connection, Integer.class, "SELECT COUNT(*) FROM person");
      assertEquals(preCount + 1, postCount);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRollback() throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection        connection        = connectionManager.getConnection(TestUtils.defineBackend());
      Integer           preCount          = QueryManager.executeStatementForSingleValue(connection, Integer.class, "SELECT COUNT(*) FROM person");

      Connection       connectionForTransaction = connectionManager.getConnection(TestUtils.defineBackend());
      RDBMSTransaction transaction              = new RDBMSTransaction(connectionForTransaction);

      QueryManager.executeUpdate(transaction.getConnection(), "INSERT INTO person (first_name, last_name, email) VALUES (?, ?, ?)", testToken, testToken, testToken);
      transaction.rollback();

      Integer postCount = QueryManager.executeStatementForSingleValue(connection, Integer.class, "SELECT COUNT(*) FROM person");
      assertEquals(preCount, postCount);
   }

}