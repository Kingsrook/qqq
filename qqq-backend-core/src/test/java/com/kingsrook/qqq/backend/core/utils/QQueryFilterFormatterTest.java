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

package com.kingsrook.qqq.backend.core.utils;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for QQueryFilterFormatter 
 *******************************************************************************/
class QQueryFilterFormatterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestUtils.insertDefaultShapes(QContext.getQInstance());

      QQueryFilter filter = new QQueryFilter()
         .withCriteria("firstName", QCriteriaOperator.EQUALS, "Darin")
         .withCriteria("lastName", QCriteriaOperator.IN, List.of("Kelkhoff", "Smellkhoff", "Dumbkhoff"))
         .withCriteria("favoriteShapeId", QCriteriaOperator.NOT_EQUALS, List.of(1));

      assertEquals("First Name equals Darin AND Last Name is any of Kelkhoff and 2 other values AND Favorite Shape does not equal Triangle", QQueryFilterFormatter.formatQueryFilter(TestUtils.TABLE_NAME_PERSON, filter));
   }

}