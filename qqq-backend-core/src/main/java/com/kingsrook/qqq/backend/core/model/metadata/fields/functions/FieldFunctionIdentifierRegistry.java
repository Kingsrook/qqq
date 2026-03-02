/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.fields.functions;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.ClassPathUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 * Singleton registry that auto-discovers all {@link FieldFunctionType} implementations
 * on the classpath and maps their identifier names to {@link FieldFunctionTypeIdentifier}
 * objects.
 *
 * <p>This allows non-Java contexts (e.g., JSON-deserialized filter criteria from a
 * frontend) to look up a FieldFunctionTypeIdentifier by its string name.  For
 * Java code, prefer referencing the IDENTIFIER constant on the implementation class
 * directly rather than going through this registry.</p>
 *
 * <p>Out-of-the-box, this registry will find all implementations of {@link FieldFunctionType}
 * that are in the same package as FieldFunctionType itself.  An application can
 * add custom functions and manually register them via {@link #register(FieldFunctionTypeIdentifier)}</p>
 *******************************************************************************/
public class FieldFunctionIdentifierRegistry
{
   private static final QLogger LOG = QLogger.getLogger(FieldFunctionIdentifierRegistry.class);



   /***************************************************************************
    * singleton holder - avoid double-lock in the constructor
    ***************************************************************************/
   private static final class SingletonHolder
   {
      private static final FieldFunctionIdentifierRegistry INSTANCE = new FieldFunctionIdentifierRegistry();
   }

   //////////////////////////////////////////
   // keys here are names from identifiers //
   //////////////////////////////////////////
   private Map<String, FieldFunctionTypeIdentifier> identifierRegistry = new LinkedHashMap<>();



   /*******************************************************************************
    ** Singleton constructor
    *******************************************************************************/
   private FieldFunctionIdentifierRegistry()
   {
      scanClasspathForFunctionsAndRegisterThem();
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void scanClasspathForFunctionsAndRegisterThem()
   {
      try
      {
         for(Class<?> c : ClassPathUtils.getClassesInPackage(FieldFunctionType.class.getPackageName()))
         {
            if(FieldFunctionType.class.isAssignableFrom(c) && !c.isInterface())
            {
               try
               {
                  FieldFunctionType fieldFunctionType = (FieldFunctionType) c.getConstructor().newInstance();
                  register(fieldFunctionType.getIdentifier());
               }
               catch(Exception e)
               {
                  LOG.info("Exception trying out possible field function type class: " + c.getName(), e);
               }
            }
         }
      }
      catch(Exception e)
      {
         LOG.error("Error registering field function identities", e);
      }
   }



   /***************************************************************************
    * Singleton accessor
    ***************************************************************************/
   public static FieldFunctionIdentifierRegistry getInstance()
   {
      return SingletonHolder.INSTANCE;
   }



   /***************************************************************************
    * Registers the given identifier. If a different identifier is already registered
    * under the same name, logs a warning and replaces it.
    ***************************************************************************/
   public void register(FieldFunctionTypeIdentifier identifier)
   {
      //////////////////////////////////////////////////////////////////////////////////
      // if a different value is already registered under this key, then log about it //
      //////////////////////////////////////////////////////////////////////////////////
      String key = identifier.getName();
      if(identifierRegistry.get(key) != null)
      {
         if(!identifierRegistry.get(key).equals(identifier))
         {
            LOG.info("Replacing FieldFunction type in identity registry.", logPair("identifier", key), logPair("old", identifierRegistry.get(key)), logPair("new", identifier));
         }
      }

      //////////////////////////////////////////////
      // put the code reference into the registry //
      //////////////////////////////////////////////
      identifierRegistry.put(key, identifier);
   }



   /***************************************************************************
    * Returns the registered FieldFunctionTypeIdentifier for the given name, or
    * null if not found.
    ***************************************************************************/
   public FieldFunctionTypeIdentifier getFieldFunctionTypeIdentifier(String name)
   {
      return (identifierRegistry.get(name));
   }

}
