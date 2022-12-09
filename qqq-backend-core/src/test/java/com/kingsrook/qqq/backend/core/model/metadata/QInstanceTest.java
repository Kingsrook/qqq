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

package com.kingsrook.qqq.backend.core.model.metadata;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for QInstance
 *******************************************************************************/
class QInstanceTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetTablePath() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      GetInput getInput = new GetInput(qInstance);
      getInput.setSession(new QSession());

      String tablePath = qInstance.getTablePath(getInput, TestUtils.TABLE_NAME_PERSON);
      assertEquals("/peopleApp/person", tablePath);

      ////////////////////////////////////////////////////////////////////////////////////////
      // call again (to make sure getting from memoization works - verify w/ breakpoint...) //
      ////////////////////////////////////////////////////////////////////////////////////////
      tablePath = qInstance.getTablePath(getInput, TestUtils.TABLE_NAME_PERSON);
      assertEquals("/peopleApp/person", tablePath);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetTablePathNotInAnyApp() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      GetInput getInput = new GetInput(qInstance);
      getInput.setSession(new QSession());

      String tablePath = qInstance.getTablePath(getInput, "notATable");
      assertNull(tablePath);

      ////////////////////////////////////////////////////////////////////////////////////////
      // call again (to make sure getting from memoization works - verify w/ breakpoint...) //
      ////////////////////////////////////////////////////////////////////////////////////////
      tablePath = qInstance.getTablePath(getInput, "notATable");
      assertNull(tablePath);
   }

}