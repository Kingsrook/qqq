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

package com.kingsrook.qqq.backend.core.utils.collections;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for TypeTolerantKeyMap 
 *******************************************************************************/
class TypeTolerantKeyMapTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      TypeTolerantKeyMap<QRecord> map = new TypeTolerantKeyMap<>(QFieldType.INTEGER);
      map.put(1, new QRecord().withValue("id", 1));
      map.put("2", new QRecord().withValue("id", 2));
      map.put(3.0, new QRecord().withValue("id", 3));
      map.put(new BigDecimal("4.00"), new QRecord().withValue("id", 4));

      for(int i=1; i<=4; i++)
      {
         assertTrue(map.containsKey(i));
         assertEquals(i, map.get(i).getValueInteger("id"));
      }
   }

}