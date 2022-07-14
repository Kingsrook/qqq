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

package com.kingsrook.qqq.backend.core.actions.metadata;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataOutput;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for TableMetaDataAction
 **
 *******************************************************************************/
class TableMetaDataActionTest
{

   /*******************************************************************************
    ** Test basic success case.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      TableMetaDataInput request = new TableMetaDataInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      request.setTableName("person");
      TableMetaDataOutput result = new TableMetaDataAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getTable());
      assertEquals("person", result.getTable().getName());
      assertEquals("Person", result.getTable().getLabel());
   }



   /*******************************************************************************
    ** Test exeption is thrown for the "not-found" case.
    **
    *******************************************************************************/
   @Test
   public void test_notFound()
   {
      assertThrows(QUserFacingException.class, () -> {
         TableMetaDataInput request = new TableMetaDataInput(TestUtils.defineInstance());
         request.setSession(TestUtils.getMockSession());
         request.setTableName("willNotBeFound");
         new TableMetaDataAction().execute(request);
      });
   }

}
