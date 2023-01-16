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

package com.kingsrook.qqq.backend.core.model.actions.tables.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.EQUALS;
import static com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator.IS_BLANK;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QFilterCriteria
 *******************************************************************************/
class QFilterCriteriaTest extends BaseTest
{

   /*******************************************************************************
    ** Make sure that the constructors that takes a List or Serializable... and does
    ** the right thing - e.g., never making a List-of-List, or List of array, and
    ** that we never have null values - always an empty list as the degenerate case.
    *******************************************************************************/
   @Test
   void test()
   {
      assertEquals(1, new QFilterCriteria("foo", EQUALS, "A").getValues().size());
      assertEquals(1, new QFilterCriteria("foo", EQUALS, List.of("A")).getValues().size());
      assertEquals(2, new QFilterCriteria("foo", EQUALS, List.of("A", "B")).getValues().size());
      assertEquals(2, new QFilterCriteria("foo", EQUALS, "A", "B").getValues().size());

      List<Serializable> list = List.of("A", "B", "C");
      assertEquals(3, new QFilterCriteria("foo", EQUALS, list).getValues().size());
      assertEquals(List.of("A", "B", "C"), new QFilterCriteria("foo", EQUALS, list).getValues());

      Serializable[] array = new Serializable[] { "A", "B", "C", "D" };
      assertEquals(4, new QFilterCriteria("foo", EQUALS, array).getValues().size());
      assertEquals(List.of("A", "B", "C", "D"), new QFilterCriteria("foo", EQUALS, array).getValues());

      assertEquals(3, new QFilterCriteria("foo", EQUALS, new ArrayList<>(list)).getValues().size());
      assertEquals(List.of("A", "B", "C"), new QFilterCriteria("foo", EQUALS, new ArrayList<>(list)).getValues());

      assertEquals(0, new QFilterCriteria("foo", IS_BLANK).getValues().size());

      Serializable maybeNull = null;
      assertEquals(0, new QFilterCriteria("foo", EQUALS, maybeNull).getValues().size());

      List<Serializable> nullList = null;
      assertEquals(0, new QFilterCriteria("foo", EQUALS, nullList).getValues().size());

      assertEquals(0, new QFilterCriteria("foo", EQUALS, (List<Serializable>) null).getValues().size());
   }

}