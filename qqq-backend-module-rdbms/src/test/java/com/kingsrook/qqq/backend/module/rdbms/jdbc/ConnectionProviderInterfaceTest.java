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

package com.kingsrook.qqq.backend.module.rdbms.jdbc;


import java.sql.Connection;
import java.sql.SQLException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.module.rdbms.BaseTest;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for ConnectionProviderInterface 
 *******************************************************************************/
class ConnectionProviderInterfaceTest extends BaseTest
{


   /***************************************************************************
    * jacoco doesn't like that our interface isn't covered, so... cover it
    ***************************************************************************/
   @Test
   void dumpDebug() throws SQLException
   {
      new ConnectionProviderInterface()
      {
         @Override
         public void init(RDBMSBackendMetaData backend) throws QException
         {

         }



         @Override
         public Connection getConnection() throws SQLException
         {
            return null;
         }
      }.dumpDebug();
   }
}