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

package com.kingsrook.qqq.backend.core.model.metadata.serialization;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class DeserializerUtils
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static QBackendModuleInterface getBackendModule(TreeNode treeNode) throws IOException
   {
      TreeNode backendTypeTreeNode = treeNode.get("backendType");
      if(backendTypeTreeNode == null || backendTypeTreeNode instanceof NullNode)
      {
         throw new IOException("Missing backendType in backendMetaData");
      }

      if(!(backendTypeTreeNode instanceof TextNode textNode))
      {
         throw new IOException("backendType is not a string value (is: " + backendTypeTreeNode.getClass().getSimpleName() + ")");
      }
      else
      {
         String backendType = textNode.asText();

         try
         {
            return new QBackendModuleDispatcher().getQBackendModule(backendType);
         }
         catch(QModuleDispatchException e)
         {
            throw (new IOException(e));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T> T reflectivelyDeserialize(Class<T> _class, TreeNode treeNode) throws IOException
   {
      try
      {
         T output = _class.getConstructor().newInstance();
         System.out.println("Reflectively deserializing a: " + _class.getName());

         Map<String, Consumer<String>> setterMap = new HashMap<>();
         for(Method method : _class.getMethods())
         {
            if(method.getName().startsWith("set") && method.getParameterTypes().length == 1)
            {
               Class<?> parameterType = method.getParameterTypes()[0];
               String   fieldName     = method.getName().substring(3, 4).toLowerCase(Locale.ROOT) + method.getName().substring(4);

               setterMap.put(fieldName, (String value) ->
               {
                  try
                  {
                     if(parameterType.equals(String.class))
                     {
                        method.invoke(output, value);
                     }
                     else if(parameterType.equals(Integer.class))
                     {
                        method.invoke(output, StringUtils.hasContent(value) ? Integer.parseInt(value) : null);
                     }
                     else if(parameterType.equals(Long.class))
                     {
                        method.invoke(output, StringUtils.hasContent(value) ? Long.parseLong(value) : null);
                     }
                     else if(parameterType.equals(BigDecimal.class))
                     {
                        method.invoke(output, StringUtils.hasContent(value) ? new BigDecimal(value) : null);
                     }
                     else if(parameterType.equals(Boolean.class))
                     {
                        method.invoke(output, StringUtils.hasContent(value) ? Boolean.parseBoolean(value) : null);
                     }
                     else if(parameterType.equals(Class.class))
                     {
                        ////////////////////////////////////////////////////////////
                        // specifically do NOT try to handle Class type arguments //
                        ////////////////////////////////////////////////////////////
                     }
                     else if(parameterType.getPackageName().startsWith("java."))
                     {
                        ////////////////////////////////////////////////////////////////////////
                        // if we hit this, we might want to add an else-if to handle the type //
                        ////////////////////////////////////////////////////////////////////////
                        throw (new RuntimeException("Field " + fieldName + " is of an unhandled type " + parameterType.getName() + " when deserializing " + _class.getName()));
                     }
                     else
                     {
                        ////////////////////////////////////
                        // gracefully ignore other types. //
                        ////////////////////////////////////
                     }
                  }
                  catch(IllegalAccessException | InvocationTargetException e)
                  {
                     throw new RuntimeException(e);
                  }
               });
            }
         }

         DeserializerUtils.deserializeBean(treeNode, setterMap);

         return output;
      }
      catch(Exception e)
      {
         throw (new IOException("Error reflectively deserializing table details", e));
      }

   }



   /*******************************************************************************
    ** Helper for custom jackson serializers - allows the caller to specify a map
    ** of field names to setter methods.
    **
    ** Note, the consumers in the map all work on strings, so you may need to do
    ** Integer.parseInt, for example, in a lambda in the map.
    *******************************************************************************/
   private static void deserializeBean(TreeNode treeNode, Map<String, Consumer<String>> setterMap) throws IOException
   {
      Iterator<String> fieldNamesIterator = treeNode.fieldNames();
      while(fieldNamesIterator.hasNext())
      {
         String fieldName = fieldNamesIterator.next();
         System.out.println("Handling field [" + fieldName + "]");

         if(!setterMap.containsKey(fieldName))
         {
            throw (new IllegalArgumentException("Unexpected value: " + fieldName));
         }

         TreeNode fieldNode = treeNode.get(fieldName);
         if(fieldNode instanceof NullNode)
         {
            setterMap.get(fieldName).accept(null);
         }
         else
         {
            setterMap.get(fieldName).accept(((TextNode) fieldNode).asText());
         }
      }
   }
}
