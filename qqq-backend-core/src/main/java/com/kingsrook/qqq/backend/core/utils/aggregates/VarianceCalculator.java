/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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
import java.math.RoundingMode;


/*******************************************************************************
 ** see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Welford's_online_algorithm
 **
 *******************************************************************************/
public class VarianceCalculator
{
   private int        n;
   private BigDecimal runningMean = BigDecimal.ZERO;
   private BigDecimal m2          = BigDecimal.ZERO;

   public static int scaleForVarianceCalculations = 4;



   /*******************************************************************************
    **
    *******************************************************************************/
   public void updateVariance(BigDecimal newInput)
   {
      n++;
      BigDecimal delta = newInput.subtract(runningMean);
      runningMean = runningMean.add(delta.divide(new BigDecimal(n), scaleForVarianceCalculations, RoundingMode.HALF_UP));
      BigDecimal delta2 = newInput.subtract(runningMean);
      m2 = m2.add(delta.multiply(delta2));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public BigDecimal getVariance()
   {
      if(n < 2)
      {
         return (null);
      }

      return m2.divide(new BigDecimal(n - 1), scaleForVarianceCalculations, RoundingMode.HALF_UP);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public BigDecimal getVarP()
   {
      if(n < 2)
      {
         return (null);
      }

      return m2.divide(new BigDecimal(n), scaleForVarianceCalculations, RoundingMode.HALF_UP);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public BigDecimal getStandardDeviation()
   {
      BigDecimal variance = getVariance();
      if(variance == null)
      {
         return (null);
      }

      return BigDecimal.valueOf(Math.sqrt(variance.doubleValue())).setScale(scaleForVarianceCalculations, RoundingMode.HALF_UP);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public BigDecimal getStdDevP()
   {
      BigDecimal varP = getVarP();
      if(varP == null)
      {
         return (null);
      }

      return BigDecimal.valueOf(Math.sqrt(varP.doubleValue())).setScale(scaleForVarianceCalculations, RoundingMode.HALF_UP);
   }

}
