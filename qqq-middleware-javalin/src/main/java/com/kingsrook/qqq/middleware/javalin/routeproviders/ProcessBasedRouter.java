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


import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.backend.javalin.QJavalinUtils;
import com.kingsrook.qqq.middleware.javalin.QJavalinRouteProviderInterface;
import com.kingsrook.qqq.middleware.javalin.metadata.JavalinRouteProviderMetaData;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;


/*******************************************************************************
 **
 *******************************************************************************/
public class ProcessBasedRouter implements QJavalinRouteProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(ProcessBasedRouter.class);

   private final String       hostedPath;
   private final String       processName;
   private final List<String> methods;
   private       QInstance    qInstance;



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

      try
      {
         QJavalinImplementation.setupSession(context, input);
      }
      catch(Exception e)
      {
         context.header("WWW-Authenticate", "Basic realm=\"Access to this QQQ site\"");
         context.status(HttpStatus.UNAUTHORIZED);
         return;
      }

      /*
      boolean authorized = false;
      String authorization = context.header("Authorization");
      if(authorization != null && authorization.matches("^Basic .+"))
      {
         String base64Authorization = authorization.substring("Basic ".length());
         String decoded = new String(Base64.getDecoder().decode(base64Authorization), StandardCharsets.UTF_8);
         String[] parts = decoded.split(":", 2);

         QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
         QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());
      }

      if(!authorized)
      {
      }

      // todo - not always system-user session!!
      QContext.init(this.qInstance, new QSystemUserSession());
      */

      try
      {
         LOG.info("Running [" + processName + "] to serve [" + context.path() + "]...");

         /////////////////////
         // run the process //
         /////////////////////
         input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
         input.addValue("path", context.path());
         input.addValue("method", context.method());
         input.addValue("pathParams", new HashMap<>(context.pathParamMap()));
         input.addValue("queryParams", new HashMap<>(context.queryParamMap()));
         input.addValue("formParams", new HashMap<>(context.formParamMap()));
         RunProcessOutput runProcessOutput = new RunProcessAction().execute(input);

         /////////////////
         // status code //
         /////////////////
         Integer statusCode = runProcessOutput.getValueInteger("statusCode");
         if(statusCode != null)
         {
            context.status(statusCode);
         }

         /////////////////
         // headers map //
         /////////////////
         Serializable headers = runProcessOutput.getValue("responseHeaders");
         if(headers instanceof Map headersMap)
         {
            for(Object key : headersMap.keySet())
            {
               context.header(ValueUtils.getValueAsString(key), ValueUtils.getValueAsString(headersMap.get(key)));
            }
         }

         // todo - make the inputStream available to the process
         //  maybe via the callback object??? input.setCallback(new QProcessCallback() {});
         //  context.resultInputStream();

         ///////////////////
         // response body //
         ///////////////////
         Serializable response = runProcessOutput.getValue("response");
         if(response instanceof String s)
         {
            context.result(s);
         }
         else if(response instanceof byte[] ba)
         {
            context.result(ba);
         }
         else if(response instanceof InputStream is)
         {
            context.result(is);
         }
         else
         {
            context.result(ValueUtils.getValueAsString(response));
         }
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

}
