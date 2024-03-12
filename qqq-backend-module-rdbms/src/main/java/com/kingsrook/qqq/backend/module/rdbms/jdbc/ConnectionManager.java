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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.mchange.v2.c3p0.ComboPooledDataSource;


/*******************************************************************************
 **
 *******************************************************************************/
public class ConnectionManager
{
   private boolean mayUseConnectionPool = true;

   private static Map<String, Boolean>               initedConnectionPool = new HashMap<>();
   private static Map<String, ComboPooledDataSource> connectionPoolMap    = new HashMap<>();

   private static int usageCounter = 0;


   /*******************************************************************************
    **
    *******************************************************************************/
   public Connection getConnection(RDBMSBackendMetaData backend) throws SQLException
   {
      usageCounter++;

      if(mayUseConnectionPool)
      {
         return (getConnectionFromPool(backend));
      }

      String jdbcURL = getJdbcUrl(backend);
      return DriverManager.getConnection(jdbcURL, backend.getUsername(), backend.getPassword());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static void checkPools()
   {
      try
      {
         System.out.println("Usages: " + usageCounter);

         for(Map.Entry<String, ComboPooledDataSource> entry : CollectionUtils.nonNullMap(connectionPoolMap).entrySet())
         {
            System.out.println("POOL USAGE: " + entry.getKey() + ": " + entry.getValue().getNumBusyConnections());
            if(entry.getValue().getNumBusyConnections() > 2)
            {
               System.out.println("break!");
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Connection getConnectionFromPool(RDBMSBackendMetaData backend) throws SQLException
   {
      try
      {
         if(!initedConnectionPool.getOrDefault(backend.getName(), false))
         {
            // todo - some syncrhonized
            ComboPooledDataSource connectionPool = new ComboPooledDataSource();
            connectionPool.setDriverClass(getJdbcDriverClassName(backend));
            connectionPool.setJdbcUrl(getJdbcUrl(backend));
            connectionPool.setUser(backend.getUsername());
            connectionPool.setPassword(backend.getPassword());

            connectionPool.setTestConnectionOnCheckout(true);

            //////////////////////////////////////////////////////////////////////////
            // useful to debug leaking connections - meant for tests only though... //
            //////////////////////////////////////////////////////////////////////////
            // connectionPool.setDebugUnreturnedConnectionStackTraces(true);
            // connectionPool.setUnreturnedConnectionTimeout(10);

            connectionPoolMap.put(backend.getName(), connectionPool);
            initedConnectionPool.put(backend.getName(), true);
         }

         return (connectionPoolMap.get(backend.getName()).getConnection());
      }
      catch(Exception e)
      {
         throw (new SQLException("Error getting connection from pool", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getJdbcDriverClassName(RDBMSBackendMetaData backend)
   {
      if(StringUtils.hasContent(backend.getJdbcDriverClassName()))
      {
         return backend.getJdbcDriverClassName();
      }

      return switch(backend.getVendor())
      {
         case "mysql", "aurora" -> "com.mysql.cj.jdbc.Driver";
         case "h2" -> "org.h2.Driver";
         default -> throw (new IllegalStateException("We do not know what jdbc driver to use for vendor name [" + backend.getVendor() + "].  Try setting jdbcDriverClassName in your backend meta data."));
      };
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getJdbcUrl(RDBMSBackendMetaData backend)
   {
      if(StringUtils.hasContent(backend.getJdbcUrl()))
      {
         return backend.getJdbcUrl();
      }

      return switch(backend.getVendor())
      {
         // TODO aws-mysql-jdbc driver not working when running on AWS
         // jdbcURL = "jdbc:mysql:aws://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=CONVERT_TO_NULL";
         case "aurora" -> "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&useSSL=false";
         case "mysql" -> "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull";
         case "h2" -> "jdbc:h2:" + backend.getHostName() + ":" + backend.getDatabaseName() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
         default -> throw new IllegalArgumentException("Unsupported rdbms backend vendor: " + backend.getVendor());
      };
   }

}
