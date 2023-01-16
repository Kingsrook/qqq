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

package com.kingsrook.qqq.backend.core.adapters;


import java.io.File;
import java.io.IOException;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 **
 *******************************************************************************/
class QInstanceAdapterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void qInstanceToJson()
   {
      QInstance qInstance = QContext.getQInstance();
      String    json      = new QInstanceAdapter().qInstanceToJson(qInstance);
      System.out.println(json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void qInstanceToJsonIncludingBackend()
   {
      QInstance qInstance = QContext.getQInstance();
      String    json      = new QInstanceAdapter().qInstanceToJsonIncludingBackend(qInstance);
      System.out.println(json);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("Pending custom deserializer on QStepMetaData")
   void jsonToQInstance() throws IOException
   {
      String    json      = FileUtils.readFileToString(new File("src/test/resources/personQInstance.json"));
      QInstance qInstance = new QInstanceAdapter().jsonToQInstance(json);
      System.out.println(qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   @Disabled("Pending custom deserializer on QStepMetaData")
   void jsonToQInstanceIncludingBackend() throws IOException
   {
      String    json      = FileUtils.readFileToString(new File("src/test/resources/personQInstanceIncludingBackend.json"));
      QInstance qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(json);
      System.out.println(qInstance);
      assertNotNull(qInstance.getBackends());
      assertTrue(qInstance.getBackends().size() > 0);
   }
}