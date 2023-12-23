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


/*******************************************************************************
 ** Long version of data aggregator
 *******************************************************************************/
public class LongAggregates implements AggregatesInterface<Long>
{
   private int  count = 0;
   // private Long countDistinct;
   private Long sum;
   private Long min;
   private Long max;



   /*******************************************************************************
    ** Add a new value to this aggregate set
    *******************************************************************************/
   public void add(Long input)
   {
      if(input == null)
      {
         return;
      }

      count++;

      if(sum == null)
      {
         sum = input;
      }
      else
      {
         sum = sum + input;
      }

      if(min == null || input < min)
      {
         min = input;
      }

      if(max == null || input > max)
      {
         max = input;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public int getCount()
   {
      return (count);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Long getSum()
   {
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Long getMin()
   {
      return (min);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Long getMax()
   {
      return (max);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getAverage()
   {
      if(this.count > 0)
      {
         return (BigDecimal.valueOf(this.sum.doubleValue() / (double) this.count));
      }
      else
      {
         return (null);
      }
   }

}
