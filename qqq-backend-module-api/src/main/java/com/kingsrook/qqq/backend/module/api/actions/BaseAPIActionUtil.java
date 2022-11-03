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

package com.kingsrook.qqq.backend.module.api.actions;


import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;
import com.kingsrook.qqq.backend.module.api.model.metadata.APITableBackendDetails;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 ** Base class for utility functions that make up the unique ways in which an
 ** API can be implemented.
 *******************************************************************************/
public class BaseAPIActionUtil
{
   private static final Logger LOG = LogManager.getLogger(BaseAPIActionUtil.class);

   protected APIBackendMetaData       backendMetaData;
   protected AbstractTableActionInput actionInput;



   /*******************************************************************************
    **
    *******************************************************************************/
   public long getMillisToSleepAfterEveryCall()
   {
      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int getInitialRateLimitBackoffMillis()
   {
      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public int getMaxAllowedRateLimitErrors()
   {
      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer getApiStandardLimit()
   {
      return (20);
   }



   /*******************************************************************************
    ** method to build up a query string based on a given QFilter object
    **
    *******************************************************************************/
   protected String buildQueryStringForGet(QQueryFilter filter, Integer limit, Integer skip, Map<String, QFieldMetaData> fields) throws QException
   {
      // todo: reasonable default action
      return (null);
   }



   /*******************************************************************************
    ** Do a default query string for a single-record GET - e.g., a query for just 1 record.
    *******************************************************************************/
   public String buildUrlSuffixForSingleRecordGet(Serializable primaryKey) throws QException
   {
      QTableMetaData table = actionInput.getTable();
      QQueryFilter filter = new QQueryFilter()
         .withCriteria(new QFilterCriteria(table.getPrimaryKeyField(), QCriteriaOperator.EQUALS, List.of(primaryKey)));
      return (buildQueryStringForGet(filter, 1, 0, table.getFields()));
   }



   /*******************************************************************************
    ** As part of making a request - set up its authorization header (not just
    ** strictly "Authorization", but whatever is needed for auth).
    **
    ** Can be overridden if an API uses an authorization type we don't natively support.
    *******************************************************************************/
   protected void setupAuthorizationInRequest(HttpRequestBase request)
   {
      switch(backendMetaData.getAuthorizationType())
      {
         case BASIC_AUTH_API_KEY:
            request.addHeader("Authorization", getBasicAuthenticationHeader(backendMetaData.getApiKey()));
            break;

         case BASIC_AUTH_USERNAME_PASSWORD:
            request.addHeader("Authorization", getBasicAuthenticationHeader(backendMetaData.getUsername(), backendMetaData.getPassword()));
            break;

         case API_KEY_HEADER:
            request.addHeader("API-Key", backendMetaData.getApiKey());
            break;

         default:
            throw new IllegalArgumentException("Unexpected authorization type: " + backendMetaData.getAuthorizationType());
      }
   }



   /*******************************************************************************
    ** As part of making a request - set up its content-type header.
    *******************************************************************************/
   protected void setupContentTypeInRequest(HttpRequestBase request)
   {
      request.addHeader("Content-Type", backendMetaData.getContentType());
   }



   /*******************************************************************************
    ** Helper method to create a value for an Authentication header, using just an
    ** apiKey - encoded as Basic + base64(apiKey)
    *******************************************************************************/
   protected String getBasicAuthenticationHeader(String apiKey)
   {
      return "Basic " + Base64.getEncoder().encodeToString(apiKey.getBytes());
   }



   /*******************************************************************************
    ** As part of making a request - set up additional headers.  Noop in base -
    ** meant to override in subclasses.
    *******************************************************************************/
   public void setupAdditionalHeaders(HttpRequestBase request)
   {
      request.addHeader("Accept", "application/json");
   }



   /*******************************************************************************
    ** Helper method to create a value for an Authentication header, using just a
    ** username & password - encoded as Basic + base64(username:password)
    *******************************************************************************/
   protected String getBasicAuthenticationHeader(String username, String password)
   {
      return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
   }



   /*******************************************************************************
    ** Helper method to build the URL for a table.  Can be overridden, if a
    ** particular API isn't just "base" + "tablePath".
    **
    ** Note:  you may want to look at the actionInput object, to help figure out
    ** what path you need, depending on your API.
    *******************************************************************************/
   protected String buildTableUrl(QTableMetaData table)
   {
      return (backendMetaData.getBaseUrl() + getBackendDetails(table).getTablePath());
   }



   /*******************************************************************************
    ** Build an HTTP Entity (e.g., for a PUT or POST) from a QRecord.  Can be
    ** overridden if an API doesn't do a basic json object.  Or, can override a
    ** helper method, such as recordToJsonObject.
    **
    *******************************************************************************/
   protected AbstractHttpEntity recordToEntity(QTableMetaData table, QRecord record) throws IOException
   {
      JSONObject body = recordToJsonObject(table, record);
      String     json = body.toString();

      String tablePath = getBackendDetails(table).getTablePath();
      if(tablePath != null)
      {
         body = new JSONObject();
         body.put(tablePath, new JSONObject(json));
         json = body.toString();
      }
      LOG.debug(json);
      return (new StringEntity(json));
   }



   /*******************************************************************************
    ** Build an HTTP Entity (e.g., for a PUT or POST) from a list of QRecords.  Can be
    ** overridden if an API doesn't do a basic json object.  Or, can override a
    ** helper method, such as recordToJsonObject.
    **
    *******************************************************************************/
   protected AbstractHttpEntity recordsToEntity(QTableMetaData table, List<QRecord> recordList) throws IOException
   {
      JSONArray entityListJson = new JSONArray();
      for(QRecord record : recordList)
      {
         entityListJson.put(entityListJson.length(), recordToJsonObject(table, record));
      }

      String json      = entityListJson.toString();
      String tablePath = getBackendDetails(table).getTablePath();
      if(tablePath != null)
      {
         JSONObject body = new JSONObject();
         body.put(tablePath, new JSONArray(json));
         json = body.toString();
      }
      LOG.debug(json);
      return (new StringEntity(json));
   }



   /*******************************************************************************
    ** Helper for recordToEntity - builds a basic JSON object.  Can be
    ** overridden if an API doesn't do a basic json object.
    **
    *******************************************************************************/
   protected JSONObject recordToJsonObject(QTableMetaData table, QRecord record)
   {
      JSONObject body = new JSONObject();
      for(Map.Entry<String, Serializable> entry : record.getValues().entrySet())
      {
         String       fieldName = entry.getKey();
         Serializable value     = entry.getValue();

         QFieldMetaData field;
         try
         {
            field = table.getField(fieldName);
         }
         catch(Exception e)
         {
            ////////////////////////////////////
            // skip values that aren't fields //
            ////////////////////////////////////
            continue;
         }
         body.put(getFieldBackendName(field), value);
      }
      return body;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QRecord jsonObjectToRecord(JSONObject jsonObject, Map<String, QFieldMetaData> fields) throws IOException
   {
      QRecord record = JsonUtils.parseQRecord(jsonObject, fields, true);
      record.getBackendDetails().put(QRecord.BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT, jsonObject.toString());
      return (record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int processGetResponse(QTableMetaData table, HttpResponse response, QueryOutput queryOutput) throws IOException
   {
      int statusCode = response.getStatusLine().getStatusCode();
      System.out.println(statusCode);

      if(statusCode >= 400)
      {
         handleGetResponseError(table, response);
      }

      HttpEntity entity       = response.getEntity();
      String     resultString = EntityUtils.toString(entity);

      int count = 0;
      if(StringUtils.hasContent(resultString) && !resultString.equals("null"))
      {
         JSONArray  resultList = null;
         JSONObject jsonObject = null;

         if(resultString.startsWith("["))
         {
            resultList = JsonUtils.toJSONArray(resultString);
         }
         else
         {
            String tablePath = getBackendDetails(table).getTablePath();
            jsonObject = JsonUtils.toJSONObject(resultString);
            if(jsonObject.has(tablePath))
            {
               resultList = jsonObject.getJSONArray(getBackendDetails(table).getTablePath());
            }
         }

         if(resultList != null)
         {
            for(int i = 0; i < resultList.length(); i++)
            {
               queryOutput.addRecord(jsonObjectToRecord(resultList.getJSONObject(i), table.getFields()));
               count++;
            }
         }
         else
         {
            queryOutput.addRecord(jsonObjectToRecord(jsonObject, table.getFields()));
            count++;
         }
      }

      return (count);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void handleGetResponseError(QTableMetaData table, HttpResponse response) throws IOException
   {
      HttpEntity entity       = response.getEntity();
      String     resultString = EntityUtils.toString(entity);
      throw new IOException("Error performing query: " + resultString);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QRecord processPostResponse(QTableMetaData table, QRecord record, HttpResponse response) throws IOException
   {
      JSONObject jsonObject = getJsonObject(response);

      String primaryKeyFieldName   = table.getPrimaryKeyField();
      String primaryKeyBackendName = getFieldBackendName(table.getField(primaryKeyFieldName));
      if(jsonObject.has(primaryKeyBackendName))
      {
         Serializable primaryKey = (Serializable) jsonObject.get(primaryKeyBackendName);
         record.setValue(primaryKeyFieldName, primaryKey);
      }
      else
      {
         if(jsonObject.has("error"))
         {
            JSONObject errorObject = jsonObject.getJSONObject("error");
            if(errorObject.has("message"))
            {
               record.addError("Error: " + errorObject.getString("message"));
            }
         }

         if(CollectionUtils.nullSafeIsEmpty(record.getErrors()))
         {
            record.addError("Unspecified error executing insert.");
         }
      }

      return (record);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected JSONObject getJsonObject(HttpResponse response) throws IOException
   {
      int statusCode = response.getStatusLine().getStatusCode();
      LOG.debug(statusCode);

      HttpEntity entity       = response.getEntity();
      String     resultString = EntityUtils.toString(entity);
      LOG.debug(resultString);

      JSONObject jsonObject = JsonUtils.toJSONObject(resultString);
      return jsonObject;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected APITableBackendDetails getBackendDetails(QTableMetaData tableMetaData)
   {
      return (APITableBackendDetails) tableMetaData.getBackendDetails();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getFieldBackendName(QFieldMetaData field)
   {
      String backendName = field.getBackendName();
      if(!StringUtils.hasContent(backendName))
      {
         backendName = field.getName();
      }
      return (backendName);
   }



   /*******************************************************************************
    ** Getter for backendMetaData
    **
    *******************************************************************************/
   public APIBackendMetaData getBackendMetaData()
   {
      return backendMetaData;
   }



   /*******************************************************************************
    ** Setter for backendMetaData
    **
    *******************************************************************************/
   public void setBackendMetaData(APIBackendMetaData backendMetaData)
   {
      this.backendMetaData = backendMetaData;
   }



   /*******************************************************************************
    ** Getter for actionInput
    **
    *******************************************************************************/
   public AbstractTableActionInput getActionInput()
   {
      return actionInput;
   }



   /*******************************************************************************
    ** Setter for actionInput
    **
    *******************************************************************************/
   public void setActionInput(AbstractTableActionInput actionInput)
   {
      this.actionInput = actionInput;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected String urlEncode(Serializable s)
   {
      return (URLEncoder.encode(ValueUtils.getValueAsString(s), StandardCharsets.UTF_8));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord processSingleRecordGetResponse(QTableMetaData table, HttpResponse response) throws IOException
   {
      return (jsonObjectToRecord(getJsonObject(response), table.getFields()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer processGetResponseForCount(QTableMetaData table, HttpResponse response) throws IOException
   {
      /////////////////////////////////////////////////////////////////////////////////////////
      // set up a query output with a blank query input - e.g., one that isn't using a pipe. //
      /////////////////////////////////////////////////////////////////////////////////////////
      QueryOutput queryOutput = new QueryOutput(new QueryInput());
      processGetResponse(table, response, queryOutput);
      List<QRecord> records = queryOutput.getRecords();

      return (records == null ? null : records.size());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void throwUnsupportedCriteriaField(QFilterCriteria criteria) throws QUserFacingException
   {
      throw new QUserFacingException("Unsupported query field [" + criteria.getFieldName() + "]");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void throwUnsupportedCriteriaOperator(QFilterCriteria criteria) throws QUserFacingException
   {
      throw new QUserFacingException("Unsupported operator [" + criteria.getOperator() + "] for query field [" + criteria.getFieldName() + "]");
   }
}
