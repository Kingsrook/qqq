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

package com.kingsrook.qqq.backend.core.utils.aggregates;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for Aggregates
 *******************************************************************************/
class AggregatesTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInteger()
   {
      IntegerAggregates aggregates = new IntegerAggregates();

      assertEquals(0, aggregates.getCount());
      assertNull(aggregates.getMin());
      assertNull(aggregates.getMax());
      assertNull(aggregates.getSum());
      assertNull(aggregates.getAverage());

      aggregates.add(5);
      assertEquals(1, aggregates.getCount());
      assertEquals(5, aggregates.getMin());
      assertEquals(5, aggregates.getMax());
      assertEquals(5, aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("5"), Offset.offset(BigDecimal.ZERO));

      aggregates.add(10);
      assertEquals(2, aggregates.getCount());
      assertEquals(5, aggregates.getMin());
      assertEquals(10, aggregates.getMax());
      assertEquals(15, aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("7.5"), Offset.offset(BigDecimal.ZERO));

      aggregates.add(15);
      assertEquals(3, aggregates.getCount());
      assertEquals(5, aggregates.getMin());
      assertEquals(15, aggregates.getMax());
      assertEquals(30, aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("10"), Offset.offset(BigDecimal.ZERO));

      aggregates.add(null);
      assertEquals(3, aggregates.getCount());
      assertEquals(5, aggregates.getMin());
      assertEquals(15, aggregates.getMax());
      assertEquals(30, aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("10"), Offset.offset(BigDecimal.ZERO));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBigDecimal()
   {
      BigDecimalAggregates aggregates = new BigDecimalAggregates();

      assertEquals(0, aggregates.getCount());
      assertNull(aggregates.getMin());
      assertNull(aggregates.getMax());
      assertNull(aggregates.getSum());
      assertNull(aggregates.getAverage());

      BigDecimal bd51 = new BigDecimal("5.1");
      aggregates.add(bd51);
      assertEquals(1, aggregates.getCount());
      assertEquals(bd51, aggregates.getMin());
      assertEquals(bd51, aggregates.getMax());
      assertEquals(bd51, aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(bd51, Offset.offset(BigDecimal.ZERO));

      BigDecimal bd101 = new BigDecimal("10.1");
      aggregates.add(new BigDecimal("10.1"));
      assertEquals(2, aggregates.getCount());
      assertEquals(bd51, aggregates.getMin());
      assertEquals(bd101, aggregates.getMax());
      assertEquals(new BigDecimal("15.2"), aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("7.6"), Offset.offset(BigDecimal.ZERO));

      BigDecimal bd148 = new BigDecimal("14.8");
      aggregates.add(bd148);

      aggregates.add(null);
      assertEquals(3, aggregates.getCount());
      assertEquals(bd51, aggregates.getMin());
      assertEquals(bd148, aggregates.getMax());
      assertEquals(new BigDecimal("30.0"), aggregates.getSum());
      assertThat(aggregates.getAverage()).isCloseTo(new BigDecimal("10.0"), Offset.offset(BigDecimal.ZERO));
   }

}