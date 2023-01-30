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


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataOutput;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for ProcessMetaDataAction
 **
 *******************************************************************************/
class ProcessMetaDataActionTest extends BaseTest
{

   /*******************************************************************************
    ** Test basic success case.
    **
    *******************************************************************************/
   @Test
   public void test() throws QException
   {
      ProcessMetaDataInput request = new ProcessMetaDataInput();
      request.setProcessName(TestUtils.PROCESS_NAME_GREET_PEOPLE_INTERACTIVE);
      ProcessMetaDataOutput result = new ProcessMetaDataAction().execute(request);
      assertNotNull(result);
      assertNotNull(result.getProcess());
      assertEquals("greetInteractive", result.getProcess().getName());
      assertEquals("Greet Interactive", result.getProcess().getLabel());
      assertEquals(2, result.getProcess().getFrontendSteps().size());
   }



   /*******************************************************************************
    ** Test exception is thrown for the "not-found" case.
    **
    *******************************************************************************/
   @Test
   public void test_notFound()
   {
      assertThrows(QNotFoundException.class, () -> {
         ProcessMetaDataInput request = new ProcessMetaDataInput();
         request.setProcessName("willNotBeFound");
         new ProcessMetaDataAction().execute(request);
      });
   }

}
