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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.QLogger;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.api.exceptions.OAuthCredentialsException;
import com.kingsrook.qqq.backend.module.api.exceptions.OAuthExpiredTokenException;
import com.kingsrook.qqq.backend.module.api.exceptions.RateLimitException;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;
import com.kingsrook.qqq.backend.module.api.model.metadata.APITableBackendDetails;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 ** Base class for utility functions that make up the unique ways in which an
 ** API can be implemented.
 *******************************************************************************/
public class BaseAPIActionUtil
{
   private final QLogger LOG = QLogger.getLogger(BaseAPIActionUtil.class);

   protected QSession                 session; // todo not commit - delete!!
   protected APIBackendMetaData       backendMetaData;
   protected AbstractTableActionInput actionInput;



   /*******************************************************************************
    **
    *******************************************************************************/
   public CountOutput doCount(QTableMetaData table, CountInput countInput) throws QException
   {
      try
      {
         QQueryFilter filter      = countInput.getFilter();
         String       paramString = buildQueryStringForGet(filter, null, null, table.getFields());
         String       url         = buildTableUrl(table) + paramString;

         HttpGet       request  = new HttpGet(url);
         QHttpResponse response = makeRequest(table, request);

         Integer     count = processGetResponseForCount(table, response);
         CountOutput rs    = new CountOutput();
         rs.setCount(count);
         return rs;
      }
      catch(Exception e)
      {
         LOG.error("Error in API count", e);
         throw new QException("Error executing count: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public GetOutput doGet(QTableMetaData table, GetInput getInput) throws QException
   {
      try
      {
         String urlSuffix = getInput.getPrimaryKey() != null
            ? buildUrlSuffixForSingleRecordGet(getInput.getPrimaryKey())
            : buildUrlSuffixForSingleRecordGet(getInput.getUniqueKey());
         String  url     = buildTableUrl(table);
         HttpGet request = new HttpGet(url + urlSuffix);

         GetOutput     rs       = new GetOutput();
         QHttpResponse response = makeRequest(table, request);

         if(response.getStatusCode() != HttpStatus.SC_NOT_FOUND)
         {
            QRecord record = processSingleRecordGetResponse(table, response);
            rs.setRecord(record);
         }

         return rs;
      }
      catch(Exception e)
      {
         LOG.error("Error in API get", e);
         throw new QException("Error executing get: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public InsertOutput doInsert(QTableMetaData table, InsertInput insertInput) throws QException
   {
      InsertOutput insertOutput = new InsertOutput();
      insertOutput.setRecords(new ArrayList<>());

      if(CollectionUtils.nullSafeIsEmpty(insertInput.getRecords()))
      {
         LOG.debug("Insert request called with 0 records.  Returning with no-op");
         return (insertOutput);
      }

      try
      {
         // todo - supports bulk post?
         for(QRecord record : insertInput.getRecords())
         {
            //////////////////////////////////////////////////////////
            // hmm, unclear if this should always be done...        //
            // is added initially for registering easypost trackers //
            //////////////////////////////////////////////////////////
            insertInput.getAsyncJobCallback().incrementCurrent();

            try
            {
               String   url     = buildTableUrl(table);
               HttpPost request = new HttpPost(url);
               request.setEntity(recordToEntity(table, record));

               QHttpResponse response = makeRequest(table, request);
               record = processPostResponse(table, record, response);
               insertOutput.addRecord(record);
            }
            catch(Exception e)
            {
               record.addError("Error: " + e.getMessage());
               insertOutput.addRecord(record);
            }

            if(insertInput.getRecords().size() > 1 && getMillisToSleepAfterEveryCall() > 0)
            {
               SleepUtils.sleep(getMillisToSleepAfterEveryCall(), TimeUnit.MILLISECONDS);
            }
         }

         return (insertOutput);
      }
      catch(Exception e)
      {
         LOG.error("Error in API Insert for [" + table.getName() + "]", e);
         throw new QException("Error executing insert: " + e.getMessage(), e);
      }

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryOutput doQuery(QTableMetaData table, QueryInput queryInput) throws QException
   {
      QueryOutput queryOutput   = new QueryOutput(queryInput);
      Integer     originalLimit = queryInput.getLimit();
      Integer     limit         = originalLimit;
      Integer     skip          = queryInput.getSkip();

      if(limit == null)
      {
         limit = getApiStandardLimit();
      }

      int totalCount = 0;
      while(true)
      {
         try
         {
            QQueryFilter filter      = queryInput.getFilter();
            String       paramString = buildQueryStringForGet(filter, limit, skip, table.getFields());
            String       url         = buildTableUrl(table) + paramString;
            HttpGet      request     = new HttpGet(url);

            QHttpResponse response = makeRequest(table, request);
            int           count    = processGetResponse(table, response, queryOutput);
            totalCount += count;

            /////////////////////////////////////////////////////////////////////////
            // if we've fetched at least as many as the original limit, then break //
            /////////////////////////////////////////////////////////////////////////
            if(originalLimit != null && totalCount >= originalLimit)
            {
               return (queryOutput);
            }

            ////////////////////////////////////////////////////////////////////////////////////
            // if we got back less than a full page this time, then we must be done, so break //
            ////////////////////////////////////////////////////////////////////////////////////
            if(count == 0 || (limit != null && count < limit))
            {
               return (queryOutput);
            }

            ///////////////////////////////////////////////////////////////////
            // if there's an async callback that says we're cancelled, break //
            ///////////////////////////////////////////////////////////////////
            if(queryInput.getAsyncJobCallback().wasCancelRequested())
            {
               LOG.info("Breaking query job, as requested.");
               return (queryOutput);
            }

            ////////////////////////////////////////////////////////////////////////////
            // else, increment the skip by the count we just got, and query for more. //
            ////////////////////////////////////////////////////////////////////////////
            if(skip == null)
            {
               skip = 0;
            }
            skip += count;
         }
         catch(Exception e)
         {
            LOG.error("Error in API Query", e);
            throw new QException("Error executing query: " + e.getMessage(), e);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public UpdateOutput doUpdate(QTableMetaData table, UpdateInput updateInput) throws QException
   {
      UpdateOutput updateOutput = new UpdateOutput();
      updateOutput.setRecords(new ArrayList<>());

      if(CollectionUtils.nullSafeIsEmpty(updateInput.getRecords()))
      {
         LOG.debug("Update request called with 0 records.  Returning with no-op");
         return (updateOutput);
      }

      try
      {
         ///////////////////////////////////////////////////////////////
         // make post requests for groups of orders that need updated //
         ///////////////////////////////////////////////////////////////
         for(List<QRecord> recordList : CollectionUtils.getPages(updateInput.getRecords(), 20))
         {
            try
            {
               String   url     = buildTableUrl(table);
               HttpPost request = new HttpPost(url);
               request.setEntity(recordsToEntity(table, recordList));

               QHttpResponse response = makeRequest(table, request);
               validateResponse(response);
            }
            catch(QException e)
            {
               throw (e);
            }
            catch(Exception e)
            {
               String errorMessage = "An unexpected error occurred updating entities.";
               LOG.error(errorMessage, e);
               throw (new QException(errorMessage, e));
            }

            for(QRecord qRecord : recordList)
            {
               updateOutput.addRecord(qRecord);
            }
            if(recordList.size() == 20 && getMillisToSleepAfterEveryCall() > 0)
            {
               SleepUtils.sleep(getMillisToSleepAfterEveryCall(), TimeUnit.MILLISECONDS);
            }
         }

         return (updateOutput);
      }
      catch(Exception e)
      {
         LOG.error("Error in API Update for [" + table.getName() + "]", e);
         throw new QException("Error executing update: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void validateResponse(QHttpResponse response) throws QException
   {
      ////////////////////////
      // noop at base level //
      ////////////////////////
      return;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Integer processGetResponseForCount(QTableMetaData table, QHttpResponse response) throws QException
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
   public QRecord processSingleRecordGetResponse(QTableMetaData table, QHttpResponse response) throws QException
   {
      return (jsonObjectToRecord(getJsonObject(response), table.getFields()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QRecord processPostResponse(QTableMetaData table, QRecord record, QHttpResponse response) throws QException
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
   protected int processGetResponse(QTableMetaData table, QHttpResponse response, QueryOutput queryOutput) throws QException
   {
      String resultString = response.getContent();

      int count = 0;
      if(StringUtils.hasContent(response.getContent()) && !resultString.equals("null"))
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
   protected void handleResponseError(QTableMetaData table, HttpRequestBase request, QHttpResponse response) throws QException
   {
      checkForOAuthExpiredToken(table, request, response);

      int    statusCode   = response.getStatusCode();
      String resultString = response.getContent();
      String errorMessage = "HTTP " + request.getMethod() + " for table [" + table.getName() + "] failed with status " + statusCode + ": " + resultString;
      LOG.error(errorMessage);

      if("GET".equals(request.getMethod()))
      {
         if(statusCode == HttpStatus.SC_NOT_FOUND)
         {
            return;
         }
      }

      throw (new QException(errorMessage));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void checkForOAuthExpiredToken(QTableMetaData table, HttpRequestBase request, QHttpResponse response) throws OAuthExpiredTokenException
   {
      if(backendMetaData.getAuthorizationType().equals(AuthorizationType.OAUTH2))
      {
         if(response.getStatusCode().equals(HttpStatus.SC_UNAUTHORIZED)) // 401
         {
            throw (new OAuthExpiredTokenException("Expired token indicated by response: " + response));
         }
      }
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
    **
    *******************************************************************************/
   public String buildUrlSuffixForSingleRecordGet(Map<String, Serializable> uniqueKey) throws QException
   {
      QTableMetaData table  = actionInput.getTable();
      QQueryFilter   filter = new QQueryFilter();
      for(Map.Entry<String, Serializable> entry : uniqueKey.entrySet())
      {
         filter.addCriteria(new QFilterCriteria(entry.getKey(), QCriteriaOperator.EQUALS, entry.getValue()));
      }
      return (buildQueryStringForGet(filter, 1, 0, table.getFields()));
   }



   /*******************************************************************************
    ** As part of making a request - set up its authorization header (not just
    ** strictly "Authorization", but whatever is needed for auth).
    **
    ** Can be overridden if an API uses an authorization type we don't natively support.
    *******************************************************************************/
   protected void setupAuthorizationInRequest(HttpRequestBase request) throws QException
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

         case OAUTH2:
            request.setHeader("Authorization", "Bearer " + getOAuth2Token());
            break;

         default:
            throw new IllegalArgumentException("Unexpected authorization type: " + backendMetaData.getAuthorizationType());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getOAuth2Token() throws OAuthCredentialsException
   {
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // check for the access token in the backend meta data.  if it's not there, then issue a request for a token. //
      // this is not generally meant to be put in the meta data by the app programmer - rather, we're just using    //
      // it as a "cheap & easy" way to "cache" the token within our process's memory...                             //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      String accessToken = ValueUtils.getValueAsString(backendMetaData.getCustomValue("accessToken"));

      if(!StringUtils.hasContent(accessToken))
      {
         String fullURL  = backendMetaData.getBaseUrl() + "oauth/token";
         String postBody = "grant_type=client_credentials&client_id=" + backendMetaData.getClientId() + "&client_secret=" + backendMetaData.getClientSecret();

         LOG.info("Fetching OAuth2 token from " + fullURL);

         try(CloseableHttpClient client = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager()).build())
         {
            HttpPost request = new HttpPost(fullURL);
            request.setEntity(new StringEntity(postBody));
            request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

            HttpResponse response     = client.execute(request);
            int          statusCode   = response.getStatusLine().getStatusCode();
            HttpEntity   entity       = response.getEntity();
            String       resultString = EntityUtils.toString(entity);
            if(statusCode != HttpStatus.SC_OK)
            {
               throw (new OAuthCredentialsException("Did not receive successful response when requesting oauth token [" + statusCode + "]: " + resultString));
            }

            JSONObject resultJSON = new JSONObject(resultString);
            accessToken = (resultJSON.getString("access_token"));
            LOG.debug("Fetched access token: " + accessToken);

            ///////////////////////////////////////////////////////////////////////////////////////////////////
            // stash the access token in the backendMetaData, from which it will be used for future requests //
            ///////////////////////////////////////////////////////////////////////////////////////////////////
            backendMetaData.withCustomValue("accessToken", accessToken);
         }
         catch(OAuthCredentialsException oce)
         {
            throw (oce);
         }
         catch(Exception e)
         {
            String errorMessage = "Error getting OAuth Token";
            LOG.warn(errorMessage, e);
            throw (new OAuthCredentialsException(errorMessage, e));
         }
      }

      return (accessToken);
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
      return (new StringEntity(json));
   }



   /*******************************************************************************
    ** Build an HTTP Entity (e.g., for a PUT or POST) from a list of QRecords.  Can be
    ** overridden if an API doesn't do a basic json object.  Or, can override a
    ** helper method, such as recordToJsonObject.
    **
    *******************************************************************************/
   protected AbstractHttpEntity recordsToEntity(QTableMetaData table, List<QRecord> recordList) throws QException
   {
      try
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
         return (new StringEntity(json));
      }
      catch(Exception e)
      {
         throw (new QException(e.getMessage(), e));
      }
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
   protected QRecord jsonObjectToRecord(JSONObject jsonObject, Map<String, QFieldMetaData> fields) throws QException
   {
      try
      {
         QRecord record = JsonUtils.parseQRecord(jsonObject, fields, true);
         record.getBackendDetails().put(QRecord.BACKEND_DETAILS_TYPE_JSON_SOURCE_OBJECT, jsonObject.toString());
         return (record);
      }
      catch(Exception e)
      {
         throw (new QException(e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected JSONObject getJsonObject(QHttpResponse response) throws QException
   {
      try
      {
         return JsonUtils.toJSONObject(response.getContent());
      }
      catch(Exception e)
      {
         throw (new QException(e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected QHttpResponse makeRequest(QTableMetaData table, HttpRequestBase request) throws QException
   {
      int     sleepMillis               = getInitialRateLimitBackoffMillis();
      int     rateLimitsCaught          = 0;
      boolean caughtAnOAuthExpiredToken = false;

      while(true)
      {
         //////////////////////////////////////////////////////
         // make sure to use closeable client to avoid leaks //
         //////////////////////////////////////////////////////
         try(CloseableHttpClient httpClient = HttpClientBuilder.create().build())
         {
            ////////////////////////////////////////////////////////////
            // call utility methods that populate data in the request //
            ////////////////////////////////////////////////////////////
            setupAuthorizationInRequest(request);
            setupContentTypeInRequest(request);
            setupAdditionalHeaders(request);

            LOG.info("Making [" + request.getMethod() + "] request to URL [" + request.getURI() + "] on table [" + table.getName() + "].");
            if("POST".equals(request.getMethod()))
            {
               LOG.info("POST contents [" + ((HttpPost) request).getEntity().toString() + "]");
            }

            try(CloseableHttpResponse response = httpClient.execute(request))
            {
               QHttpResponse qResponse = new QHttpResponse(response);

               int statusCode = qResponse.getStatusCode();
               if(statusCode == HttpStatus.SC_TOO_MANY_REQUESTS)
               {
                  throw (new RateLimitException(qResponse.getContent()));
               }
               if(statusCode >= 400)
               {
                  handleResponseError(table, request, qResponse);
               }

               LOG.info("Received successful response with code [" + qResponse.getStatusCode() + "] and content [" + qResponse.getContent() + "].");
               return (qResponse);
            }
         }
         catch(OAuthCredentialsException oce)
         {
            LOG.error("OAuth Credential failure for [" + table.getName() + "]");
            throw (oce);
         }
         catch(OAuthExpiredTokenException oete)
         {
            if(!caughtAnOAuthExpiredToken)
            {
               LOG.info("OAuth Expired token for [" + table.getName() + "] - retrying");
               backendMetaData.withCustomValue("accessToken", null);
               caughtAnOAuthExpiredToken = true;
            }
            else
            {
               LOG.info("OAuth Expired token for [" + table.getName() + "] even after a retry.  Giving up.");
               throw (oete);
            }
         }
         catch(RateLimitException rle)
         {
            rateLimitsCaught++;
            if(rateLimitsCaught > getMaxAllowedRateLimitErrors())
            {
               LOG.error("Giving up POST to [" + table.getName() + "] after too many rate-limit errors (" + getMaxAllowedRateLimitErrors() + ")");
               throw (new QException(rle));
            }

            LOG.warn("Caught RateLimitException [#" + rateLimitsCaught + "] during HTTP request to [" + request.getURI() + "] on table [" + table.getName() + "] - sleeping [" + sleepMillis + "]...");
            SleepUtils.sleep(sleepMillis, TimeUnit.MILLISECONDS);
            sleepMillis *= 2;
         }
         catch(QException qe)
         {
            ///////////////////////////////////////////////////////////////
            // re-throw exceptions that QQQ or application code produced //
            ///////////////////////////////////////////////////////////////
            throw (qe);
         }
         catch(Exception e)
         {
            String message = "An unknown error occurred trying to make an HTTP request to [" + request.getURI() + "] on table [" + table.getName() + "].";
            LOG.error(message, e);
            throw (new QException(message, e));
         }
      }
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
    **
    *******************************************************************************/
   protected String urlEncode(Serializable s)
   {
      return (URLEncoder.encode(ValueUtils.getValueAsString(s), StandardCharsets.UTF_8));
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
    ** Setter for actionInput
    **
    *******************************************************************************/
   public void setActionInput(AbstractTableActionInput actionInput)
   {
      this.actionInput = actionInput;
   }



   /*******************************************************************************
    ** Setter for session
    **
    *******************************************************************************/
   public void setSession(QSession session)
   {
      this.session = session;
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



   /*******************************************************************************
    **
    *******************************************************************************/
   protected long getMillisToSleepAfterEveryCall()
   {
      return (0);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int getInitialRateLimitBackoffMillis()
   {
      return (500);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int getMaxAllowedRateLimitErrors()
   {
      return (3);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected Integer getApiStandardLimit()
   {
      return (20);
   }
}
