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


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Entity bean for OutboundApiLogResponse table
 *******************************************************************************/
public class OutboundAPILogResponse extends QRecordEntity
{
   public static final String TABLE_NAME = "outboundApiLogResponse";

   @QField(isEditable = false)
   private Integer id;

   @QField(possibleValueSourceName = OutboundAPILogHeader.TABLE_NAME)
   private Integer outboundApiLogHeaderId;

   @QField()
   private String responseBody;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILogResponse()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public OutboundAPILogResponse(QRecord qRecord) throws QException
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
   public OutboundAPILogResponse withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for responseBody
    *******************************************************************************/
   public String getResponseBody()
   {
      return (this.responseBody);
   }



   /*******************************************************************************
    ** Setter for responseBody
    *******************************************************************************/
   public void setResponseBody(String responseBody)
   {
      this.responseBody = responseBody;
   }



   /*******************************************************************************
    ** Fluent setter for responseBody
    *******************************************************************************/
   public OutboundAPILogResponse withResponseBody(String responseBody)
   {
      this.responseBody = responseBody;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outboundApiLogHeaderId
    *******************************************************************************/
   public Integer getOutboundApiLogHeaderId()
   {
      return (this.outboundApiLogHeaderId);
   }



   /*******************************************************************************
    ** Setter for outboundApiLogHeaderId
    *******************************************************************************/
   public void setOutboundApiLogHeaderId(Integer outboundApiLogHeaderId)
   {
      this.outboundApiLogHeaderId = outboundApiLogHeaderId;
   }



   /*******************************************************************************
    ** Fluent setter for outboundApiLogHeaderId
    *******************************************************************************/
   public OutboundAPILogResponse withOutboundApiLogHeaderId(Integer outboundApiLogHeaderId)
   {
      this.outboundApiLogHeaderId = outboundApiLogHeaderId;
      return (this);
   }

}

