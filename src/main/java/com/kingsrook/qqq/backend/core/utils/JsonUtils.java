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

package com.kingsrook.qqq.backend.core.utils;


import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
 ** Utility class for working with JSON.
 **
 ** See: https://www.baeldung.com/jackson-vs-gson
 **
 *******************************************************************************/
public class JsonUtils
{
   private static final Logger LOG = LogManager.getLogger(JsonUtils.class);



   /*******************************************************************************
    ** Serialize any object into a JSON String.
    **
    ** Internally using jackson - so jackson annotations apply!
    **
    *******************************************************************************/
   public static String toJson(Object object)
   {
      try
      {
         ObjectMapper mapper     = newObjectMapper();
         String       jsonResult = mapper.writeValueAsString(object);
         return (jsonResult);
      }
      catch(JsonProcessingException e)
      {
         LOG.error("Error serializing object of type [" + object.getClass().getSimpleName() + "] to json", e);
         throw new IllegalArgumentException("Error in JSON Serialization", e);
      }
   }



   /*******************************************************************************
    ** Serialize any object into a "pretty" / formatted JSON String.
    **
    ** Internally using jackson - so jackson annotations apply!
    **
    *******************************************************************************/
   public static String toPrettyJson(Object object)
   {
      try
      {
         ObjectMapper mapper       = newObjectMapper();
         ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
         String       jsonResult   = objectWriter.writeValueAsString(object);
         return (jsonResult);
      }
      catch(JsonProcessingException e)
      {
         LOG.error("Error serializing object of type [" + object.getClass().getSimpleName() + "] to json", e);
         throw new IllegalArgumentException("Error in JSON Serialization", e);
      }
   }



   /*******************************************************************************
    ** Serialize any object into a "pretty" / formatted JSON String.
    **
    *******************************************************************************/
   public static String prettyPrint(String json)
   {
      try
      {
         ObjectMapper mapper       = newObjectMapper();
         Object       object       = mapper.reader().readValue(json, Map.class);
         ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
         String       jsonResult   = objectWriter.writeValueAsString(object);
         return (jsonResult);
      }
      catch(Exception e)
      {
         LOG.error("Error pretty printing JSON string", e);
         throw new IllegalArgumentException("Error in pretty printing JSON", e);
      }
   }



   /*******************************************************************************
    ** De-serialize a json string into an object of the specified class.
    **
    ** Internally using jackson - so jackson annotations apply!
    **
    *******************************************************************************/
   public static <T> T toObject(String json, Class<T> targetClass) throws IOException
   {
      ObjectMapper objectMapper = newObjectMapper();
      return objectMapper.reader().readValue(json, targetClass);
   }



   /*******************************************************************************
    ** De-serialize a json string into an object of the specified class.
    **
    ** Internally using jackson - so jackson annotations apply!
    **
    *******************************************************************************/
   public static <T> T toObject(String json, TypeReference<T> typeReference) throws IOException
   {
      ObjectMapper objectMapper = newObjectMapper();
      return objectMapper.readValue(json, typeReference);
   }



   /*******************************************************************************
    ** De-serialize a json string into a JSONObject (string must start with "{")
    **
    *******************************************************************************/
   public static JSONObject toJSONObject(String json) throws JSONException
   {
      JSONObject jsonObject = new JSONObject(json);
      return (jsonObject);
   }



   /*******************************************************************************
    ** De-serialize a json string into a JSONArray (string must start with "[")
    **
    *******************************************************************************/
   public static JSONArray toJSONArray(String json) throws JSONException
   {
      JSONArray jsonArray = new JSONArray(json);
      return (jsonArray);
   }



   /*******************************************************************************
    ** Standard private method to build jackson ObjectMapper with standard features.
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
    ** Check if a string looks like it could be a JSON object (e.g., starts with "{"
    ** (plus optional whitespace))
    **
    *******************************************************************************/
   public static boolean looksLikeObject(String json)
   {
      return (json != null && json.matches("(?s)\\s*\\{.*"));
   }



   /*******************************************************************************
    ** Check if a string looks like it could be a JSON array (e.g., starts with "["
    ** (plus optional whitespace))
    **
    *******************************************************************************/
   public static boolean looksLikeArray(String json)
   {
      return (json != null && json.matches("(?s)\\s*\\[.*"));
   }

}
