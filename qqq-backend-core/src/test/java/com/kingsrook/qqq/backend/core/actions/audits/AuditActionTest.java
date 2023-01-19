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


import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.processes.utils.GeneralProcessUtils;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for AuditAction
 *******************************************************************************/
class AuditActionTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
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

}