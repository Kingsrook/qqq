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
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.helpers.ValidateRecordSecurityLockHelper.RecordWithErrors;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock.BooleanOperator.AND;


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

}