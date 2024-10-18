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

package com.kingsrook.qqq.api.wip.v1;


import com.kingsrook.qqq.api.wip.TestContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for QueryGetSpecV1 
 *******************************************************************************/
class QueryGetSpecV1Test
{


   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testBuildInput() throws Exception
   {
      TestContext context = new TestContext()
         .withPathParam("table", "person")
         .withQueryParam("filter", "{}");

      //      QueryMiddlewareInput queryMiddlewareInput = new QueryGetSpecV1().buildInput(context);
      //      assertEquals("person", queryMiddlewareInput.getTable());
      //      assertNotNull(queryMiddlewareInput.getFilter());
      //      assertEquals(0, queryMiddlewareInput.getFilter().getCriteria().size());
      //      assertEquals(0, queryMiddlewareInput.getFilter().getOrderBys().size());
      //      assertNull(queryMiddlewareInput.getQueryJoins());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Test
   void testBuildOutput() throws Exception
   {
      TestContext context     = new TestContext();
      QueryOutput queryOutput = new QueryOutput(new QueryInput());
      queryOutput.addRecord(new QRecord().withValue("firstName", "Darin").withDisplayValue("firstName", "Darin"));

      //      QueryGetSpecV1 queryGetSpecV1 = new QueryGetSpecV1();
      //      queryGetSpecV1.buildOutput(context, new QueryMiddlewareOutput(queryOutput));
      //      String resultJson = context.getResultAsString();
      //      TestUtils.assertResultJsonVsSpec(queryGetSpecV1.defineSimpleSuccessResponse(), resultJson);
   }

}