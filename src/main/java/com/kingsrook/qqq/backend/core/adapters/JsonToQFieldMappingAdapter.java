package com.kingsrook.qqq.backend.core.adapters;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQFieldMapping;
import com.kingsrook.qqq.backend.core.model.actions.QIndexBasedFieldMapping;
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
