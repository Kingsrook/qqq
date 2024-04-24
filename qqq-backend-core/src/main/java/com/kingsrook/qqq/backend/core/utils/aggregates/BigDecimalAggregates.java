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
 ** BigDecimal version of data aggregator
 *******************************************************************************/
public class BigDecimalAggregates implements AggregatesInterface<BigDecimal, BigDecimal>
{
   private int        count = 0;
   // private Integer countDistinct;
   private BigDecimal sum;
   private BigDecimal min;
   private BigDecimal max;
   private BigDecimal product;

   private VarianceCalculator varianceCalculator = new VarianceCalculator();



   /*******************************************************************************
    ** Add a new value to this aggregate set
    *******************************************************************************/
   public void add(BigDecimal input)
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
         sum = sum.add(input);
      }

      if(product == null)
      {
         product = input;
      }
      else
      {
         product = product.multiply(input);
      }

      if(min == null || input.compareTo(min) < 0)
      {
         min = input;
      }

      if(max == null || input.compareTo(max) > 0)
      {
         max = input;
      }

      varianceCalculator.updateVariance(input);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getVariance()
   {
      return (varianceCalculator.getVariance());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getVarP()
   {
      return (varianceCalculator.getVarP());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getStandardDeviation()
   {
      return (varianceCalculator.getStandardDeviation());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getStdDevP()
   {
      return (varianceCalculator.getStdDevP());
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
   public BigDecimal getSum()
   {
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getMin()
   {
      return (min);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public BigDecimal getMax()
   {
      return (max);
   }



   /*******************************************************************************
    ** Getter for product
    **
    *******************************************************************************/
   @Override
   public BigDecimal getProduct()
   {
      return product;
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
