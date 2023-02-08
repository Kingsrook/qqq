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


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditInput;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for AuditAction
 *******************************************************************************/
class AuditActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSingle() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      String userName = "John Doe";
      QContext.init(qInstance, new QSession().withUser(new QUser().withFullName(userName)));

      Integer recordId = 1701;
      AuditAction.execute(TestUtils.TABLE_NAME_PERSON_MEMORY, recordId, Map.of(), "Test Audit");

      /////////////////////////////////////
      // make sure things can be fetched //
      /////////////////////////////////////
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditTable", "name", TestUtils.TABLE_NAME_PERSON_MEMORY);
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditUser", "name", userName);
      QRecord auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId);
      assertEquals("Test Audit", auditRecord.getValueString("message"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFailWithoutSecurityKey() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      String userName = "John Doe";
      QContext.init(qInstance, new QSession().withUser(new QUser().withFullName(userName)));

      int recordId = 1701;
      AuditAction.execute(TestUtils.TABLE_NAME_ORDER, recordId, Map.of(), "Test Audit");

      ///////////////////////////////////////////////////////////////////
      // it should not throw, but it should also not insert the audit. //
      ///////////////////////////////////////////////////////////////////
      Optional<QRecord> auditRecord = GeneralProcessUtils.getRecordByField(null, "audit", "recordId", recordId);
      assertFalse(auditRecord.isPresent());

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // try again with a null value in the key - that should be ok - as at least you were thinking //
      // about the key and put in SOME value (null has its own semantics in security keys)          //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      Map<String, Serializable> securityKeys = new HashMap<>();
      securityKeys.put(TestUtils.SECURITY_KEY_TYPE_STORE, null);
      AuditAction.execute(TestUtils.TABLE_NAME_ORDER, recordId, securityKeys, "Test Audit");

      /////////////////////////////////////
      // now the audit should be stored. //
      /////////////////////////////////////
      auditRecord = GeneralProcessUtils.getRecordByField(null, "audit", "recordId", recordId);
      assertTrue(auditRecord.isPresent());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMulti() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      String userName = "John Doe";
      QContext.init(qInstance, new QSession().withUser(new QUser().withFullName(userName)));

      Integer    recordId1  = 1701;
      Integer    recordId2  = 1702;
      AuditInput auditInput = new AuditInput();
      AuditAction.appendToInput(auditInput, TestUtils.TABLE_NAME_PERSON_MEMORY, recordId1, Map.of(), "Test Audit");
      AuditAction.appendToInput(auditInput, TestUtils.TABLE_NAME_ORDER, recordId2, Map.of(TestUtils.SECURITY_KEY_TYPE_STORE, 47), "Test Another Audit");
      new AuditAction().execute(auditInput);

      /////////////////////////////////////
      // make sure things can be fetched //
      /////////////////////////////////////
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditTable", "name", TestUtils.TABLE_NAME_PERSON_MEMORY);
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditUser", "name", userName);
      QRecord auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId1);
      assertEquals("Test Audit", auditRecord.getValueString("message"));

      auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId2);
      assertEquals("Test Another Audit", auditRecord.getValueString("message"));
      assertEquals(47, auditRecord.getValueInteger(TestUtils.SECURITY_KEY_TYPE_STORE));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMultiWithDetails() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();
      new AuditsMetaDataProvider().defineAll(qInstance, TestUtils.MEMORY_BACKEND_NAME, null);

      String userName = "John Doe";
      QContext.init(qInstance, new QSession().withUser(new QUser().withFullName(userName)));

      Integer    recordId1  = 1701;
      Integer    recordId2  = 1702;
      Integer    recordId3  = 1703;
      AuditInput auditInput = new AuditInput();
      AuditAction.appendToInput(auditInput, TestUtils.TABLE_NAME_PERSON_MEMORY, recordId1, Map.of(), "Test Audit", List.of("Detail1", "Detail2"));
      AuditAction.appendToInput(auditInput, TestUtils.TABLE_NAME_ORDER, recordId2, Map.of(TestUtils.SECURITY_KEY_TYPE_STORE, 47), "Test Another Audit", null);
      AuditAction.appendToInput(auditInput, TestUtils.TABLE_NAME_PERSON_MEMORY, recordId3, Map.of(TestUtils.SECURITY_KEY_TYPE_STORE, 42), "Audit 3", List.of("Detail3"));
      new AuditAction().execute(auditInput);

      /////////////////////////////////////
      // make sure things can be fetched //
      /////////////////////////////////////
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditTable", "name", TestUtils.TABLE_NAME_PERSON_MEMORY);
      GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "auditUser", "name", userName);
      QRecord auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId1);
      assertEquals("Test Audit", auditRecord.getValueString("message"));

      List<QRecord> auditDetails = GeneralProcessUtils.getRecordListByField(null, "auditDetail", "auditId", auditRecord.getValue("id"));
      assertEquals(2, auditDetails.size());
      assertThat(auditDetails).anyMatch(r -> r.getValueString("message").equals("Detail1"));
      assertThat(auditDetails).anyMatch(r -> r.getValueString("message").equals("Detail2"));

      auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId2);
      assertEquals("Test Another Audit", auditRecord.getValueString("message"));
      assertEquals(47, auditRecord.getValueInteger(TestUtils.SECURITY_KEY_TYPE_STORE));
      auditDetails = GeneralProcessUtils.getRecordListByField(null, "auditDetail", "auditId", auditRecord.getValue("id"));
      assertEquals(0, auditDetails.size());

      auditRecord = GeneralProcessUtils.getRecordByFieldOrElseThrow(null, "audit", "recordId", recordId3);
      assertEquals("Audit 3", auditRecord.getValueString("message"));
      assertEquals(42, auditRecord.getValueInteger(TestUtils.SECURITY_KEY_TYPE_STORE));
      auditDetails = GeneralProcessUtils.getRecordListByField(null, "auditDetail", "auditId", auditRecord.getValue("id"));
      assertEquals(1, auditDetails.size());
      assertThat(auditDetails).anyMatch(r -> r.getValueString("message").equals("Detail3"));
   }

}