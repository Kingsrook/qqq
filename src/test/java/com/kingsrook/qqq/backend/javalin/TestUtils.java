package com.kingsrook.qqq.backend.javalin;


import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.RDBSMBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.apache.commons.io.IOUtils;
import static junit.framework.Assert.assertNotNull;


/*******************************************************************************
 ** Utility methods for unit tests.
 **
 *******************************************************************************/
public class TestUtils
{

   /*******************************************************************************
    ** Prime a test database (e.g., h2, in-memory)
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static void primeTestDatabase() throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection connection = connectionManager.getConnection(new RDBSMBackendMetaData(TestUtils.defineBackend()));
      InputStream primeTestDatabaseSqlStream = TestUtils.class.getResourceAsStream("/prime-test-database.sql");
      assertNotNull(primeTestDatabaseSqlStream);
      List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
      String joinedSQL = String.join("\n", lines);
      for(String sql : joinedSQL.split(";"))
      {
         QueryManager.executeUpdate(connection, sql);
      }
   }



   /*******************************************************************************
    ** Run an SQL Query in the test database
    **
    *******************************************************************************/
   public static void runTestSql(String sql, QueryManager.ResultSetProcessor resultSetProcessor) throws Exception
   {
      ConnectionManager connectionManager = new ConnectionManager();
      Connection connection = connectionManager.getConnection(new RDBSMBackendMetaData(defineBackend()));
      QueryManager.executeStatement(connection, sql, resultSetProcessor);
   }



   /*******************************************************************************
    ** Define the q-instance for testing (h2 rdbms and 'person' table)
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      return (qInstance);
   }



   /*******************************************************************************
    ** Define the h2 rdbms backend
    **
    *******************************************************************************/
   public static QBackendMetaData defineBackend()
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
    ** Define the person table
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
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

}
