/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.common;


import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;


/*******************************************************************************
 ** Meta Data Producer for DayOfWeek.
 *******************************************************************************/
public class DayOfWeekPossibleValueSourceMetaDataProducer extends MetaDataProducer<QPossibleValueSource>
{
   public static final String NAME = "DayOfWeek";



   /*******************************************************************************
    ** Produces and returns a {@link QPossibleValueSource} containing the seven days
    * of the week as enum values, ordered starting from the locale's first day of the week.
    *******************************************************************************/
   @Override
   public QPossibleValueSource produce(QInstance qInstance)
   {
      DayOfWeek firstDay = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();

      List<QPossibleValue<?>> possibleValues = new ArrayList<>();
      DayOfWeek current = firstDay;
      for (int i = 0; i < 7; i++)
      {
         possibleValues.add(getPossibleValue(current));
         current = current.plus(1);
      }

      return (new QPossibleValueSource()
         .withType(QPossibleValueSourceType.ENUM)
         .withName(NAME)
         .withEnumValues(possibleValues)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY));
   }



   /*******************************************************************************
    ** Maps the given {@link java.time.DayOfWeek} to a {@link QPossibleValue} using
    * its ISO-8601 integer value and locale-formatted display name.
    *******************************************************************************/
   private QPossibleValue<Integer> getPossibleValue(DayOfWeek dayOfWeek)
   {
      return new QPossibleValue<>(dayOfWeek.getValue(), dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()));
   }

}
