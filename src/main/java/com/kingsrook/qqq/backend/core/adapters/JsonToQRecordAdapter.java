package com.kingsrook.qqq.backend.core.adapters;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
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
            rs.add(buildRecordFromJsonObject(jsonObject));
         }
         else if(JsonUtils.looksLikeArray(json))
         {
            JSONArray jsonArray = JsonUtils.toJSONArray(json);
            for(Object object : jsonArray)
            {
               if(object instanceof JSONObject jsonObject)
               {
                  rs.add(buildRecordFromJsonObject(jsonObject));
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
    ** todo - meta-data validation, mapping, type handling
    *******************************************************************************/
   private QRecord buildRecordFromJsonObject(JSONObject jsonObject)
   {
      QRecord record = new QRecord();

      for(String key : jsonObject.keySet())
      {
         record.setValue(key, (Serializable) jsonObject.get(key));
      }

      return (record);
   }

}
