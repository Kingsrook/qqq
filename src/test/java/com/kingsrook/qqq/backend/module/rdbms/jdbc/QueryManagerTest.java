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

package com.kingsrook.qqq.backend.module.rdbms.jdbc;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.GregorianCalendar;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 **
 *******************************************************************************/
class QueryManagerTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws SQLException
   {
      Connection connection = getConnection();
      QueryManager.executeUpdate(connection, "CREATE TABLE t (i INTEGER, dt DATETIME, c CHAR(1))");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   void afterEach() throws SQLException
   {
      Connection connection = getConnection();
      QueryManager.executeUpdate(connection, "DROP TABLE t");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Connection getConnection() throws SQLException
   {
      return new ConnectionManager().getConnection(TestUtils.defineBackend());
   }



   /*******************************************************************************
    ** Test the various overloads that bind params
    *******************************************************************************/
   @Test
   void testBindParams() throws SQLException
   {
      long              ctMillis   = System.currentTimeMillis();
      Connection        connection = getConnection();
      PreparedStatement ps         = connection.prepareStatement("UPDATE t SET i = ? WHERE i > 0");

      ///////////////////////////////////////////////////////////////////////////////
      // these calls - we just want to assert that they don't throw any exceptions //
      ///////////////////////////////////////////////////////////////////////////////
      QueryManager.bindParamObject(ps, 1, (short) 1);
      QueryManager.bindParamObject(ps, 1, (long) 1);
      QueryManager.bindParamObject(ps, 1, true);
      QueryManager.bindParamObject(ps, 1, BigDecimal.ONE);
      QueryManager.bindParamObject(ps, 1, "hello".getBytes(StandardCharsets.UTF_8));
      QueryManager.bindParamObject(ps, 1, new Timestamp(ctMillis));
      QueryManager.bindParamObject(ps, 1, new Date(ctMillis));
      QueryManager.bindParamObject(ps, 1, new GregorianCalendar());
      QueryManager.bindParamObject(ps, 1, LocalDate.now());
      QueryManager.bindParamObject(ps, 1, OffsetDateTime.now());
      QueryManager.bindParamObject(ps, 1, LocalDateTime.now());

      assertThrows(SQLException.class, () ->
      {
         QueryManager.bindParamObject(ps, 1, new Object());
      });

      QueryManager.bindParam(ps, 1, (Integer) null);
      QueryManager.bindParam(ps, 1, (Boolean) null);
      QueryManager.bindParam(ps, 1, (BigDecimal) null);
      QueryManager.bindParam(ps, 1, (byte[]) null);
      QueryManager.bindParam(ps, 1, (Timestamp) null);
      QueryManager.bindParam(ps, 1, (String) null);
      QueryManager.bindParam(ps, 1, (Date) null);
      QueryManager.bindParam(ps, 1, (GregorianCalendar) null);
      QueryManager.bindParam(ps, 1, (LocalDate) null);
      QueryManager.bindParam(ps, 1, (LocalDateTime) null);

      QueryManager.bindParam(ps, 1, 1);
      QueryManager.bindParam(ps, 1, true);
      QueryManager.bindParam(ps, 1, BigDecimal.ONE);
      QueryManager.bindParam(ps, 1, "hello".getBytes(StandardCharsets.UTF_8));
      QueryManager.bindParam(ps, 1, new Timestamp(ctMillis));
      QueryManager.bindParam(ps, 1, "hello");
      QueryManager.bindParam(ps, 1, new Date(ctMillis));
      QueryManager.bindParam(ps, 1, new GregorianCalendar());
      QueryManager.bindParam(ps, 1, LocalDate.now());
      QueryManager.bindParam(ps, 1, LocalDateTime.now());
   }



   /*******************************************************************************
    ** Test the various getXXX methods from result sets
    *******************************************************************************/
   @Test
   void testGetValueMethods() throws SQLException
   {
      Connection connection = getConnection();
      QueryManager.executeUpdate(connection, "INSERT INTO t (i, dt, c) VALUES (1, now(), 'A')");
      PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from t");
      preparedStatement.execute();
      ResultSet rs = preparedStatement.getResultSet();
      rs.next();

      assertEquals(1, QueryManager.getInteger(rs, "i"));
      assertEquals(1, QueryManager.getInteger(rs, 1));
      assertEquals(1L, QueryManager.getLong(rs, "i"));
      assertEquals(1L, QueryManager.getLong(rs, 1));
      assertArrayEquals(new byte[] { 0, 0, 0, 1 }, QueryManager.getByteArray(rs, "i"));
      assertArrayEquals(new byte[] { 0, 0, 0, 1 }, QueryManager.getByteArray(rs, 1));
      assertEquals(1, QueryManager.getObject(rs, "i"));
      assertEquals(1, QueryManager.getObject(rs, 1));
      assertEquals(BigDecimal.ONE, QueryManager.getBigDecimal(rs, "i"));
      assertEquals(BigDecimal.ONE, QueryManager.getBigDecimal(rs, 1));
      assertEquals(true, QueryManager.getBoolean(rs, "i"));
      assertEquals(true, QueryManager.getBoolean(rs, 1));
      assertNotNull(QueryManager.getDate(rs, "dt"));
      assertNotNull(QueryManager.getDate(rs, 2));
      assertNotNull(QueryManager.getCalendar(rs, "dt"));
      assertNotNull(QueryManager.getCalendar(rs, 2));
      assertNotNull(QueryManager.getLocalDate(rs, "dt"));
      assertNotNull(QueryManager.getLocalDate(rs, 2));
      assertNotNull(QueryManager.getLocalDateTime(rs, "dt"));
      assertNotNull(QueryManager.getLocalDateTime(rs, 2));
      assertNotNull(QueryManager.getOffsetDateTime(rs, "dt"));
      assertNotNull(QueryManager.getOffsetDateTime(rs, 2));
      assertNotNull(QueryManager.getTimestamp(rs, "dt"));
      assertNotNull(QueryManager.getTimestamp(rs, 2));
      assertEquals("A", QueryManager.getObject(rs, "c"));
      assertEquals("A", QueryManager.getObject(rs, 3));
   }



   /*******************************************************************************
    ** Test the various getXXX methods from result sets, when they return null
    *******************************************************************************/
   @Test
   void testGetValueMethodsReturningNull() throws SQLException
   {
      Connection connection = getConnection();
      QueryManager.executeUpdate(connection, "INSERT INTO t (i, dt, c) VALUES (null, null, null)");
      PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from t");
      preparedStatement.execute();
      ResultSet rs = preparedStatement.getResultSet();
      rs.next();

      assertNull(QueryManager.getInteger(rs, "i"));
      assertNull(QueryManager.getInteger(rs, 1));
      assertNull(QueryManager.getLong(rs, "i"));
      assertNull(QueryManager.getLong(rs, 1));
      assertNull(QueryManager.getByteArray(rs, "i"));
      assertNull(QueryManager.getByteArray(rs, 1));
      assertNull(QueryManager.getObject(rs, "i"));
      assertNull(QueryManager.getObject(rs, 1));
      assertNull(QueryManager.getBigDecimal(rs, "i"));
      assertNull(QueryManager.getBigDecimal(rs, 1));
      assertNull(QueryManager.getBoolean(rs, "i"));
      assertNull(QueryManager.getBoolean(rs, 1));
      assertNull(QueryManager.getDate(rs, "dt"));
      assertNull(QueryManager.getDate(rs, 2));
      assertNull(QueryManager.getCalendar(rs, "dt"));
      assertNull(QueryManager.getCalendar(rs, 2));
      assertNull(QueryManager.getLocalDate(rs, "dt"));
      assertNull(QueryManager.getLocalDate(rs, 2));
      assertNull(QueryManager.getLocalDateTime(rs, "dt"));
      assertNull(QueryManager.getLocalDateTime(rs, 2));
      assertNull(QueryManager.getOffsetDateTime(rs, "dt"));
      assertNull(QueryManager.getOffsetDateTime(rs, 2));
      assertNull(QueryManager.getTimestamp(rs, "dt"));
      assertNull(QueryManager.getTimestamp(rs, 2));
      assertNull(QueryManager.getObject(rs, "c"));
      assertNull(QueryManager.getObject(rs, 3));
   }

}