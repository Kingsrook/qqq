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


import java.io.Serializable;
import java.math.BigDecimal;


/*******************************************************************************
 ** Classes that support doing data aggregations (e.g., count, sum, min, max, average).
 ** Sub-classes should supply the type parameter.
 **
 ** The AVG_T parameter describes the type used for the average getAverage method
 ** which, e.g, for date types, might be a date, vs. numbers, they'd probably be
 ** BigDecimal.
 *******************************************************************************/
public interface AggregatesInterface<T extends Serializable, AVG_T extends Serializable>
{
   /*******************************************************************************
    **
    *******************************************************************************/
   void add(T t);

   /*******************************************************************************
    **
    *******************************************************************************/
   int getCount();

   /*******************************************************************************
    **
    *******************************************************************************/
   T getSum();

   /*******************************************************************************
    **
    *******************************************************************************/
   T getMin();

   /*******************************************************************************
    **
    *******************************************************************************/
   T getMax();

   /*******************************************************************************
    **
    *******************************************************************************/
   AVG_T getAverage();


   /*******************************************************************************
    **
    *******************************************************************************/
   default BigDecimal getProduct()
   {
      return (null);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default BigDecimal getVariance()
   {
      return (null);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default BigDecimal getVarP()
   {
      return (null);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default BigDecimal getStandardDeviation()
   {
      return (null);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   default BigDecimal getStdDevP()
   {
      return (null);
   }

}
