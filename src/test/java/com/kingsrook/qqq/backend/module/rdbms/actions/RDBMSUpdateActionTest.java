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


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/*******************************************************************************
 **
 *******************************************************************************/
public class RDBMSUpdateActionTest extends RDBMSActionTest
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
   public void testUpdateOne() throws Exception
   {
      UpdateRequest updateRequest = initUpdateRequest();
      QRecord record = new QRecord().withTableName("person")
         .withValue("id", 2)
         .withValue("firstName", "James")
         .withValue("lastName", "Kirk")
         .withValue("email", "jamestk@starfleet.net")
         .withValue("birthDate", "2210-05-20");
      updateRequest.setRecords(List.of(record));
      UpdateResult updateResult = new RDBMSUpdateAction().execute(updateRequest);
      assertEquals(1, updateResult.getRecords().size(), "Should return 1 row");
      assertEquals(2, updateResult.getRecords().get(0).getValue("id"), "Should have id=2 in the row");
      assertTrue(updateResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT * FROM person WHERE last_name = 'Kirk'", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertEquals(2, rs.getInt("id"));
            assertEquals("James", rs.getString("first_name"));
            assertEquals("2210-05-20", rs.getString("birth_date"));
         }
         assertEquals(1, rowsFound);
      }));
      runTestSql("SELECT * FROM person WHERE last_name = 'Maes'", (rs -> {
         if(rs.next())
         {
            fail("Should not have found Maes any more.");
         }
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testUpdateMany() throws Exception
   {
      UpdateRequest updateRequest = initUpdateRequest();
      QRecord record1 = new QRecord().withTableName("person")
         .withValue("id", 1)
         .withValue("firstName", "Darren")
         .withValue("lastName", "From Bewitched")
         .withValue("birthDate", "1900-01-01");

      QRecord record2 = new QRecord().withTableName("person")
         .withValue("id", 3)
         .withValue("firstName", "Wilt")
         .withValue("birthDate", null);

      updateRequest.setRecords(List.of(record1, record2));
      UpdateResult updateResult = new RDBMSUpdateAction().execute(updateRequest);
      assertEquals(2, updateResult.getRecords().size(), "Should return 2 rows");
      assertEquals(1, updateResult.getRecords().get(0).getValue("id"), "Should have expected ids in the row");
      assertEquals(3, updateResult.getRecords().get(1).getValue("id"), "Should have expected ids in the row");
      assertTrue(updateResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT * FROM person WHERE last_name = 'From Bewitched'", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertEquals(1, rs.getInt("id"));
            assertEquals("Darren", rs.getString("first_name"));
            assertEquals("From Bewitched", rs.getString("last_name"));
            assertEquals("1900-01-01", rs.getString("birth_date"));
         }
         assertEquals(1, rowsFound);
      }));
      runTestSql("SELECT * FROM person WHERE last_name = 'Chamberlain'", (rs -> {
         int rowsFound = 0;
         while(rs.next())
         {
            rowsFound++;
            assertEquals(3, rs.getInt("id"));
            assertEquals("Wilt", rs.getString("first_name"));
            assertNull(rs.getString("birth_date"));
         }
         assertEquals(1, rowsFound);
      }));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private UpdateRequest initUpdateRequest()
   {
      UpdateRequest updateRequest = new UpdateRequest();
      updateRequest.setInstance(defineInstance());
      updateRequest.setTableName(defineTablePerson().getName());
      return updateRequest;
   }

}