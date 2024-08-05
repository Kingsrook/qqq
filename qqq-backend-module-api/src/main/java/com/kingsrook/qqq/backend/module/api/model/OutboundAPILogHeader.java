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
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Entity bean for OutboundApiLog table
 *******************************************************************************/
public class OutboundAPILogHeader extends QRecordEntity
{
   public static final String TABLE_NAME = "outboundApiLogHeader";

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

   @QAssociation(name = OutboundAPILogRequest.TABLE_NAME)
   private List<OutboundAPILogRequest> outboundAPILogRequestList;

   @QAssociation(name = OutboundAPILogResponse.TABLE_NAME)
   private List<OutboundAPILogResponse> outboundAPILogResponseList;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILogHeader()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILogHeader(QRecord qRecord) throws QException
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
   public OutboundAPILogHeader withId(Integer id)
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
   public OutboundAPILogHeader withTimestamp(Instant timestamp)
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
   public OutboundAPILogHeader withMethod(String method)
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
   public OutboundAPILogHeader withStatusCode(Integer statusCode)
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
   public OutboundAPILogHeader withUrl(String url)
   {
      this.url = url;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outboundAPILogRequestList
    *******************************************************************************/
   public List<OutboundAPILogRequest> getOutboundAPILogRequestList()
   {
      return (this.outboundAPILogRequestList);
   }



   /*******************************************************************************
    ** Setter for outboundAPILogRequestList
    *******************************************************************************/
   public void setOutboundAPILogRequestList(List<OutboundAPILogRequest> outboundAPILogRequestList)
   {
      this.outboundAPILogRequestList = outboundAPILogRequestList;
   }



   /*******************************************************************************
    ** Fluent setter for outboundAPILogRequestList
    *******************************************************************************/
   public OutboundAPILogHeader withOutboundAPILogRequestList(List<OutboundAPILogRequest> outboundAPILogRequestList)
   {
      this.outboundAPILogRequestList = outboundAPILogRequestList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outboundAPILogResponseList
    *******************************************************************************/
   public List<OutboundAPILogResponse> getOutboundAPILogResponseList()
   {
      return (this.outboundAPILogResponseList);
   }



   /*******************************************************************************
    ** Setter for outboundAPILogResponseList
    *******************************************************************************/
   public void setOutboundAPILogResponseList(List<OutboundAPILogResponse> outboundAPILogResponseList)
   {
      this.outboundAPILogResponseList = outboundAPILogResponseList;
   }



   /*******************************************************************************
    ** Fluent setter for outboundAPILogResponseList
    *******************************************************************************/
   public OutboundAPILogHeader withOutboundAPILogResponseList(List<OutboundAPILogResponse> outboundAPILogResponseList)
   {
      this.outboundAPILogResponseList = outboundAPILogResponseList;
      return (this);
   }

}

