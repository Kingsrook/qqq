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


import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import static junit.framework.Assert.assertNotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   private void afterEachRDBMSActionTest()
   {
      QueryManager.resetPageSize();
      QueryManager.resetStatistics();
      QueryManager.setCollectStatistics(false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void primeTestDatabase() throws Exception
   {
      primeTestDatabase("prime-test-database.sql");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   protected void primeTestDatabase(String sqlFileName) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      try(Connection connection = connectionManager.getConnection(TestUtils.defineBackend()))
      {
         InputStream primeTestDatabaseSqlStream = RDBMSActionTest.class.getResourceAsStream("/" + sqlFileName);
         assertNotNull(primeTestDatabaseSqlStream);
         List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
         lines = lines.stream().filter(line -> !line.startsWith("-- ")).toList();
         String joinedSQL = String.join("\n", lines);
         for(String sql : joinedSQL.split(";"))
         {
            QueryManager.executeUpdate(connection, sql);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void runTestSql(String sql, QueryManager.ResultSetProcessor resultSetProcessor) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection        connection        = connectionManager.getConnection(TestUtils.defineBackend());
      QueryManager.executeStatement(connection, sql, resultSetProcessor);
   }
}
