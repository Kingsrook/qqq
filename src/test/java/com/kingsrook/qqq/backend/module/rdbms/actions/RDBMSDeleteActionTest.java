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
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
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
      DeleteRequest deleteRequest = initDeleteRequest();
      deleteRequest.setPrimaryKeys(List.of(1, 2, 3, 4, 5));
      DeleteResult deleteResult = new RDBMSDeleteAction().execute(deleteRequest);
      assertEquals(5, deleteResult.getRecords().size(), "Unfiltered delete should return all rows");
      assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT id FROM person", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteOne() throws Exception
   {
      DeleteRequest deleteRequest = initDeleteRequest();
      deleteRequest.setPrimaryKeys(List.of(1));
      DeleteResult deleteResult = new RDBMSDeleteAction().execute(deleteRequest);
      assertEquals(1, deleteResult.getRecords().size(), "Should delete one row");
      assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
      runTestSql("SELECT id FROM person WHERE id = 1", (rs -> assertFalse(rs.next())));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testDeleteSome() throws Exception
   {
      DeleteRequest deleteRequest = initDeleteRequest();
      deleteRequest.setPrimaryKeys(List.of(1, 3, 5));
      DeleteResult deleteResult = new RDBMSDeleteAction().execute(deleteRequest);
      assertEquals(3, deleteResult.getRecords().size(), "Should delete one row");
      assertTrue(deleteResult.getRecords().stream().noneMatch(qrs -> CollectionUtils.nullSafeHasContents(qrs.getErrors())), "There should be no errors");
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
   private DeleteRequest initDeleteRequest()
   {
      DeleteRequest deleteRequest = new DeleteRequest();
      deleteRequest.setInstance(defineInstance());
      deleteRequest.setTableName(defineTablePerson().getName());
      return deleteRequest;
   }

}