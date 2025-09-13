/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for OldRecordHelper 
 *******************************************************************************/
class OldRecordHelperTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      OldRecordHelper oldRecordHelper = new OldRecordHelper(TestUtils.TABLE_NAME_PERSON_MEMORY, Optional.of(List.of(
         new QRecord().withValue("id", 1)
      )));

      assertTrue(oldRecordHelper.getOldRecord(new QRecord().withValue("id", 1)).isPresent());
      assertTrue(oldRecordHelper.getOldRecord(new QRecord().withValue("id", "1")).isPresent());
      assertFalse(oldRecordHelper.getOldRecord(new QRecord().withValue("id", 2)).isPresent());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEmptyOldRecords()
   {
      OldRecordHelper oldRecordHelper = new OldRecordHelper(TestUtils.TABLE_NAME_PERSON_MEMORY, Optional.empty());
      assertFalse(oldRecordHelper.getOldRecord(new QRecord().withValue("id", 1)).isPresent());
      assertFalse(oldRecordHelper.getOldRecord(new QRecord().withValue("id", "1")).isPresent());
      assertFalse(oldRecordHelper.getOldRecord(new QRecord().withValue("id", 2)).isPresent());
   }

}