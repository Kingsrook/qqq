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


import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.json.JSONObject;


/*******************************************************************************
 ** Methods for adapting qInstances to serialized (string) formats (e.g., json),
 ** and vice versa.
 *******************************************************************************/
public class QInstanceAdapter
{

   /*******************************************************************************
    ** Convert a qInstance to JSON.
    **
    *******************************************************************************/
   public String qInstanceToJson(QInstance qInstance)
   {
      return (JsonUtils.toJson(qInstance));
   }



   /*******************************************************************************
    ** Convert a qInstance to JSON.
    **
    *******************************************************************************/
   public String qInstanceToJsonIncludingBackend(QInstance qInstance)
   {
      String jsonString = JsonUtils.toJson(qInstance);
      JSONObject jsonObject = JsonUtils.toJSONObject(jsonString);

      String backendsJsonString = JsonUtils.toJson(qInstance.getBackends());
      JSONObject backendsJsonObject = JsonUtils.toJSONObject(backendsJsonString);
      jsonObject.put("backends", backendsJsonObject);

      return (jsonObject.toString());
   }



   /*******************************************************************************
    ** Build a qInstance from JSON.
    **
    *******************************************************************************/
   public QInstance jsonToQInstance(String json) throws IOException
   {
      return (JsonUtils.toObject(json, QInstance.class));
   }



   /*******************************************************************************
    ** Build a qInstance from JSON.
    **
    *******************************************************************************/
   public QInstance jsonToQInstanceIncludingBackends(String json) throws IOException
   {
      QInstance qInstance = JsonUtils.toObject(json, QInstance.class);
      JSONObject jsonObject = JsonUtils.toJSONObject(json);
      JSONObject backendsJsonObject = jsonObject.getJSONObject("backends");
      Map<String, QBackendMetaData> backends = JsonUtils.toObject(backendsJsonObject.toString(), new TypeReference<>()
      {
      });
      qInstance.setBackends(backends);
      return qInstance;
   }

}
