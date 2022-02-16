/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.defaults;


import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 **
 *******************************************************************************/
public class FullyAnonymousAuthenticationModule implements QAuthenticationModuleInterface
{
   private static final Logger logger = LogManager.getLogger(FullyAnonymousAuthenticationModule.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(Map<String, String> context)
   {
      QUser qUser = new QUser();
      qUser.setIdReference("anonymous");
      qUser.setFullName("Anonymous");

      QSession qSession = new QSession();
      if (context.get("sessionId") != null)
      {
         qSession.setIdReference(context.get("sessionId"));
      }
      else
      {
         qSession.setIdReference("Session:" + UUID.randomUUID());
      }
      qSession.setUser(qUser);

      return (qSession);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isSessionValid(QSession session)
   {
      return (true);
   }
}
