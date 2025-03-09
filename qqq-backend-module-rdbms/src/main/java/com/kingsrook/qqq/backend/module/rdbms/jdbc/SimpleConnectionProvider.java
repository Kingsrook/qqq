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


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import static com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager.getJdbcUrl;


/*******************************************************************************
 ** Simple connection provider - no pooling, just opens a new connection for
 ** every request.
 *******************************************************************************/
public class SimpleConnectionProvider implements ConnectionProviderInterface
{
   private RDBMSBackendMetaData backend;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void init(RDBMSBackendMetaData backend)
   {
      this.backend = backend;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Connection getConnection() throws SQLException
   {
      String jdbcURL = getJdbcUrl(backend);
      Connection connection = DriverManager.getConnection(jdbcURL, backend.getUsername(), backend.getPassword());

      if(CollectionUtils.nullSafeHasContents(backend.getQueriesForNewConnections()))
      {
         runQueriesForNewConnections(connection);
      }

      return (connection);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void runQueriesForNewConnections(Connection connection) throws SQLException
   {
      for(String sql : backend.getQueriesForNewConnections())
      {
         Statement statement = connection.createStatement();
         statement.execute(sql);
      }
   }

}
