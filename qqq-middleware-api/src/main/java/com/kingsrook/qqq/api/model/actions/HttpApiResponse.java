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

package com.kingsrook.qqq.api.model.actions;


import java.io.Serializable;
import org.eclipse.jetty.http.HttpStatus;


/*******************************************************************************
 ** class to contain http api responses.
 **
 *******************************************************************************/
public class HttpApiResponse
{
   private HttpStatus.Code statusCode;
   private Serializable    responseBodyObject;

   private String  contentType;

   ////////////////////////////////////////////////////////////////////////////////////////////////////////
   // by default - QJavalinApiHandler will format the responseBodyObject as JSON.                        //
   // set this field to false if you don't want it to do that (e.g., if your response is, say, a byte[]) //
   ////////////////////////////////////////////////////////////////////////////////////////////////////////
   private boolean needsFormattedAsJson = true;



   /*******************************************************************************
    ** Default Constructor
    **
    *******************************************************************************/
   public HttpApiResponse()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public HttpApiResponse(HttpStatus.Code statusCode, Serializable responseBodyObject)
   {
      this.statusCode = statusCode;
      this.responseBodyObject = responseBodyObject;
   }



   /*******************************************************************************
    ** Getter for statusCode
    *******************************************************************************/
   public HttpStatus.Code getStatusCode()
   {
      return (this.statusCode);
   }



   /*******************************************************************************
    ** Setter for statusCode
    *******************************************************************************/
   public void setStatusCode(HttpStatus.Code statusCode)
   {
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Fluent setter for statusCode
    *******************************************************************************/
   public HttpApiResponse withStatusCode(HttpStatus.Code statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseBodyObject
    *******************************************************************************/
   public Serializable getResponseBodyObject()
   {
      return (this.responseBodyObject);
   }



   /*******************************************************************************
    ** Setter for responseBodyObject
    *******************************************************************************/
   public void setResponseBodyObject(Serializable responseBodyObject)
   {
      this.responseBodyObject = responseBodyObject;
   }



   /*******************************************************************************
    ** Fluent setter for responseBodyObject
    *******************************************************************************/
   public HttpApiResponse withResponseBodyObject(Serializable responseBodyObject)
   {
      this.responseBodyObject = responseBodyObject;
      return (this);
   }



   /*******************************************************************************
    ** Getter for needsFormattedAsJson
    *******************************************************************************/
   public boolean getNeedsFormattedAsJson()
   {
      return (this.needsFormattedAsJson);
   }



   /*******************************************************************************
    ** Setter for needsFormattedAsJson
    *******************************************************************************/
   public void setNeedsFormattedAsJson(boolean needsFormattedAsJson)
   {
      this.needsFormattedAsJson = needsFormattedAsJson;
   }



   /*******************************************************************************
    ** Fluent setter for needsFormattedAsJson
    *******************************************************************************/
   public HttpApiResponse withNeedsFormattedAsJson(boolean needsFormattedAsJson)
   {
      this.needsFormattedAsJson = needsFormattedAsJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for contentType
    *******************************************************************************/
   public String getContentType()
   {
      return (this.contentType);
   }



   /*******************************************************************************
    ** Setter for contentType
    *******************************************************************************/
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }



   /*******************************************************************************
    ** Fluent setter for contentType
    *******************************************************************************/
   public HttpApiResponse withContentType(String contentType)
   {
      this.contentType = contentType;
      return (this);
   }

}