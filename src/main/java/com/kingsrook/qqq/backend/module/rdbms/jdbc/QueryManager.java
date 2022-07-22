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


import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;


/*******************************************************************************
 **
 *******************************************************************************/
public class QueryManager
{
   public static final int DEFAULT_PAGE_SIZE = 2000;
   public static       int PAGE_SIZE         = DEFAULT_PAGE_SIZE;

   private static final int MS_PER_SEC       = 1000;
   private static final int NINETEEN_HUNDRED = 1900;

   private static boolean collectStatistics = false;

   private static final Map<String, Integer> statistics = Collections.synchronizedMap(new HashMap<>());

   public static final String STAT_QUERIES_RAN = "queriesRan";
   public static final String STAT_BATCHES_RAN = "batchesRan";



   /*******************************************************************************
    **
    *******************************************************************************/
   @FunctionalInterface
   public interface ResultSetProcessor
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      void processResultSet(ResultSet rs) throws SQLException;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeStatement(Connection connection, String sql, ResultSetProcessor procesor, Object... params) throws SQLException
   {
      PreparedStatement statement = null;
      ResultSet         resultSet = null;

      try
      {
         statement = prepareStatementAndBindParams(connection, sql, params);
         incrementStatistic(STAT_QUERIES_RAN);
         statement.execute();
         resultSet = statement.getResultSet();

         procesor.processResultSet(resultSet);
      }
      finally
      {
         if(statement != null)
         {
            statement.close();
         }

         if(resultSet != null)
         {
            resultSet.close();
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeStatementForeachResult(Connection connection, String sql, ResultSetProcessor processor, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      PreparedStatement statement = null;
      ResultSet resultSet = null;

      try
      {
         if(params.length == 1 && params[0] instanceof Collection)
         {
            params = ((Collection) params[0]).toArray();
         }

         statement = prepareStatementAndBindParams(connection, sql, params);
         statement.execute();
         resultSet = statement.getResultSet();

         while(resultSet.next())
         {
            processor.processResultSet(resultSet);
         }
      }
      finally
      {
         if(statement != null)
         {
            statement.close();
         }

         if(resultSet != null)
         {
            resultSet.close();
         }
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static <T> T executeStatementForSingleValue(Connection connection, Class<T> returnClass, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      statement.execute();
      ResultSet resultSet = statement.getResultSet();
      if(resultSet.next())
      {
         Object object = resultSet.getObject(1);
         if(resultSet.wasNull() || object == null)
         {
            return (null);
         }

         if(object instanceof Long && returnClass.equals(Integer.class))
         {
            return (T) Integer.valueOf(((Long) object).intValue());
         }
         else if(object instanceof BigInteger && returnClass.equals(Integer.class))
         {
            return (T) Integer.valueOf(((BigInteger) object).intValue());
         }
         else if(object instanceof BigDecimal && returnClass.equals(Integer.class))
         {
            return (T) Integer.valueOf(((BigDecimal) object).intValue());
         }
         else if(object instanceof Integer && returnClass.equals(Long.class))
         {
            return (T) Long.valueOf(((Integer) object));
         }
         else if(object instanceof Timestamp && returnClass.equals(LocalDateTime.class))
         {
            Timestamp timestamp = (Timestamp) object;
            return ((T) LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneId.systemDefault()));
         }
         else
         {
            if(returnClass.equals(String.class))
            {
               return (T) String.valueOf(object);
            }
            else
            {
               return (returnClass.cast(object));
            }
         }
      }
      else
      {
         return (null);
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, Object> executeStatementForSingleRow(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      statement.execute();
      ResultSet resultSet = statement.getResultSet();
      if(resultSet.next())
      {
         Map<String, Object> rs = new LinkedHashMap<>();

         ResultSetMetaData metaData = resultSet.getMetaData();
         for(int i = 1; i <= metaData.getColumnCount(); i++)
         {
            rs.put(metaData.getColumnName(i), getObject(resultSet, i));
         }

         return (rs);
      }
      else
      {
         return (null);
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static SimpleEntity executeStatementForSimpleEntity(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      statement.execute();
      ResultSet resultSet = statement.getResultSet();
      if(resultSet.next())
      {
         return (buildSimpleEntity(resultSet));
      }
      else
      {
         return (null);
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<Map<String, Object>> executeStatementForRows(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      List<Map<String, Object>> rs = new ArrayList<>();

      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      statement.execute();
      ResultSet resultSet = statement.getResultSet();
      while(resultSet.next())
      {
         Map<String, Object> row = new HashMap<>();
         rs.add(row);

         ResultSetMetaData metaData = resultSet.getMetaData();
         for(int i = 1; i <= metaData.getColumnCount(); i++)
         {
            row.put(metaData.getColumnName(i), getObject(resultSet, i));
         }
      }

      return (rs);
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<SimpleEntity> executeStatementForSimpleEntityList(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      List<SimpleEntity> rs = new ArrayList<>();

      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      statement.execute();
      ResultSet resultSet = statement.getResultSet();
      while(resultSet.next())
      {
         SimpleEntity row = buildSimpleEntity(resultSet);

         rs.add(row);
      }

      return (rs);
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static SimpleEntity buildSimpleEntity(ResultSet resultSet) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      SimpleEntity row = new SimpleEntity();

      ResultSetMetaData metaData = resultSet.getMetaData();
      for(int i = 1; i <= metaData.getColumnCount(); i++)
      {
         row.put(metaData.getColumnName(i), getObject(resultSet, i));
      }
      return row;
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static PreparedStatement executeUpdate(Connection connection, String sql, Object... params) throws SQLException
   {
      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      incrementStatistic(STAT_QUERIES_RAN);
      statement.executeUpdate();
      return (statement);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static PreparedStatement executeUpdate(Connection connection, String sql, List<Object> params) throws SQLException
   {
      PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params);
      incrementStatistic(STAT_QUERIES_RAN);
      statement.executeUpdate();
      return (statement);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeUpdateVoid(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      try(PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params))
      {
         statement.executeUpdate();
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeUpdateVoid(Connection connection, String sql, List<Object> params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      try(PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params))
      {
         statement.executeUpdate();
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer executeUpdateForRowCount(Connection connection, String sql, Object... params) throws SQLException
   {
      try(PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params))
      {
         incrementStatistic(STAT_QUERIES_RAN);
         statement.executeUpdate();
         return (statement.getUpdateCount());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer executeUpdateForRowCount(Connection connection, String sql, List<Object> params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      try(PreparedStatement statement = prepareStatementAndBindParams(connection, sql, params))
      {
         statement.executeUpdate();
         return (statement.getUpdateCount());
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer executeInsertForGeneratedId(Connection connection, String sql, Object... params) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      try(PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
      {
         bindParams(params, statement);
         statement.executeUpdate();
         ResultSet generatedKeys = statement.getGeneratedKeys();
         if(generatedKeys.next())
         {
            return (getInteger(generatedKeys, 1));
         }
         else
         {
            return (null);
         }
      }
      */
   }



   /*******************************************************************************
    ** todo - needs (specific) unit test
    *******************************************************************************/
   public static List<Integer> executeInsertForGeneratedIds(Connection connection, String sql, List<Object> params) throws SQLException
   {
      List<Integer> rs = new ArrayList<>();
      try(PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
      {
         bindParams(params.toArray(), statement);
         incrementStatistic(STAT_QUERIES_RAN);
         statement.executeUpdate();
         ResultSet generatedKeys = statement.getGeneratedKeys();
         while(generatedKeys.next())
         {
            rs.add(getInteger(generatedKeys, 1));
         }
      }
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeInsertForList(Connection connection, List<SimpleEntity> entityList) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      List<List<SimpleEntity>> pages = CollectionUtils.getPages(entityList, PAGE_SIZE);
      for(List<SimpleEntity> page : pages)
      {
         ArrayList<String> columns = new ArrayList<>(page.get(0).keySet());
         String sql = "INSERT INTO " + page.get(0).getTableName() + "(" + StringUtils.join(",", columns) + ") VALUES (" + columns.stream().map(s -> "?").collect(Collectors.joining(",")) + ")";

         PreparedStatement insertPS = connection.prepareStatement(sql);
         for(SimpleEntity entity : page)
         {
            Object[] params = new Object[columns.size()];
            for(int i = 0; i < columns.size(); i++)
            {
               params[i] = entity.get(columns.get(i));
            }

            bindParams(insertPS, params);
            insertPS.addBatch();
         }
         insertPS.executeBatch();
      }

      for(List<SimpleEntity> page : pages)
      {
         page.clear();
      }
      pages.clear();
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer executeInsert(Connection connection, SimpleEntity entity) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      ArrayList<String> columns = new ArrayList<>(entity.keySet());
      String sql = "INSERT INTO " + entity.getTableName() + "(" + StringUtils.join(",", columns) + ") VALUES (" + columns.stream().map(s -> "?").collect(Collectors.joining(",")) + ")";

      Object[] params = new Object[columns.size()];
      for(int i = 0; i < columns.size(); i++)
      {
         params[i] = entity.get(columns.get(i));
      }

      return (executeInsertForGeneratedId(connection, sql, params));
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void executeBatchUpdate(Connection connection, String updateSQL, List<List<Serializable>> values) throws SQLException
   {
      for(List<List<Serializable>> page : CollectionUtils.getPages(values, PAGE_SIZE))
      {
         PreparedStatement updatePS = connection.prepareStatement(updateSQL);
         for(List<Serializable> row : page)
         {
            Object[] params = new Object[row.size()];
            for(int i = 0; i < row.size(); i++)
            {
               params[i] = row.get(i);
            }

            bindParams(updatePS, params);
            updatePS.addBatch();
         }
         incrementStatistic(STAT_BATCHES_RAN);
         updatePS.executeBatch();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static PreparedStatement prepareStatementAndBindParams(Connection connection, String sql, Object[] params) throws SQLException
   {
      PreparedStatement statement = connection.prepareStatement(sql);
      bindParams(params, statement);
      return statement;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static PreparedStatement prepareStatementAndBindParams(Connection connection, String sql, List<Object> params) throws SQLException
   {
      PreparedStatement statement = connection.prepareStatement(sql);

      if(params != null)
      {
         for(int i = 0; i < params.size(); i++)
         {
            bindParamObject(statement, (i + 1), params.get(i));
         }
      }
      return statement;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bindParams(Object[] params, PreparedStatement statement) throws SQLException
   {
      int paramIndex = 0;
      if(params != null)
      {
         for(int i = 0; i < params.length; i++)
         {
            int paramsBound = bindParamObject(statement, (paramIndex + 1), params[i]);
            paramIndex += paramsBound;
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void bindParams(PreparedStatement statement, Object... params) throws SQLException
   {
      int paramIndex = 0;
      if(params != null)
      {
         for(int i = 0; i < params.length; i++)
         {
            int paramsBound = bindParamObject(statement, (paramIndex + 1), params[i]);
            paramIndex += paramsBound;
         }
      }
   }



   /*******************************************************************************
    * index is 1-based!!
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static int bindParamObject(PreparedStatement statement, int index, Object value) throws SQLException
   {
      /* if(value instanceof TypeValuePair)
      {
         bindParamTypeValuePair(statement, index, (TypeValuePair<Object>) value);
         return (1);
      }
      else*/
      if(value instanceof Integer)
      {
         bindParam(statement, index, (Integer) value);
         return (1);
      }
      else if(value instanceof Short)
      {
         bindParam(statement, index, ((Short) value).intValue());
         return (1);
      }
      else if(value instanceof Long)
      {
         bindParam(statement, index, ((Long) value).intValue());
         return (1);
      }
      else if(value instanceof String)
      {
         bindParam(statement, index, (String) value);
         return (1);
      }
      else if(value instanceof Boolean)
      {
         bindParam(statement, index, (Boolean) value);
         return (1);
      }
      else if(value instanceof Timestamp)
      {
         bindParam(statement, index, (Timestamp) value);
         return (1);
      }
      else if(value instanceof Date)
      {
         bindParam(statement, index, (Date) value);
         return (1);
      }
      else if(value instanceof Calendar)
      {
         bindParam(statement, index, (Calendar) value);
         return (1);
      }
      else if(value instanceof BigDecimal)
      {
         bindParam(statement, index, (BigDecimal) value);
         return (1);
      }
      else if(value == null)
      {
         statement.setNull(index, Types.CHAR);
         return (1);
      }
      else if(value instanceof Collection)
      {
         Collection<?> collection  = (Collection<?>) value;
         int           paramsBound = 0;
         for(Object o : collection)
         {
            paramsBound += bindParamObject(statement, (index + paramsBound), o);
         }
         return (paramsBound);
      }
      else if(value instanceof byte[])
      {
         statement.setBytes(index, (byte[]) value);
         return (1);
      }
      else if(value instanceof Instant)
      {
         Timestamp timestamp = new Timestamp(((Instant) value).toEpochMilli());
         statement.setTimestamp(index, timestamp);
         return (1);
      }
      else if(value instanceof LocalDate)
      {
         Timestamp timestamp = new Timestamp(((LocalDate) value).atTime(0, 0).toEpochSecond(ZoneOffset.UTC) * MS_PER_SEC);
         statement.setTimestamp(index, timestamp);
         return (1);
      }
      else if(value instanceof OffsetDateTime)
      {
         Timestamp timestamp = new Timestamp(((OffsetDateTime) value).toEpochSecond() * MS_PER_SEC);
         statement.setTimestamp(index, timestamp);
         return (1);
      }
      else if(value instanceof LocalDateTime)
      {
         Timestamp timestamp = new Timestamp(((LocalDateTime) value).toEpochSecond(ZoneOffset.UTC) * MS_PER_SEC);
         statement.setTimestamp(index, timestamp);
         return (1);
      }
      else
      {
         throw (new SQLException("Unexpected value type [" + value.getClass().getSimpleName() + "] in bindParamObject."));
      }
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
   public static <T> TypeValuePair<T> param(Class<T> c, T v)
   {
      return (new TypeValuePair<>(c, v));
   }
   */

   /*******************************************************************************
    **
    *******************************************************************************/
   /*
   private static void bindParamTypeValuePair(PreparedStatement statement, int index, TypeValuePair<Object> value) throws SQLException
   {
      Object        v = value.getValue();
      Class<Object> t = value.getType();

      if(t.equals(Integer.class))
      {
         bindParam(statement, index, (Integer) v);
      }
      else if(t.equals(String.class))
      {
         bindParam(statement, index, (String) v);
      }
      else if(t.equals(Boolean.class))
      {
         bindParam(statement, index, (Boolean) v);
      }
      else if(t.equals(Timestamp.class))
      {
         bindParam(statement, index, (Timestamp) v);
      }
      else if(t.equals(Date.class))
      {
         bindParam(statement, index, (Date) v);
      }
      else if(t.equals(Calendar.class))
      {
         bindParam(statement, index, (Calendar) v);
      }
      else if(t.equals(LocalDate.class))
      {
         bindParam(statement, index, (LocalDate) v);
      }
      else if(t.equals(LocalDateTime.class))
      {
         bindParam(statement, index, (LocalDateTime) v);
      }
      else if(t.equals(BigDecimal.class))
      {
         bindParam(statement, index, (BigDecimal) v);
      }
      else
      {
         throw (new SQLException("Unexpected value type [" + t.getSimpleName() + "] in bindParamTypeValuePair."));
      }
   }
   */



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, Integer value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.INTEGER);
      }
      else
      {
         statement.setInt(index, value);
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, String value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.CHAR);
      }
      else
      {
         statement.setString(index, value);
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, Boolean value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.BOOLEAN);
      }
      else
      {
         statement.setBoolean(index, value);
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, Date value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.DATE);
      }
      else
      {
         statement.setDate(index, new java.sql.Date(value.getTime()));
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, Timestamp value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.TIMESTAMP);
      }
      else
      {
         statement.setTimestamp(index, value);
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, Calendar value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.DATE);
      }
      else
      {
         statement.setTimestamp(index, new Timestamp(value.getTimeInMillis()));
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, LocalDate value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.DATE);
      }
      else
      {
         LocalDateTime localDateTime = value.atTime(0, 0);
         Timestamp     timestamp     = new Timestamp(localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * MS_PER_SEC); // TimeStamp expects millis, not seconds, after epoch
         statement.setTimestamp(index, timestamp);
      }
   }



   /*******************************************************************************
    *
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, LocalDateTime value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.TIMESTAMP);
      }
      else
      {
         Timestamp timestamp = new Timestamp(value.atZone(ZoneId.systemDefault()).toEpochSecond() * MS_PER_SEC); // TimeStamp expects millis, not seconds, after epoch
         statement.setTimestamp(index, timestamp);
      }
   }



   /*******************************************************************************
    **
    **
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, BigDecimal value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.DECIMAL);
      }
      else
      {
         statement.setBigDecimal(index, value);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void bindParam(PreparedStatement statement, int index, byte[] value) throws SQLException
   {
      if(value == null)
      {
         statement.setNull(index, Types.ARRAY);
      }
      else
      {
         statement.setBytes(index, value);
      }
   }



   /*******************************************************************************
    **
    **
    *******************************************************************************/
   public static void bindParamNull(PreparedStatement statement, int index) throws SQLException
   {
      statement.setNull(index, Types.NULL);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer getInteger(ResultSet resultSet, String column) throws SQLException
   {
      int value = resultSet.getInt(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer getInteger(ResultSet resultSet, int column) throws SQLException
   {
      int value = resultSet.getInt(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static byte[] getByteArray(ResultSet resultSet, String column) throws SQLException
   {
      byte[] value = resultSet.getBytes(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static byte[] getByteArray(ResultSet resultSet, int column) throws SQLException
   {
      byte[] value = resultSet.getBytes(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Object getObject(ResultSet resultSet, String column) throws SQLException
   {
      Object value = resultSet.getObject(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Object getObject(ResultSet resultSet, int column) throws SQLException
   {
      Object value = resultSet.getObject(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getString(ResultSet resultSet, String column) throws SQLException
   {
      String value = resultSet.getString(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getString(ResultSet resultSet, int column) throws SQLException
   {
      String value = resultSet.getString(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static BigDecimal getBigDecimal(ResultSet resultSet, String column) throws SQLException
   {
      BigDecimal value = resultSet.getBigDecimal(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static BigDecimal getBigDecimal(ResultSet resultSet, int column) throws SQLException
   {
      BigDecimal value = resultSet.getBigDecimal(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Date getDate(ResultSet resultSet, String column) throws SQLException
   {
      Date value = resultSet.getDate(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Date getDate(ResultSet resultSet, int column) throws SQLException
   {
      Date value = resultSet.getDate(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Calendar getCalendar(ResultSet resultSet, String column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      Calendar rs = Calendar.getInstance();
      rs.setTimeInMillis(value.getTime());
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Calendar getCalendar(ResultSet resultSet, int column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      Calendar rs = Calendar.getInstance();
      rs.setTimeInMillis(value.getTime());
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static LocalDate getLocalDate(ResultSet resultSet, String column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      LocalDate date = LocalDate.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate());
      return (date);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static LocalDate getLocalDate(ResultSet resultSet, int column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      LocalDate date = LocalDate.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate());
      return (date);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static LocalDateTime getLocalDateTime(ResultSet resultSet, String column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      LocalDateTime dateTime = LocalDateTime.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate(), value.getHours(), value.getMinutes(), value.getSeconds(), 0);
      return (dateTime);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static LocalDateTime getLocalDateTime(ResultSet resultSet, int column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      LocalDateTime dateTime = LocalDateTime.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate(), value.getHours(), value.getMinutes(), value.getSeconds(), 0);
      return (dateTime);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static OffsetDateTime getOffsetDateTime(ResultSet resultSet, String column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      OffsetDateTime dateTime = OffsetDateTime.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate(), value.getHours(), value.getMinutes(), value.getSeconds(), 0, OffsetDateTime.now().getOffset());
      return (dateTime);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("deprecation")
   public static OffsetDateTime getOffsetDateTime(ResultSet resultSet, int column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }

      OffsetDateTime dateTime = OffsetDateTime.of(value.getYear() + NINETEEN_HUNDRED, value.getMonth() + 1, value.getDate(), value.getHours(), value.getMinutes(), value.getSeconds(), 0, OffsetDateTime.now().getOffset());
      return (dateTime);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Boolean getBoolean(ResultSet resultSet, String column) throws SQLException
   {
      Boolean value = resultSet.getBoolean(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Boolean getBoolean(ResultSet resultSet, int column) throws SQLException
   {
      Boolean value = resultSet.getBoolean(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Long getLong(ResultSet resultSet, int column) throws SQLException
   {
      long value = resultSet.getLong(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Long getLong(ResultSet resultSet, String column) throws SQLException
   {
      long value = resultSet.getLong(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Timestamp getTimestamp(ResultSet resultSet, int column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Timestamp getTimestamp(ResultSet resultSet, String column) throws SQLException
   {
      Timestamp value = resultSet.getTimestamp(column);
      if(resultSet.wasNull())
      {
         return (null);
      }
      return (value);
   }



   /*******************************************************************************
    ** Find an id from a "large" table that was created X days ago (assumes the date
    ** field in the table isn't indexed, but id is - so do a binary search on id,
    ** selecting the date of the min & max & mid id, then sub-dividing until the goal
    ** days-ago is found).
    **
    *******************************************************************************/
   public static Integer findIdForDaysAgo(Connection connection, String tableName, String dateFieldName, int goalDaysAgo) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      return (findIdForTimeUnitAgo(connection, tableName, dateFieldName, goalDaysAgo, ChronoUnit.DAYS));
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer findIdForTimestamp(Connection connection, String tableName, String dateFieldName, LocalDateTime timestamp) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      long between = ChronoUnit.SECONDS.between(timestamp, LocalDateTime.now());
      return (findIdForTimeUnitAgo(connection, tableName, dateFieldName, (int) between, ChronoUnit.SECONDS));
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Integer findIdForTimeUnitAgo(Connection connection, String tableName, String dateFieldName, int goalUnitsAgo, ChronoUnit unit) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      Integer maxId = executeStatementForSingleValue(connection, Integer.class, "SELECT MAX(id) FROM " + tableName);
      Integer minId = executeStatementForSingleValue(connection, Integer.class, "SELECT MIN(id) FROM " + tableName);

      if(maxId == null || minId == null)
      {
         // Logger.logDebug("For [" + tableName + "], returning null id for X time-units ago, because either a min or max wasn't found.");
         return (null);
      }

      Integer idForGoal = findIdForTimeUnitAgo(connection, tableName, dateFieldName, goalUnitsAgo, minId, maxId, unit);
      long foundUnitsAgo = getTimeUnitAgo(connection, tableName, dateFieldName, idForGoal, unit);

      // Logger.logDebug("For [" + tableName + "], using min id [" + idForGoal + "], which is from [" + foundUnitsAgo + "] Units[" + unit + "] ago.");

      return (idForGoal);
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer findIdForTimeUnitAgo(Connection connection, String tableName, String dateFieldName, int goalUnitsAgo, Integer minId, Integer maxId, ChronoUnit unit) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      Integer midId = minId + ((maxId - minId) / 2);
      if(midId.equals(minId) || midId.equals(maxId))
      {
         return (midId);
      }

      long foundUnitsAgo = getTimeUnitAgo(connection, tableName, dateFieldName, midId, unit);
      if(foundUnitsAgo == goalUnitsAgo)
      {
         return (midId);
      }
      else if(foundUnitsAgo > goalUnitsAgo)
      {
         return (findIdForTimeUnitAgo(connection, tableName, dateFieldName, goalUnitsAgo, midId, maxId, unit));
      }
      else
      {
         return (findIdForTimeUnitAgo(connection, tableName, dateFieldName, goalUnitsAgo, minId, midId, unit));
      }
      */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static long getTimeUnitAgo(Connection connection, String tableName, String dateFieldName, Integer id, ChronoUnit unit) throws SQLException
   {
      throw (new NotImplementedException());
      /*
      LocalDateTime now = LocalDateTime.now();

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // note, we used to just do where id=? here - but if that row is ever missing, we have a bad time - so - do id >= ? order by id, and just the first row. //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      LocalDateTime date = executeStatementForSingleValue(connection, LocalDateTime.class, "SELECT " + dateFieldName + " FROM " + tableName + " WHERE id >= ? ORDER BY id LIMIT 1", id);
      // System.out.println(date);

      // if(date == null)
      {
         // return now.
      }
      // else
      {
         long diff = unit.between(date, now);
         // System.out.println("Unit[" + unit + "]'s ago:  " + diff);
         return diff;
      }
      */
   }

   /*******************************************************************************
    **
    *******************************************************************************/
   // public static class TypeValuePair<T>
   // {
   //    private Class<T> type;
   //    private T value;

   //    /*******************************************************************************
   //     **
   //     *******************************************************************************/
   //    @SuppressWarnings("unchecked")
   //    public TypeValuePair(T value)
   //    {
   //       this.value = value;
   //       this.type = (Class<T>) value.getClass();
   //    }

   //    /*******************************************************************************
   //     **
   //     *******************************************************************************/
   //    public TypeValuePair(Class<T> type, T value)
   //    {
   //       this.type = type;
   //       this.value = value;
   //    }

   //    /*******************************************************************************
   //     **
   //     *******************************************************************************/
   //    public T getValue()
   //    {
   //       return (value);
   //    }

   //    /*******************************************************************************
   //     **
   //     *******************************************************************************/
   //    public Class<T> getType()
   //    {
   //       return (type);
   //    }

   // }



   /*******************************************************************************
    ** Setter for collectStatistics
    **
    *******************************************************************************/
   public static void setCollectStatistics(boolean collectStatistics)
   {
      QueryManager.collectStatistics = collectStatistics;
   }



   /*******************************************************************************
    ** Increment a statistic
    **
    *******************************************************************************/
   public static void incrementStatistic(String statName)
   {
      if(collectStatistics)
      {
         statistics.putIfAbsent(statName, 0);
         statistics.put(statName, statistics.get(statName) + 1);
      }
   }



   /*******************************************************************************
    ** clear the map of statistics
    **
    *******************************************************************************/
   public static void resetStatistics()
   {
      statistics.clear();
   }



   /*******************************************************************************
    ** Getter for statistics
    **
    *******************************************************************************/
   public static Map<String, Integer> getStatistics()
   {
      return statistics;
   }



   /*******************************************************************************
    ** Note - this changes a static field that impacts all usages.  Really, it's meant
    ** to only be called in unit tests (at least as of the time of this writing).
    *******************************************************************************/
   public static void setPageSize(int pageSize)
   {
      PAGE_SIZE = pageSize;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void resetPageSize()
   {
      PAGE_SIZE = DEFAULT_PAGE_SIZE;
   }

}
