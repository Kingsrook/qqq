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


import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for MetaDataAction
 **
 *******************************************************************************/
class MetaDataActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      MetaDataInput request = new MetaDataInput(TestUtils.defineInstance());
      request.setSession(TestUtils.getMockSession());
      MetaDataOutput result = new MetaDataAction().execute(request);
      assertNotNull(result);

      assertNotNull(result.getTables());
      assertNotNull(result.getTables().get("person"));
      assertEquals("person", result.getTables().get("person").getName());
      assertEquals("Person", result.getTables().get("person").getLabel());

      assertNotNull(result.getProcesses().get("greet"));
      assertNotNull(result.getProcesses().get("greetInteractive"));
      assertNotNull(result.getProcesses().get("etl.basic"));
      assertNotNull(result.getProcesses().get("person.bulkInsert"));
      assertNotNull(result.getProcesses().get("person.bulkEdit"));
      assertNotNull(result.getProcesses().get("person.bulkDelete"));

   }
}
