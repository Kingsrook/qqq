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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
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

}
