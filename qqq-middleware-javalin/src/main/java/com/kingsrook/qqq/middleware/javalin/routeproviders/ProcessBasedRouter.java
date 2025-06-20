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

package com.kingsrook.qqq.middleware.javalin.routeproviders;


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.backend.javalin.QJavalinUtils;
import com.kingsrook.qqq.middleware.javalin.QJavalinRouteProviderInterface;
import com.kingsrook.qqq.middleware.javalin.metadata.JavalinRouteProviderMetaData;
import com.kingsrook.qqq.middleware.javalin.routeproviders.authentication.RouteAuthenticatorInterface;
import com.kingsrook.qqq.middleware.javalin.routeproviders.contexthandlers.DefaultRouteProviderContextHandler;
import com.kingsrook.qqq.middleware.javalin.routeproviders.contexthandlers.RouteProviderContextHandlerInterface;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessBasedRouter implements QJavalinRouteProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(ProcessBasedRouter.class);

   private final String       hostedPath;
   private final String       processName;
   private final List<String> methods;

   private QCodeReference routeAuthenticator;
   private QCodeReference contextHandler;

   private QInstance qInstance;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessBasedRouter(String hostedPath, String processName)
   {
      this(hostedPath, processName, null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public ProcessBasedRouter(JavalinRouteProviderMetaData routeProvider)
   {
      this(routeProvider.getHostedPath(), routeProvider.getProcessName(), routeProvider.getMethods());
      setRouteAuthenticator(routeProvider.getRouteAuthenticator());
      setContextHandler(routeProvider.getContextHandler());
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessBasedRouter(String hostedPath, String processName, List<String> methods)
   {
      this.hostedPath = hostedPath;
      this.processName = processName;

      if(CollectionUtils.nullSafeHasContents(methods))
      {
         this.methods = methods;
      }
      else
      {
         this.methods = List.of("GET");
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void setQInstance(QInstance qInstance)
   {
      this.qInstance = qInstance;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public EndpointGroup getJavalinEndpointGroup()
   {
      return (() ->
      {
         for(String method : methods)
         {
            switch(method.toLowerCase())
            {
               case "get" -> ApiBuilder.get(hostedPath, this::handleRequest);
               case "post" -> ApiBuilder.post(hostedPath, this::handleRequest);
               case "put" -> ApiBuilder.put(hostedPath, this::handleRequest);
               case "patch" -> ApiBuilder.patch(hostedPath, this::handleRequest);
               case "delete" -> ApiBuilder.delete(hostedPath, this::handleRequest);
               default -> throw (new IllegalArgumentException("Unrecognized method: " + method));
            }
         }
      });
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void handleRequest(Context context)
   {
      RunProcessInput input = new RunProcessInput();
      input.setProcessName(processName);

      QContext.init(qInstance, new QSystemUserSession());

      boolean isAuthenticated = false;
      if(routeAuthenticator == null)
      {
         isAuthenticated = true;
      }
      else
      {
         try
         {
            RouteAuthenticatorInterface routeAuthenticator = QCodeLoader.getAdHoc(RouteAuthenticatorInterface.class, this.routeAuthenticator);
            isAuthenticated = routeAuthenticator.authenticateRequest(context);
         }
         catch(Exception e)
         {
            context.skipRemainingHandlers();
            QJavalinImplementation.handleException(context, e);
         }
      }

      if(!isAuthenticated)
      {
         LOG.info("Request is not authenticated, so returning before running process", logPair("processName", processName), logPair("path", context.path()));
         return;
      }

      try
      {
         LOG.info("Running process to serve route", logPair("processName", processName), logPair("path", context.path()));

         //////////////////////////////////////////////////////////////////////////////////////
         // handle request (either using route's specific context handler, or a default one) //
         //////////////////////////////////////////////////////////////////////////////////////
         RouteProviderContextHandlerInterface contextHandler = createContextHandler();
         contextHandler.handleRequest(context, input);

         // todo - make the inputStream available to the process to stream results?
         //  maybe via the callback object??? input.setCallback(new QProcessCallback() {});
         //  context.resultInputStream();

         /////////////////////
         // run the process //
         /////////////////////
         input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

         /////////////////////
         // handle response //
         /////////////////////
         if(contextHandler.handleResponse(context, runProcessOutput))
         {
            return;
         }

         LOG.debug("No response value was set in the process output state.");
      }
      catch(Exception e)
      {
         QJavalinUtils.handleException(null, context, e);
      }
      finally
      {
         QContext.clear();
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private RouteProviderContextHandlerInterface createContextHandler()
   {
      if(contextHandler != null)
      {
         return QCodeLoader.getAdHoc(RouteProviderContextHandlerInterface.class, this.contextHandler);
      }
      else
      {
         return (new DefaultRouteProviderContextHandler());
      }
   }



   /*******************************************************************************
    ** Getter for routeAuthenticator
    *******************************************************************************/
   public QCodeReference getRouteAuthenticator()
   {
      return (this.routeAuthenticator);
   }



   /*******************************************************************************
    ** Setter for routeAuthenticator
    *******************************************************************************/
   public void setRouteAuthenticator(QCodeReference routeAuthenticator)
   {
      this.routeAuthenticator = routeAuthenticator;
   }



   /*******************************************************************************
    ** Fluent setter for routeAuthenticator
    *******************************************************************************/
   public ProcessBasedRouter withRouteAuthenticator(QCodeReference routeAuthenticator)
   {
      this.routeAuthenticator = routeAuthenticator;
      return (this);
   }


   /*******************************************************************************
    ** Getter for contextHandler
    *******************************************************************************/
   public QCodeReference getContextHandler()
   {
      return (this.contextHandler);
   }



   /*******************************************************************************
    ** Setter for contextHandler
    *******************************************************************************/
   public void setContextHandler(QCodeReference contextHandler)
   {
      this.contextHandler = contextHandler;
   }



   /*******************************************************************************
    ** Fluent setter for contextHandler
    *******************************************************************************/
   public ProcessBasedRouter withContextHandler(QCodeReference contextHandler)
   {
      this.contextHandler = contextHandler;
      return (this);
   }


}
