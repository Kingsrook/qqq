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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.mapping;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model.BulkInsertMapping;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ValueMapper
{
   private static final QLogger LOG = QLogger.getLogger(ValueMapper.class);



   /***************************************************************************
    **
    ***************************************************************************/
   public static void valueMapping(List<QRecord> records, BulkInsertMapping mapping, QTableMetaData table) throws QException
   {
      valueMapping(records, mapping, table, null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void valueMapping(List<QRecord> records, BulkInsertMapping mapping, QTableMetaData table, String associationNameChain) throws QException
   {
      if(CollectionUtils.nullSafeIsEmpty(records))
      {
         return;
      }

      Map<String, Map<String, Serializable>> mappingForTable = mapping.getFieldNameToValueMappingForTable(associationNameChain);
      for(QRecord record : records)
      {
         for(Map.Entry<String, Serializable> valueEntry : record.getValues().entrySet())
         {
            QFieldMetaData field = table.getField(valueEntry.getKey());
            Serializable   value = valueEntry.getValue();

            ///////////////////
            // value mappin' //
            ///////////////////
            if(mappingForTable.containsKey(field.getName()) && value != null)
            {
               Serializable mappedValue = mappingForTable.get(field.getName()).get(ValueUtils.getValueAsString(value));
               if(mappedValue != null)
               {
                  value = mappedValue;
               }
            }

            /////////////////////
            // type convertin' //
            /////////////////////
            if(value != null)
            {
               QFieldType type = field.getType();
               try
               {
                  value = ValueUtils.getValueAsFieldType(type, value);
               }
               catch(Exception e)
               {
                  record.addError(new BadInputStatusMessage("Value [" + value + "] for field [" + field.getLabel() + "] could not be converted to type [" + type + "]"));
               }
            }

            record.setValue(field.getName(), value);
         }

         //////////////////////////////////////
         // recursively process associations //
         //////////////////////////////////////
         for(Map.Entry<String, List<QRecord>> entry : record.getAssociatedRecords().entrySet())
         {
            String                associationName = entry.getKey();
            Optional<Association> association     = table.getAssociations().stream().filter(a -> a.getName().equals(associationName)).findFirst();
            if(association.isPresent())
            {
               QTableMetaData associatedTable = QContext.getQInstance().getTable(association.get().getAssociatedTableName());
               valueMapping(entry.getValue(), mapping, associatedTable, StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + associationName : associationName);
            }
            else
            {
               throw new QException("Missing association [" + associationName + "] on table [" + table.getName() + "]");
            }
         }
      }
   }

}
