/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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


import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.apache.commons.io.IOUtils;
import static junit.framework.Assert.assertNotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QBackendMetaData defineBackend()
   {
      return new QBackendMetaData()
         .withName("default")
         .withType("rdbms")
         .withValue("vendor", "h2")
         .withValue("hostName", "mem")
         .withValue("databaseName", "test_database")
         .withValue("username", "sa")
         .withValue("password", "");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName("person")
         .withLabel("Person")
         .withBackendName(defineBackend().getName())
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   protected void primeTestDatabase() throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection connection = connectionManager.getConnection(new RDBMSBackendMetaData(defineBackend()));
      InputStream primeTestDatabaseSqlStream = RDBMSActionTest.class.getResourceAsStream("/prime-test-database.sql");
      assertNotNull(primeTestDatabaseSqlStream);
      List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
      String joinedSQL = String.join("\n", lines);
      for(String sql : joinedSQL.split(";"))
      {
         QueryManager.executeUpdate(connection, sql);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void runTestSql(String sql, QueryManager.ResultSetProcessor resultSetProcessor) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection connection = connectionManager.getConnection(new RDBMSBackendMetaData(defineBackend()));
      QueryManager.executeStatement(connection, sql, resultSetProcessor);
   }
}
