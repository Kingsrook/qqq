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

package com.kingsrook.qqq.backend.core.modules.authentication;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.modules.authentication.metadata.QAuthenticationMetaData;


/*******************************************************************************
 ** This class is responsible for loading an authentication module, by its name, and
 ** returning an instance.
 **
 ** TODO - make this mapping runtime-bound, not pre-compiled in.
 **
 *******************************************************************************/
public class QAuthenticationModuleDispatcher
{
   private final Map<String, String> authenticationTypeToModuleClassNameMap;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationModuleDispatcher()
   {
      authenticationTypeToModuleClassNameMap = new HashMap<>();
      authenticationTypeToModuleClassNameMap.put(QAuthenticationType.MOCK.getName(), "com.kingsrook.qqq.backend.core.modules.authentication.MockAuthenticationModule");
      authenticationTypeToModuleClassNameMap.put(QAuthenticationType.FULLY_ANONYMOUS.getName(), "com.kingsrook.qqq.backend.core.modules.authentication.FullyAnonymousAuthenticationModule");
      authenticationTypeToModuleClassNameMap.put(QAuthenticationType.AUTH_0.getName(), "com.kingsrook.qqq.backend.core.modules.authentication.Auth0AuthenticationModule");
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

      return getQModule(authenticationMetaData.getType().getName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QAuthenticationModuleInterface getQModule(String authenticationType) throws QModuleDispatchException
   {
      try
      {
         String className = authenticationTypeToModuleClassNameMap.get(authenticationType);
         if(className == null)
         {
            throw (new QModuleDispatchException("Unrecognized authentication type [" + authenticationType + "] in dispatcher."));
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
         throw (new QModuleDispatchException("Error getting authentication module of type: " + authenticationType, e));
      }
   }

}
