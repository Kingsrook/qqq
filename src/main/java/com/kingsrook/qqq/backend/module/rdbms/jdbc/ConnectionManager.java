/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.module.rdbms.jdbc;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ConnectionManager
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public Connection getConnection(RDBMSBackendMetaData backend) throws SQLException
   {
      String jdbcURL;

      switch (backend.getVendor())
      {
         case "aurora":
         {
            //TODO AWS version not working and why ssl=false required?
            jdbcURL = "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&useSSL=false";
            //jdbcURL = "jdbc:mysql:aws://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull";
            break;
         }
         case "mysql":
         {
            jdbcURL = "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull";
            break;
         }
         case "h2":
         {
            jdbcURL = "jdbc:h2:" + backend.getHostName() + ":" + backend.getDatabaseName() + ";MODE=MySQL";
            break;
         }
         default:
         {
            throw new IllegalArgumentException("Unsupported rdbms backend vendor: " + backend.getVendor());
         }
      }

      return DriverManager.getConnection(jdbcURL, backend.getUsername(), backend.getPassword());
   }

}
