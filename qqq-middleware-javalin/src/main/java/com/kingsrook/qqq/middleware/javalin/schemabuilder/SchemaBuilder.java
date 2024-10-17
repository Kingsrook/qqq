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

package com.kingsrook.qqq.middleware.javalin.schemabuilder;


import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIDescription;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIExclude;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIHasAdditionalProperties;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIIncludeProperties;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIListItems;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapKnownEntries;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIMapValueType;
import com.kingsrook.qqq.middleware.javalin.schemabuilder.annotations.OpenAPIOneOf;
import com.kingsrook.qqq.openapi.model.Schema;
import com.kingsrook.qqq.openapi.model.Type;


/*******************************************************************************
 ** This class facilitates generating OpenAPI Schema objects based on reflectively
 ** reading classes and annotations
 *******************************************************************************/
public class SchemaBuilder
{

   /***************************************************************************
    **
    ***************************************************************************/
   public Schema classToSchema(Class<?> c)
   {
      return classToSchema(c, c);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Schema classToSchema(Class<?> c, AnnotatedElement element)
   {
      Schema schema = new Schema();

      if(c.isEnum())
      {
         schema.withType(Type.STRING);
         schema.withEnumValues(Arrays.stream(c.getEnumConstants()).map(e -> String.valueOf(e)).collect(Collectors.toList()));
      }
      else if(c.equals(String.class))
      {
         schema.withType(Type.STRING);
      }
      else if(c.equals(Integer.class) || c.equals(BigDecimal.class))
      {
         schema.withType(Type.NUMBER);
      }
      else if(c.equals(Boolean.class))
      {
         schema.withType(Type.BOOLEAN);
      }
      else if(c.equals(List.class))
      {
         schema.withType(Type.ARRAY);
         // Class<?> itemType = field.getType().getTypeParameters()[0].getBounds().getClass();

         OpenAPIListItems openAPIListItemsAnnotation = element.getAnnotation(OpenAPIListItems.class);
         if(openAPIListItemsAnnotation == null)
         {
            // todo - can this be allowed, to make a generic list?  maybe.
            // throw (new QRuntimeException("A List field [" + field.getName() + "] was missing its @OpenAPIItems annotation"));
         }
         else
         {
            if(openAPIListItemsAnnotation.useRef())
            {
               schema.withItems(new Schema().withRef("#/components/schemas/" + openAPIListItemsAnnotation.value().getSimpleName()));
            }
            else
            {
               Class<?> itemType = openAPIListItemsAnnotation.value();
               schema.withItems(classToSchema(itemType));
            }
         }
      }
      else if(c.equals(Map.class))
      {
         schema.withType(Type.OBJECT);

         OpenAPIMapKnownEntries openAPIMapKnownEntriesAnnotation = element.getAnnotation(OpenAPIMapKnownEntries.class);
         if(openAPIMapKnownEntriesAnnotation != null)
         {
            schema.withRef("#/components/schemas/" + openAPIMapKnownEntriesAnnotation.value().getSimpleName());
//            if(openAPIMapKnownEntriesAnnotation.additionalProperties())
//            {
//               schema.withAdditionalProperties(true);
//            }
         }

         OpenAPIMapValueType openAPIMapValueTypeAnnotation = element.getAnnotation(OpenAPIMapValueType.class);
         if(openAPIMapValueTypeAnnotation != null)
         {
            if(openAPIMapValueTypeAnnotation.useRef())
            {
               schema.withAdditionalProperties(new Schema().withRef("#/components/schemas/" + openAPIMapValueTypeAnnotation.value().getSimpleName()));
            }
            else
            {
               schema.withAdditionalProperties(classToSchema(openAPIMapValueTypeAnnotation.value()));
            }
         }
      }
      else
      {
         OpenAPIOneOf openAPIOneOfAnnotation = element.getAnnotation(OpenAPIOneOf.class);
         if(openAPIOneOfAnnotation != null)
         {
            String       description = "[" + element + "]";
            List<Schema> oneOfList   = processOneOfAnnotation(openAPIOneOfAnnotation, c, description);
            schema.withOneOf(oneOfList);
         }
         else
         {
            /////////////////////////////////////////////////////////////////////////////////////////////
            // else, if not a one-of then assume the schema is an object, and build out its properties //
            /////////////////////////////////////////////////////////////////////////////////////////////
            schema.withType(Type.OBJECT);

            Map<String, Schema> properties = new TreeMap<>();
            schema.withProperties(properties);

            //////////////////////////////////////////////////////////////////////////////////////////
            // if we're told to includeProperties (e.g., from ancestor classes), then go find those //
            //////////////////////////////////////////////////////////////////////////////////////////
            OpenAPIIncludeProperties openAPIIncludePropertiesAnnotation = c.getAnnotation(OpenAPIIncludeProperties.class);
            if(openAPIIncludePropertiesAnnotation != null)
            {
               Set<Class<?>> ancestorClasses = Arrays.stream(openAPIIncludePropertiesAnnotation.ancestorClasses()).collect(Collectors.toSet());
               Class<?>      superClass      = c.getSuperclass();
               do
               {
                  if(ancestorClasses.contains(superClass))
                  {
                     addDeclaredFieldsToProperties(superClass, properties);
                     addDeclaredMethodsToProperties(superClass, properties);
                  }
                  superClass = superClass.getSuperclass();
               }
               while(superClass != null);
            }

            ///////////////////////////////////////////////////////////////////////
            // make all declared-fields and getters in the class into properties //
            ///////////////////////////////////////////////////////////////////////
            addDeclaredFieldsToProperties(c, properties);
            addDeclaredMethodsToProperties(c, properties);
         }
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////
      // now (after schema may have been replaced, e.g., in a recursive call), add more details to it //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      OpenAPIDescription openAPIDescriptionAnnotation = element.getAnnotation(OpenAPIDescription.class);
      if(openAPIDescriptionAnnotation != null)
      {
         schema.setDescription(openAPIDescriptionAnnotation.value());
      }

      if(element.isAnnotationPresent(OpenAPIHasAdditionalProperties.class))
      {
         schema.withAdditionalProperties(true);
      }

      return (schema);
   }



   /***************************************************************************
    ** Getter methods with an annotation
    ***************************************************************************/
   private void addDeclaredMethodsToProperties(Class<?> c, Map<String, Schema> properties)
   {
      for(Method method : c.getDeclaredMethods())
      {
         OpenAPIDescription methodDescription = method.getAnnotation(OpenAPIDescription.class);
         OpenAPIExclude     openAPIExclude    = method.getAnnotation(OpenAPIExclude.class);
         if(method.getName().startsWith("get") && method.getParameterCount() == 0 && methodDescription != null && openAPIExclude == null)
         {
            String name = StringUtils.lcFirst(method.getName().substring(3));
            properties.put(name, getMemberSchema(method));
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void addDeclaredFieldsToProperties(Class<?> c, Map<String, Schema> properties)
   {
      for(Field declaredField : c.getDeclaredFields())
      {
         OpenAPIExclude openAPIExclude = declaredField.getAnnotation(OpenAPIExclude.class);
         if(openAPIExclude == null)
         {
            properties.put(declaredField.getName(), getMemberSchema(declaredField));
         }
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Schema getMemberSchema(AccessibleObject member)
   {
      Class<?> type;
      if(member instanceof Field field)
      {
         type = field.getType();
      }
      else if(member instanceof Method method)
      {
         type = method.getReturnType();
      }
      else
      {
         throw (new IllegalArgumentException("Unsupported AccessibleObject: " + member));
      }

      return (classToSchema(type, member));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private List<Schema> processOneOfAnnotation(OpenAPIOneOf openAPIOneOfAnnotation, Class<?> type, String description)
   {
      List<Schema> oneOfList = new ArrayList<>();

      if(openAPIOneOfAnnotation.mode().equals(OpenAPIOneOf.Mode.PERMITTED_SUBCLASSES))
      {
         Class<?>[] permittedSubclasses = type.getPermittedSubclasses();
         for(Class<?> permittedSubclass : permittedSubclasses)
         {
            oneOfList.add(classToSchema(permittedSubclass));
         }
      }
      else if(openAPIOneOfAnnotation.mode().equals(OpenAPIOneOf.Mode.SPECIFIED_LIST))
      {
         for(Class<?> oneOfClass : openAPIOneOfAnnotation.options())
         {
            oneOfList.add(classToSchema(oneOfClass));
         }
      }

      if(oneOfList.isEmpty())
      {
         throw (new QRuntimeException("Could not find any options to use for an @OpenAPIOneOf annotation on " + description));
      }
      return oneOfList;
   }


}
