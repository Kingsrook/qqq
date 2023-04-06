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

package com.kingsrook.qqq.api.model.metadata;


import java.util.List;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for com.kingsrook.qqq.api.model.metadata.ApiOperation
 *******************************************************************************/
class ApiOperationTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      assertTrue(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData(), new ApiTableMetaData())));
      assertTrue(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData(), new ApiTableMetaData().withEnabledOperation(ApiOperation.GET))));
      assertFalse(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData(), new ApiTableMetaData().withDisabledOperation(ApiOperation.GET))));
      assertTrue(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withEnabledOperation(ApiOperation.GET), new ApiTableMetaData())));
      assertTrue(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withEnabledOperation(ApiOperation.GET), new ApiTableMetaData().withEnabledOperation(ApiOperation.GET))));
      assertFalse(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withEnabledOperation(ApiOperation.GET), new ApiTableMetaData().withDisabledOperation(ApiOperation.GET))));
      assertFalse(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withDisabledOperation(ApiOperation.GET), new ApiTableMetaData())));
      assertTrue(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withDisabledOperation(ApiOperation.GET), new ApiTableMetaData().withEnabledOperation(ApiOperation.GET))));
      assertFalse(ApiOperation.GET.isOperationEnabled(List.of(new ApiInstanceMetaData().withDisabledOperation(ApiOperation.GET), new ApiTableMetaData().withDisabledOperation(ApiOperation.GET))));
   }

}