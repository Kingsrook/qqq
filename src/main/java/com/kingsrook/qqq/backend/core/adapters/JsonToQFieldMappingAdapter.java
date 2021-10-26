package com.kingsrook.qqq.backend.core.adapters;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QKeyBasedFieldMapping;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 **
 *******************************************************************************/
public class JsonToQFieldMappingAdapter
{

   /*******************************************************************************
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

         QKeyBasedFieldMapping mapping = new QKeyBasedFieldMapping();
         for(String key : jsonObject.keySet())
         {
            mapping.addMapping(key, jsonObject.getString(key));
         }
         return (mapping);
      }
      catch(JSONException je)
      {
         throw (new IllegalArgumentException("Malformed JSON value: " + je.getMessage(), je));
      }
   }

}
