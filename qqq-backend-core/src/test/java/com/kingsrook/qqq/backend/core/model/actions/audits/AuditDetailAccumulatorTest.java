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

package com.kingsrook.qqq.backend.core.model.actions.audits;


import java.util.Collection;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for AuditDetailAccumulator 
 *******************************************************************************/
class AuditDetailAccumulatorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      AuditDetailAccumulator auditDetailAccumulator = new AuditDetailAccumulator("During test");
      auditDetailAccumulator.addAuditDetail(TestUtils.TABLE_NAME_PERSON, new QRecord().withValue("id", 1701), "Something happened");
      auditDetailAccumulator.addAuditDetail(TestUtils.TABLE_NAME_PERSON, new QRecord().withValue("id", 1701), "Something else happened");
      auditDetailAccumulator.addAuditDetail(TestUtils.TABLE_NAME_PERSON, new QRecord().withValue("id", 74256), "Something happened here too");
      auditDetailAccumulator.addAuditDetail(TestUtils.TABLE_NAME_ORDER, new QRecord().withValue("id", 74256), "Something happened to an order");

      Collection<AuditSingleInput> auditSingleInputs = auditDetailAccumulator.getAccumulatedAuditSingleInputs();
      assertEquals(3, auditSingleInputs.size());
      assertThat(auditSingleInputs).anyMatch(asi -> asi.getAuditTableName().equals(TestUtils.TABLE_NAME_PERSON) && asi.getRecordId().equals(1701) && asi.getDetails().size() == 2);
      assertThat(auditSingleInputs).anyMatch(asi -> asi.getAuditTableName().equals(TestUtils.TABLE_NAME_PERSON) && asi.getRecordId().equals(74256) && asi.getDetails().size() == 1);
      assertThat(auditSingleInputs).anyMatch(asi -> asi.getAuditTableName().equals(TestUtils.TABLE_NAME_ORDER) && asi.getRecordId().equals(74256) && asi.getDetails().size() == 1);

      auditDetailAccumulator.clear();;
      auditSingleInputs = auditDetailAccumulator.getAccumulatedAuditSingleInputs();
      assertEquals(0, auditSingleInputs.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testContext()
   {
      AuditDetailAccumulator auditDetailAccumulator = new AuditDetailAccumulator("During test");
      auditDetailAccumulator.setInContext();

      AuditDetailAccumulator.getFromContext().ifPresent(ada -> ada.addAuditDetail(TestUtils.TABLE_NAME_PERSON, new QRecord().withValue("id", 1701), "Something happened"));

      Collection<AuditSingleInput> auditSingleInputs = auditDetailAccumulator.getAccumulatedAuditSingleInputs();
      assertEquals(1, auditSingleInputs.size());
      assertThat(auditSingleInputs).anyMatch(asi -> asi.getAuditTableName().equals(TestUtils.TABLE_NAME_PERSON) && asi.getRecordId().equals(1701) && asi.getDetails().size() == 1);
   }

}