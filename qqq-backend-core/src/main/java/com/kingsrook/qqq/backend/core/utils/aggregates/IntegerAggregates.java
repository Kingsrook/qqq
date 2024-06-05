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
 ** Integer version of data aggregator
 *******************************************************************************/
public class IntegerAggregates implements AggregatesInterface<Integer, BigDecimal>
{
   private int        count = 0;
   // private Integer countDistinct;
   private Integer    sum;
   private Integer    min;
   private Integer    max;
   private BigDecimal product;

   private VarianceCalculator varianceCalculator = new VarianceCalculator();



   /*******************************************************************************
    ** Add a new value to this aggregate set
    *******************************************************************************/
   public void add(Integer input)
   {
      if(input == null)
      {
         return;
      }

      BigDecimal inputBD = new BigDecimal(input);

      count++;

      if(sum == null)
      {
         sum = input;
      }
      else
      {
         sum = sum + input;
      }

      if(product == null)
      {
         product = inputBD;
      }
      else
      {
         product = product.multiply(inputBD);
      }

      if(min == null || input < min)
      {
         min = input;
      }

      if(max == null || input > max)
      {
         max = input;
      }

      varianceCalculator.updateVariance(inputBD);
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
   public Integer getSum()
   {
      return (sum);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getMin()
   {
      return (min);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getMax()
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
