/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.middleware.javalin.routeproviders.authentication;


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import io.javalin.http.Context;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** simple implementation of a route authenticator.  Assumes that unauthenticated
 ** requests should redirect to a login page.  Note though, maybe that should be
 ** more intelligent, like, only redirect requests for a .html file, but not
 ** requests for include files like images or .js/.css?
 *******************************************************************************/
public class SimpleRouteAuthenticator implements RouteAuthenticatorInterface
{
   private static final QLogger LOG = QLogger.getLogger(SimpleRouteAuthenticator.class);


   /***************************************************************************
    **
    ***************************************************************************/
   public boolean authenticateRequest(Context context) throws QException
   {
      try
      {
         QSession qSession = QJavalinImplementation.setupSession(context, null);
         LOG.debug("Session has been activated", logPair("uuid", qSession.getUuid()));
         return (true);
      }
      catch(QAuthenticationException e)
      {
         QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
         QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(QContext.getQInstance().getAuthentication());

         String redirectURL = authenticationModule.getLoginRedirectUrl(context.fullUrl());

         context.redirect(redirectURL);
         LOG.debug("Redirecting request, due to required session missing");
         return (false);
      }
      catch(QModuleDispatchException e)
      {
         throw (new QException("Error authenticating request", e));
      }
   }

}
