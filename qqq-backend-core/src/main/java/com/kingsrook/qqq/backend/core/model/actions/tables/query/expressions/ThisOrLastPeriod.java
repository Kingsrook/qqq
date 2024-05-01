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


import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ThisOrLastPeriod extends AbstractFilterExpression<Instant>
{
   private Operator   operator;
   private ChronoUnit timeUnit;



   public enum Operator
   {THIS, LAST}



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ThisOrLastPeriod()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   private ThisOrLastPeriod(Operator operator, ChronoUnit timeUnit)
   {
      this.operator = operator;
      this.timeUnit = timeUnit;
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   public static ThisOrLastPeriod this_(ChronoUnit timeUnit)
   {
      return (new ThisOrLastPeriod(Operator.THIS, timeUnit));
   }



   /*******************************************************************************
    ** Factory
    **
    *******************************************************************************/
   public static ThisOrLastPeriod last(int amount, ChronoUnit timeUnit)
   {
      return (new ThisOrLastPeriod(Operator.LAST, timeUnit));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Instant evaluate() throws QException
   {
      ZoneId zoneId = ValueUtils.getSessionOrInstanceZoneId();

      switch(timeUnit)
      {
         case HOURS ->
         {
            if(operator.equals(Operator.THIS))
            {
               return Instant.now().truncatedTo(ChronoUnit.HOURS);
            }
            else
            {
               return Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
            }
         }
         case DAYS ->
         {
            Instant startOfToday = ValueUtils.getStartOfTodayInZoneId(zoneId.getId());
            return operator.equals(Operator.THIS) ? startOfToday : startOfToday.minus(1, ChronoUnit.DAYS);
         }
         case WEEKS ->
         {
            Instant       startOfToday       = ValueUtils.getStartOfTodayInZoneId(zoneId.getId());
            LocalDateTime startOfThisWeekLDT = LocalDateTime.ofInstant(startOfToday, zoneId);
            while(startOfThisWeekLDT.get(ChronoField.DAY_OF_WEEK) != DayOfWeek.SUNDAY.getValue())
            {
               ////////////////////////////////////////
               // go backwards until sunday is found //
               ////////////////////////////////////////
               startOfThisWeekLDT = startOfThisWeekLDT.minus(1, ChronoUnit.DAYS);
            }
            Instant startOfThisWeek = startOfThisWeekLDT.toInstant(zoneId.getRules().getOffset(startOfThisWeekLDT));

            return operator.equals(Operator.THIS) ? startOfThisWeek : startOfThisWeek.minus(7, ChronoUnit.DAYS);
         }
         case MONTHS ->
         {
            Instant       startOfThisMonth    = ValueUtils.getStartOfMonthInZoneId(zoneId.getId());
            LocalDateTime startOfThisMonthLDT = LocalDateTime.ofInstant(startOfThisMonth, ZoneId.of(zoneId.getId()));
            LocalDateTime startOfLastMonthLDT = startOfThisMonthLDT.minus(1, ChronoUnit.MONTHS);
            Instant       startOfLastMonth    = startOfLastMonthLDT.toInstant(ZoneId.of(zoneId.getId()).getRules().getOffset(Instant.now()));

            return operator.equals(Operator.THIS) ? startOfThisMonth : startOfLastMonth;
         }
         case YEARS ->
         {
            Instant       startOfThisYear    = ValueUtils.getStartOfYearInZoneId(zoneId.getId());
            LocalDateTime startOfThisYearLDT = LocalDateTime.ofInstant(startOfThisYear, zoneId);
            LocalDateTime startOfLastYearLDT = startOfThisYearLDT.minus(1, ChronoUnit.YEARS);
            Instant       startOfLastYear    = startOfLastYearLDT.toInstant(zoneId.getRules().getOffset(Instant.now()));

            return operator.equals(Operator.THIS) ? startOfThisYear : startOfLastYear;
         }
         default -> throw (new QRuntimeException("Unsupported timeUnit: " + timeUnit));
      }
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
    ** Getter for timeUnit
    **
    *******************************************************************************/
   public ChronoUnit getTimeUnit()
   {
      return timeUnit;
   }
}
