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

package com.kingsrook.qqq.backend.module.mongodb;


import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBBackendMetaData;
import com.kingsrook.qqq.backend.module.mongodb.model.metadata.MongoDBTableBackendDetails;


/*******************************************************************************
 ** Test Utils class for this module
 *******************************************************************************/
public class TestUtils
{
   public static final String DEFAULT_BACKEND_NAME = "default";

   public static final String TABLE_NAME_PERSON = "personTable";

   public static final String SECURITY_KEY_STORE_ALL_ACCESS = "storeAllAccess";

   public static final String  MONGO_USERNAME = "mongoUser";
   public static final String  MONGO_PASSWORD = "password";
   public static final Integer MONGO_PORT     = 27017;
   public static final String  MONGO_DATABASE = "testDatabase";

   public static final String TEST_COLLECTION = "testTable";



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static void primeTestDatabase(String sqlFileName) throws Exception
   {
      /*
      ConnectionManager connectionManager = new ConnectionManager();
      try(Connection connection = connectionManager.getConnection(TestUtils.defineBackend()))
      {
         InputStream primeTestDatabaseSqlStream = RDBMSActionTest.class.getResourceAsStream("/" + sqlFileName);
         assertNotNull(primeTestDatabaseSqlStream);
         List<String> lines = (List<String>) IOUtils.readLines(primeTestDatabaseSqlStream);
         lines = lines.stream().filter(line -> !line.startsWith("-- ")).toList();
         String joinedSQL = String.join("\n", lines);
         for(String sql : joinedSQL.split(";"))
         {
            QueryManager.executeUpdate(connection, sql);
         }
      }
       */
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.setAuthentication(defineAuthentication());
      return (qInstance);
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   public static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType(QAuthenticationType.MOCK);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static MongoDBBackendMetaData defineBackend()
   {
      return (new MongoDBBackendMetaData()
         .withName(DEFAULT_BACKEND_NAME)
         .withHost("localhost")
         .withPort(TestUtils.MONGO_PORT)
         .withUsername(TestUtils.MONGO_USERNAME)
         .withPassword(TestUtils.MONGO_PASSWORD)
         .withAuthSourceDatabase("admin")
         .withDatabaseName(TestUtils.MONGO_DATABASE));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withRecordLabelFormat("%s %s")
         .withRecordLabelFields("firstName", "lastName")
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.STRING).withBackendName("_id"))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("metaData.createDate"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("metaData.modifyDate"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("isEmployed", QFieldType.BOOLEAN))
         .withField(new QFieldMetaData("annualSalary", QFieldType.DECIMAL))
         .withField(new QFieldMetaData("daysWorked", QFieldType.INTEGER))
         .withField(new QFieldMetaData("homeTown", QFieldType.STRING))
         .withBackendDetails(new MongoDBTableBackendDetails()
            .withTableName(TEST_COLLECTION));
   }

}
