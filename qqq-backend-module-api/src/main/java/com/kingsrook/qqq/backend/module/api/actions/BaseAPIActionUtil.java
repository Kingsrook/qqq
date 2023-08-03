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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractTableActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
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
import com.kingsrook.qqq.backend.core.model.statusmessages.SystemErrorStatusMessage;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.SleepUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.api.exceptions.OAuthCredentialsException;
import com.kingsrook.qqq.backend.module.api.exceptions.OAuthExpiredTokenException;
import com.kingsrook.qqq.backend.module.api.exceptions.RateLimitException;
import com.kingsrook.qqq.backend.module.api.exceptions.RetryableServerErrorException;
import com.kingsrook.qqq.backend.module.api.model.AuthorizationType;
import com.kingsrook.qqq.backend.module.api.model.OutboundAPILog;
import com.kingsrook.qqq.backend.module.api.model.metadata.APIBackendMetaData;
import com.kingsrook.qqq.backend.module.api.model.metadata.APITableBackendDetails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


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



   public enum UpdateHttpMethod
   {PUT, POST}



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
               validateResponse(response, List.of(record));
               record = processPostResponse(table, record, response);
               insertOutput.addRecord(record);
            }
            catch(Exception e)
            {
               record.addError(new SystemErrorStatusMessage("Error: " + e.getMessage()));
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
    ** OK - so - we will potentially make multiple GET calls to the backend, to
    ** fetch up to the full limit from the filter (and, if there is no limit in the
    ** filter, then we'll keep fetching until we stop getting results).
    **
    ** This is managed internally here by copying the limit into the originalLimit
    ** var.  Then "limit" in this method becomes the api's "how many to fetch per page"
    ** parameter (either the originalLimit, or the api's standard limit).
    **
    ** Then we break the loop (return from the method) either when:
    ** - we've fetch a total count >= the originalLimit
    ** - we got back less than a page full (e.g., we're at the end of the result set).
    ** - an async job was cancelled.
    *******************************************************************************/
   public QueryOutput doQuery(QTableMetaData table, QueryInput queryInput) throws QException
   {
      QueryOutput  queryOutput = new QueryOutput(queryInput);
      QQueryFilter filter      = queryInput.getFilter();

      Integer originalLimit = filter == null ? null : filter.getLimit();
      Integer limit         = originalLimit;
      Integer skip          = filter == null ? null : filter.getSkip();

      if(limit == null)
      {
         limit = getApiStandardLimit();
      }

      int totalCount = 0;
      while(true)
      {
         try
         {
            String  paramString = buildQueryStringForGet(filter, limit, skip, table.getFields());
            String  url         = buildTableUrl(table) + paramString;
            HttpGet request     = new HttpGet(url);

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
               String                         paramString = buildQueryStringForUpdate(table, recordList);
               String                         url         = buildTableUrl(table) + paramString;
               HttpEntityEnclosingRequestBase request     = getUpdateMethod().equals(UpdateHttpMethod.PUT) ? new HttpPut(url) : new HttpPost(url);
               request.setEntity(recordsToEntity(table, recordList));

               QHttpResponse response = makeRequest(table, request);
               validateResponse(response, recordList);
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
    *
    *******************************************************************************/
   public DeleteOutput doDelete(QTableMetaData table, DeleteInput deleteInput) throws QException
   {
      try
      {
         DeleteOutput deleteOutput = new DeleteOutput();

         String     urlSuffix = buildQueryStringForDelete(deleteInput.getQueryFilter(), deleteInput.getPrimaryKeys());
         String     url       = buildTableUrl(table);
         HttpDelete request   = new HttpDelete(url + urlSuffix);

         QHttpResponse response = makeRequest(table, request);
         if(response.getStatusCode() == 204)
         {
            deleteOutput.setDeletedRecordCount(1);
         }
         else
         {
            deleteOutput.setDeletedRecordCount(0);
         }

         return (deleteOutput);
      }
      catch(Exception e)
      {
         LOG.error("Error in API Delete", e);
         throw new QException("Error executing Delete: " + e.getMessage(), e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void validateResponse(QHttpResponse response, List<QRecord> recordList) throws QException
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
               record.addError(new SystemErrorStatusMessage("Error: " + errorObject.getString("message")));
            }
         }

         if(CollectionUtils.nullSafeIsEmpty(record.getErrors()))
         {
            record.addError(new SystemErrorStatusMessage("Unspecified error executing insert."));
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
            String wrapperObjectName = getBackendDetails(table).getTableWrapperObjectName();
            jsonObject = JsonUtils.toJSONObject(resultString);
            if(jsonObject.has(wrapperObjectName))
            {
               Object o = jsonObject.get(wrapperObjectName);
               if(o instanceof JSONArray jsonArray)
               {
                  resultList = jsonArray;
               }
               else if(o instanceof JSONObject recordJsonObject)
               {
                  resultList = new JSONArray();
                  resultList.put(recordJsonObject);
               }
               else
               {
                  throw (new QException("Unrecognized object until wrapperObjectName: " + o));
               }
            }
         }

         if(resultList != null)
         {
            for(int i = 0; i < resultList.length(); i++)
            {
               QRecord record = jsonObjectToRecord(resultList.getJSONObject(i), table.getFields());
               if(record != null)
               {
                  queryOutput.addRecord(record);
                  count++;
               }
            }
         }
         else
         {
            QRecord record = jsonObjectToRecord(jsonObject, table.getFields());
            if(record != null)
            {
               queryOutput.addRecord(record);
               count++;
            }
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

      boolean didLog = false;
      if("GET".equals(request.getMethod()))
      {
         if(statusCode == HttpStatus.SC_NOT_FOUND)
         {
            return;
         }
         else if(statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_GATEWAY_TIMEOUT)
         {
            LOG.info("HTTP " + request.getMethod() + " failed", logPair("table", table.getName()), logPair("statusCode", statusCode), logPair("responseContent", StringUtils.safeTruncate(resultString, 1024, "...")));
            didLog = true;
         }
      }

      if(!didLog)
      {
         LOG.warn("HTTP " + request.getMethod() + " failed", logPair("table", table.getName()), logPair("statusCode", statusCode), logPair("responseContent", StringUtils.safeTruncate(resultString, 1024, "...")));
      }

      String warningMessage = "HTTP " + request.getMethod() + " for table [" + table.getName() + "] failed with status " + statusCode + ": " + resultString;
      throw (new QException(warningMessage));
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
    ** method to build up a query string for updates based on a given QFilter object
    **
    *******************************************************************************/
   protected String buildQueryStringForUpdate(QTableMetaData table, List<QRecord> recordList) throws QException
   {
      return ("");
   }



   /*******************************************************************************
    ** method to build up a query string based on a given QFilter object
    **
    *******************************************************************************/
   protected String buildQueryStringForGet(QQueryFilter filter, Integer limit, Integer skip, Map<String, QFieldMetaData> fields) throws QException
   {
      return ("");
   }



   /*******************************************************************************
    ** method to build up delete string based on a given QFilter object
    **
    *******************************************************************************/
   protected String buildQueryStringForDelete(QQueryFilter filter, List<Serializable> primaryKeys) throws QException
   {
      return ("");
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
   public void setupAuthorizationInRequest(HttpRequestBase request) throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////
      // if backend specifies that it uses variants, look for that data in the session //
      ///////////////////////////////////////////////////////////////////////////////////
      if(backendMetaData.getUsesVariants())
      {
         QSession session = QContext.getQSession();
         if(session.getBackendVariants() == null || !session.getBackendVariants().containsKey(backendMetaData.getVariantOptionsTableTypeValue()))
         {
            throw (new QException("Could not find Backend Variant information for Backend '" + backendMetaData.getName() + "'"));
         }

         Serializable variantId = session.getBackendVariants().get(backendMetaData.getVariantOptionsTableTypeValue());
         GetInput     getInput  = new GetInput();
         getInput.setShouldMaskPasswords(false);
         getInput.setTableName(backendMetaData.getVariantOptionsTableName());
         getInput.setPrimaryKey(variantId);
         GetOutput getOutput = new GetAction().execute(getInput);

         QRecord record = getOutput.getRecord();
         if(record == null)
         {
            throw (new QException("Could not find Backend Variant in table " + backendMetaData.getVariantOptionsTableName() + " with id '" + variantId + "'"));
         }

         if(backendMetaData.getAuthorizationType().equals(AuthorizationType.BASIC_AUTH_USERNAME_PASSWORD))
         {
            request.addHeader("Authorization", getBasicAuthenticationHeader(record.getValueString(backendMetaData.getVariantOptionsTableUsernameField()), record.getValueString(backendMetaData.getVariantOptionsTablePasswordField())));
         }
         else if(backendMetaData.getAuthorizationType().equals(AuthorizationType.API_KEY_HEADER))
         {
            request.addHeader("API-Key", record.getValueString(backendMetaData.getVariantOptionsTableApiKeyField()));
         }
         else
         {
            throw (new IllegalArgumentException("Unexpected variant authorization type specified: " + backendMetaData.getAuthorizationType()));
         }
         return;
      }

      ///////////////////////////////////////////////////////////////////////////////////////////
      // if not using variants, the authorization data will be in the backend meta data object //
      ///////////////////////////////////////////////////////////////////////////////////////////
      switch(backendMetaData.getAuthorizationType())
      {
         case BASIC_AUTH_API_KEY -> request.addHeader("Authorization", getBasicAuthenticationHeader(backendMetaData.getApiKey()));
         case BASIC_AUTH_USERNAME_PASSWORD -> request.addHeader("Authorization", getBasicAuthenticationHeader(backendMetaData.getUsername(), backendMetaData.getPassword()));
         case API_KEY_HEADER -> request.addHeader("API-Key", backendMetaData.getApiKey());
         case API_TOKEN -> request.addHeader("Authorization", "Token " + backendMetaData.getApiKey());
         case OAUTH2 -> request.setHeader("Authorization", "Bearer " + getOAuth2Token());
         case API_KEY_QUERY_PARAM ->
         {
            try
            {
               String uri = request.getURI().toString();
               uri += (uri.contains("?") ? "&" : "?");
               uri += backendMetaData.getApiKeyQueryParamName() + "=" + backendMetaData.getApiKey();
               request.setURI(new URI(uri));
            }
            catch(URISyntaxException e)
            {
               throw (new QException("Error setting authorization query parameter", e));
            }
         }
         default -> throw new IllegalArgumentException("Unexpected authorization type: " + backendMetaData.getAuthorizationType());
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
      String  accessToken            = ValueUtils.getValueAsString(backendMetaData.getCustomValue("accessToken"));
      Boolean setCredentialsInHeader = BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(backendMetaData.getCustomValue("setCredentialsInHeader")));

      if(!StringUtils.hasContent(accessToken))
      {
         String fullURL  = backendMetaData.getBaseUrl() + "oauth/token";
         String postBody = "grant_type=client_credentials";

         if(!setCredentialsInHeader)
         {
            postBody += "&client_id=" + backendMetaData.getClientId() + "&client_secret=" + backendMetaData.getClientSecret();
         }

         try(CloseableHttpClient client = HttpClients.custom().setConnectionManager(new PoolingHttpClientConnectionManager()).build())
         {
            HttpPost request = new HttpPost(fullURL);
            request.setEntity(new StringEntity(postBody));

            if(setCredentialsInHeader)
            {
               request.addHeader("Authorization", getBasicAuthenticationHeader(backendMetaData.getClientId(), backendMetaData.getClientSecret()));
            }
            request.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

            HttpResponse response     = executeOAuthTokenRequest(client, request);
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
    ** one-line method, factored out so mock/tests can override
    *******************************************************************************/
   protected CloseableHttpResponse executeOAuthTokenRequest(CloseableHttpClient client, HttpPost request) throws IOException
   {
      return client.execute(request);
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
   public String getBasicAuthenticationHeader(String username, String password)
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

      String wrapperObjectName = getBackendDetails(table).getTableWrapperObjectName();
      if(wrapperObjectName != null)
      {
         body = new JSONObject();
         body.put(wrapperObjectName, new JSONObject(json));
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

         String json              = entityListJson.toString();
         String wrapperObjectName = getBackendDetails(table).getTableWrapperObjectName();
         if(wrapperObjectName != null)
         {
            JSONObject body = new JSONObject();
            body.put(wrapperObjectName, new JSONArray(json));
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
   public QHttpResponse makeRequest(QTableMetaData table, HttpRequestBase request) throws QException
   {
      int     rateLimitSleepMillis      = getInitialRateLimitBackoffMillis();
      int     serverErrorsSleepMillis   = getInitialServerErrorBackoffMillis();
      int     rateLimitsCaught          = 0;
      int     serverErrorsCaught        = 0;
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

            try(CloseableHttpResponse response = executeHttpRequest(request, httpClient))
            {
               QHttpResponse qResponse = new QHttpResponse(response);

               logOutboundApiCall(request, qResponse);

               int statusCode = qResponse.getStatusCode();
               if(statusCode == HttpStatus.SC_TOO_MANY_REQUESTS)
               {
                  throw (new RateLimitException(qResponse.getContent()));
               }
               else if(shouldBeRetryableServerErrorException(qResponse))
               {
                  throw (new RetryableServerErrorException(statusCode, qResponse.getContent()));
               }
               else if(statusCode >= 400)
               {
                  handleResponseError(table, request, qResponse);
               }

               /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // trim response body (just to keep logs smaller, or, in case someone consuming logs doesn't want such long lines) //
               /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               LOG.info("Received successful response with code [" + qResponse.getStatusCode() + "] and content [" + StringUtils.safeTruncate(qResponse.getContent(), getMaxResponseMessageLengthForLog(), "...") + "].");
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
               LOG.error("Giving up " + request.getMethod() + " to [" + table.getName() + "] after too many rate-limit errors (" + getMaxAllowedRateLimitErrors() + ")");
               throw (new QException(rle));
            }

            LOG.info("Caught RateLimitException", logPair("rateLimitsCaught", rateLimitsCaught), logPair("uri", request.getURI()), logPair("table", table.getName()), logPair("sleeping", rateLimitSleepMillis));
            SleepUtils.sleep(rateLimitSleepMillis, TimeUnit.MILLISECONDS);
            rateLimitSleepMillis *= 2;
         }
         catch(RetryableServerErrorException see)
         {
            serverErrorsCaught++;
            if(serverErrorsCaught > getMaxAllowedServerErrors())
            {
               LOG.error("Giving up " + request.getMethod() + " to [" + table.getName() + "] after too many server-side errors (" + getMaxAllowedServerErrors() + ")");
               throw (new QException(see));
            }

            LOG.info("Caught Server-side error during API request", logPair("serverErrorsCaught", serverErrorsCaught), logPair("uri", request.getURI()), logPair("code", see.getCode()), logPair("table", table.getName()), logPair("sleeping", serverErrorsSleepMillis));
            SleepUtils.sleep(serverErrorsSleepMillis, TimeUnit.MILLISECONDS);
            serverErrorsSleepMillis *= 2;
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
   protected boolean shouldBeRetryableServerErrorException(QHttpResponse qResponse)
   {
      if(actionInput instanceof QueryInput || actionInput instanceof GetInput)
      {
         return (qResponse.getStatusCode() != null && qResponse.getStatusCode() >= 500);
      }

      return (false);
   }



   /*******************************************************************************
    ** one-line method, factored out so mock/tests can override
    *******************************************************************************/
   protected CloseableHttpResponse executeHttpRequest(HttpRequestBase request, CloseableHttpClient httpClient) throws IOException
   {
      return httpClient.execute(request);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected void logOutboundApiCall(HttpRequestBase request, QHttpResponse response)
   {
      try
      {
         QTableMetaData table = QContext.getQInstance().getTable(OutboundAPILog.TABLE_NAME);
         if(table == null)
         {
            return;
         }

         String requestBody = null;
         if(request instanceof HttpEntityEnclosingRequest entityRequest)
         {
            try
            {
               requestBody = StringUtils.join("\n", IOUtils.readLines(entityRequest.getEntity().getContent()));
            }
            catch(Exception e)
            {
               // leave it null...
            }
         }

         ////////////////////////////////////
         // mask api keys in query strings //
         ////////////////////////////////////
         String url = request.getURI().toString();
         if(backendMetaData.getAuthorizationType().equals(AuthorizationType.API_KEY_QUERY_PARAM))
         {
            url = url.replaceFirst(backendMetaData.getApiKey(), "******");
         }

         InsertInput insertInput = new InsertInput();
         insertInput.setTableName(table.getName());
         insertInput.setRecords(List.of(new OutboundAPILog()
            .withMethod(request.getMethod())
            .withUrl(url)
            .withTimestamp(Instant.now())
            .withRequestBody(requestBody)
            .withStatusCode(response.getStatusCode())
            .withResponseBody(response.getContent())
            .toQRecord()
         ));
         new InsertAction().executeAsync(insertInput);
      }
      catch(Exception e)
      {
         LOG.warn("Error logging outbound api call", e);
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
   protected int getInitialServerErrorBackoffMillis()
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
   protected int getMaxAllowedServerErrors()
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



   /*******************************************************************************
    **
    *******************************************************************************/
   protected UpdateHttpMethod getUpdateMethod()
   {
      return (UpdateHttpMethod.PUT);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   protected int getMaxResponseMessageLengthForLog()
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // rsyslog default limit appears to be 8K - we've got some extra content, so 7 feels safe enough //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      return (7 * 1024);
   }
}
