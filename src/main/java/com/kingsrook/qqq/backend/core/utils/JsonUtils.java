package com.kingsrook.qqq.backend.core.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** See: https://www.baeldung.com/jackson-vs-gson
 **
 *******************************************************************************/
public class JsonUtils
{
   private static final Logger LOG = LogManager.getLogger(JsonUtils.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String toJson(Object object)
   {
      try
      {
         ObjectMapper mapper = newObjectMapper();
         String jsonResult = mapper.writeValueAsString(object);
         return (jsonResult);
      }
      catch(JsonProcessingException e)
      {
         LOG.error("Error serializing object of type [" + object.getClass().getSimpleName() + "] to json", e);
         throw new IllegalArgumentException("Error in JSON Serialization", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String toPrettyJson(Object object)
   {
      try
      {
         ObjectMapper mapper = newObjectMapper();
         ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
         String jsonResult = objectWriter.writeValueAsString(object);
         return (jsonResult);
      }
      catch(JsonProcessingException e)
      {
         LOG.error("Error serializing object of type [" + object.getClass().getSimpleName() + "] to json", e);
         throw new IllegalArgumentException("Error in JSON Serialization", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static JSONObject toJSONObject(String json) throws JSONException
   {
      JSONObject jsonObject = new JSONObject(json);
      return (jsonObject);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static JSONArray toJSONArray(String json) throws JSONException
   {
      JSONArray jsonArray = new JSONArray(json);
      return (jsonArray);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ObjectMapper newObjectMapper()
   {
      ObjectMapper mapper = new ObjectMapper()
         .registerModule(new JavaTimeModule())
         .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

      /* todo - some future version we may need to do inclusion/exclusion lists like this:
      // this is what we'd put on the class or member we wanted to 'filter':  @JsonFilter("secretsFilter")

      SimpleFilterProvider filterProvider = new SimpleFilterProvider();
      filterProvider.addFilter("secretsFilter", SimpleBeanPropertyFilter.serializeAllExcept("password"));
      mapper.setFilterProvider(filterProvider);
       */

      return (mapper);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static boolean looksLikeObject(String json)
   {
      return (json != null && json.matches("(?s)\\s*\\{.*"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static boolean looksLikeArray(String json)
   {
      return (json != null && json.matches("(?s)\\s*\\[.*"));
   }
}
