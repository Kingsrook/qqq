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

package com.kingsrook.qqq.api.model;


import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.api.model.metadata.APILogMetaDataProvider;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** In addition to the standard/known fields in this entity, you can also add
 ** name/value pairs of security key values - e.g., a clientId field
 *******************************************************************************/
public class APILog extends QRecordEntity
{
   public static final String TABLE_NAME = "apiLog";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant timestamp;

   @QField(possibleValueSourceName = APILogMetaDataProvider.TABLE_NAME_API_LOG_USER, label = "User")
   private Integer apiLogUserId;

   @QField()
   private String method;

   @QField()
   private Integer statusCode;

   @QField()
   private String version;

   @QField()
   private String path;

   @QField()
   private String queryString;

   @QField()
   private String requestBody;

   @QField()
   private String responseBody;

   private Map<String, Serializable> securityKeyValues = new HashMap<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public APILog()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QRecord toQRecord() throws QRuntimeException
   {
      QRecord qRecord = super.toQRecord();

      for(Map.Entry<String, Serializable> entry : CollectionUtils.nonNullMap(this.securityKeyValues).entrySet())
      {
         qRecord.setValue(entry.getKey(), entry.getValue());
      }

      return (qRecord);
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public APILog(QRecord qRecord) throws QException
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
   public APILog withId(Integer id)
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
   public APILog withTimestamp(Instant timestamp)
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
   public APILog withMethod(String method)
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
   public APILog withStatusCode(Integer statusCode)
   {
      this.statusCode = statusCode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for version
    **
    *******************************************************************************/
   public String getVersion()
   {
      return version;
   }



   /*******************************************************************************
    ** Setter for version
    **
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.version = version;
   }



   /*******************************************************************************
    ** Fluent setter for version
    **
    *******************************************************************************/
   public APILog withVersion(String version)
   {
      this.version = version;
      return (this);
   }



   /*******************************************************************************
    ** Getter for path
    **
    *******************************************************************************/
   public String getPath()
   {
      return path;
   }



   /*******************************************************************************
    ** Setter for path
    **
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    **
    *******************************************************************************/
   public APILog withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryString
    **
    *******************************************************************************/
   public String getQueryString()
   {
      return queryString;
   }



   /*******************************************************************************
    ** Setter for queryString
    **
    *******************************************************************************/
   public void setQueryString(String queryString)
   {
      this.queryString = queryString;
   }



   /*******************************************************************************
    ** Fluent setter for queryString
    **
    *******************************************************************************/
   public APILog withQueryString(String queryString)
   {
      this.queryString = queryString;
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
   public APILog withRequestBody(String requestBody)
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
   public APILog withResponseBody(String responseBody)
   {
      this.responseBody = responseBody;
      return (this);
   }



   /*******************************************************************************
    ** Getter for securityKeyValues
    *******************************************************************************/
   public Map<String, Serializable> getSecurityKeyValues()
   {
      return (this.securityKeyValues);
   }



   /*******************************************************************************
    ** Setter for securityKeyValues
    *******************************************************************************/
   public void setSecurityKeyValues(Map<String, Serializable> securityKeyValues)
   {
      this.securityKeyValues = securityKeyValues;
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyValues
    *******************************************************************************/
   public APILog withSecurityKeyValues(Map<String, Serializable> securityKeyValues)
   {
      this.securityKeyValues = securityKeyValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for securityKeyValues
    *******************************************************************************/
   public APILog withSecurityKeyValue(String key, Serializable value)
   {
      if(this.securityKeyValues == null)
      {
         this.securityKeyValues = new HashMap<>();
      }
      this.securityKeyValues.put(key, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiLogUserId
    *******************************************************************************/
   public Integer getApiLogUserId()
   {
      return (this.apiLogUserId);
   }



   /*******************************************************************************
    ** Setter for apiLogUserId
    *******************************************************************************/
   public void setApiLogUserId(Integer apiLogUserId)
   {
      this.apiLogUserId = apiLogUserId;
   }



   /*******************************************************************************
    ** Fluent setter for apiLogUserId
    *******************************************************************************/
   public APILog withApiLogUserId(Integer apiLogUserId)
   {
      this.apiLogUserId = apiLogUserId;
      return (this);
   }

}
