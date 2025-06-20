/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.tables.helpers;


import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ValidateRecordSecurityLockHelper.RecordWithErrors;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock.BooleanOperator.AND;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ValidateRecordSecurityLockHelper 
 *******************************************************************************/
class ValidateRecordSecurityLockHelperTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testRecordWithErrors()
   {
      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("0"), List.of(0));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withOperator(AND).withLocks(List.of(new RecordSecurityLock())));
         System.out.println("----------------------------------------------------------------------------");
      }

      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("1"), List.of(1));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock())));
         System.out.println("----------------------------------------------------------------------------");
      }

      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("0"), List.of(0));
         recordWithErrors.add(new BadInputStatusMessage("1"), List.of(1));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock())));
         System.out.println("----------------------------------------------------------------------------");
      }

      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("1,1"), List.of(1, 1));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withLocks(List.of(
            new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock())),
            new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock()))
         )));
         System.out.println("----------------------------------------------------------------------------");
      }

      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("0,0"), List.of(0, 0));
         recordWithErrors.add(new BadInputStatusMessage("1,1"), List.of(1, 1));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withLocks(List.of(
            new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock())),
            new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock()))
         )));
         System.out.println("----------------------------------------------------------------------------");
      }

      {
         RecordWithErrors recordWithErrors = new RecordWithErrors(new QRecord());
         recordWithErrors.add(new BadInputStatusMessage("0"), List.of(0));
         recordWithErrors.add(new BadInputStatusMessage("1,1"), List.of(1, 1));
         System.out.println(recordWithErrors);
         recordWithErrors.propagateErrorsToRecord(new MultiRecordSecurityLock().withLocks(List.of(
            new RecordSecurityLock(),
            new MultiRecordSecurityLock().withLocks(List.of(new RecordSecurityLock(), new RecordSecurityLock()))
         )));
         System.out.println("----------------------------------------------------------------------------");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAllowedToReadRecord() throws QException
   {
      QTableMetaData table = QContext.getQInstance().getTables().get(TestUtils.TABLE_NAME_ORDER);

      QSession sessionWithStore1          = new QSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 1);
      QSession sessionWithStore2          = new QSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE, 2);
      QSession sessionWithStore1and2      = new QSession().withSecurityKeyValues(Map.of(TestUtils.SECURITY_KEY_TYPE_STORE, List.of(1, 2)));
      QSession sessionWithStoresAllAccess = new QSession().withSecurityKeyValue(TestUtils.SECURITY_KEY_TYPE_STORE_ALL_ACCESS, true);
      QSession sessionWithNoStores        = new QSession();

      QRecord recordStore1  = new QRecord().withValue("storeId", 1);

      assertTrue(ValidateRecordSecurityLockHelper.allowedToReadRecord(table, recordStore1, sessionWithStore1, null));
      assertFalse(ValidateRecordSecurityLockHelper.allowedToReadRecord(table, recordStore1, sessionWithStore2, null));
      assertTrue(ValidateRecordSecurityLockHelper.allowedToReadRecord(table, recordStore1, sessionWithStore1and2, null));
      assertTrue(ValidateRecordSecurityLockHelper.allowedToReadRecord(table, recordStore1, sessionWithStoresAllAccess, null));
      assertFalse(ValidateRecordSecurityLockHelper.allowedToReadRecord(table, recordStore1, sessionWithNoStores, null));
   }

}