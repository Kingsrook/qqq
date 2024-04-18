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

package com.kingsrook.qqq.backend.core.modules.backend;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;


/*******************************************************************************
 ** This class is responsible for loading a backend module, by its name, and 
 ** returning an instance.
 **
 *******************************************************************************/
public class QBackendModuleDispatcher
{
   private static final QLogger LOG = QLogger.getLogger(QBackendModuleDispatcher.class);

   private static Map<String, String> backendTypeToModuleClassNameMap = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendModuleDispatcher()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void registerBackendModule(QBackendModuleInterface moduleInstance)
   {
      String backendType = moduleInstance.getBackendType();
      if(backendTypeToModuleClassNameMap.containsKey(backendType))
      {
         LOG.info("Overwriting backend type [" + backendType + "] with [" + moduleInstance.getClass() + "]");
      }
      backendTypeToModuleClassNameMap.put(backendType, moduleInstance.getClass().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendModuleInterface getQBackendModule(QBackendMetaData backend) throws QModuleDispatchException
   {
      return (getQBackendModule(backend.getBackendType()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendModuleInterface getQBackendModule(String backendType) throws QModuleDispatchException
   {
      try
      {
         String className = backendTypeToModuleClassNameMap.get(backendType);
         if(className == null)
         {
            throw (new QModuleDispatchException("Unrecognized backend type [" + backendType + "] in dispatcher."));
         }

         Class<?> moduleClass = Class.forName(className);
         return (QBackendModuleInterface) moduleClass.getDeclaredConstructor().newInstance();
      }
      catch(QModuleDispatchException qmde)
      {
         throw (qmde);
      }
      catch(Exception e)
      {
         throw (new QModuleDispatchException("Error getting backend module of type: " + backendType, e));
      }
   }
}
