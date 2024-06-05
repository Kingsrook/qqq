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

package com.kingsrook.qqq.backend.module.rdbms.jdbc;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.module.rdbms.BaseTest;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.ConnectionPoolSettings;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.mchange.v2.resourcepool.TimeoutException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 ** Unit test for C3P0PooledConnectionProvider 
 *******************************************************************************/
class C3P0PooledConnectionProviderTest extends BaseTest
{


   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws Exception
   {
      TestUtils.primeTestDatabase("prime-test-database.sql");

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // must call this after the primeTestDatabase call (as i uses a raw version of the backend, w/o our updated settings) //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      ConnectionManager.resetConnectionProviders();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach()
   {
      ////////////////////////////////////////////////////////////
      // just for good measure, do this after each test in here //
      ////////////////////////////////////////////////////////////
      ConnectionManager.resetConnectionProviders();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   // @RepeatedTest(100)
   void test() throws Exception
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // change the default database backend to use the class under test here - the C3PL connection pool provider //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance            qInstance = TestUtils.defineInstance();
      RDBMSBackendMetaData backend   = (RDBMSBackendMetaData) qInstance.getBackend(TestUtils.DEFAULT_BACKEND_NAME);
      backend.setConnectionProvider(new QCodeReference(C3P0PooledConnectionProvider.class));
      QContext.init(qInstance, new QSession());

      for(int i = 0; i < 5; i++)
      {
         new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON));
      }

      JSONObject debugValues = getDebugStateValues(true);
      assertThat(debugValues.getInt("numConnections")).isEqualTo(3); // one time (in a @RepeatedTest(100) we saw a 3 != 6 here...)

      ////////////////////////////////////////////////////////////////////
      // open up 4 transactions - confirm the pool opens some new conns //
      ////////////////////////////////////////////////////////////////////
      List<QBackendTransaction> transactions = new ArrayList<>();
      for(int i = 0; i < 5; i++)
      {
         transactions.add(QBackendTransaction.openFor(new InsertInput(TestUtils.TABLE_NAME_PERSON)));
      }

      debugValues = getDebugStateValues(true);
      assertThat(debugValues.getInt("numConnections")).isGreaterThan(3);

      transactions.forEach(transaction -> transaction.close());

      /////////////////////////////////////////////////////////////////////////
      // might take a second for the pool to re-claim the closed connections //
      /////////////////////////////////////////////////////////////////////////
      boolean foundMatch = false;
      for(int i = 0; i < 5; i++)
      {
         debugValues = getDebugStateValues(true);
         if(debugValues.getInt("numConnections") == debugValues.getInt("numIdleConnections"))
         {
            foundMatch = true;
            break;
         }
         System.out.println("oops!");
         SleepUtils.sleep(250, TimeUnit.MILLISECONDS);
      }

      assertTrue(foundMatch, "The pool didn't re-claim all connections...");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testPoolSettings() throws Exception
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // change the default database backend to use the class under test here - the C3PL connection pool provider //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////
      QInstance            qInstance = TestUtils.defineInstance();
      RDBMSBackendMetaData backend   = (RDBMSBackendMetaData) qInstance.getBackend(TestUtils.DEFAULT_BACKEND_NAME);
      backend.setConnectionProvider(new QCodeReference(C3P0PooledConnectionProvider.class));
      backend.setConnectionPoolSettings(new ConnectionPoolSettings()
         .withInitialPoolSize(2)
         .withAcquireIncrement(1)
         .withMinPoolSize(1)
         .withMaxPoolSize(4)
         .withCheckoutTimeoutSeconds(1));
      QContext.init(qInstance, new QSession());

      /////////////////////////
      // assert initial size //
      /////////////////////////
      new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON));
      JSONObject debugValues = getDebugStateValues(true);
      assertThat(debugValues.getInt("numConnections")).isEqualTo(2);

      ///////////////////////////////////////////////////////////////////////
      // open (and close) 5 conns - shouldn't get bigger than initial size //
      ///////////////////////////////////////////////////////////////////////
      for(int i = 0; i < 5; i++)
      {
         new QueryAction().execute(new QueryInput(TestUtils.TABLE_NAME_PERSON));
      }
      debugValues = getDebugStateValues(true);
      assertThat(debugValues.getInt("numConnections")).isEqualTo(2); // one time (in a @RepeatedTest(100) we saw a 3 != 6 here...)

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // open up 4 transactions - confirm the pool opens some new conns, but stops at the max, and throws based on checkoutTimeout setting //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      List<QBackendTransaction> transactions = new ArrayList<>();
      for(int i = 0; i < 5; i++)
      {
         if(i == 4)
         {
            //////////////////////////////////////////
            // expect this one to fail - full pool! //
            //////////////////////////////////////////
            assertThatThrownBy(() -> QBackendTransaction.openFor(new InsertInput(TestUtils.TABLE_NAME_PERSON)))
               .hasRootCauseInstanceOf(TimeoutException.class);
         }
         else
         {
            transactions.add(QBackendTransaction.openFor(new InsertInput(TestUtils.TABLE_NAME_PERSON)));
         }
      }

      debugValues = getDebugStateValues(true);
      assertThat(debugValues.getInt("numConnections")).isEqualTo(4);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static JSONObject getDebugStateValues(boolean printIt)
   {
      JSONArray debugArray = ConnectionManager.dumpConnectionProviderDebug();
      for(int i = 0; i < debugArray.length(); i++)
      {
         JSONObject object = debugArray.getJSONObject(i);
         if(TestUtils.DEFAULT_BACKEND_NAME.equals(object.optString("backendName")))
         {
            JSONObject values = object.getJSONObject("values");
            if(printIt)
            {
               System.out.println(values.toString(3));
            }

            JSONObject state = values.getJSONObject("state");
            return state;
         }
      }

      fail("Didn't find debug values...");
      return (null);
   }

}