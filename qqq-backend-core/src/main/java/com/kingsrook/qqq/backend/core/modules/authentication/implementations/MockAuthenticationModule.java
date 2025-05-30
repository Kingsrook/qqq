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

package com.kingsrook.qqq.backend.core.modules.authentication.implementations;


import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockAuthenticationModule implements QAuthenticationModuleInterface
{
   private static final QLogger logger         = QLogger.getLogger(MockAuthenticationModule.class);
   private static final int     USER_ID_MODULO = 10_000;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(QInstance qInstance, Map<String, String> context) throws QAuthenticationException
   {
      if("Deny".equalsIgnoreCase(context.get("accessToken")))
      {
         throw (new QAuthenticationException("Access denied (per accessToken requesting as such)"));
      }

      QUser qUser = new QUser();
      qUser.setIdReference("User:" + (System.currentTimeMillis() % USER_ID_MODULO));
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
   public boolean isSessionValid(QInstance instance, QSession session)
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



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getLoginRedirectUrl(String originalUrl)
   {
      return originalUrl + "?createMockSession=true";
   }

}
