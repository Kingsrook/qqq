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

package com.kingsrook.qqq.backend.core.modules.backend.implementations.utils;


import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for BackendQueryFilterUtils
 *******************************************************************************/
class BackendQueryFilterUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDoesCriterionMatch()
   {
      //////////////////////////////////////////////
      // < and > w/ mix of numbers and strings... //
      //////////////////////////////////////////////
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "2"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 2));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, 1), "f", "0"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN, "1"), "f", 0));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "2"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 2));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, 1), "f", "0"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.GREATER_THAN_OR_EQUALS, "1"), "f", 0));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 2), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "2"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 1), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, 0), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN, "0"), "f", 1));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 2), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "2"), "f", 1));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 1), "f", "1"));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "1"), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, 0), "f", "1"));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.LESS_THAN_OR_EQUALS, "0"), "f", 1));

      /////////////////////////////
      // between and not-between //
      /////////////////////////////
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 0));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 1));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 2));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 3));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.BETWEEN, List.of(1, 3)), "f", 4));

      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 0));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 1));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 2));
      assertFalse(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 3));
      assertTrue(BackendQueryFilterUtils.doesCriteriaMatch(new QFilterCriteria("f", QCriteriaOperator.NOT_BETWEEN, List.of(1, 3)), "f", 4));
   }

}