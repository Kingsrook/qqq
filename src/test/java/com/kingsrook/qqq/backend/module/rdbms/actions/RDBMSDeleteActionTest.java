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
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.module.rdbms.TestUtils;
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
      DeleteInput deleteInput = initDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1, 2, 3, 4, 5));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(5, deleteResult.getRecords().size(), "Unfiltered delete should return all rows");
      // todo - add errors to QRecord? assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT id FROM person", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteOne() throws Exception
   {
      DeleteInput deleteInput = initDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(1, deleteResult.getRecords().size(), "Should delete one row");
      // todo - add errors to QRecord? assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT id FROM person WHERE id = 1", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteSome() throws Exception
   {
      DeleteInput deleteInput = initDeleteRequest();
      deleteInput.setPrimaryKeys(List.of(1, 3, 5));
      DeleteOutput deleteResult = new RDBMSDeleteAction().execute(deleteInput);
      assertEquals(3, deleteResult.getRecords().size(), "Should delete one row");
      // todo - add errors to QRecord? assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
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
   private DeleteInput initDeleteRequest()
   {
      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setInstance(TestUtils.defineInstance());
      deleteInput.setTableName(TestUtils.defineTablePerson().getName());
      return deleteInput;
   }

}