/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
