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

package com.kingsrook.qqq.backend.core.actions.tables;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for QueryAction
 **
 *******************************************************************************/
class QueryActionTest
{

   /*******************************************************************************
    ** At the core level, there isn't much that can be asserted, as it uses the
    ** mock implementation - just confirming that all of the "wiring" works.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      QueryInput queryInput = new QueryInput(TestUtils.defineInstance());
      queryInput.setSession(TestUtils.getMockSession());
      queryInput.setTableName("person");
      QueryOutput queryOutput = new QueryAction().execute(queryInput);
      assertNotNull(queryOutput);

      assertThat(queryOutput.getRecords()).isNotEmpty();
      for(QRecord record : queryOutput.getRecords())
      {
         assertThat(record.getValues()).isNotEmpty();
         assertThat(record.getErrors()).isEmpty();

         ///////////////////////////////////////////////////////////////
         // this SHOULD be empty, based on the default for the should //
         ///////////////////////////////////////////////////////////////
         assertThat(record.getDisplayValues()).isEmpty();
      }

      ////////////////////////////////////
      // now flip that field and re-run //
      ////////////////////////////////////
      queryInput.setShouldGenerateDisplayValues(true);
      assertThat(queryOutput.getRecords()).isNotEmpty();
      queryOutput = new QueryAction().execute(queryInput);
      for(QRecord record : queryOutput.getRecords())
      {
         assertThat(record.getDisplayValues()).isNotEmpty();
      }
   }
}
