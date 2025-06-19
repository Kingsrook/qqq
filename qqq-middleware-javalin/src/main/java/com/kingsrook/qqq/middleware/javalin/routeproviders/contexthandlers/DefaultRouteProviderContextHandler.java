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

package com.kingsrook.qqq.middleware.javalin.routeproviders.contexthandlers;


import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.tables.StorageAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.storage.StorageInput;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;


/*******************************************************************************
 ** default implementation of this interface.  reads the request body as a string
 *******************************************************************************/
public class DefaultRouteProviderContextHandler implements RouteProviderContextHandlerInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void handleRequest(Context context, RunProcessInput input)
   {
      input.addValue("path", context.path());
      input.addValue("method", context.method());
      input.addValue("pathParams", new HashMap<>(context.pathParamMap()));
      input.addValue("queryParams", new HashMap<>(context.queryParamMap()));
      input.addValue("cookies", new HashMap<>(context.cookieMap()));
      input.addValue("requestHeaders", new HashMap<>(context.headerMap()));

      handleRequestBody(context, input);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void handleRequestBody(Context context, RunProcessInput input)
   {
      input.addValue("formParams", new HashMap<>(context.formParamMap()));
      input.addValue("bodyString", context.body());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public boolean handleResponse(Context context, RunProcessOutput runProcessOutput) throws QException
   {
      handleResponseHeaders(context, runProcessOutput);

      //////////////
      // response //
      //////////////
      Integer statusCode  = runProcessOutput.getValueInteger("statusCode");
      String  redirectURL = runProcessOutput.getValueString("redirectURL");

      if(StringUtils.hasContent(redirectURL))
      {
         context.redirect(redirectURL, statusCode == null ? HttpStatus.FOUND : HttpStatus.forStatus(statusCode));
         return true;
      }

      if(statusCode != null)
      {
         context.status(statusCode);
      }

      if(handleResponseBody(context, runProcessOutput))
      {
         return true;
      }

      return false;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void handleResponseHeaders(Context context, RunProcessOutput runProcessOutput)
   {
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
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected boolean handleResponseBody(Context context, RunProcessOutput runProcessOutput) throws QException
   {
      String       responseString       = runProcessOutput.getValueString("responseString");
      byte[]       responseBytes        = runProcessOutput.getValueByteArray("responseBytes");
      StorageInput responseStorageInput = (StorageInput) runProcessOutput.getValue("responseStorageInput");
      if(StringUtils.hasContent(responseString))
      {
         context.result(responseString);
         return true;
      }

      if(responseBytes != null && responseBytes.length > 0)
      {
         context.result(responseBytes);
         return true;
      }

      if(responseStorageInput != null)
      {
         InputStream inputStream = new StorageAction().getInputStream(responseStorageInput);
         context.result(inputStream);
         return true;
      }
      return false;
   }

}
