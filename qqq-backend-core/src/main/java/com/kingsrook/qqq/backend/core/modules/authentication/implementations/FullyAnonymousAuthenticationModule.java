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
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;


/*******************************************************************************
 ** An authentication module with no actual backing system - all users are treated
 ** as anonymous, and all sessions are always valid.
 *******************************************************************************/
public class FullyAnonymousAuthenticationModule implements QAuthenticationModuleInterface
{
   public static final String TEST_ACCESS_TOKEN = "b0a88d00-8439-48e8-8b48-e0ef40c40ed9";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QSession createSession(QInstance qInstance, Map<String, String> context)
   {
      QUser qUser = new QUser();
      qUser.setIdReference("anonymous");
      qUser.setFullName("Anonymous");

      QSession qSession = new QSession();
      if(context != null && context.get("sessionId") != null)
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
   public boolean isSessionValid(QInstance instance, QSession session)
   {
      return session != null;
   }

}
