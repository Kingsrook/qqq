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

package com.kingsrook.qqq.backend.module.api.model.metadata;


import com.kingsrook.qqq.backend.module.api.BaseTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for APITableBackendDetails 
 *******************************************************************************/
class APITableBackendDetailsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testClone()
   {
      APITableBackendDetails tableBackendDetails = (APITableBackendDetails) new APITableBackendDetails()
         .withTablePath("a")
         .withTableWrapperObjectName("b")
         .withBackendType("c");

      APITableBackendDetails clonedTableBackendDetails = (APITableBackendDetails) tableBackendDetails.clone();
      clonedTableBackendDetails.withTablePath("x");

      assertEquals("a", tableBackendDetails.getTablePath());
      assertEquals("b", tableBackendDetails.getTableWrapperObjectName());
      assertEquals("c", tableBackendDetails.getBackendType());

      assertEquals("x", clonedTableBackendDetails.getTablePath());
      assertEquals("b", clonedTableBackendDetails.getTableWrapperObjectName());
      assertEquals("c", clonedTableBackendDetails.getBackendType());

      clonedTableBackendDetails.withBackendType("z");
      assertEquals("c", tableBackendDetails.getBackendType());
      assertEquals("z", clonedTableBackendDetails.getBackendType());

   }

}