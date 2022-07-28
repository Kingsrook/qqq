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


import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
import com.kingsrook.qqq.backend.module.rdbms.jdbc.QueryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSInsertActionTest extends RDBMSActionTest
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
   public void testInsertNullList() throws QException
   {
      InsertInput insertInput = initInsertRequest();
      insertInput.setRecords(null);
      InsertOutput insertOutput = new RDBMSInsertAction().execute(insertInput);
      assertEquals(0, insertOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testInsertEmptyList() throws QException
   {
      InsertInput insertInput = initInsertRequest();
      insertInput.setRecords(Collections.emptyList());
      InsertOutput insertOutput = new RDBMSInsertAction().execute(insertInput);
      assertEquals(0, insertOutput.getRecords().size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testInsertOne() throws Exception
   {
      InsertInput insertInput = initInsertRequest();
      QRecord record = new QRecord().withTableName("person")
         .withValue("firstName", "James")
         .withValue("lastName", "Kirk")
         .withValue("email", "jamestk@starfleet.net")
         .withValue("birthDate", "2210-05-20");
      insertInput.setRecords(List.of(record));
      InsertOutput insertOutput = new RDBMSInsertAction().execute(insertInput);
      assertEquals(1, insertOutput.getRecords().size(), "Should return 1 row");
      assertNotNull(insertOutput.getRecords().get(0).getValue("id"), "Should have an id in the row");
      // todo - add errors to QRecord? assertTrue(insertResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      assertAnInsertedPersonRecord("James", "Kirk", 6);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testInsertMany() throws Exception
   {
      QueryManager.setPageSize(2);

      InsertInput insertInput = initInsertRequest();
      QRecord record1 = new QRecord().withTableName("person")
         .withValue("firstName", "Jean-Luc")
         .withValue("lastName", "Picard")
         .withValue("email", "jl@starfleet.net")
         .withValue("birthDate", "2310-05-20");
      QRecord record2 = new QRecord().withTableName("person")
         .withValue("firstName", "William")
         .withValue("lastName", "Riker")
         .withValue("email", "notthomas@starfleet.net")
         .withValue("birthDate", "2320-05-20");
      QRecord record3 = new QRecord().withTableName("person")
         .withValue("firstName", "Beverly")
         .withValue("lastName", "Crusher")
         .withValue("email", "doctor@starfleet.net")
         .withValue("birthDate", "2320-06-26");
      insertInput.setRecords(List.of(record1, record2, record3));
      InsertOutput insertOutput = new RDBMSInsertAction().execute(insertInput);
      assertEquals(3, insertOutput.getRecords().size(), "Should return right # of rows");
      assertEquals(6, insertOutput.getRecords().get(0).getValue("id"), "Should have next id in the row");
      assertEquals(7, insertOutput.getRecords().get(1).getValue("id"), "Should have next id in the row");
      assertEquals(8, insertOutput.getRecords().get(2).getValue("id"), "Should have next id in the row");
      assertAnInsertedPersonRecord("Jean-Luc", "Picard", 6);
      assertAnInsertedPersonRecord("William", "Riker", 7);
      assertAnInsertedPersonRecord("Beverly", "Crusher", 8);
   }



   private void assertAnInsertedPersonRecord(String firstName, String lastName, Integer id) throws Exception
   {
      runTestSql("SELECT * FROM person WHERE last_name = '" + lastName + "'", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertEquals(id, rs.getInt("id"));
            assertEquals(firstName, rs.getString("first_name"));
            assertNotNull(rs.getString("create_date"));
            assertNotNull(rs.getString("modify_date"));
         }
         assertEquals(1, rowsFound);
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private InsertInput initInsertRequest()
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setInstance(TestUtils.defineInstance());
      insertInput.setTableName(TestUtils.defineTablePerson().getName());
      return insertInput;
   }

}