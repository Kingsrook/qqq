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

package com.kingsrook.qqq.backend.core.utils;


import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Unit test for YamlUtils
 *******************************************************************************/
class YamlUtilsTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws JsonProcessingException
   {
      String yaml = """
         orderNo: "B-9910"
         date: "2019-04-18"
         customerName: "Customer, Jane"
         orderLines:
         - item: "Copper Wire (200ft)"
           quantity: 1
           unitPrice: 50.67
         - item: "Washers (1/4\\")"
           quantity: 24
           unitPrice: 0.15
         """;

      Map<String, Object> map = YamlUtils.toMap(yaml);
      System.out.println(map);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testQInstanceToYaml()
   {
      QInstance qInstance = TestUtils.defineInstance();
      String    yaml      = YamlUtils.toYaml(qInstance);
      System.out.println(yaml);
   }

}