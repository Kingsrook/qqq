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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.QTableMetaDataLoader;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.utils.ClassPathUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.YamlUtils;
import org.apache.commons.io.IOUtils;
import static com.kingsrook.qqq.backend.core.utils.ValueUtils.getValueAsBoolean;
import static com.kingsrook.qqq.backend.core.utils.ValueUtils.getValueAsInteger;
import static com.kingsrook.qqq.backend.core.utils.ValueUtils.getValueAsString;


/*******************************************************************************
 ** Abstract base class in hierarchy of classes that know how to construct &
 ** populate QMetaDataObject instances, based on input streams (e.g., from files).
 *******************************************************************************/
public abstract class AbstractMetaDataLoader<T extends QMetaDataObject>
{
   private static final QLogger LOG = QLogger.getLogger(AbstractMetaDataLoader.class);

   private static Map<Class<?>, Class<? extends AbstractMetaDataLoader<?>>> registeredLoaders = new HashMap<>();

   static
   {
      try
      {
         List<Class<?>> classesInPackage = ClassPathUtils.getClassesInPackage(QTableMetaDataLoader.class.getPackageName());
         for(Class<?> loaderClass : classesInPackage)
         {
            Type superClass = loaderClass.getGenericSuperclass();
            if(superClass.getTypeName().startsWith(AbstractMetaDataLoader.class.getName() + "<"))
            // if(superClass instanceof Class<?> c && AbstractMetaDataLoader.class.isAssignableFrom(c))
            {
               Type     actualTypeArgument = ((ParameterizedType) superClass).getActualTypeArguments()[0];
               Class<?> metaDataObjectType = Class.forName(actualTypeArgument.getTypeName());
               registeredLoaders.put(metaDataObjectType, (Class<? extends AbstractMetaDataLoader<?>>) loaderClass);
            }
         }

         System.out.println("Registered loaders: " + registeredLoaders);
      }
      catch(Exception e)
      {
         LOG.error("Error in static init block for AbstractMetaDataLoader", e);
      }
   }

   private String fileName;



