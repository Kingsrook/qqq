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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Utility methods to help with deserializing JSON streams into QQQ models.
 ** Specifically meant to be used within a jackson custom deserializer (e.g.,
 ** an implementation of JsonDeserializer).
 *******************************************************************************/
public class DeserializerUtils
{
   private static final Logger LOG = LogManager.getLogger(DeserializerUtils.class);



   /*******************************************************************************
    ** For a given (jackson, JSON) treeNode, look at its backendType property,
    ** and return an instance of the corresponding QBackendModule.
    *******************************************************************************/
   public static QBackendModuleInterface getBackendModule(TreeNode treeNode) throws IOException
   {
      /////////////////////////////////////////////////////////////////////////////////
      // validate the backendType property is present, as text, in the json treeNode //
      /////////////////////////////////////////////////////////////////////////////////
      TreeNode backendTypeTreeNode = treeNode.get("backendType");
      if(backendTypeTreeNode == null || backendTypeTreeNode instanceof NullNode)
      {
         throw new IOException("Missing backendType in backendMetaData");
      }

      if(!(backendTypeTreeNode instanceof TextNode textNode))
      {
         throw new IOException("backendType is not a string value (is: " + backendTypeTreeNode.getClass().getSimpleName() + ")");
      }

      try
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // get the value of the backendType json node, and use it to look up the qBackendModule object //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         String backendType = textNode.asText();
         return new QBackendModuleDispatcher().getQBackendModule(backendType);
      }
      catch(QModuleDispatchException e)
      {
         throw (new IOException(e));
      }
   }



   /*******************************************************************************
    ** Using reflection, create & populate an instance of a class, based on the
    ** properties in a jackson/json treeNode.
    **
    *******************************************************************************/
   public static <T> T reflectivelyDeserialize(Class<T> outputClass, TreeNode treeNode) throws IOException
   {
      try
      {
         /////////////////////////////////
         // construct the output object //
         /////////////////////////////////
         T output = outputClass.getConstructor().newInstance();

         /////////////////////////////////////////////////////////////////////////////////////////////////
         // set up a mapping between field names, and lambdas which will take a String (from the json), //
         // and set it in the output object, doing type conversion as needed.                           //
         // do this by iterating over methods on the output class that look like setters.               //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         Map<String, Consumer<String>> setterMap = new HashMap<>();
         for(Method method : outputClass.getMethods())
         {
            /////////////////////////////////////////////////////////////
            // setters start with the word "set", and have 1 parameter //
            /////////////////////////////////////////////////////////////
            if(method.getName().startsWith("set") && method.getParameterTypes().length == 1)
            {
               /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // get the parameter type, and the name of the field (remove set from the method name, and downshift the first letter) //
               /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               Class<?> parameterType = method.getParameterTypes()[0];
               String   fieldName     = method.getName().substring(3, 4).toLowerCase(Locale.ROOT) + method.getName().substring(4);

               ///////////////////////////////////////////////////////////////////////////////////
               // put the entry in the map - where the value here is a consumer lambda function //
               ///////////////////////////////////////////////////////////////////////////////////
               setterMap.put(fieldName, (String value) ->
               {
                  try
                  {
                     //////////////////////////////////////////////////////////////////////////////////////////////////
                     // based on the parameter type, handle it differently - either type-converting (e.g., parseInt) //
                     // or gracefully ignoring, or failing.                                                          //
                     //////////////////////////////////////////////////////////////////////////////////////////////////
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
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // specifically do NOT try to handle Class type arguments                                                    //
                        // we hit this when trying to de-serialize a QBackendMetaData, and we found its setBackendType(Class) method //
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
                     }
                     else if(parameterType.getPackageName().startsWith("java."))
                     {
                        /////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // if we hit this, we might want to add an else-if to handle the type.                                 //
                        // otherwise, either find some jackson annotation that makes sense, and apply it to the setter method, //
                        // or if no jackson annotation is right, then come up with annotation of our own.                      //
                        /////////////////////////////////////////////////////////////////////////////////////////////////////////
                        throw (new RuntimeException("Field " + fieldName + " is of an unhandled type " + parameterType.getName() + " when deserializing " + outputClass.getName()));
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
         throw (new IOException("Error deserializing json object into instance of " + outputClass.getName(), e));
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
      ///////////////////////////////////////////////////////
      // iterate over fields in the json object (treeNode) //
      ///////////////////////////////////////////////////////
      Iterator<String> fieldNamesIterator = treeNode.fieldNames();
      while(fieldNamesIterator.hasNext())
      {
         String fieldName = fieldNamesIterator.next();

         //////////////////////////////////////////////////////////////////////////
         // error if we find a field in the json that we don't have a setter for //
         //////////////////////////////////////////////////////////////////////////
         if(!setterMap.containsKey(fieldName))
         {
            throw (new IOException("Unexpected field (no corresponding setter): " + fieldName));
         }

         // call the setter -
         TreeNode fieldNode = treeNode.get(fieldName);
         if(fieldNode instanceof NullNode)
         {
            setterMap.get(fieldName).accept(null);
         }
         else if(fieldNode instanceof TextNode textNode)
         {
            setterMap.get(fieldName).accept(textNode.asText());
         }
         else
         {
            throw (new IOException("Unexpected node type (" + fieldNode.getClass() + ") for field: " + fieldName));
         }
      }
   }
}
