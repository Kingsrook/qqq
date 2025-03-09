/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import com.kingsrook.qqq.backend.core.actions.automation.AutomationStatus;
import com.kingsrook.qqq.backend.module.rdbms.BaseTest;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for BaseRDBMSActionStrategy 
 *******************************************************************************/
class BaseRDBMSActionStrategyTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws SQLException
   {
      try(Connection connection = getConnection())
      {
         QueryManager.executeUpdate(connection, """
            CREATE TABLE test_table
            (
               int_col INTEGER,
               datetime_col DATETIME,
               char_col CHAR(1),
               date_col DATE,
               time_col TIME,
               long_col LONG
            )
            """);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach() throws SQLException
   {
      try(Connection connection = getConnection())
      {
         QueryManager.executeUpdate(connection, "DROP TABLE test_table");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Connection getConnection() throws SQLException
   {
      return new ConnectionManager().getConnection(TestUtils.defineBackend());
   }



   /*******************************************************************************
    ** Test the various overloads that bind params.
    ** Note, we're just confirming that these methods don't throw...
    *******************************************************************************/
   @Test
   void testBindParams() throws SQLException
   {
      try(Connection connection = getConnection())
      {
         long              ctMillis = System.currentTimeMillis();
         PreparedStatement ps       = connection.prepareStatement("UPDATE test_table SET int_col = ? WHERE int_col > 0");

         BaseRDBMSActionStrategy strategy = new BaseRDBMSActionStrategy();

         ///////////////////////////////////////////////////////////////////////////////
         // these calls - we just want to assert that they don't throw any exceptions //
         ///////////////////////////////////////////////////////////////////////////////
         strategy.bindParamObject(ps, 1, (short) 1);
         strategy.bindParamObject(ps, 1, (long) 1);
         strategy.bindParamObject(ps, 1, true);
         strategy.bindParamObject(ps, 1, BigDecimal.ONE);
         strategy.bindParamObject(ps, 1, "hello".getBytes(StandardCharsets.UTF_8));
         strategy.bindParamObject(ps, 1, new Timestamp(ctMillis));
         strategy.bindParamObject(ps, 1, new Date(ctMillis));
         strategy.bindParamObject(ps, 1, new GregorianCalendar());
         strategy.bindParamObject(ps, 1, LocalDate.now());
         strategy.bindParamObject(ps, 1, OffsetDateTime.now());
         strategy.bindParamObject(ps, 1, LocalDateTime.now());
         strategy.bindParamObject(ps, 1, AutomationStatus.PENDING_INSERT_AUTOMATIONS);

         assertThrows(SQLException.class, () -> strategy.bindParamObject(ps, 1, new Object()));

         strategy.bindParam(ps, 1, (Integer) null);
         strategy.bindParam(ps, 1, (Boolean) null);
         strategy.bindParam(ps, 1, (BigDecimal) null);
         strategy.bindParam(ps, 1, (byte[]) null);
         strategy.bindParam(ps, 1, (Timestamp) null);
         strategy.bindParam(ps, 1, (String) null);
         strategy.bindParam(ps, 1, (Date) null);
         strategy.bindParam(ps, 1, (GregorianCalendar) null);
         strategy.bindParam(ps, 1, (LocalDate) null);
         strategy.bindParam(ps, 1, (LocalDateTime) null);

         strategy.bindParam(ps, 1, 1);
         strategy.bindParam(ps, 1, true);
         strategy.bindParam(ps, 1, BigDecimal.ONE);
         strategy.bindParam(ps, 1, "hello".getBytes(StandardCharsets.UTF_8));
         strategy.bindParam(ps, 1, new Timestamp(ctMillis));
         strategy.bindParam(ps, 1, "hello");
         strategy.bindParam(ps, 1, new Date(ctMillis));
         strategy.bindParam(ps, 1, new GregorianCalendar());
         strategy.bindParam(ps, 1, LocalDate.now());
         strategy.bindParam(ps, 1, LocalDateTime.now());

         ////////////////////////////////////////////////////////////////////////////////////////////////
         // originally longs were being downgraded to int when binding, so, verify that doesn't happen //
         ////////////////////////////////////////////////////////////////////////////////////////////////
      }
   }

}