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

package com.kingsrook.qqq.backend.core.processes.implementations.basepull;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ExtractViaBasepullQueryStep
 *******************************************************************************/
class ExtractViaBasepullQueryStepTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      Instant timestamp = Instant.parse("1980-05-31T15:36:00Z");
      Instant now       = Instant.now();

      RunBackendStepInput input = new RunBackendStepInput(TestUtils.defineInstance());
      input.addValue(RunProcessAction.BASEPULL_TIMESTAMP_FIELD, "createDate");
      input.addValue(RunProcessAction.BASEPULL_THIS_RUNTIME_KEY, now);
      input.setBasepullLastRunTime(timestamp);
      QQueryFilter queryFilter = new ExtractViaBasepullQueryStep().getQueryFilter(input);

      System.out.println(queryFilter);

      assertEquals(2, queryFilter.getCriteria().size());
      assertEquals("createDate", queryFilter.getCriteria().get(0).getFieldName());
      assertEquals(QCriteriaOperator.GREATER_THAN, queryFilter.getCriteria().get(0).getOperator());
      assertEquals(timestamp, queryFilter.getCriteria().get(0).getValues().get(0));

      assertEquals("createDate", queryFilter.getCriteria().get(1).getFieldName());
      assertEquals(QCriteriaOperator.LESS_THAN_OR_EQUALS, queryFilter.getCriteria().get(1).getOperator());
      assertEquals(now, queryFilter.getCriteria().get(1).getValues().get(0));

      assertEquals(1, queryFilter.getOrderBys().size());
      assertEquals("createDate", queryFilter.getOrderBys().get(0).getFieldName());
      assertTrue(queryFilter.getOrderBys().get(0).getIsAscending());
   }

}