   /***************************************************************************
    **
    ***************************************************************************/
   public T fileToMetaDataObject(QInstance qInstance, InputStream inputStream, String fileName) throws QMetaDataLoaderException
   {
      this.fileName = fileName;
      Map<String, Object> map = fileToMap(inputStream, fileName);
      return (mapToMetaDataObject(qInstance, map));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public abstract T mapToMetaDataObject(QInstance qInstance, Map<String, Object> map) throws QMetaDataLoaderException;



   /***************************************************************************
    **
    ***************************************************************************/
   protected Map<String, Object> fileToMap(InputStream inputStream, String fileName) throws QMetaDataLoaderException
   {
      try
      {
         String string = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
         string = StringUtils.ltrim(string);
         if(fileName.toLowerCase().endsWith(".json"))
         {
            return JsonUtils.toObject(string, new TypeReference<>() {});
         }
         else if(fileName.toLowerCase().endsWith(".yaml") || fileName.toLowerCase().endsWith(".yml"))
         {
            return YamlUtils.toMap(string);
         }

         throw (new QMetaDataLoaderException("Unsupported file format (based on file name: " + fileName + ")"));
      }
      catch(IOException e)
      {
         throw new QMetaDataLoaderException("Error building map from file: " + fileName, e);
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected void reflectivelyMap(QInstance qInstance, QMetaDataObject targetObject, Map<String, Object> map)
   {
      Class<? extends QMetaDataObject> targetClass = targetObject.getClass();

      for(Method method : targetClass.getMethods())
      {
         try
         {
            if(method.getName().startsWith("set") && method.getParameterTypes().length == 1)
            {
               String propertyName = StringUtils.lcFirst(method.getName().substring(3));

               if(map.containsKey(propertyName))
               {
                  Class<?> parameterType = method.getParameterTypes()[0];
                  Object   rawValue      = map.get(propertyName);

                  try
                  {
                     Object mappedValue = reflectivelyMapValue(qInstance, method, parameterType, rawValue);
                     method.invoke(targetObject, mappedValue);
                  }
                  catch(NoValueException nve)
                  {
                     ///////////////////////
                     // don't call setter //
                     ///////////////////////
                  }
               }
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error reflectively mapping on " + targetClass.getName() + "." + method.getName(), e);
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private Object reflectivelyMapValue(QInstance qInstance, Method method, Class<?> parameterType, Object rawValue) throws Exception
   {
      if(parameterType.equals(String.class))
      {
         return (getValueAsString(rawValue));
      }
      else if(parameterType.equals(Integer.class))
      {
         return (getValueAsInteger(rawValue));
      }
      else if(parameterType.equals(Boolean.class))
      {
         return (getValueAsBoolean(rawValue));
      }
      else if(parameterType.equals(boolean.class))
      {
         Boolean valueAsBoolean = getValueAsBoolean(rawValue);
         if(valueAsBoolean != null)
         {
            return (valueAsBoolean);
         }
      }
      else if(parameterType.equals(List.class))
      {
         Type     actualTypeArgument = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
         Class<?> actualTypeClass    = Class.forName(actualTypeArgument.getTypeName());

         Object value = rawValue;
         if(value instanceof List valueList)
         {
            List<Object> mappedValueList = new ArrayList<>();
            for(Object o : valueList)
            {
               try
               {
                  Object mappedValue = reflectivelyMapValue(qInstance, null, actualTypeClass, o);
                  mappedValueList.add(mappedValue);
               }
               catch(NoValueException nve)
               {
                  // leave off list
               }
            }
            return (mappedValueList);
         }
      }
      else if(parameterType.equals(Set.class))
      {
         Type   actualTypeArgument = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
         Class<?> actualTypeClass = Class.forName(actualTypeArgument.getTypeName());

         Object value = rawValue;
         if(value instanceof List valueList)
         {
            Set<Object> mappedValueSet = new LinkedHashSet<>();
            for(Object o : valueList)
            {
               try
               {
                  Object mappedValue = reflectivelyMapValue(qInstance, null, actualTypeClass, o);
                  mappedValueSet.add(mappedValue);
               }
               catch(NoValueException nve)
               {
                  // leave off list
               }
            }
            return (mappedValueSet);
         }
      }
      else if(parameterType.equals(Map.class))
      {
         Type keyType = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
         if(!keyType.equals(String.class))
         {
            LOG.warn("Unsupported key type for " + method + " (" + keyType + ")");
            throw new NoValueException();
         }
         // todo make sure string

         Type     actualTypeArgument = ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[1];
         Class<?> actualTypeClass    = Class.forName(actualTypeArgument.getTypeName());

         Object value = rawValue;
         if(value instanceof Map valueMap)
         {
            Map<String, Object> mappedValueMap = new LinkedHashMap<>();
            for(Object o : valueMap.entrySet())
            {
               try
               {
                  Map.Entry<String, Object> entry       = (Map.Entry<String, Object>) o;
                  Object                    mappedValue = reflectivelyMapValue(qInstance, null, actualTypeClass, entry.getValue());
                  mappedValueMap.put(entry.getKey(), mappedValue);
               }
               catch(NoValueException nve)
               {
                  // leave out of map
               }
            }
            return (mappedValueMap);
         }
      }
      else if(parameterType.isEnum())
      {
         String value = getValueAsString(rawValue);
         for(Object enumConstant : parameterType.getEnumConstants())
         {
            if(((Enum<?>) enumConstant).name().equals(value))
            {
               return (enumConstant);
            }
         }
      }
      else if(registeredLoaders.containsKey(parameterType))
      {
         Object value = rawValue;
         if(value instanceof Map valueMap)
         {
            Class<? extends AbstractMetaDataLoader<?>> loaderClass = registeredLoaders.get(parameterType);
            AbstractMetaDataLoader<?>                  loader      = loaderClass.getConstructor().newInstance();
            QMetaDataObject                            loadedValue = loader.mapToMetaDataObject(qInstance, valueMap);
            return (loadedValue);
         }
      }
      else if(QMetaDataObject.class.isAssignableFrom(parameterType))
      {
         Object value = rawValue;
         if(value instanceof Map valueMap)
         {
            QMetaDataObject childObject = (QMetaDataObject) parameterType.getConstructor().newInstance();
            reflectivelyMap(qInstance, childObject, valueMap);
            return (childObject);
         }
      }
      else if(parameterType.equals(Serializable.class))
      {
         if(rawValue instanceof String
            || rawValue instanceof Integer
            || rawValue instanceof BigDecimal
            || rawValue instanceof Boolean
         )
         {
            return rawValue;
         }
      }
      else
      {
         // todo clean up this message/level
         LOG.warn("No case for " + parameterType + " (arg to: " + method + ")");
      }

      throw new NoValueException();
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected ListOfMapOrMapOfMap getListOfMapOrMapOfMap(Map<String, Object> map, String key)
   {
      if(map.containsKey(key))
      {
         if(map.get(key) instanceof List)
         {
            return (new ListOfMapOrMapOfMap((List<Map<String, Object>>) map.get(key)));
         }
         else if(map.get(key) instanceof Map)
         {
            return (new ListOfMapOrMapOfMap((Map<String, Map<String, Object>>) map.get(key)));
         }
         else
         {
            LOG.warn("Expected list or map under key [" + key + "] while processing [" + getClass().getSimpleName() + "] from [" + fileName + "], but found: " + (map.get(key) == null ? "null" : map.get(key).getClass().getSimpleName()));
         }
      }

      return (null);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected List<Map<String, Object>> getListOfMap(Map<String, Object> map, String key)
   {
      if(map.containsKey(key))
      {
         if(map.get(key) instanceof List)
         {
            return (List<Map<String, Object>>) map.get(key);
         }
         else
         {
            LOG.warn("Expected list under key [" + key + "] while processing [" + getClass().getSimpleName() + "] from [" + fileName + "], but found: " + (map.get(key) == null ? "null" : map.get(key).getClass().getSimpleName()));
         }
      }

      return (null);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected Map<String, Map<String, Object>> getMapOfMap(Map<String, Object> map, String key)
   {
      if(map.containsKey(key))
      {
         if(map.get(key) instanceof Map)
         {
            return (Map<String, Map<String, Object>>) map.get(key);
         }
         else
         {
            LOG.warn("Expected map under key [" + key + "] while processing [" + getClass().getSimpleName() + "] from [" + fileName + "], but found: " + (map.get(key) == null ? "null" : map.get(key).getClass().getSimpleName()));
         }
      }

      return (null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected record ListOfMapOrMapOfMap(List<Map<String, Object>> listOf, Map<String, Map<String, Object>> mapOf)
   {
      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public ListOfMapOrMapOfMap(List<Map<String, Object>> listOf)
      {
         this(listOf, null);
      }



      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public ListOfMapOrMapOfMap(Map<String, Map<String, Object>> mapOf)
      {
         this(null, mapOf);
      }

   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void warnNotImplemented(Map<String, Object> map, String key)
   {
      if(StringUtils.hasContent(ValueUtils.getValueAsString(map.get(key))))
      {
         LOG.warn("Unsupported meta-data attribute [" + key + "] found while processing [" + getClass().getSimpleName() + "] from [" + fileName + "]");
      }
   }



   /*******************************************************************************
    ** Getter for fileName
    **
    *******************************************************************************/
   public String getFileName()
   {
      return fileName;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static class NoValueException extends Exception
   {
      /*******************************************************************************
       ** Constructor
       **
       *******************************************************************************/
      public NoValueException()
      {
         super("No value");
      }
   }
}
