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
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessState;
import com.kingsrook.qqq.backend.core.model.actions.processes.QProcessPayload;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** process payload shared the processes which are used as process-based-router
 ** processes.  e.g., the fields here are those written to and read by
 ** ProcessBasedRouter.
 *******************************************************************************/
public class ProcessBasedRouterPayload extends QProcessPayload
{
   private String                    path;
   private String                    method;
   private Map<String, String>       pathParams;
   private Map<String, List<String>> queryParams;
   private Map<String, List<String>> formParams;
   private Map<String, String>       cookies;
   private String                    bodyString;

   private Integer             statusCode;
   private String              redirectURL;
   private Map<String, String> responseHeaders;
   private String              responseString;
   private byte[]              responseBytes;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessBasedRouterPayload()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ProcessBasedRouterPayload(ProcessState processState)
   {
      this.populateFromProcessState(processState);
   }



   /***************************************************************************
    ** for the common use-case, get a single formParam by name (vs the list that the
    ** actual proper formal interface would give).
    ***************************************************************************/
   public String getFormParam(String name)
   {
      if(formParams != null)
      {
         List<String> values = formParams.get(name);
         if(CollectionUtils.nullSafeHasContents(values))
         {
            return values.get(0);
         }
      }

      return (null);
   }



   /***************************************************************************
    ** for the common use-case, get a single queryParam by name (vs the list that the
    ** actual proper formal interface would give).
    ***************************************************************************/
   public String getQueryParam(String name)
   {
      if(queryParams != null)
      {
         List<String> values = queryParams.get(name);
         if(CollectionUtils.nullSafeHasContents(values))
         {
            return values.get(0);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for path
    *******************************************************************************/
   public String getPath()
   {
      return (this.path);
   }



   /*******************************************************************************
    ** Setter for path
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    *******************************************************************************/
   public ProcessBasedRouterPayload withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for method
    *******************************************************************************/
   public String getMethod()
   {
      return (this.method);
   }



   /*******************************************************************************
    ** Setter for method
    *******************************************************************************/
   public void setMethod(String method)
   {
      this.method = method;
   }



   /*******************************************************************************
    ** Fluent setter for method
    *******************************************************************************/
   public ProcessBasedRouterPayload withMethod(String method)
   {
      this.method = method;
      return (this);
   }



   /*******************************************************************************
    ** Getter for pathParams
    *******************************************************************************/
   public Map<String, String> getPathParams()
   {
      return (this.pathParams);
   }



   /*******************************************************************************
    ** Setter for pathParams
    *******************************************************************************/
   public void setPathParams(Map<String, String> pathParams)
   {
      this.pathParams = pathParams;
   }



   /*******************************************************************************
    ** Fluent setter for pathParams
    *******************************************************************************/
   public ProcessBasedRouterPayload withPathParams(Map<String, String> pathParams)
   {
      this.pathParams = pathParams;
      return (this);
   }



   /*******************************************************************************
    ** Getter for cookies
    *******************************************************************************/
   public Map<String, String> getCookies()
   {
      return (this.cookies);
   }



   /*******************************************************************************
    ** Setter for cookies
    *******************************************************************************/
   public void setCookies(Map<String, String> cookies)
   {
      this.cookies = cookies;
   }



   /*******************************************************************************
    ** Fluent setter for cookies
    *******************************************************************************/
   public ProcessBasedRouterPayload withCookies(Map<String, String> cookies)
   {
      this.cookies = cookies;
      return (this);
   }



   /*******************************************************************************
    ** Getter for statusCode
    *******************************************************************************/
   public Integer getStatusCode()
   {
      return (this.statusCode);
   }



   /*******************************************************************************
    ** Setter for statusCode
    *******************************************************************************/
   public void setStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Fluent setter for statusCode
    *******************************************************************************/
   public ProcessBasedRouterPayload withStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseHeaders
    *******************************************************************************/
   public Map<String, String> getResponseHeaders()
   {
      return (this.responseHeaders);
   }



   /*******************************************************************************
    ** Setter for responseHeaders
    *******************************************************************************/
   public void setResponseHeaders(Map<String, String> responseHeaders)
   {
      this.responseHeaders = responseHeaders;
   }



   /*******************************************************************************
    ** Fluent setter for responseHeaders
    *******************************************************************************/
   public ProcessBasedRouterPayload withResponseHeaders(Map<String, String> responseHeaders)
   {
      this.responseHeaders = responseHeaders;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseString
    *******************************************************************************/
   public String getResponseString()
   {
      return (this.responseString);
   }



   /*******************************************************************************
    ** Setter for responseString
    *******************************************************************************/
   public void setResponseString(String responseString)
   {
      this.responseString = responseString;
   }



   /*******************************************************************************
    ** Fluent setter for responseString
    *******************************************************************************/
   public ProcessBasedRouterPayload withResponseString(String responseString)
   {
      this.responseString = responseString;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseBytes
    *******************************************************************************/
   public byte[] getResponseBytes()
   {
      return (this.responseBytes);
   }



   /*******************************************************************************
    ** Setter for responseBytes
    *******************************************************************************/
   public void setResponseBytes(byte[] responseBytes)
   {
      this.responseBytes = responseBytes;
   }



   /*******************************************************************************
    ** Fluent setter for responseBytes
    *******************************************************************************/
   public ProcessBasedRouterPayload withResponseBytes(byte[] responseBytes)
   {
      this.responseBytes = responseBytes;
      return (this);
   }



   /*******************************************************************************
    ** Getter for redirectURL
    *******************************************************************************/
   public String getRedirectURL()
   {
      return (this.redirectURL);
   }



   /*******************************************************************************
    ** Setter for redirectURL
    *******************************************************************************/
   public void setRedirectURL(String redirectURL)
   {
      this.redirectURL = redirectURL;
   }



   /*******************************************************************************
    ** Fluent setter for redirectURL
    *******************************************************************************/
   public ProcessBasedRouterPayload withRedirectURL(String redirectURL)
   {
      this.redirectURL = redirectURL;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryParams
    *******************************************************************************/
   public Map<String, List<String>> getQueryParams()
   {
      return (this.queryParams);
   }



   /*******************************************************************************
    ** Setter for queryParams
    *******************************************************************************/
   public void setQueryParams(Map<String, List<String>> queryParams)
   {
      this.queryParams = queryParams;
   }



   /*******************************************************************************
    ** Fluent setter for queryParams
    *******************************************************************************/
   public ProcessBasedRouterPayload withQueryParams(Map<String, List<String>> queryParams)
   {
      this.queryParams = queryParams;
      return (this);
   }



   /*******************************************************************************
    ** Getter for formParams
    *******************************************************************************/
   public Map<String, List<String>> getFormParams()
   {
      return (this.formParams);
   }



   /*******************************************************************************
    ** Setter for formParams
    *******************************************************************************/
   public void setFormParams(Map<String, List<String>> formParams)
   {
      this.formParams = formParams;
   }



   /*******************************************************************************
    ** Fluent setter for formParams
    *******************************************************************************/
   public ProcessBasedRouterPayload withFormParams(Map<String, List<String>> formParams)
   {
      this.formParams = formParams;
      return (this);
   }


   /*******************************************************************************
    ** Getter for bodyString
    *******************************************************************************/
   public String getBodyString()
   {
      return (this.bodyString);
   }



   /*******************************************************************************
    ** Setter for bodyString
    *******************************************************************************/
   public void setBodyString(String bodyString)
   {
      this.bodyString = bodyString;
   }



   /*******************************************************************************
    ** Fluent setter for bodyString
    *******************************************************************************/
   public ProcessBasedRouterPayload withBodyString(String bodyString)
   {
      this.bodyString = bodyString;
      return (this);
   }


}
