/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQRequest;
import com.kingsrook.qqq.backend.core.modules.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class ActionHelper
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public static void validateSession(AbstractQRequest request) throws QException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface authenticationModule = qAuthenticationModuleDispatcher.getQModule(request.getAuthenticationMetaData());
      if(!authenticationModule.isSessionValid(request.getSession()))
      {
         throw new QException("Invalid session in request");
      }
   }

}
