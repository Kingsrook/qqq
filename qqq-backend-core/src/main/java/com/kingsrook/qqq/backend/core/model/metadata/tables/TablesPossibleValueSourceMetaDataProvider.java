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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TablesPossibleValueSourceMetaDataProvider
{
   public static final String NAME = "tables";



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QPossibleValueSource defineTablesPossibleValueSource(QInstance qInstance)
   {
      QPossibleValueSource possibleValueSource = new QPossibleValueSource()
         .withName(NAME)
         .withType(QPossibleValueSourceType.ENUM)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY);

      List<QPossibleValue<?>> enumValues = new ArrayList<>();
      for(QTableMetaData table : qInstance.getTables().values())
      {
         if(BooleanUtils.isNotTrue(table.getIsHidden()))
         {
            String label = StringUtils.hasContent(table.getLabel()) ? table.getLabel() : QInstanceEnricher.nameToLabel(table.getName());
            enumValues.add(new QPossibleValue<>(table.getName(), label));
         }
      }

      enumValues.sort(Comparator.comparing(QPossibleValue::getLabel));

      possibleValueSource.withEnumValues(enumValues);
      return (possibleValueSource);
   }

}
