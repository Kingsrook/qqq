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

package com.kingsrook.qqq.backend.core.model.common;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Predicate;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;


/*******************************************************************************
 **
 *******************************************************************************/
public class TimeZonePossibleValueSourceMetaDataProvider
{
   public static final String NAME = "timeZones";



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource produce()
   {
      return (produce(null, null, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource produce(Predicate<String> filter, Function<String, String> labelMapper)
   {
      return (produce(filter, labelMapper, null));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource produce(Predicate<String> filter, Function<String, String> labelMapper, Comparator<QPossibleValue<?>> comparator)
   {
      QPossibleValueSource possibleValueSource = new QPossibleValueSource()
         .withName("timeZones")
         .withType(QPossibleValueSourceType.ENUM)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY);

      List<QPossibleValue<?>> enumValues = new ArrayList<>();
      for(String availableID : TimeZone.getAvailableIDs())
      {
         if(filter == null || filter.test(availableID))
         {
            String label = labelMapper == null ? availableID : labelMapper.apply(availableID);
            enumValues.add(new QPossibleValue<>(availableID, label));
         }
      }

      if(comparator != null)
      {
         enumValues.sort(comparator);
      }

      possibleValueSource.withEnumValues(enumValues);
      return (possibleValueSource);
   }
}
