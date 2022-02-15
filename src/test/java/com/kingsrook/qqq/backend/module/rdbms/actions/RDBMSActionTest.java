/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
