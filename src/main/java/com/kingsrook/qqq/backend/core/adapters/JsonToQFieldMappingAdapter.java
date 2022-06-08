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


import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QIndexBasedFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.shared.mapping.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** Adapter class to convert a JSON string into a QFieldMapping object
 **
 *******************************************************************************/
public class JsonToQFieldMappingAdapter
{

   /*******************************************************************************
    ** adapts a json string into an AbstractQFieldMapping.
    **
    ** The mapping will be a QKeyBasedFieldMapping if the keys in the json object are
    ** Strings.  It will be a QIndexBasedFieldMapping if the keys are integers.
    **
    *******************************************************************************/
   public AbstractQFieldMapping<?> buildMappingFromJson(String json)
   {
      if(!StringUtils.hasContent(json))
      {
         throw (new IllegalArgumentException("Empty json value was provided."));
      }

      try
      {
         JSONObject jsonObject = JsonUtils.toJSONObject(json);

         //////////////////////////////////////////////////////////////////////////////////////////////
         // look at the keys in the mapping - if they're strings, then we're doing key-based mapping //
         // if they're numbers, then we're doing index based -- and if they're a mix, that's illegal //
         //////////////////////////////////////////////////////////////////////////////////////////////
         AbstractQFieldMapping.SourceType sourceType = determineSourceType(jsonObject);

         @SuppressWarnings("rawtypes")
         AbstractQFieldMapping mapping = null;

         switch(sourceType)
         {
            case KEY:
            {
               mapping = new QKeyBasedFieldMapping();
               for(String fieldName : jsonObject.keySet())
               {
                  ((QKeyBasedFieldMapping) mapping).addMapping(fieldName, jsonObject.getString(fieldName));
               }
               break;
            }
            case INDEX:
            {
               mapping = new QIndexBasedFieldMapping();
               for(String fieldName : jsonObject.keySet())
               {
                  ((QIndexBasedFieldMapping) mapping).addMapping(fieldName, jsonObject.getInt(fieldName));
               }
               break;
            }
            default:
            {
               throw (new IllegalArgumentException("Unsupported sourceType: " + sourceType));
            }
         }

         return (mapping);
      }
      catch(JSONException je)
      {
         throw (new IllegalArgumentException("Malformed JSON value: " + je.getMessage(), je));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private AbstractQFieldMapping.SourceType determineSourceType(JSONObject jsonObject)
   {
      for(String fieldName : jsonObject.keySet())
      {
         Object sourceObject = jsonObject.get(fieldName);
         if(sourceObject instanceof String)
         {
            return (AbstractQFieldMapping.SourceType.KEY);
         }
         else if(sourceObject instanceof Integer)
         {
            return (AbstractQFieldMapping.SourceType.INDEX);
         }
         else
         {
            throw new IllegalArgumentException("Source object is unsupported type: " + sourceObject.getClass().getSimpleName());
         }
      }
      throw new IllegalArgumentException("No fields were found in the mapping.");
   }

}
