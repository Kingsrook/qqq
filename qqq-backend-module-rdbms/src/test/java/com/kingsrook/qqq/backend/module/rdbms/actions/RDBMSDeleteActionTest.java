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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSDeleteActionTest extends RDBMSActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach() throws Exception
   {
      super.primeTestDatabase();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteAll() throws Exception
   {
      DeleteInput deleteInput = initStandardPersonDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1, 2, 3, 4, 5));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(5, deleteResult.getDeletedRecordCount(), "Unfiltered delete should return all rows");
      assertEquals(0, deleteResult.getRecordsWithErrors().size(), "should have no errors");
      runTestSql("SELECT id FROM person", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteOne() throws Exception
   {
      DeleteInput deleteInput = initStandardPersonDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(1, deleteResult.getDeletedRecordCount(), "Should delete one row");
      assertEquals(0, deleteResult.getRecordsWithErrors().size(), "should have no errors");
      runTestSql("SELECT id FROM person WHERE id = 1", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteSome() throws Exception
   {
      DeleteInput deleteInput = initStandardPersonDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1, 3, 5));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(3, deleteResult.getDeletedRecordCount(), "Should delete one row");
      assertEquals(0, deleteResult.getRecordsWithErrors().size(), "should have no errors");
      runTestSql("SELECT id FROM person", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertTrue(rs.getInt(1) == 2 || rs.getInt(1) == 4);
         }
         assertEquals(2, rowsFound);
      }));

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDeleteSomeIdsThatExistAndSomeThatDoNot() throws Exception
   {
      DeleteInput deleteInput = initStandardPersonDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1, -1));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(1, deleteResult.getDeletedRecordCount(), "Should delete one row");
      assertEquals(0, deleteResult.getRecordsWithErrors().size(), "should have no errors (the one not found is just noop)");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private DeleteInput initStandardPersonDeleteRequest()
   {
      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setInstance(TestUtils.defineInstance());
      deleteInput.setTableName(TestUtils.defineTablePerson().getName());
      return deleteInput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteWhereForeignKeyBlocksSome() throws Exception
   {
      //////////////////////////////////////////////////////////////////
      // load the parent-child tables, with foreign keys and instance //
      //////////////////////////////////////////////////////////////////
      super.primeTestDatabase("prime-test-database-parent-child-tables.sql");
      DeleteInput deleteInput = initChildTableInstanceAndDeleteRequest();

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // try to delete all of the child records - 2 should fail, because they are referenced by parent_table.child_id //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      deleteInput.setPrimaryKeys(List.of(1, 2, 3, 4, 5));

      QueryManager.setCollectStatistics(true);
      QueryManager.resetStatistics();

      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);

      ////////////////////////////////////////////////////////////////////////////////
      // assert that 6 queries ran - the initial delete (which failed), then 6 more //
      ////////////////////////////////////////////////////////////////////////////////
      QueryManager.setCollectStatistics(false);
      Map<String, Integer> queryStats = QueryManager.getStatistics();
      assertEquals(6, queryStats.get(QueryManager.STAT_QUERIES_RAN), "Number of queries ran");

      assertEquals(2, deleteResult.getRecordsWithErrors().size(), "Should get back the 2 records with errors");
      assertTrue(deleteResult.getRecordsWithErrors().stream().noneMatch(r -> r.getErrors().isEmpty()), "All we got back should have errors");
      assertEquals(3, deleteResult.getDeletedRecordCount(), "Should get back that 3 were deleted");

      runTestSql("SELECT id FROM child_table", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            ///////////////////////////////////////////
            // child_table rows 1 & 3 should survive //
            ///////////////////////////////////////////
            assertTrue(rs.getInt(1) == 1 || rs.getInt(1) == 3);
         }
         assertEquals(2, rowsFound);
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private DeleteInput initChildTableInstanceAndDeleteRequest()
   {
      QInstance qInstance = TestUtils.defineInstance();

      String childTableName = "childTable";
      qInstance.addTable(new QTableMetaData()
         .withName(childTableName)
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("child_table")));

      qInstance.addTable(new QTableMetaData()
         .withName("parentTable")
         .withBackendName(TestUtils.DEFAULT_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("childId", QFieldType.INTEGER).withBackendName("child_id"))
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("parent_table")));

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setInstance(qInstance);
      deleteInput.setTableName(childTableName);
      return deleteInput;
   }
}