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
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


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
            // TODO aws-mysql-jdbc driver not working when running on AWS
            // jdbcURL = "jdbc:mysql:aws://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=CONVERT_TO_NULL";
            jdbcURL = "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&useSSL=false";
            break;
         }
         case "mysql":
         {
            jdbcURL = "jdbc:mysql://" + backend.getHostName() + ":" + backend.getPort() + "/" + backend.getDatabaseName() + "?rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull";
            break;
         }
         case "h2":
         {
            jdbcURL = "jdbc:h2:" + backend.getHostName() + ":" + backend.getDatabaseName() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
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
