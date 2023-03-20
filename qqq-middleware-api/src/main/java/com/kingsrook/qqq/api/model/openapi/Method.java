/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.api.model.openapi;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/*******************************************************************************
 **
 *******************************************************************************/
public class Method
{
   private String                 method;
   private String                 summary;
   private String                 description;
   private String                 operationId;
   private List<String>           tags;
   private RequestBody            requestBody;
   private List<Parameter>        parameters;
   private Map<Integer, Response> responses;



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
   public Method withMethod(String method)
   {
      this.method = method;
      return (this);
   }



   /*******************************************************************************
    ** Getter for summary
    *******************************************************************************/
   public String getSummary()
   {
      return (this.summary);
   }



   /*******************************************************************************
    ** Setter for summary
    *******************************************************************************/
   public void setSummary(String summary)
   {
      this.summary = summary;
   }



   /*******************************************************************************
    ** Fluent setter for summary
    *******************************************************************************/
   public Method withSummary(String summary)
   {
      this.summary = summary;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public Method withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for operationId
    *******************************************************************************/
   public String getOperationId()
   {
      return (this.operationId);
   }



   /*******************************************************************************
    ** Setter for operationId
    *******************************************************************************/
   public void setOperationId(String operationId)
   {
      this.operationId = operationId;
   }



   /*******************************************************************************
    ** Fluent setter for operationId
    *******************************************************************************/
   public Method withOperationId(String operationId)
   {
      this.operationId = operationId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tags
    *******************************************************************************/
   public List<String> getTags()
   {
      return (this.tags);
   }



   /*******************************************************************************
    ** Setter for tags
    *******************************************************************************/
   public void setTags(List<String> tags)
   {
      this.tags = tags;
   }



   /*******************************************************************************
    ** Fluent setter for tags
    *******************************************************************************/
   public Method withTags(List<String> tags)
   {
      this.tags = tags;
      return (this);
   }



   /*******************************************************************************
    ** Getter for requestBody
    *******************************************************************************/
   public RequestBody getRequestBody()
   {
      return (this.requestBody);
   }



   /*******************************************************************************
    ** Setter for requestBody
    *******************************************************************************/
   public void setRequestBody(RequestBody requestBody)
   {
      this.requestBody = requestBody;
   }



   /*******************************************************************************
    ** Fluent setter for requestBody
    *******************************************************************************/
   public Method withRequestBody(RequestBody requestBody)
   {
      this.requestBody = requestBody;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parameters
    *******************************************************************************/
   public List<Parameter> getParameters()
   {
      return (this.parameters);
   }



   /*******************************************************************************
    ** Setter for parameters
    *******************************************************************************/
   public void setParameters(List<Parameter> parameters)
   {
      this.parameters = parameters;
   }



   /*******************************************************************************
    ** Fluent setter for parameters
    *******************************************************************************/
   public Method withParameters(List<Parameter> parameters)
   {
      this.parameters = parameters;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responses
    *******************************************************************************/
   public Map<Integer, Response> getResponses()
   {
      return (this.responses);
   }



   /*******************************************************************************
    ** Setter for responses
    *******************************************************************************/
   public void setResponses(Map<Integer, Response> responses)
   {
      this.responses = responses;
   }



   /*******************************************************************************
    ** Fluent setter for responses
    *******************************************************************************/
   public Method withResponses(Map<Integer, Response> responses)
   {
      this.responses = responses;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Method withResponse(Integer code, Response response)
   {
      if(this.responses == null)
      {
         this.responses = new LinkedHashMap<>();
      }
      this.responses.put(code, response);
      return (this);
   }
}
