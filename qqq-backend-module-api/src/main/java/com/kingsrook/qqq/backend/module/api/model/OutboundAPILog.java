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

package com.kingsrook.qqq.backend.module.api.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Entity bean for OutboundApiLog table
 *******************************************************************************/
public class OutboundAPILog extends QRecordEntity
{
   public static final String TABLE_NAME = "outboundApiLog";

   @QField(isEditable = false)
   private Integer id;

   @QField()
   private Instant timestamp;

   @QField(possibleValueSourceName = "outboundApiMethod")
   private String method;

   @QField(possibleValueSourceName = "outboundApiStatusCode")
   private Integer statusCode;

   @QField(label = "URL")
   private String url;

   @QField()
   private String requestBody;

   @QField()
   private String responseBody;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILog()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILog(QRecord qRecord) throws QException
   {
      populateFromQRecord(qRecord);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Setter for id
    **
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    **
    *******************************************************************************/
   public OutboundAPILog withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for timestamp
    **
    *******************************************************************************/
   public Instant getTimestamp()
   {
      return timestamp;
   }



   /*******************************************************************************
    ** Setter for timestamp
    **
    *******************************************************************************/
   public void setTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
   }



   /*******************************************************************************
    ** Fluent setter for timestamp
    **
    *******************************************************************************/
   public OutboundAPILog withTimestamp(Instant timestamp)
   {
      this.timestamp = timestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for method
    **
    *******************************************************************************/
   public String getMethod()
   {
      return method;
   }



   /*******************************************************************************
    ** Setter for method
    **
    *******************************************************************************/
   public void setMethod(String method)
   {
      this.method = method;
   }



   /*******************************************************************************
    ** Fluent setter for method
    **
    *******************************************************************************/
   public OutboundAPILog withMethod(String method)
   {
      this.method = method;
      return (this);
   }



   /*******************************************************************************
    ** Getter for statusCode
    **
    *******************************************************************************/
   public Integer getStatusCode()
   {
      return statusCode;
   }



   /*******************************************************************************
    ** Setter for statusCode
    **
    *******************************************************************************/
   public void setStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
   }



   /*******************************************************************************
    ** Fluent setter for statusCode
    **
    *******************************************************************************/
   public OutboundAPILog withStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for url
    **
    *******************************************************************************/
   public String getUrl()
   {
      return url;
   }



   /*******************************************************************************
    ** Setter for url
    **
    *******************************************************************************/
   public void setUrl(String url)
   {
      this.url = url;
   }



   /*******************************************************************************
    ** Fluent setter for url
    **
    *******************************************************************************/
   public OutboundAPILog withUrl(String url)
   {
      this.url = url;
      return (this);
   }



   /*******************************************************************************
    ** Getter for requestBody
    **
    *******************************************************************************/
   public String getRequestBody()
   {
      return requestBody;
   }



   /*******************************************************************************
    ** Setter for requestBody
    **
    *******************************************************************************/
   public void setRequestBody(String requestBody)
   {
      this.requestBody = requestBody;
   }



   /*******************************************************************************
    ** Fluent setter for requestBody
    **
    *******************************************************************************/
   public OutboundAPILog withRequestBody(String requestBody)
   {
      this.requestBody = requestBody;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseBody
    **
    *******************************************************************************/
   public String getResponseBody()
   {
      return responseBody;
   }



   /*******************************************************************************
    ** Setter for responseBody
    **
    *******************************************************************************/
   public void setResponseBody(String responseBody)
   {
      this.responseBody = responseBody;
   }



   /*******************************************************************************
    ** Fluent setter for responseBody
    **
    *******************************************************************************/
   public OutboundAPILog withResponseBody(String responseBody)
   {
      this.responseBody = responseBody;
      return (this);
   }

}

