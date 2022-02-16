/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
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
