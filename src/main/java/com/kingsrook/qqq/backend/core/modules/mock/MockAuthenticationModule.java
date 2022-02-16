/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.mock;


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
public class MockAuthenticationModule implements QAuthenticationModuleInterface
{
   private static final Logger logger = LogManager.getLogger(MockAuthenticationModule.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(Map<String, String> context)
   {
      QUser qUser = new QUser();
      qUser.setIdReference("User:" + (System.currentTimeMillis() % 10_000));
      qUser.setFullName("John Smith");

      QSession qSession = new QSession();
      qSession.setIdReference("Session:" + UUID.randomUUID());
      qSession.setUser(qUser);

      return (qSession);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public boolean isSessionValid(QSession session)
   {
      if(session == null)
      {
         logger.info("Session is null, which is not valid.");
         return (false);
      }

      if(session.getValue("isInvalid") != null)
      {
         logger.info("Session contains the valid 'isInvalid', which is not valid.");
         return (false);
      }

      return (true);
   }
}
