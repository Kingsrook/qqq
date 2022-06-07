/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/intellij-commentator-plugin
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

package com.kingsrook.qqq.backend.core.modules;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


/*******************************************************************************
 ** This class is responsible for loading a backend module, by its name, and 
 ** returning an instance.
 **
 ** TODO - make this mapping runtime-bound, not pre-compiled in.
 **
 *******************************************************************************/
public class QBackendModuleDispatcher
{
   private Map<String, String> backendTypeToModuleClassNameMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendModuleDispatcher()
   {
      backendTypeToModuleClassNameMap = new HashMap<>();
      backendTypeToModuleClassNameMap.put("mock", "com.kingsrook.qqq.backend.core.modules.mock.MockBackendModule");
      backendTypeToModuleClassNameMap.put("rdbms", "com.kingsrook.qqq.backend.module.rdbms.RDBMSBackendModule");
      // todo - let user define custom type -> classes
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendModuleInterface getQModule(QBackendMetaData backend) throws QModuleDispatchException
   {
      try
      {
         String className = backendTypeToModuleClassNameMap.get(backend.getType());
         if (className == null)
         {
            throw (new QModuleDispatchException("Unrecognized backend type [" + backend.getType() + "] in dispatcher."));
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
         throw (new QModuleDispatchException("Error getting backend module of type: " + backend.getType(), e));
      }
   }
}
