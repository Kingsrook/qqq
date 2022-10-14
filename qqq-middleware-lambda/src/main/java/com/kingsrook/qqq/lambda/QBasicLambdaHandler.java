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

package com.kingsrook.qqq.lambda;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.lambda.model.QLambdaRequest;
import com.kingsrook.qqq.lambda.model.QLambdaResponse;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONException;
import org.json.JSONObject;


/*******************************************************************************
 ** QQQ base class for lambda handlers. Meant to be sub-classed, where QQQ apps
 ** can just override `handleJsonRequest`, and avoid seeing the lambda-ness of
 ** lambda.
 **
 ** Such subclasses can then have easy standalone unit tests - just testing their
 ** logic, and not the lambda-ness.
 *******************************************************************************/
public class QBasicLambdaHandler implements RequestStreamHandler
{
   private static Logger LOG; // = LogManager.getLogger(QBasicLambdaHandler.class);

   private Context context;

   @SuppressWarnings("checkstyle:MemberName")
   protected final QLambdaResponse GENERIC_SERVER_ERROR = new QLambdaResponse(500, "Internal Server Error");
   protected final QLambdaResponse OK                   = new QLambdaResponse(200);



   /*******************************************************************************
    ** Entrypoint from AWS Lambda.
    *******************************************************************************/
   public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
   {
      this.context = context;

      ///////////////////////////////////////////////////////////////////////
      // tell log4j to use a config that won't fail when running in lambda //
      ///////////////////////////////////////////////////////////////////////
      Configurator.initialize(null, "qqq-lambda-log4j2.xml");

      String requestId = "unknown";
      try
      {
         String input = IOUtils.toString(inputStream, "UTF-8");
         log("Full Input: " + input);

         //////////////////////////////////////////////////////////////////////////////////
         // parse the input as json - then pull parts out of it that we know to look for //
         //////////////////////////////////////////////////////////////////////////////////
         JSONObject inputJsonObject;
         try
         {
            inputJsonObject = JsonUtils.toJSONObject(input);
         }
         catch(JSONException je)
         {
            writeResponse(outputStream, requestId, new QLambdaResponse(400, "Unable to parse input as JSON: " + je.getMessage()));
            return;
         }

         JSONObject headers        = inputJsonObject.optJSONObject("headers");
         String     contentType    = headers != null ? headers.optString("content-type") : null;
         String     path           = inputJsonObject.optString("rawPath");
         String     queryString    = inputJsonObject.optString("rawQueryString");
         JSONObject requestContext = inputJsonObject.optJSONObject("requestContext");
         requestId = requestContext.optString("requestId");

         if("application/json".equals(contentType))
         {
            String body = inputJsonObject.optString("body");

            JSONObject bodyJsonObject;
            try
            {
               bodyJsonObject = JsonUtils.toJSONObject(body);
            }
            catch(JSONException je)
            {
               writeResponse(outputStream, requestId, new QLambdaResponse(400, "Unable to parse request body as JSON: " + je.getMessage()));
               return;
            }

            QLambdaResponse response = handleJsonRequest(new QLambdaRequest(headers, path, queryString, bodyJsonObject));
            writeResponse(outputStream, requestId, response);
         }
         else
         {
            writeResponse(outputStream, requestId, new QLambdaResponse(400, "Unsupported content-type: " + contentType));
         }
      }
      catch(QUserFacingException ufe)
      {
         writeResponse(outputStream, requestId, new QLambdaResponse(400, "Error handing request: " + ufe.getMessage()));
      }
      catch(Exception e)
      {
         writeResponse(outputStream, requestId, new QLambdaResponse(500, "Uncaught error handing request: " + e.getMessage()));
      }
   }



   /*******************************************************************************
    ** Meant to be overridden by subclasses, to provide functionality, if needed.
    *******************************************************************************/
   protected QLambdaResponse handleJsonRequest(QLambdaRequest request) throws QException
   {
      log(this.getClass().getSimpleName() + " did not override handleJsonRequest - so noop and return 200.");
      return (OK);
   }



   /*******************************************************************************
    ** Write to the cloudwatch logs.
    *******************************************************************************/
   protected void log(String message)
   {
      if(message == null)
      {
         return;
      }

      if(this.context != null && this.context.getLogger() != null)
      {
         context.getLogger().log(message + (message.endsWith("\n") ? "" : "\n"));
      }
      else
      {
         initLOG();
         LOG.info(message);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void log(Throwable e)
   {
      if(e == null)
      {
         return;
      }

      if(this.context != null && this.context.getLogger() != null)
      {
         StringWriter sw = new StringWriter();
         PrintWriter  pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         log(sw + "\n");
      }
      else
      {
         initLOG();
         LOG.warn("Exception", e);
      }
   }



   /*******************************************************************************
    ** So, when running inside lambda, we cannot
    *******************************************************************************/
   private void initLOG()
   {
      if(LOG == null)
      {
         LOG = LogManager.getLogger(QBasicLambdaHandler.class);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void writeResponse(OutputStream outputStream, String requestId, QLambdaResponse response) throws IOException
   {
      QLambdaResponse.Body body = response.getBody();
      body.setRequestId(requestId);

      outputStream.write(JsonUtils.toJson(body).getBytes(StandardCharsets.UTF_8));
   }

}
