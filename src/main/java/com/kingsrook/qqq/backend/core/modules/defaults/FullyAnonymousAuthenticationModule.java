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
