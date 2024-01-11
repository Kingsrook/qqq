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

package com.kingsrook.qqq.backend.module.filesystem.processes.implementations.filesystem.importer;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPostQueryCustomizer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** combine all unstructured fields of the record into a JSON blob in the "values" field.
 *******************************************************************************/
public class ImportRecordPostQueryCustomizer extends AbstractPostQueryCustomizer
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public List<QRecord> apply(List<QRecord> records)
   {
      if(CollectionUtils.nullSafeHasContents(records))
      {
         QTableMetaData table = null;
         if(StringUtils.hasContent(records.get(0).getTableName()))
         {
            table = QContext.getQInstance().getTable(records.get(0).getTableName());
         }

         for(QRecord record : records)
         {
            Map<String, Serializable> values = record.getValues();

            if(table != null)
            {
               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // remove known values from a clone of the values map - then only put the un-structured values in a JSON document in the values field //
               ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               values = new HashMap<>(values);
               for(String fieldName : table.getFields().keySet())
               {
                  values.remove(fieldName);
               }
            }

            String valuesJson = JsonUtils.toJson(values);
            record.setValue("values", valuesJson);
         }
      }

      return (records);
   }

}
