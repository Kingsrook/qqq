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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.utils.collections.MultiLevelMapHelper.getOrPutAndIncrement;
import static com.kingsrook.qqq.backend.core.utils.collections.MultiLevelMapHelper.getOrPutNextLevel;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for MultiLevelMapHelper
 *******************************************************************************/
class MultiLevelMapHelperTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      Map<Integer, Map<String, Map<Integer, Map<String, Integer>>>> bigOleMap = new HashMap<>();

      Integer clientId    = 120;
      String  sku         = "BASIC1";
      Integer warehouseId = 10;
      String  state       = "MO";
      Integer quantity    = 5;

      getOrPutNextLevel(bigOleMap, clientId,
         map -> getOrPutNextLevel(map, sku,
            map2 -> getOrPutNextLevel(map2, warehouseId,
               map3 -> MultiLevelMapHelper.getOrPutFinalLevel(map3, state, () -> 0, v -> v + quantity))));
      assertEquals(5, bigOleMap.get(120).get("BASIC1").get(10).get("MO"));

      getOrPutNextLevel(bigOleMap, clientId,
         map -> getOrPutNextLevel(map, sku,
            map2 -> getOrPutNextLevel(map2, warehouseId,
               map3 -> getOrPutAndIncrement(map3, state))));
      assertEquals(6, bigOleMap.get(120).get("BASIC1").get(10).get("MO"));

      getOrPutNextLevel(bigOleMap, clientId,
         map -> getOrPutNextLevel(map, sku,
            map2 -> getOrPutNextLevel(map2, warehouseId,
               map3 -> getOrPutAndIncrement(map3, state, quantity))));
      assertEquals(11, bigOleMap.get(120).get("BASIC1").get(10).get("MO"));
   }

}