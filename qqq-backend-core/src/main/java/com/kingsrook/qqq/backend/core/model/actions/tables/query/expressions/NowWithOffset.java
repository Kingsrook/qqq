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

package com.kingsrook.qqq.backend.core.model.actions.tables.query.expressions;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;


/*******************************************************************************
 **
 *******************************************************************************/
public class NowWithOffset extends AbstractFilterExpression<Instant>
{
   private Operator   operator;
   private int        amount;
   private ChronoUnit timeUnit;



   public enum Operator
   {PLUS, MINUS}



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public NowWithOffset()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private NowWithOffset(Operator operator, int amount, ChronoUnit timeUnit)
   {
      this.operator = operator;
      this.amount = amount;
      this.timeUnit = timeUnit;
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   @Deprecated
   public static NowWithOffset minus(int amount, TimeUnit timeUnit)
   {
      return (minus(amount, timeUnit.toChronoUnit()));
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   public static NowWithOffset minus(int amount, ChronoUnit timeUnit)
   {
      return (new NowWithOffset(Operator.MINUS, amount, timeUnit));
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   @Deprecated
   public static NowWithOffset plus(int amount, TimeUnit timeUnit)
   {
      return (plus(amount, timeUnit.toChronoUnit()));
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   public static NowWithOffset plus(int amount, ChronoUnit timeUnit)
   {
      return (new NowWithOffset(Operator.PLUS, amount, timeUnit));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Instant evaluate()
   {
      /////////////////////////////////////////////////////////////////////////////
      // Instant doesn't let us plus/minus WEEK, MONTH, or YEAR...               //
      // but LocalDateTime does.  So, make a LDT in UTC, do the plus/minus, then //
      // convert back to Instant @ UTC                                           //
      /////////////////////////////////////////////////////////////////////////////
      LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));

      LocalDateTime then;
      if(operator.equals(Operator.PLUS))
      {
         then = now.plus(amount, timeUnit);
      }
      else
      {
         then = now.minus(amount, timeUnit);
      }

      return (then.toInstant(ZoneOffset.UTC));
   }



   /*******************************************************************************
    ** Getter for operator
    **
    *******************************************************************************/
   public Operator getOperator()
   {
      return operator;
   }



   /*******************************************************************************
    ** Getter for amount
    **
    *******************************************************************************/
   public int getAmount()
   {
      return amount;
   }



   /*******************************************************************************
    ** Getter for timeUnit
    **
    *******************************************************************************/
   public ChronoUnit getTimeUnit()
   {
      return timeUnit;
   }
}
