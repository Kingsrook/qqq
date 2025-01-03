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
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.module.rdbms.BaseTest;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.strategy.BaseRDBMSActionStrategy;
import org.junit.jupiter.api.AfterEach;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEachRDBMSActionTest()
   {
      BaseRDBMSActionStrategy actionStrategy = getBaseRDBMSActionStrategy();
      actionStrategy.setPageSize(BaseRDBMSActionStrategy.DEFAULT_PAGE_SIZE);
      actionStrategy.resetStatistics();
      actionStrategy.setCollectStatistics(false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void primeTestDatabase() throws Exception
   {
      TestUtils.primeTestDatabase("prime-test-database.sql");
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected static BaseRDBMSActionStrategy getBaseRDBMSActionStrategy()
   {
      RDBMSBackendMetaData    backend        = (RDBMSBackendMetaData) QContext.getQInstance().getBackend(TestUtils.DEFAULT_BACKEND_NAME);
      BaseRDBMSActionStrategy actionStrategy = (BaseRDBMSActionStrategy) backend.getActionStrategy();
      return actionStrategy;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected static BaseRDBMSActionStrategy getBaseRDBMSActionStrategyAndActivateCollectingStatistics()
   {
      BaseRDBMSActionStrategy actionStrategy = getBaseRDBMSActionStrategy();
      actionStrategy.setCollectStatistics(true);
      actionStrategy.resetStatistics();
      return actionStrategy;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void runTestSql(String sql, QueryManager.ResultSetProcessor resultSetProcessor) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection        connection        = connectionManager.getConnection(TestUtils.defineBackend());
      QueryManager.executeStatement(connection, sql, resultSetProcessor);
      connection.close();
   }
}
