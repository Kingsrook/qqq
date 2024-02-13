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

package com.kingsrook.qqq.backend.core.modules.messaging;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.messaging.QMessagingProviderMetaData;


/*******************************************************************************
 ** This class is responsible for loading a messaging provider, by its name, and
 ** returning an instance.
 **
 *******************************************************************************/
public class QMessagingProviderDispatcher
{
   private static final QLogger LOG = QLogger.getLogger(QMessagingProviderDispatcher.class);

   private static Map<String, String> typeToProviderClassNameMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QMessagingProviderDispatcher()
   {
      initBackendTypeToModuleClassNameMap();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void initBackendTypeToModuleClassNameMap()
   {
      if(typeToProviderClassNameMap != null)
      {
         return;
      }

      Map<String, String> newMap = new HashMap<>();

      typeToProviderClassNameMap = newMap;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void registerMessagingProvider(MessagingProviderInterface messagingProviderInstance)
   {
      initBackendTypeToModuleClassNameMap();
      String type = messagingProviderInstance.getType();
      if(typeToProviderClassNameMap.containsKey(type))
      {
         LOG.info("Overwriting messagingProvider type [" + type + "] with [" + messagingProviderInstance.getClass() + "]");
      }
      typeToProviderClassNameMap.put(type, messagingProviderInstance.getClass().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MessagingProviderInterface getMessagingProviderInterface(QMessagingProviderMetaData messagingProviderMetaData) throws QModuleDispatchException
   {
      return (getMessagingProviderInterface(messagingProviderMetaData.getType()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public MessagingProviderInterface getMessagingProviderInterface(String type) throws QModuleDispatchException
   {
      try
      {
         String className = typeToProviderClassNameMap.get(type);
         if(className == null)
         {
            throw (new QModuleDispatchException("Unrecognized messaging provider type [" + type + "] in dispatcher."));
         }

         Class<?> moduleClass = Class.forName(className);
         return (MessagingProviderInterface) moduleClass.getDeclaredConstructor().newInstance();
      }
      catch(QModuleDispatchException qmde)
      {
         throw (qmde);
      }
      catch(Exception e)
      {
         throw (new QModuleDispatchException("Error getting messaging provider of type: " + type, e));
      }
   }
}
