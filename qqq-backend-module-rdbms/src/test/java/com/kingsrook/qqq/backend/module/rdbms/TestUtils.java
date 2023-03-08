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

package com.kingsrook.qqq.backend.module.rdbms;


import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.actions.RDBMSActionTest;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.ConnectionManager;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import org.apache.commons.io.IOUtils;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String DEFAULT_BACKEND_NAME = "default";

   public static final String TABLE_NAME_PERSON              = "personTable";
   public static final String TABLE_NAME_PERSONAL_ID_CARD    = "personalIdCard";
   public static final String TABLE_NAME_STORE               = "store";
   public static final String TABLE_NAME_ORDER               = "order";
   public static final String TABLE_NAME_ITEM                = "item";
   public static final String TABLE_NAME_ORDER_LINE          = "orderLine";
   public static final String TABLE_NAME_WAREHOUSE           = "warehouse";
   public static final String TABLE_NAME_WAREHOUSE_STORE_INT = "warehouseStoreInt";

   public static final String SECURITY_KEY_STORE_ALL_ACCESS = "storeAllAccess";



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("unchecked")
   public static void primeTestDatabase(String sqlFileName) throws Exception
   {
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
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();
      qInstance.addBackend(defineBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.addPossibleValueSource(definePvsPerson());
      qInstance.addTable(defineTablePersonalIdCard());
      qInstance.addJoin(defineJoinPersonAndPersonalIdCard());
      addOmsTablesAndJoins(qInstance);
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
   public static RDBMSBackendMetaData defineBackend()
   {
      return (new RDBMSBackendMetaData()
         .withName(DEFAULT_BACKEND_NAME)
         .withVendor("h2")
         .withHostName("mem")
         .withDatabaseName("test_database")
         .withUsername("sa"));
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
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING).withBackendName("email"))
         .withField(new QFieldMetaData("isEmployed", QFieldType.BOOLEAN).withBackendName("is_employed"))
         .withField(new QFieldMetaData("annualSalary", QFieldType.DECIMAL).withBackendName("annual_salary"))
         .withField(new QFieldMetaData("daysWorked", QFieldType.INTEGER).withBackendName("days_worked"))
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("person"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QPossibleValueSource definePvsPerson()
   {
      return (new QPossibleValueSource()
         .withName(TABLE_NAME_PERSON)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(TABLE_NAME_PERSON)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY)
      );
   }



   /*******************************************************************************
    ** Define a 1:1 table with Person.
    **
    *******************************************************************************/
   private static QTableMetaData defineTablePersonalIdCard()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSONAL_ID_CARD)
         .withLabel("Personal Id Card")
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("personal_id_card"))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("personId", QFieldType.INTEGER).withBackendName("person_id"))
         .withField(new QFieldMetaData("idNumber", QFieldType.STRING).withBackendName("id_number"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QJoinMetaData defineJoinPersonAndPersonalIdCard()
   {
      return new QJoinMetaData()
         .withLeftTable(TABLE_NAME_PERSON)
         .withRightTable(TABLE_NAME_PERSONAL_ID_CARD)
         .withInferredName()
         .withType(JoinType.ONE_TO_ONE)
         .withJoinOn(new JoinOn("id", "personId"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void addOmsTablesAndJoins(QInstance qInstance)
   {
      qInstance.addTable(defineBaseTable(TABLE_NAME_STORE, "store")
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("name")
         .withRecordSecurityLock(new RecordSecurityLock().withSecurityKeyType(TABLE_NAME_STORE).withFieldName("id"))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
      );

      qInstance.addTable(defineBaseTable(TABLE_NAME_ORDER, "order")
         .withRecordSecurityLock(new RecordSecurityLock().withSecurityKeyType(TABLE_NAME_STORE).withFieldName("storeId"))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER).withBackendName("store_id").withPossibleValueSourceName(TABLE_NAME_STORE))
         .withField(new QFieldMetaData("billToPersonId", QFieldType.INTEGER).withBackendName("bill_to_person_id").withPossibleValueSourceName(TABLE_NAME_PERSON))
         .withField(new QFieldMetaData("shipToPersonId", QFieldType.INTEGER).withBackendName("ship_to_person_id").withPossibleValueSourceName(TABLE_NAME_PERSON))
      );

      qInstance.addTable(defineBaseTable(TABLE_NAME_ITEM, "item")
         .withRecordSecurityLock(new RecordSecurityLock().withSecurityKeyType(TABLE_NAME_STORE).withFieldName("storeId"))
         .withField(new QFieldMetaData("sku", QFieldType.STRING))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER).withBackendName("store_id").withPossibleValueSourceName(TABLE_NAME_STORE))
      );

      qInstance.addTable(defineBaseTable(TABLE_NAME_ORDER_LINE, "order_line")
         .withRecordSecurityLock(new RecordSecurityLock().withSecurityKeyType(TABLE_NAME_STORE).withFieldName("storeId"))
         .withField(new QFieldMetaData("orderId", QFieldType.INTEGER).withBackendName("order_id"))
         .withField(new QFieldMetaData("sku", QFieldType.STRING))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER).withBackendName("store_id").withPossibleValueSourceName(TABLE_NAME_STORE))
         .withField(new QFieldMetaData("quantity", QFieldType.INTEGER))
      );

      qInstance.addTable(defineBaseTable(TABLE_NAME_WAREHOUSE_STORE_INT, "warehouse_store_int")
         .withField(new QFieldMetaData("warehouseId", QFieldType.INTEGER).withBackendName("warehouse_id"))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER).withBackendName("store_id"))
      );

      qInstance.addTable(defineBaseTable(TABLE_NAME_WAREHOUSE, "warehouse")
         .withRecordSecurityLock(new RecordSecurityLock()
            .withSecurityKeyType(TABLE_NAME_STORE)
            .withFieldName(TABLE_NAME_WAREHOUSE_STORE_INT + ".storeId")
            .withJoinNameChain(List.of(QJoinMetaData.makeInferredJoinName(TestUtils.TABLE_NAME_WAREHOUSE, TestUtils.TABLE_NAME_WAREHOUSE_STORE_INT)))
         )
         .withField(new QFieldMetaData("name", QFieldType.STRING).withBackendName("name"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TestUtils.TABLE_NAME_WAREHOUSE)
         .withRightTable(TestUtils.TABLE_NAME_WAREHOUSE_STORE_INT)
         .withInferredName()
         .withJoinOn(new JoinOn("id", "warehouseId"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("orderJoinStore")
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_STORE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("storeId", "id"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("orderJoinBillToPerson")
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_PERSON)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("billToPersonId", "id"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("orderJoinShipToPerson")
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_PERSON)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("shipToPersonId", "id"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("itemJoinStore")
         .withLeftTable(TABLE_NAME_ITEM)
         .withRightTable(TABLE_NAME_STORE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("storeId", "id"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("orderJoinOrderLine")
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_ORDER_LINE)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "orderId"))
      );

      qInstance.addJoin(new QJoinMetaData()
         .withName("orderLineJoinItem")
         .withLeftTable(TABLE_NAME_ORDER_LINE)
         .withRightTable(TABLE_NAME_ITEM)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("sku", "sku"))
         .withJoinOn(new JoinOn("storeId", "storeId"))
      );

      qInstance.addPossibleValueSource(new QPossibleValueSource()
         .withName("store")
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(TABLE_NAME_STORE)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY)
      );

      qInstance.addSecurityKeyType(new QSecurityKeyType()
         .withName(TABLE_NAME_STORE)
         .withAllAccessKeyName(SECURITY_KEY_STORE_ALL_ACCESS)
         .withPossibleValueSourceName(TABLE_NAME_STORE));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData defineBaseTable(String tableName, String backendTableName)
   {
      return new QTableMetaData()
         .withName(tableName)
         .withBackendName(DEFAULT_BACKEND_NAME)
         .withBackendDetails(new RDBMSTableBackendDetails().withTableName(backendTableName))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER));
   }

}
