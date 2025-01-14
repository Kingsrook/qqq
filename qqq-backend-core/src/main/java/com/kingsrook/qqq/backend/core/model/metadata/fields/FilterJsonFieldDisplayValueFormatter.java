/*
 * Copyright © 2022-2024. ColdTrack <contact@coldtrack.com>.  All Rights Reserved.
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
