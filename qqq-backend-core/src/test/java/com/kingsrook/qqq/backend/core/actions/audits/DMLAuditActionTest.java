/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.audits;


import java.time.Instant;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.audits.DMLAuditInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryRecordStore;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for DMLAuditAction
 *******************************************************************************/
class DMLAuditActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = QContext.getQInstance();
      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(TestUtils.TABLE_NAME_PERSON_MEMORY);

      List<QRecord> recordList    = List.of(new QRecord().withValue("id", 1).withValue("firstName", "Darin").withValue("noOfShoes", 5).withValue("favoriteShapeId", null).withValue("createDate", Instant.now()).withValue("modifyDate", Instant.now()));
      List<QRecord> oldRecordList = List.of(new QRecord().withValue("id", 1).withValue("firstName", "Tim").withValue("noOfShoes", null).withValue("favoriteShapeId", 1).withValue("lastName", "Simpson"));

      ////////////////////////////////////////////////////////
      // set audit rules null - confirm no audits are built //
      ////////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(null);
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(deleteInput).withRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertTrue(auditList.isEmpty());
      }

      ////////////////////////////////////////////////////////
      // set audit level NONE - confirm no audits are built //
      ////////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.NONE));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(deleteInput).withRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertTrue(auditList.isEmpty());
      }

      /////////////////////////////////////////////////////////////
      // set audit level RECORD - confirm only header, no detail //
      /////////////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.RECORD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(insertInput).withRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertEquals(1, auditList.size());
         assertEquals("Record was Inserted", auditList.get(0).getValueString("message"));
         List<QRecord> auditDetailList = TestUtils.queryTable("auditDetail");
         assertTrue(auditDetailList.isEmpty());
         MemoryRecordStore.getInstance().reset();
      }

      ////////////////////////////////////////////////////////
      // set audit level FIELD - confirm header, and detail //
      ////////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput).withRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertEquals(1, auditList.size());
         assertEquals("Record was Edited", auditList.get(0).getValueString("message"));
         List<QRecord> auditDetailList = TestUtils.queryTable("auditDetail");

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // since we didn't provide old-records, there should be a detail for every field in the updated record - OTHER THAN createDate and modifyDate //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         assertEquals(recordList.get(0).getValues().size() - 2, auditDetailList.size());
         assertTrue(auditDetailList.stream().allMatch(r -> r.getValueString("message").matches("Set.*to.*")));
         assertTrue(auditDetailList.stream().allMatch(r -> r.getValueString("fieldName") != null));
         assertTrue(auditDetailList.stream().allMatch(r -> r.getValueString("oldValue") == null));
         assertTrue(auditDetailList.stream().anyMatch(r -> r.getValueString("newValue") != null));
         MemoryRecordStore.getInstance().reset();
      }

      //////////////////////////////////////////////
      // this time supply old-records to the edit //
      //////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_SHAPE).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.NONE));
         TestUtils.insertDefaultShapes(qInstance);

         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput).withOldRecordList(oldRecordList).withRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertEquals(1, auditList.size());
         assertEquals("Record was Edited", auditList.get(0).getValueString("message"));
         List<QRecord> auditDetailList = TestUtils.queryTable("auditDetail");

         assertEquals(3, auditDetailList.size());

         assertEquals("Removed \"Triangle\" from Favorite Shape", auditDetailList.get(0).getValueString("message"));
         assertEquals("favoriteShapeId", auditDetailList.get(0).getValueString("fieldName"));
         assertEquals("Triangle", auditDetailList.get(0).getValueString("oldValue"));
         assertNull(auditDetailList.get(0).getValueString("newValue"));

         assertEquals("Changed First Name from \"Tim\" to \"Darin\"", auditDetailList.get(1).getValueString("message"));
         assertEquals("firstName", auditDetailList.get(1).getValueString("fieldName"));
         assertEquals("Tim", auditDetailList.get(1).getValueString("oldValue"));
         assertEquals("Darin", auditDetailList.get(1).getValueString("newValue"));

         assertEquals("Set No Of Shoes to 5", auditDetailList.get(2).getValueString("message"));
         assertEquals("noOfShoes", auditDetailList.get(2).getValueString("fieldName"));
         assertNull(auditDetailList.get(2).getValueString("oldValue"));
         assertEquals("5", auditDetailList.get(2).getValueString("newValue"));

         MemoryRecordStore.getInstance().reset();
      }

      /////////////////////////////////////////////////
      // confirm we don't log null fields on inserts //
      /////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(insertInput).withRecordList(recordList));
         List<QRecord> auditDetailList = TestUtils.queryTable("auditDetail");
         assertFalse(auditDetailList.isEmpty());
         assertTrue(auditDetailList.stream().noneMatch(r -> r.getValueString("message").contains("Favorite Shape")));
         MemoryRecordStore.getInstance().reset();
      }

      ///////////////////////////////////////////////////////////
      // confirm if nothing changed on an edit, that no audit. //
      ///////////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput).withRecordList(recordList).withOldRecordList(recordList));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertEquals(0, auditList.size());
         MemoryRecordStore.getInstance().reset();
      }

      ////////////////////////////////////////////////////
      // confirm we don't audit for records with errors //
      ////////////////////////////////////////////////////
      {
         qInstance.getTable(TestUtils.TABLE_NAME_PERSON_MEMORY).setAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD));
         new DMLAuditAction().execute(new DMLAuditInput().withTableActionInput(updateInput)
            .withRecordList(List.of(new QRecord().withValue("id", 1).withValue("firstName", "B").withError(new BadInputStatusMessage("Error"))))
            .withOldRecordList(List.of(new QRecord().withValue("id", 1).withValue("firstName", "A"))));
         List<QRecord> auditList = TestUtils.queryTable("audit");
         assertEquals(0, auditList.size());
         MemoryRecordStore.getInstance().reset();
      }
   }

}