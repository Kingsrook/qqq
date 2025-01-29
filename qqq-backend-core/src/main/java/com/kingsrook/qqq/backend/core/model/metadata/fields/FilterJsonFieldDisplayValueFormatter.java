/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields;


import java.util.List;
import java.util.function.Consumer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kingsrook.qqq.backend.core.actions.values.ValueBehaviorApplier;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Display value formatter for fields which store a QQueryFilter as JSON.
 *******************************************************************************/
public class FilterJsonFieldDisplayValueFormatter implements FieldDisplayBehavior<FilterJsonFieldDisplayValueFormatter>
{
   private static Consumer<ObjectMapper> jsonMapperCustomizer = om -> om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void apply(ValueBehaviorApplier.Action action, List<QRecord> recordList, QInstance instance, QTableMetaData table, QFieldMetaData field)
   {
      for(QRecord record : CollectionUtils.nonNullList(recordList))
      {
         String queryFilterJson = record.getValueString(field.getName());
         if(StringUtils.hasContent(queryFilterJson))
         {
            try
            {
               QQueryFilter qQueryFilter  = JsonUtils.toObject(queryFilterJson, QQueryFilter.class, jsonMapperCustomizer);
               int          criteriaCount = CollectionUtils.nonNullList(qQueryFilter.getCriteria()).size();
               record.setDisplayValue(field.getName(), criteriaCount + " Filter" + StringUtils.plural(criteriaCount));
            }
            catch(Exception e)
            {
               record.setDisplayValue(field.getName(), "Invalid Filter...");
            }
         }
      }
   }

}
