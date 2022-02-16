/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;


/*******************************************************************************
 ** This class is responsible for loading an authentication module, by its name, and
 ** returning an instance.
 **
 ** TODO - make this mapping runtime-bound, not pre-compiled in.
 **
 *******************************************************************************/
public class QAuthenticationModuleDispatcher
{
   private Map<String, String> authenticationTypeToModuleClassNameMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationModuleDispatcher()
   {
      authenticationTypeToModuleClassNameMap = new HashMap<>();
      authenticationTypeToModuleClassNameMap.put("mock", "com.kingsrook.qqq.backend.core.modules.mock.MockAuthenticationModule");
      authenticationTypeToModuleClassNameMap.put("fullyAnonymous", "com.kingsrook.qqq.backend.core.modules.defaults.FullyAnonymousAuthenticationModule");
      authenticationTypeToModuleClassNameMap.put("TODO:google", "com.kingsrook.qqq.authentication.module.google.GoogleAuthenticationModule");
      // todo - let user define custom type -> classes
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationModuleInterface getQModule(QAuthenticationMetaData authenticationMetaData) throws QModuleDispatchException
   {
      if(authenticationMetaData == null)
      {
         throw (new QModuleDispatchException("No authentication meta data defined."));
      }

      try
      {
         String className = authenticationTypeToModuleClassNameMap.get(authenticationMetaData.getType());
         if(className == null)
         {
            throw (new QModuleDispatchException("Unrecognized authentication type [" + authenticationMetaData.getType() + "] in dispatcher."));
         }

         Class<?> moduleClass = Class.forName(className);
         return (QAuthenticationModuleInterface) moduleClass.getDeclaredConstructor().newInstance();
      }
      catch(QModuleDispatchException qmde)
      {
         throw (qmde);
      }
      catch(Exception e)
      {
         throw (new QModuleDispatchException("Error getting authentication module of type: " + authenticationMetaData.getType(), e));
      }
   }
}
