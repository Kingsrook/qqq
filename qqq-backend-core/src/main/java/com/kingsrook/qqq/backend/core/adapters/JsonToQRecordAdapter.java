/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.adapters;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** Adapter class to convert a JSON string into a list of QRecords.
 **
 *******************************************************************************/
public class JsonToQRecordAdapter
{

   /*******************************************************************************
    ** convert a JSON String into a List of QRecords, for a given table, optionally
    ** using a given mapping.
    **
    ** todo - meta-data validation, use the mapping, type handling
    *******************************************************************************/
   public List<QRecord> buildRecordsFromJson(String json, QTableMetaData table, AbstractQFieldMapping<?> mapping)
   {
      if(!StringUtils.hasContent(json))
      {
         throw (new IllegalArgumentException("Empty json value was provided."));
      }

      List<QRecord> rs = new ArrayList<>();
      try
      {
         if(JsonUtils.looksLikeObject(json))
         {
            JSONObject jsonObject = JsonUtils.toJSONObject(json);
            rs.add(buildRecordFromJsonObject(jsonObject, table, mapping));
         }
         else if(JsonUtils.looksLikeArray(json))
         {
            JSONArray jsonArray = JsonUtils.toJSONArray(json);
            for(Object object : jsonArray)
            {
               if(object instanceof JSONObject jsonObject)
               {
                  rs.add(buildRecordFromJsonObject(jsonObject, table, mapping));
               }
               else
               {
                  throw (new IllegalArgumentException("Element at index " + rs.size() + " in json array was not a json object."));
               }
            }
         }
         else
         {
            throw (new IllegalArgumentException("Malformed JSON value - did not start with '{' or '['."));
         }
      }
      catch(JSONException je)
      {
         throw (new IllegalArgumentException("Malformed JSON value: " + je.getMessage(), je));
      }

      return (rs);
   }



   /*******************************************************************************
    ** private method to build one QRecord from one jsonObject
    **
    ** todo - meta-data validation, type handling
    *******************************************************************************/
   private QRecord buildRecordFromJsonObject(JSONObject jsonObject, QTableMetaData table, AbstractQFieldMapping<?> mapping)
   {
      QRecord record = new QRecord();

      if(table == null)
      {
         jsonObject.keys().forEachRemaining(key ->
         {
            record.setValue(key, jsonObject.optString(key));
         });
      }
      else
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            String fieldSource = mapping == null ? field.getName() : String.valueOf(mapping.getFieldSource(field.getName()));
            // todo - so if the mapping didn't say how to map this field, does that mean we should use the default name for the field?
            if(jsonObject.has(fieldSource))
            {
               record.setValue(field.getName(), (Serializable) jsonObject.get(fieldSource));
            }
         }
      }

      return (record);
   }

}
