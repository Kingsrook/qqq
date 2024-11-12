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
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ValueMapper
{
   /***************************************************************************
    **
    ***************************************************************************/
   public static void valueMapping(List<QRecord> records, BulkInsertMapping mapping)
   {
      valueMapping(records, mapping, null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void valueMapping(List<QRecord> records, BulkInsertMapping mapping, String associationNameChain)
   {
      if(CollectionUtils.nullSafeIsEmpty(records))
      {
         return;
      }

      Map<String, Map<String, Serializable>> mappingForTable = mapping.getFieldNameToValueMappingForTable(associationNameChain);
      for(QRecord record : records)
      {
         for(Map.Entry<String, Map<String, Serializable>> entry : mappingForTable.entrySet())
         {
            String                    fieldName = entry.getKey();
            Map<String, Serializable> map       = entry.getValue();
            String                    value     = record.getValueString(fieldName);
            if(value != null && map.containsKey(value))
            {
               record.setValue(fieldName, map.get(value));
            }
         }

         for(Map.Entry<String, List<QRecord>> entry : record.getAssociatedRecords().entrySet())
         {
            valueMapping(entry.getValue(), mapping, StringUtils.hasContent(associationNameChain) ? associationNameChain + "." + entry.getKey() : entry.getKey());
         }
      }
   }

}
