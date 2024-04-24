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


import java.math.BigInteger;
import java.time.Instant;


/*******************************************************************************
 ** Instant version of data aggregator
 *******************************************************************************/
public class InstantAggregates implements AggregatesInterface<Instant, Instant>
{
   private int     count = 0;
   // private Integer countDistinct;

   private BigInteger sumMillis = BigInteger.ZERO;

   private Instant min;
   private Instant max;



   /*******************************************************************************
    ** Add a new value to this aggregate set
    *******************************************************************************/
   public void add(Instant input)
   {
      if(input == null)
      {
         return;
      }

      count++;

      sumMillis = sumMillis.add(new BigInteger(String.valueOf(input.toEpochMilli())));

      if(min == null || input.compareTo(min) < 0)
      {
         min = input;
      }

      if(max == null || input.compareTo(max) > 0)
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
   public Instant getSum()
   {
      //////////////////////////////////////////
      // sum of date-times doesn't make sense //
      //////////////////////////////////////////
      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Instant getMin()
   {
      return (min);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Instant getMax()
   {
      return (max);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Instant getAverage()
   {
      if(this.count > 0)
      {
         BigInteger averageMillis = this.sumMillis.divide(new BigInteger(String.valueOf(count)));
         if(averageMillis.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) < 0)
         {
            return (Instant.ofEpochMilli(averageMillis.longValue()));
         }
      }

      return (null);
   }

}
