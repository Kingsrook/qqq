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

package com.kingsrook.qqq.backend.core.actions.tables;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for DeleteAction
 **
 *******************************************************************************/
class DeleteActionTest extends BaseTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      DeleteInput request = new DeleteInput();
      request.setTableName("person");
      request.setPrimaryKeys(List.of(1, 2));
      DeleteOutput result = new DeleteAction().execute(request);
      assertNotNull(result);
      assertEquals(2, result.getDeletedRecordCount());
      assertTrue(CollectionUtils.nullSafeIsEmpty(result.getRecordsWithErrors()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testErrorIfBothPrimaryKeysAndFilter()
   {
      DeleteInput request = new DeleteInput();
      request.setTableName("person");
      request.setPrimaryKeys(List.of(1, 2));
      request.setQueryFilter(new QQueryFilter());

      assertThrows(QException.class, () ->
      {
         new DeleteAction().execute(request);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuditsByPrimaryKey() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)));
      new InsertAction().execute(insertInput);

      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.RECORD));

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      deleteInput.setPrimaryKeys(List.of(1, 2));
      new DeleteAction().execute(deleteInput);

      List<QRecord> audits = TestUtils.queryTable("audit");
      assertEquals(2, audits.size());
      assertTrue(audits.stream().allMatch(r -> r.getValueString("message").equals("Record was Deleted")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAuditsByFilter() throws QException
   {
      QInstance qInstance = QContext.getQInstance();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      insertInput.setRecords(List.of(
         new QRecord().withValue("id", 1),
         new QRecord().withValue("id", 2)));
      new InsertAction().execute(insertInput);

      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);
      qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.RECORD));

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);
      deleteInput.setQueryFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, 1, 2)));
      new DeleteAction().execute(deleteInput);

      List<QRecord> audits = TestUtils.queryTable("audit");
      assertEquals(2, audits.size());
      assertTrue(audits.stream().allMatch(r -> r.getValueString("message").equals("Record was Deleted")));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAssociatedDeletes() throws QException
   {
      QContext.getQSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);

      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_ORDER);
         insertInput.setRecords(List.of(
            new QRecord().withValue("id", 1),
            new QRecord().withValue("id", 2),
            new QRecord().withValue("id", 3)
         ));
         new InsertAction().execute(insertInput);
      }

      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_ORDER_EXTRINSIC);
         insertInput.setRecords(List.of(
            new QRecord().withValue("id", 1).withValue("orderId", 1),
            new QRecord().withValue("id", 2).withValue("orderId", 1),
            new QRecord().withValue("id", 3).withValue("orderId", 1),
            new QRecord().withValue("id", 4).withValue("orderId", 1),
            new QRecord().withValue("id", 5).withValue("orderId", 3),
            new QRecord().withValue("id", 6).withValue("orderId", 3),
            new QRecord().withValue("id", 7).withValue("orderId", 3)
         ));
         new InsertAction().execute(insertInput);
      }

      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM);
         insertInput.setRecords(List.of(
            new QRecord().withValue("id", 1).withValue("orderId", 1),
            new QRecord().withValue("id", 2).withValue("orderId", 1),
            new QRecord().withValue("id", 3).withValue("orderId", 2),
            new QRecord().withValue("id", 4).withValue("orderId", 3),
            new QRecord().withValue("id", 5).withValue("orderId", 3),
            new QRecord().withValue("id", 6).withValue("orderId", 3)
         ));
         new InsertAction().execute(insertInput);
      }

      {
         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC);
         insertInput.setRecords(List.of(
            new QRecord().withValue("id", 1).withValue("lineItemId", 1), // orderId: 1
            new QRecord().withValue("id", 2).withValue("lineItemId", 1), // orderId: 1
            new QRecord().withValue("id", 3).withValue("lineItemId", 2), // orderId: 1
            new QRecord().withValue("id", 4).withValue("lineItemId", 2), // orderId: 1
            new QRecord().withValue("id", 5).withValue("lineItemId", 3), // orderId: 2
            new QRecord().withValue("id", 6).withValue("lineItemId", 3), // orderId: 2
            new QRecord().withValue("id", 7).withValue("lineItemId", 4), // orderId: 3
            new QRecord().withValue("id", 8).withValue("lineItemId", 5), // orderId: 3
            new QRecord().withValue("id", 9).withValue("lineItemId", 6)  // orderId: 3
         ));
         new InsertAction().execute(insertInput);
      }

      /////////////////////////////////////////////////////////
      // assert about how many things we originally inserted //
      /////////////////////////////////////////////////////////
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(7, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());
      assertEquals(6, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(9, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());

      ////////////////////////////////
      // delete (cascading) order 1 //
      ////////////////////////////////
      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(TestUtils.TABLE_NAME_ORDER);
      deleteInput.setPrimaryKeys(List.of(1));
      new DeleteAction().execute(deleteInput);

      //////////////////////////////////////////////////
      // assert that the associated data were deleted //
      //////////////////////////////////////////////////
      assertEquals(2, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());
      assertEquals(4, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(5, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());

      ////////////////////
      // delete order 2 //
      ////////////////////
      deleteInput.setPrimaryKeys(List.of(2));
      new DeleteAction().execute(deleteInput);

      //////////////////////////////////////////////////
      // assert that the associated data were deleted //
      //////////////////////////////////////////////////
      assertEquals(1, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(3, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());

      ////////////////////
      // delete order 3 //
      ////////////////////
      deleteInput.setPrimaryKeys(List.of(3));
      new DeleteAction().execute(deleteInput);

      ///////////////////////////////
      // everything is deleted now //
      ///////////////////////////////
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_ORDER).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_ORDER_EXTRINSIC).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_LINE_ITEM).size());
      assertEquals(0, queryTable(TestUtils.TABLE_NAME_LINE_ITEM_EXTRINSIC).size());

      ////////////////////////////////////////////////
      // make sure no errors if we try more deletes //
      ////////////////////////////////////////////////
      deleteInput.setPrimaryKeys(List.of(3));
      new DeleteAction().execute(deleteInput);

      deleteInput.setPrimaryKeys(List.of(1, 2, 3, 4));
      new DeleteAction().execute(deleteInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QRecord> queryTable(String tableName) throws QException
   {
      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(tableName);
      queryInput.setFilter(new QQueryFilter().withOrderBy(new QFilterOrderBy("id")));
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      return (queryOutput.getRecords());
   }

}
