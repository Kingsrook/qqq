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

package com.kingsrook.qqq.api.javalin;


import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.api.actions.GenerateOpenApiSpecAction;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.api.model.APILog;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.metadata.APILogMetaDataProvider;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataProvider;
import com.kingsrook.qqq.api.model.metadata.ApiOperation;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.AccessTokenException;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
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
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.branding.QBrandingMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import com.kingsrook.qqq.backend.javalin.QJavalinAccessLogger;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;
import static com.kingsrook.qqq.backend.javalin.QJavalinImplementation.SLOW_LOG_THRESHOLD_MS;


/*******************************************************************************
 ** methods for handling qqq API requests in javalin.
 *******************************************************************************/
public class QJavalinApiHandler
{
   private static final QLogger LOG = QLogger.getLogger(QJavalinApiHandler.class);

   private static QInstance qInstance;

   /////////////////////////////////////
   // key:  Pair<apiName, apiVersion> //
   /////////////////////////////////////
   private static Map<Pair<String, String>, Map<String, QTableMetaData>> tableApiNameMap = new HashMap<>();

   private static Map<String, Integer> apiLogUserIdCache = new HashMap<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QJavalinApiHandler(QInstance qInstance)
   {
      QJavalinApiHandler.qInstance = qInstance;
   }



   /*******************************************************************************
    ** Define the routes
    *******************************************************************************/
   public EndpointGroup getRoutes()
   {
      return (() ->
      {
         /////////////////////////////
         // authentication endpoint //
         /////////////////////////////
         ApiBuilder.post("/api/oauth/token", QJavalinApiHandler::handleAuthorization);

         ///////////////////////////////////////////////
         // static endpoints to support rapidoc pages //
         ///////////////////////////////////////////////
         ApiBuilder.get("/api/docs/js/rapidoc.min.js", (context) -> QJavalinApiHandler.serveResource(context, "rapidoc/rapidoc-9.3.4.min.js", MapBuilder.of("Content-Type", ContentType.JAVASCRIPT)));
         ApiBuilder.get("/api/docs/css/qqq-api-styles.css", (context) -> QJavalinApiHandler.serveResource(context, "rapidoc/rapidoc-overrides.css", MapBuilder.of("Content-Type", ContentType.CSS)));

         ApiBuilder.get("/apis.json", QJavalinApiHandler::doGetApisJson);

         // todo not this? ApiBuilder.get("/api/", QJavalinApiHandler::doListApis);

         ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(qInstance);
         for(Map.Entry<String, ApiInstanceMetaData> entry : apiInstanceMetaDataContainer.getApis().entrySet())
         {
            ApiInstanceMetaData apiInstanceMetaData = entry.getValue();
            String              rootPath            = apiInstanceMetaData.getPath();

            //////////////////////////////////////////////
            // default page is the current version spec //
            //////////////////////////////////////////////
            ApiBuilder.get(rootPath, context -> doSpecHtml(context, apiInstanceMetaData));
            ApiBuilder.get(rootPath + "versions.json", context -> doVersions(context, apiInstanceMetaData));

            ApiBuilder.path(rootPath + "{version}", () ->
            {
               ////////////////////////////////////////////
               // default page for a version is its spec //
               ////////////////////////////////////////////
               ApiBuilder.get("/", context -> doSpecHtml(context, apiInstanceMetaData));

               ApiBuilder.get("/openapi.yaml", context -> doSpecYaml(context, apiInstanceMetaData));
               ApiBuilder.get("/openapi.json", context -> doSpecJson(context, apiInstanceMetaData));
               ApiBuilder.get("/openapi.html", context -> doSpecHtml(context, apiInstanceMetaData));

               ApiBuilder.path("/{tableName}", () ->
               {
                  ApiBuilder.get("/openapi.yaml", context -> doSpecYaml(context, apiInstanceMetaData));
                  ApiBuilder.get("/openapi.json", context -> doSpecJson(context, apiInstanceMetaData));

                  ApiBuilder.post("/", context -> doInsert(context, apiInstanceMetaData));

                  ApiBuilder.get("/query", context -> doQuery(context, apiInstanceMetaData));
                  // ApiBuilder.post("/query", context -> doQuery(context, apiInstanceMetaData));

                  ApiBuilder.post("/bulk", context -> bulkInsert(context, apiInstanceMetaData));
                  ApiBuilder.patch("/bulk", context -> bulkUpdate(context, apiInstanceMetaData));
                  ApiBuilder.delete("/bulk", context -> bulkDelete(context, apiInstanceMetaData));

                  //////////////////////////////////////////////////////////////////
                  // remember to keep the wildcard paths after the specific paths //
                  //////////////////////////////////////////////////////////////////
                  ApiBuilder.get("/{primaryKey}", context -> doGet(context, apiInstanceMetaData));
                  ApiBuilder.patch("/{primaryKey}", context -> doUpdate(context, apiInstanceMetaData));
                  ApiBuilder.delete("/{primaryKey}", context -> doDelete(context, apiInstanceMetaData));
               });
            });

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // default all other requests under the root path (for the methods we support) to a standard 404 response //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ApiBuilder.get(rootPath + "*", QJavalinApiHandler::doPathNotFound);
            ApiBuilder.delete(rootPath + "*", QJavalinApiHandler::doPathNotFound);
            ApiBuilder.patch(rootPath + "*", QJavalinApiHandler::doPathNotFound);
            ApiBuilder.post(rootPath + "*", QJavalinApiHandler::doPathNotFound);
         }

         ///////////////////////////////////////////////////////////////////////////////////
         // if the main implementation class has a hot-swapper installed, use it here too //
         ///////////////////////////////////////////////////////////////////////////////////
         if(QJavalinImplementation.getQInstanceHotSwapSupplier() != null)
         {
            ApiBuilder.before((context) ->
            {
               QJavalinImplementation.hotSwapQInstance(context);
               QJavalinApiHandler.qInstance = QJavalinImplementation.getQInstance();
            });
         }
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doListApis(Context context)
   {
      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(qInstance);

      StringBuilder html = new StringBuilder("<h1>Select an API</h1>\n<ul>\n");
      for(Map.Entry<String, ApiInstanceMetaData> entry : apiInstanceMetaDataContainer.getApis().entrySet())
      {
         html.append("""
            <li><a href="{link}">{name}</a></li>
            """
            .replace("{link}", entry.getValue().getPath())
            .replace("{name}", entry.getValue().getLabel())
         );
      }

      html.append("</ul>");

      context.header("Content-Type", ContentType.HTML);
      context.result(html.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void serveResource(Context context, String resourcePath, Map<String, String> headers)
   {
      InputStream resourceAsStream = QJavalinApiHandler.class.getClassLoader().getResourceAsStream(resourcePath);
      for(Map.Entry<String, String> entry : CollectionUtils.nonNullMap(headers).entrySet())
      {
         context.header(entry.getKey(), entry.getValue());
      }
      context.result(resourceAsStream);
   }



   /*******************************************************************************
    ** list the apis supported in this instance
    *******************************************************************************/
   private static void doGetApisJson(Context context)
   {
      Map<String, Object>       rs   = new HashMap<>();
      List<Map<String, Object>> apis = new ArrayList<>();
      rs.put("apis", apis);

      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(qInstance);
      for(Map.Entry<String, ApiInstanceMetaData> entry : apiInstanceMetaDataContainer.getApis().entrySet())
      {
         Map<String, Object> thisApi = new HashMap<>();

         ApiInstanceMetaData apiInstanceMetaData = entry.getValue();
         thisApi.put("name", apiInstanceMetaData.getName());
         thisApi.put("path", apiInstanceMetaData.getPath());
         thisApi.put("label", apiInstanceMetaData.getLabel());

         String tableName = context.queryParam("tableName");
         if(tableName != null)
         {
            QTableMetaData table = qInstance.getTable(tableName);

            ///////////////////////////////////////////////////////////////
            // look for reasons we might exclude this api for this table //
            ///////////////////////////////////////////////////////////////
            ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
            if(apiTableMetaDataContainer == null)
            {
               continue;
            }

            ApiTableMetaData apiTableMetaData = apiTableMetaDataContainer.getApiTableMetaData(apiInstanceMetaData.getName());
            if(apiTableMetaData == null)
            {
               continue;
            }

            if(BooleanUtils.isTrue(apiTableMetaData.getIsExcluded()))
            {
               continue;
            }
         }

         apis.add(thisApi);
      }

      context.contentType(ContentType.APPLICATION_JSON);
      context.result(JsonUtils.toJson(rs));
   }



   /*******************************************************************************
    ** list the versions in this api
    *******************************************************************************/
   private static void doVersions(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      Map<String, Object> rs = new HashMap<>();
      rs.put("supportedVersions", apiInstanceMetaData.getSupportedVersions().stream().map(String::valueOf).collect(Collectors.toList()));
      rs.put("currentVersion", apiInstanceMetaData.getCurrentVersion().toString());

      context.contentType(ContentType.APPLICATION_JSON);
      context.result(JsonUtils.toJson(rs));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setupCORS(Context context)
   {
      if(StringUtils.hasContent(context.header("Origin")))
      {
         context.res().setHeader("Access-Control-Allow-Origin", context.header("Origin"));
         context.res().setHeader("Vary", "Origin");
      }
      else
      {
         context.res().setHeader("Access-Control-Allow-Origin", "*");
      }

      context.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
      context.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization, Accept, content-type, authorization, accept");
      context.header("Access-Control-Allow-Credentials", "true");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doPathNotFound(Context context)
   {
      APILog apiLog = newAPILog(context);

      try
      {
         setupSession(context, null, null, null);
      }
      catch(Exception e)
      {
         //////////////////////////////////////////////////////////////////////////
         // if we don't have a session, we won't be able to store the api log... //
         //////////////////////////////////////////////////////////////////////////
         LOG.info("No session in a 404; will not create api log", e);
      }

      handleException(context, new QNotFoundException("Could not find any resources at path " + context.path()), apiLog);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void handleAuthorization(Context context)
   {
      try
      {
         ////////////////////////////////////////////////////////////////////////////////////////////////////////
         // clientId & clientSecret may either be provided as formParams, or in an Authorization: Basic header //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////
         String clientId;
         String clientSecret;
         String authorizationHeader = context.header("Authorization");
         if(authorizationHeader != null && authorizationHeader.startsWith("Basic "))
         {
            try
            {
               byte[]   credDecoded = Base64.getDecoder().decode(authorizationHeader.replace("Basic ", ""));
               String   credentials = new String(credDecoded, StandardCharsets.UTF_8);
               String[] parts       = credentials.split(":", 2);
               clientId = parts[0];
               clientSecret = parts[1];
            }
            catch(Exception e)
            {
               context.status(HttpStatus.BAD_REQUEST_400);
               context.result("Could not parse client_id and client_secret from Basic Authorization header.");
               return;
            }
         }
         else
         {
            clientId = context.formParam("client_id");
            if(clientId == null)
            {
               context.status(HttpStatus.BAD_REQUEST_400);
               context.result("'client_id' must be provided.");
               return;
            }
            clientSecret = context.formParam("client_secret");
            if(clientSecret == null)
            {
               context.status(HttpStatus.BAD_REQUEST_400);
               context.result("'client_secret' must be provided.");
               return;
            }
         }

         ////////////////////////////////////////////////////////
         // get the auth0 authentication module from qInstance //
         ////////////////////////////////////////////////////////
         Auth0AuthenticationMetaData     metaData                        = (Auth0AuthenticationMetaData) qInstance.getAuthentication();
         QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
         QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());

         try
         {
            //////////////////////////////////////////////////////////////////////////////////////////
            // make call to get access token data, if no exception thrown, assume 200 OK and return //
            //////////////////////////////////////////////////////////////////////////////////////////
            QContext.init(qInstance, null); // hmm...
            String accessToken = authenticationModule.createAccessToken(metaData, clientId, clientSecret);
            context.status(HttpStatus.Code.OK.getCode());
            context.result(accessToken);
            QJavalinAccessLogger.logEndSuccess();
            return;
         }
         catch(AccessTokenException aae)
         {
            LOG.info("Error getting api access token", aae, logPair("clientId", clientId));

            ///////////////////////////////////////////////////////////////////////////
            // if the exception has a status code, then return that code and message //
            ///////////////////////////////////////////////////////////////////////////
            if(aae.getStatusCode() != null)
            {
               context.status(aae.getStatusCode());
               context.result(aae.getMessage());
               QJavalinAccessLogger.logEndSuccess();
               return;
            }

            ////////////////////////////////////////////////////////
            // if no code, throw and handle like other exceptions //
            ////////////////////////////////////////////////////////
            throw (aae);
         }
      }
      catch(Exception e)
      {
         handleException(context, e);
         QJavalinAccessLogger.logEndFail(e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpecYaml(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      try
      {
         QContext.init(qInstance, null);
         String version = context.pathParam("version");

         GenerateOpenApiSpecInput input = new GenerateOpenApiSpecInput().withVersion(version);
         input.setApiName(apiInstanceMetaData.getName());

         try
         {
            if(StringUtils.hasContent(context.pathParam("tableName")))
            {
               input.setTableName(context.pathParam("tableName"));
            }
         }
         catch(Exception e)
         {
            ///////////////////////////
            // leave table param out //
            ///////////////////////////
         }

         GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecAction().execute(input);
         context.contentType(ContentType.APPLICATION_YAML);
         context.result(output.getYaml());
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpecJson(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      try
      {
         QContext.init(qInstance, null);
         String                   version = context.pathParam("version");
         GenerateOpenApiSpecInput input   = new GenerateOpenApiSpecInput().withVersion(version);
         input.setApiName(apiInstanceMetaData.getName());

         try
         {
            if(StringUtils.hasContent(context.pathParam("tableName")))
            {
               input.setTableName(context.pathParam("tableName"));
            }
         }
         catch(Exception e)
         {
            ///////////////////////////
            // leave table param out //
            ///////////////////////////
         }

         GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecAction().execute(input);
         context.contentType(ContentType.JSON);
         context.result(output.getJson());
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpecHtml(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version;

      try
      {
         version = context.pathParam("version");
      }
      catch(Exception e)
      {
         version = apiInstanceMetaData.getCurrentVersion().toString();
      }

      doSpecHtml(context, version, apiInstanceMetaData);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpecHtml(Context context, String version, ApiInstanceMetaData apiInstanceMetaData)
   {
      try
      {
         QBrandingMetaData branding = qInstance.getBranding();

         if(!apiInstanceMetaData.getSupportedVersions().contains(new APIVersion(version)))
         {
            doPathNotFound(context);
            return;
         }

         //////////////////////////////////
         // read html from resource file //
         //////////////////////////////////
         InputStream resourceAsStream = QJavalinApiHandler.class.getClassLoader().getResourceAsStream("rapidoc/rapidoc-container.html");
         String      html             = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8.name());

         /////////////////////////////////
         // do replacements in the html //
         /////////////////////////////////
         html = html.replace("{spec-url}", apiInstanceMetaData.getPath() + version + "/openapi.json");
         html = html.replace("{version}", version);
         html = html.replace("{primaryColor}", branding == null ? "#FF791A" : branding.getAccentColor());

         if(branding != null && StringUtils.hasContent(branding.getLogo()))
         {
            html = html.replace("{navLogoImg}", "<img id=\"navLogo\" slot=\"nav-logo\" src=\"" + branding.getLogo() + "\" />");
         }
         else
         {
            html = html.replace("{navLogoImg}", "");
         }

         html = html.replace("{title}", apiInstanceMetaData.getLabel() + " - " + version);

         StringBuilder                otherApisOptions             = new StringBuilder();
         ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(qInstance);
         for(Map.Entry<String, ApiInstanceMetaData> entry : apiInstanceMetaDataContainer.getApis().entrySet())
         {
            otherApisOptions.append("<option value=\"").append(entry.getValue().getPath()).append("\">").append(entry.getValue().getLabel()).append("</option>");
         }
         html = html.replace("{otherApisOptions}", otherApisOptions.toString());

         StringBuilder otherVersionOptions = new StringBuilder();
         for(APIVersion supportedVersion : apiInstanceMetaData.getSupportedVersions())
         {
            otherVersionOptions.append("<option value=\"").append(apiInstanceMetaData.getPath()).append(supportedVersion).append("/openapi.html\">").append(supportedVersion).append("</option>");
         }
         html = html.replace("{otherVersionOptions}", otherVersionOptions.toString());

         context.contentType(ContentType.HTML);
         context.result(html);
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setupSession(Context context, AbstractActionInput input, String version, ApiInstanceMetaData apiInstanceMetaData) throws QModuleDispatchException, QAuthenticationException
   {
      QSession session = QJavalinImplementation.setupSession(context, input);
      session.setValue("apiVersion", version);
      if(apiInstanceMetaData != null)
      {
         session.setValue("apiName", apiInstanceMetaData.getName());
         session.setValue("apiLabel", apiInstanceMetaData.getLabel());
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doGet(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.GET);
         String         tableName = table.getName();

         GetInput getInput = new GetInput();

         setupSession(context, getInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiGet", logPair("table", tableName), logPair("primaryKey", primaryKey));

         getInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(getInput, TablePermissionSubType.READ);

         // todo - validate that the primary key is of the proper type (e.g,. not a string for an id field)
         //  and throw a 400-series error (tell the user bad-request), rather than, we're doing a 500 (server error)

         getInput.setPrimaryKey(primaryKey);
         getInput.setIncludeAssociations(true);

         GetAction getAction = new GetAction();
         GetOutput getOutput = getAction.execute(getInput);

         ///////////////////////////////////////////////////////
         // throw a not found error if the record isn't found //
         ///////////////////////////////////////////////////////
         QRecord record = getOutput.getRecord();
         if(record == null)
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         Map<String, Serializable> outputRecord = QRecordApiAdapter.qRecordToApiMap(record, tableName, apiInstanceMetaData.getName(), version);

         QJavalinAccessLogger.logEndSuccess();
         String resultString = JsonUtils.toJson(outputRecord);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static APILog newAPILog(Context context)
   {
      APILog apiLog = new APILog()
         .withTimestamp(Instant.now())
         .withMethod(context.req().getMethod())
         .withPath(context.path())
         .withQueryString(context.queryString())
         .withRequestBody(context.body());

      try
      {
         apiLog.setVersion(context.pathParam("version"));
      }
      catch(Exception e)
      {
         //////////////////////////////////////////////////////////////////////////////////
         // pathParam throws if the param isn't found - in that case, just leave it null //
         //////////////////////////////////////////////////////////////////////////////////
      }

      return (apiLog);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void storeApiLog(APILog apiLog)
   {
      try
      {
         if(QContext.getQInstance().getTable(APILog.TABLE_NAME) != null)
         {
            QSession qSession = QContext.getQSession();
            if(qSession != null)
            {
               for(Map.Entry<String, List<Serializable>> entry : CollectionUtils.nonNullMap(qSession.getSecurityKeyValues()).entrySet())
               {
                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  // put the 1st entry for this key in the api log record                                                                                             //
                  // todo - might need revisited for users with multiple values...  e.g., look for the security key in records in the request?  or as part of the URL //
                  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                  if(CollectionUtils.nullSafeHasContents(entry.getValue()))
                  {
                     apiLog.withSecurityKeyValue(entry.getKey(), entry.getValue().get(0));
                  }
               }

               Integer userId = getApiLogUserId(qSession);
               apiLog.setApiLogUserId(userId);
            }

            InsertInput insertInput = new InsertInput();
            insertInput.setTableName(APILog.TABLE_NAME);
            insertInput.setRecords(List.of(apiLog.toQRecord()));
            new InsertAction().executeAsync(insertInput);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Error storing API log", e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer getApiLogUserId(QSession qSession) throws QException
   {
      String tableName = APILogMetaDataProvider.TABLE_NAME_API_LOG_USER;

      if(qSession == null)
      {
         return (null);
      }

      QUser qUser = qSession.getUser();
      if(qUser == null)
      {
         return (null);
      }

      String userName = qUser.getFullName();
      if(!StringUtils.hasContent(userName))
      {
         return (null);
      }

      /////////////////////////////////////////////////////////////////////////////
      // if we haven't cached this username to an id, query and/or insert it now //
      /////////////////////////////////////////////////////////////////////////////
      if(!apiLogUserIdCache.containsKey(userName))
      {
         //////////////////////////////////////////////////////////////
         // first try to get - if it's found, cache it and return it //
         //////////////////////////////////////////////////////////////
         Integer id = fetchApiLogUserIdFromName(userName);
         if(id != null)
         {
            apiLogUserIdCache.put(userName, id);
            return id;
         }

         try
         {
            ///////////////////////////////////////////////////////
            // if it wasn't found from a Get, then try an Insert //
            ///////////////////////////////////////////////////////
            LOG.info("Inserting " + tableName + " named " + userName);
            InsertInput insertInput = new InsertInput();
            insertInput.setTableName(tableName);
            QRecord record = new QRecord().withValue("name", userName);

            for(Map.Entry<String, List<Serializable>> entry : CollectionUtils.nonNullMap(qSession.getSecurityKeyValues()).entrySet())
            {
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               // put the 1st entry for this key in the api log user record                                                                                        //
               // todo - might need revisited for users with multiple values...  e.g., look for the security key in records in the request?  or as part of the URL //
               //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
               if(CollectionUtils.nullSafeHasContents(entry.getValue()))
               {
                  record.withValue(entry.getKey(), entry.getValue().get(0));
               }
            }

            insertInput.setRecords(List.of(record));
            InsertOutput insertOutput = new InsertAction().execute(insertInput);
            id = insertOutput.getRecords().get(0).getValueInteger("id");

            ////////////////////////////////////////
            // if we got an id, cache & return it //
            ////////////////////////////////////////
            if(id != null)
            {
               apiLogUserIdCache.put(userName, id);
               return id;
            }
         }
         catch(Exception e)
         {
            ////////////////////////////////////////////////////////////////////
            // assume this may mean a dupe-key - so - try another fetch below //
            ////////////////////////////////////////////////////////////////////
            LOG.info("Caught error inserting " + tableName + " named " + userName + " - will try to re-fetch", e);
         }

         //////////////////////////////////////////////////////////////////////////
         // if the insert failed, try another fetch (e.g., after a UK violation) //
         //////////////////////////////////////////////////////////////////////////
         id = fetchApiLogUserIdFromName(userName);
         if(id != null)
         {
            apiLogUserIdCache.put(userName, id);
            return id;
         }

         /////////////
         // give up //
         /////////////
         LOG.error("Unable to get id for " + tableName + " named " + userName);
         return (null);
      }

      return (apiLogUserIdCache.get(userName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Integer fetchApiLogUserIdFromName(String name) throws QException
   {
      GetInput getInput = new GetInput();
      getInput.setTableName(APILogMetaDataProvider.TABLE_NAME_API_LOG_USER);
      getInput.setUniqueKey(Map.of("name", name));
      GetOutput getOutput = new GetAction().execute(getInput);
      if(getOutput.getRecord() != null)
      {
         return (getOutput.getRecord().getValueInteger("id"));
      }

      return (null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doQuery(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String       version      = context.pathParam("version");
      String       tableApiName = context.pathParam("tableName");
      QQueryFilter filter       = null;
      APILog       apiLog       = newAPILog(context);

      try
      {
         List<String> badRequestMessages = new ArrayList<>();

         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.QUERY_BY_QUERY_STRING);
         String         tableName = table.getName();

         QueryInput queryInput = new QueryInput();
         setupSession(context, queryInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiQuery", logPair("table", tableName));

         queryInput.setTableName(tableName);
         queryInput.setIncludeAssociations(true);

         PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);

         Integer pageSize = 50;
         if(StringUtils.hasContent(context.queryParam("pageSize")))
         {
            try
            {
               pageSize = ValueUtils.getValueAsInteger(context.queryParam("pageSize"));
            }
            catch(Exception e)
            {
               badRequestMessages.add("Could not parse pageSize as an integer");
            }
         }
         if(pageSize < 1 || pageSize > 1000)
         {
            badRequestMessages.add("pageSize must be between 1 and 1000.");
         }

         Integer pageNo = 1;
         if(StringUtils.hasContent(context.queryParam("pageNo")))
         {
            try
            {
               pageNo = ValueUtils.getValueAsInteger(context.queryParam("pageNo"));
            }
            catch(Exception e)
            {
               badRequestMessages.add("Could not parse pageNo as an integer");
            }
         }
         if(pageNo < 1)
         {
            badRequestMessages.add("pageNo must be greater than 0.");
         }

         queryInput.setLimit(pageSize);
         queryInput.setSkip((pageNo - 1) * pageSize);

         // queryInput.setQueryJoins(processQueryJoinsParam(context));

         filter = new QQueryFilter();
         if("and".equalsIgnoreCase(context.queryParam("booleanOperator")))
         {
            filter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);
         }
         else if("or".equalsIgnoreCase(context.queryParam("booleanOperator")))
         {
            filter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
         }
         else if(StringUtils.hasContent(context.queryParam("booleanOperator")))
         {
            badRequestMessages.add("booleanOperator must be either AND or OR.");
         }

         boolean includeCount = true;
         if("true".equalsIgnoreCase(context.queryParam("includeCount")))
         {
            includeCount = true;
         }
         else if("false".equalsIgnoreCase(context.queryParam("includeCount")))
         {
            includeCount = false;
         }
         else if(StringUtils.hasContent(context.queryParam("includeCount")))
         {
            badRequestMessages.add("includeCount must be either true or false");
         }

         String orderBy = context.queryParam("orderBy");
         if(StringUtils.hasContent(orderBy))
         {
            for(String orderByPart : orderBy.split(","))
            {
               orderByPart = orderByPart.trim();
               String[] orderByNameDirection = orderByPart.split(" +");
               boolean  asc                  = true;
               if(orderByNameDirection.length == 2)
               {
                  if("asc".equalsIgnoreCase(orderByNameDirection[1]))
                  {
                     asc = true;
                  }
                  else if("desc".equalsIgnoreCase(orderByNameDirection[1]))
                  {
                     asc = false;
                  }
                  else
                  {
                     badRequestMessages.add("orderBy direction for field " + orderByNameDirection[0] + " must be either ASC or DESC.");
                  }
               }
               else if(orderByNameDirection.length > 2)
               {
                  badRequestMessages.add("Unrecognized format for orderBy clause: " + orderByPart + ".  Expected:  fieldName [ASC|DESC].");
               }

               try
               {
                  QFieldMetaData field = table.getField(orderByNameDirection[0]);
                  filter.withOrderBy(new QFilterOrderBy(field.getName(), asc));
               }
               catch(Exception e)
               {
                  badRequestMessages.add("Unrecognized orderBy field name: " + orderByNameDirection[0] + ".");
               }
            }
         }
         else
         {
            filter.withOrderBy(new QFilterOrderBy(table.getPrimaryKeyField(), false));
         }

         Set<String> nonFilterParams = Set.of("pageSize", "pageNo", "orderBy", "booleanOperator", "includeCount");

         ////////////////////////////
         // look for filter params //
         ////////////////////////////
         for(Map.Entry<String, List<String>> entry : context.queryParamMap().entrySet())
         {
            String       name   = entry.getKey();
            List<String> values = entry.getValue();

            if(nonFilterParams.contains(name))
            {
               continue;
            }

            try
            {
               QFieldMetaData field = table.getField(name);
               for(String value : values)
               {
                  if(StringUtils.hasContent(value))
                  {
                     try
                     {
                        filter.addCriteria(parseQueryParamToCriteria(name, value));
                     }
                     catch(Exception e)
                     {
                        badRequestMessages.add(e.getMessage());
                     }
                  }
               }
            }
            catch(Exception e)
            {
               badRequestMessages.add("Unrecognized filter criteria field: " + name);
            }
         }

         //////////////////////////////////////////
         // no more badRequest checks below here //
         //////////////////////////////////////////
         if(!badRequestMessages.isEmpty())
         {
            if(badRequestMessages.size() == 1)
            {
               throw (new QBadRequestException(badRequestMessages.get(0)));
            }
            else
            {
               throw (new QBadRequestException("Request failed with " + badRequestMessages.size() + " reasons: " + StringUtils.join(" \n", badRequestMessages)));
            }
         }

         //////////////////
         // do the query //
         //////////////////
         QueryAction queryAction = new QueryAction();
         queryInput.setFilter(filter);
         QueryOutput queryOutput = queryAction.execute(queryInput);

         Map<String, Serializable> output = new LinkedHashMap<>();
         output.put("pageNo", pageNo);
         output.put("pageSize", pageSize);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         // map record fields for api                                                                                  //
         // note - don't put them in the output until after the count, just because that looks a little nicer, i think //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
         ArrayList<Map<String, Serializable>> records = new ArrayList<>();
         for(QRecord record : queryOutput.getRecords())
         {
            records.add(QRecordApiAdapter.qRecordToApiMap(record, tableName, apiInstanceMetaData.getName(), version));
         }

         /////////////////////////////
         // optionally do the count //
         /////////////////////////////
         if(includeCount)
         {
            CountInput countInput = new CountInput();
            countInput.setTableName(tableName);
            countInput.setFilter(filter);
            CountOutput countOutput = new CountAction().execute(countInput);
            output.put("count", countOutput.getCount());
         }

         output.put("records", records);

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", queryOutput.getRecords().size()), QJavalinAccessLogger.logPairIfSlow("filter", filter, SLOW_LOG_THRESHOLD_MS));
         String resultString = JsonUtils.toJson(output);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e, logPair("filter", filter));
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData validateTableAndVersion(Context context, ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, ApiOperation operation) throws QNotFoundException
   {
      QNotFoundException qNotFoundException = new QNotFoundException("Could not find any resources at path " + context.path());

      QTableMetaData table    = getTableByApiName(apiInstanceMetaData.getName(), version, tableApiName);
      LogPair[]      logPairs = new LogPair[] { logPair("apiName", apiInstanceMetaData.getName()), logPair("version", version), logPair("tableApiName", tableApiName), logPair("operation", operation) };

      if(table == null)
      {
         LOG.info("404 because table is null", logPairs);
         throw (qNotFoundException);
      }

      if(BooleanUtils.isTrue(table.getIsHidden()))
      {
         LOG.info("404 because table isHidden", logPairs);
         throw (qNotFoundException);
      }

      ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
      if(apiTableMetaDataContainer == null)
      {
         LOG.info("404 because table apiMetaDataContainer is null", logPairs);
         throw (qNotFoundException);
      }

      ApiTableMetaData apiTableMetaData = apiTableMetaDataContainer.getApiTableMetaData(apiInstanceMetaData.getName());
      if(apiTableMetaData == null)
      {
         LOG.info("404 because table apiMetaData is null", logPairs);
         throw (qNotFoundException);
      }

      if(BooleanUtils.isTrue(apiTableMetaData.getIsExcluded()))
      {
         LOG.info("404 because table is excluded", logPairs);
         throw (qNotFoundException);
      }

      if(!operation.isOperationEnabled(List.of(apiInstanceMetaData, apiTableMetaData)))
      {
         LOG.info("404 because api operation is not enabled", logPairs);
         throw (qNotFoundException);
      }

      if(!table.isCapabilityEnabled(qInstance.getBackendForTable(table.getName()), operation.getCapability()))
      {
         LOG.info("404 because table capability is not enabled", logPairs);
         throw (qNotFoundException);
      }

      APIVersion       requestApiVersion = new APIVersion(version);
      List<APIVersion> supportedVersions = apiInstanceMetaData.getSupportedVersions();
      if(CollectionUtils.nullSafeIsEmpty(supportedVersions) || !supportedVersions.contains(requestApiVersion))
      {
         LOG.info("404 because requested version is not supported", logPairs);
         throw (qNotFoundException);
      }

      if(!apiTableMetaData.getApiVersionRange().includes(requestApiVersion))
      {
         LOG.info("404 because table version range does not include requested version", logPairs);
         throw (qNotFoundException);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData getTableByApiName(String apiName, String version, String tableApiName)
   {
      /////////////////////////////////////////////////////////////////////////////////////////////
      // tableApiNameMap is a map of (apiName,apiVersion) => Map<String, QTableMetaData>.        //
      // that is to say, a 2-level map.  The first level is keyed by (apiName,apiVersion) pairs. //
      // the second level is keyed by tableApiNames.                                             //
      /////////////////////////////////////////////////////////////////////////////////////////////
      Pair<String, String> key = new Pair<>(apiName, version);
      if(tableApiNameMap.get(key) == null)
      {
         Map<String, QTableMetaData> map = new HashMap<>();

         for(QTableMetaData table : qInstance.getTables().values())
         {
            ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
            if(apiTableMetaDataContainer != null)
            {
               ApiTableMetaData apiTableMetaData = apiTableMetaDataContainer.getApiTableMetaData(apiName);
               if(apiTableMetaData != null)
               {
                  String name = table.getName();
                  if(StringUtils.hasContent(apiTableMetaData.getApiTableName()))
                  {
                     name = apiTableMetaData.getApiTableName();
                  }
                  map.put(name, table);
               }
            }
         }

         tableApiNameMap.put(key, map);
      }

      return (tableApiNameMap.get(key).get(tableApiName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private enum Operator
   {
      ///////////////////////////////////////////////////////////////////////////////////
      // order of these is important (e.g., because some are a sub-string of others!!) //
      ///////////////////////////////////////////////////////////////////////////////////
      EQ("=", QCriteriaOperator.EQUALS, QCriteriaOperator.NOT_EQUALS, 1),
      LTE("<=", QCriteriaOperator.LESS_THAN_OR_EQUALS, null, 1),
      GTE(">=", QCriteriaOperator.GREATER_THAN_OR_EQUALS, null, 1),
      LT("<", QCriteriaOperator.LESS_THAN, null, 1),
      GT(">", QCriteriaOperator.GREATER_THAN, null, 1),
      EMPTY("EMPTY", QCriteriaOperator.IS_BLANK, QCriteriaOperator.IS_NOT_BLANK, 0),
      BETWEEN("BETWEEN ", QCriteriaOperator.BETWEEN, QCriteriaOperator.NOT_BETWEEN, 2),
      IN("IN ", QCriteriaOperator.IN, QCriteriaOperator.NOT_IN, null),
      LIKE("LIKE ", QCriteriaOperator.LIKE, QCriteriaOperator.NOT_LIKE, 1);


      private final String            prefix;
      private final QCriteriaOperator positiveOperator;
      private final QCriteriaOperator negativeOperator;
      private final Integer           noOfValues; // null means many (IN)



      /*******************************************************************************
       **
       *******************************************************************************/
      Operator(String prefix, QCriteriaOperator positiveOperator, QCriteriaOperator negativeOperator, Integer noOfValues)
      {
         this.prefix = prefix;
         this.positiveOperator = positiveOperator;
         this.negativeOperator = negativeOperator;
         this.noOfValues = noOfValues;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QFilterCriteria parseQueryParamToCriteria(String name, String value) throws QException
   {
      ///////////////////////////////////
      // process & discard a leading ! //
      ///////////////////////////////////
      boolean isNot = false;
      if(value.startsWith("!") && value.length() > 1)
      {
         isNot = true;
         value = value.substring(1);
      }

      //////////////////////////
      // look for an operator //
      //////////////////////////
      Operator selectedOperator = null;
      for(Operator op : Operator.values())
      {
         if(value.startsWith(op.prefix))
         {
            selectedOperator = op;
            if(selectedOperator.negativeOperator == null && isNot)
            {
               throw (new QBadRequestException("Unsupported operator: !" + selectedOperator.prefix));
            }
            break;
         }
      }

      /////////////////////////////////////////////////////////////////////////////////////////////
      // if an operator was found, strip it away from the value for figuring out the values part //
      /////////////////////////////////////////////////////////////////////////////////////////////
      if(selectedOperator != null)
      {
         value = value.substring(selectedOperator.prefix.length());
      }
      else
      {
         ////////////////////////////////////////////////////////////////
         // else - assume the default operator, and use the full value //
         ////////////////////////////////////////////////////////////////
         selectedOperator = Operator.EQ;
      }

      ////////////////////////////////////
      // figure out the criteria values //
      // todo - quotes?                 //
      ////////////////////////////////////
      List<Serializable> criteriaValues;
      if(selectedOperator.noOfValues == null)
      {
         criteriaValues = Arrays.asList(value.split(","));
      }
      else if(selectedOperator.noOfValues == 1)
      {
         criteriaValues = ListBuilder.of(value);
      }
      else if(selectedOperator.noOfValues == 0)
      {
         if(StringUtils.hasContent(value))
         {
            throw (new QBadRequestException("Unexpected value after operator " + selectedOperator.prefix + " for field " + name));
         }
         criteriaValues = null;
      }
      else if(selectedOperator.noOfValues == 2)
      {
         criteriaValues = Arrays.asList(value.split(","));
         if(criteriaValues.size() != 2)
         {
            throw (new QBadRequestException("Operator " + selectedOperator.prefix + " for field " + name + " requires 2 values (received " + criteriaValues.size() + ")"));
         }
      }
      else
      {
         throw (new QException("Unexpected noOfValues [" + selectedOperator.noOfValues + "] in operator [" + selectedOperator + "]"));
      }

      return (new QFilterCriteria(name, isNot ? selectedOperator.negativeOperator : selectedOperator.positiveOperator, criteriaValues));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doInsert(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.INSERT);
         String         tableName = table.getName();

         InsertInput insertInput = new InsertInput();

         setupSession(context, insertInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiInsert", logPair("table", tableName));

         insertInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required POST body"));
            }

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONObject  jsonObject  = new JSONObject(jsonTokener);

            insertInput.setRecords(List.of(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, apiInstanceMetaData.getName(), version, false)));

            if(jsonTokener.more())
            {
               throw (new QBadRequestException("Body contained more than a single JSON object."));
            }
         }
         catch(QBadRequestException qbre)
         {
            throw (qbre);
         }
         catch(Exception e)
         {
            throw (new QBadRequestException("Body could not be parsed as a JSON object: " + e.getMessage(), e));
         }

         InsertAction insertAction = new InsertAction();
         InsertOutput insertOutput = insertAction.execute(insertInput);

         List<String> errors = insertOutput.getRecords().get(0).getErrors();
         if(CollectionUtils.nullSafeHasContents(errors))
         {
            boolean isBadRequest = areAnyErrorsBadRequest(errors);

            String message = "Error inserting " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors);
            if(isBadRequest)
            {
               throw (new QBadRequestException(message));
            }
            else
            {
               throw (new QException(message));
            }
         }

         LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
         outputRecord.put(table.getPrimaryKeyField(), insertOutput.getRecords().get(0).getValue(table.getPrimaryKeyField()));

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.CREATED.getCode());
         String resultString = JsonUtils.toJson(outputRecord);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean areAnyErrorsBadRequest(List<String> errors)
   {
      boolean isBadRequest = errors.stream().anyMatch(e ->
         e.contains("Missing value in required field")
            || e.contains("You do not have permission")
      );
      return isBadRequest;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkInsert(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_INSERT);
         String         tableName = table.getName();

         InsertInput insertInput = new InsertInput();

         setupSession(context, insertInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiBulkInsert", logPair("table", tableName));

         insertInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

         /////////////////
         // build input //
         /////////////////
         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required POST body"));
            }

            ArrayList<QRecord> recordList = new ArrayList<>();
            insertInput.setRecords(recordList);

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONArray   jsonArray   = new JSONArray(jsonTokener);

            for(int i = 0; i < jsonArray.length(); i++)
            {
               JSONObject jsonObject = jsonArray.getJSONObject(i);
               recordList.add(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, apiInstanceMetaData.getName(), version, false));
            }

            if(jsonTokener.more())
            {
               throw (new QBadRequestException("Body contained more than a single JSON array."));
            }

            if(recordList.isEmpty())
            {
               throw (new QBadRequestException("No records were found in the POST body"));
            }
         }
         catch(QBadRequestException qbre)
         {
            throw (qbre);
         }
         catch(Exception e)
         {
            throw (new QBadRequestException("Body could not be parsed as a JSON array: " + e.getMessage(), e));
         }

         //////////////
         // execute! //
         //////////////
         InsertAction insertAction = new InsertAction();
         InsertOutput insertOutput = insertAction.execute(insertInput);

         ///////////////////////////////////////
         // process records to build response //
         ///////////////////////////////////////
         List<Map<String, Serializable>> response = new ArrayList<>();
         for(QRecord record : insertOutput.getRecords())
         {
            LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
            response.add(outputRecord);

            List<String> errors = record.getErrors();
            if(CollectionUtils.nullSafeHasContents(errors))
            {
               outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
               outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
               outputRecord.put("error", "Error inserting " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors));
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.CREATED.getCode());
               outputRecord.put("statusText", HttpStatus.Code.CREATED.getMessage());
               outputRecord.put(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
            }
         }

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", insertInput.getRecords().size()));
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         String resultString = JsonUtils.toJson(response);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkUpdate(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_UPDATE);
         String         tableName = table.getName();

         UpdateInput updateInput = new UpdateInput();

         setupSession(context, updateInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiBulkUpdate", logPair("table", tableName));

         updateInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

         /////////////////
         // build input //
         /////////////////
         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required PATCH body"));
            }

            ArrayList<QRecord> recordList = new ArrayList<>();
            updateInput.setRecords(recordList);

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONArray   jsonArray   = new JSONArray(jsonTokener);

            for(int i = 0; i < jsonArray.length(); i++)
            {
               JSONObject jsonObject = jsonArray.getJSONObject(i);
               recordList.add(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, apiInstanceMetaData.getName(), version, true));
            }

            if(jsonTokener.more())
            {
               throw (new QBadRequestException("Body contained more than a single JSON array."));
            }

            if(recordList.isEmpty())
            {
               throw (new QBadRequestException("No records were found in the PATCH body"));
            }
         }
         catch(QBadRequestException qbre)
         {
            throw (qbre);
         }
         catch(Exception e)
         {
            throw (new QBadRequestException("Body could not be parsed as a JSON array: " + e.getMessage(), e));
         }

         //////////////
         // execute! //
         //////////////
         UpdateAction updateAction = new UpdateAction();
         UpdateOutput updateOutput = updateAction.execute(updateInput);

         ///////////////////////////////////////
         // process records to build response //
         ///////////////////////////////////////
         List<Map<String, Serializable>> response = new ArrayList<>();
         int                             i        = 0;
         for(QRecord record : updateOutput.getRecords())
         {
            LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
            response.add(outputRecord);

            try
            {
               QRecord      inputRecord = updateInput.getRecords().get(i);
               Serializable primaryKey  = inputRecord.getValue(table.getPrimaryKeyField());
               outputRecord.put(table.getPrimaryKeyField(), primaryKey);
            }
            catch(Exception e)
            {
               //////////
               // omit //
               //////////
            }

            List<String> errors = record.getErrors();
            if(CollectionUtils.nullSafeHasContents(errors))
            {
               outputRecord.put("error", "Error updating " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors));
               if(areAnyErrorsNotFound(errors))
               {
                  outputRecord.put("statusCode", HttpStatus.Code.NOT_FOUND.getCode());
                  outputRecord.put("statusText", HttpStatus.Code.NOT_FOUND.getMessage());
               }
               else
               {
                  outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
                  outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
               }
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.NO_CONTENT.getCode());
               outputRecord.put("statusText", HttpStatus.Code.NO_CONTENT.getMessage());
            }

            i++;
         }

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", updateInput.getRecords().size()));
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         String resultString = JsonUtils.toJson(response);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean areAnyErrorsNotFound(List<String> errors)
   {
      return errors.stream().anyMatch(e -> e.startsWith(UpdateAction.NOT_FOUND_ERROR_PREFIX) || e.startsWith(DeleteAction.NOT_FOUND_ERROR_PREFIX));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkDelete(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_DELETE);
         String         tableName = table.getName();

         DeleteInput deleteInput = new DeleteInput();

         setupSession(context, deleteInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiBulkDelete", logPair("table", tableName));

         deleteInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);

         /////////////////
         // build input //
         /////////////////
         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required DELETE body"));
            }

            ArrayList<Serializable> primaryKeyList = new ArrayList<>();
            deleteInput.setPrimaryKeys(primaryKeyList);

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONArray   jsonArray   = new JSONArray(jsonTokener);

            for(int i = 0; i < jsonArray.length(); i++)
            {
               Object object = jsonArray.get(i);
               if(object instanceof JSONArray || object instanceof JSONObject)
               {
                  throw (new QBadRequestException("One or more elements inside the DELETE body JSONArray was not a primitive value"));
               }
               primaryKeyList.add(String.valueOf(object));
            }

            if(jsonTokener.more())
            {
               throw (new QBadRequestException("Body contained more than a single JSON array."));
            }

            if(primaryKeyList.isEmpty())
            {
               throw (new QBadRequestException("No primary keys were found in the DELETE body"));
            }
         }
         catch(QBadRequestException qbre)
         {
            throw (qbre);
         }
         catch(Exception e)
         {
            throw (new QBadRequestException("Body could not be parsed as a JSON array: " + e.getMessage(), e));
         }

         //////////////
         // execute! //
         //////////////
         DeleteAction deleteAction = new DeleteAction();
         DeleteOutput deleteOutput = deleteAction.execute(deleteInput);

         ///////////////////////////////////////
         // process records to build response //
         ///////////////////////////////////////
         List<Map<String, Serializable>> response = new ArrayList<>();

         List<QRecord>             recordsWithErrors     = deleteOutput.getRecordsWithErrors();
         Map<String, List<String>> primaryKeyToErrorsMap = new HashMap<>();
         for(QRecord recordWithError : CollectionUtils.nonNullList(recordsWithErrors))
         {
            String primaryKey = recordWithError.getValueString(table.getPrimaryKeyField());
            primaryKeyToErrorsMap.put(primaryKey, recordWithError.getErrors());
         }

         for(Serializable primaryKey : deleteInput.getPrimaryKeys())
         {
            LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
            response.add(outputRecord);
            outputRecord.put(table.getPrimaryKeyField(), primaryKey);

            String       primaryKeyString = ValueUtils.getValueAsString(primaryKey);
            List<String> errors           = primaryKeyToErrorsMap.get(primaryKeyString);
            if(CollectionUtils.nullSafeHasContents(errors))
            {
               outputRecord.put("error", "Error deleting " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors));
               if(areAnyErrorsNotFound(errors))
               {
                  outputRecord.put("statusCode", HttpStatus.Code.NOT_FOUND.getCode());
                  outputRecord.put("statusText", HttpStatus.Code.NOT_FOUND.getMessage());
               }
               else
               {
                  outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
                  outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
               }
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.NO_CONTENT.getCode());
               outputRecord.put("statusText", HttpStatus.Code.NO_CONTENT.getMessage());
            }
         }

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", deleteInput.getPrimaryKeys().size()));
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         String resultString = JsonUtils.toJson(response);
         context.result(resultString);
         storeApiLog(apiLog.withStatusCode(context.statusCode()).withResponseBody(resultString));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doUpdate(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.UPDATE);
         String         tableName = table.getName();

         UpdateInput updateInput = new UpdateInput();

         setupSession(context, updateInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiUpdate", logPair("table", tableName));

         updateInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required PATCH body"));
            }

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONObject  jsonObject  = new JSONObject(jsonTokener);

            QRecord qRecord = QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, apiInstanceMetaData.getName(), version, false);
            qRecord.setValue(table.getPrimaryKeyField(), primaryKey);
            updateInput.setRecords(List.of(qRecord));

            if(jsonTokener.more())
            {
               throw (new QBadRequestException("Body contained more than a single JSON object."));
            }
         }
         catch(QBadRequestException qbre)
         {
            throw (qbre);
         }
         catch(Exception e)
         {
            throw (new QBadRequestException("Body could not be parsed as a JSON object: " + e.getMessage(), e));
         }

         UpdateAction updateAction = new UpdateAction();
         UpdateOutput updateOutput = updateAction.execute(updateInput);

         List<String> errors = updateOutput.getRecords().get(0).getErrors();
         if(CollectionUtils.nullSafeHasContents(errors))
         {
            if(areAnyErrorsNotFound(errors))
            {
               throw (new QNotFoundException("Could not find " + table.getLabel() + " with " + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
            }
            else
            {
               boolean isBadRequest = areAnyErrorsBadRequest(errors);

               String message = "Error updating " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors);
               if(isBadRequest)
               {
                  throw (new QBadRequestException(message));
               }
               else
               {
                  throw (new QException(message));
               }
            }
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.NO_CONTENT.getCode());
         storeApiLog(apiLog.withStatusCode(context.statusCode()));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doDelete(Context context, ApiInstanceMetaData apiInstanceMetaData)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");
      APILog apiLog       = newAPILog(context);

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, apiInstanceMetaData, version, tableApiName, ApiOperation.DELETE);
         String         tableName = table.getName();

         DeleteInput deleteInput = new DeleteInput();

         setupSession(context, deleteInput, version, apiInstanceMetaData);
         QJavalinAccessLogger.logStart("apiDelete", logPair("table", tableName));

         deleteInput.setTableName(tableName);
         deleteInput.setPrimaryKeys(List.of(primaryKey));

         PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);

         ///////////////////
         // do the delete //
         ///////////////////
         DeleteAction deleteAction = new DeleteAction();
         DeleteOutput deleteOutput = deleteAction.execute(deleteInput);
         if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
         {
            if(areAnyErrorsNotFound(deleteOutput.getRecordsWithErrors().get(0).getErrors()))
            {
               throw (new QNotFoundException("Could not find " + table.getLabel() + " with " + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
            }
            else
            {
               throw (new QException("Error deleting " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(deleteOutput.getRecordsWithErrors().get(0).getErrors())));
            }
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.NO_CONTENT.getCode());
         storeApiLog(apiLog.withStatusCode(context.statusCode()));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e, apiLog);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(Context context, Exception e, APILog apiLog)
   {
      handleException(null, context, e, apiLog);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(Context context, Exception e)
   {
      handleException(null, context, e, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(HttpStatus.Code statusCode, Context context, Exception e, APILog apiLog)
   {
      QBadRequestException badRequestException = ExceptionUtils.findClassInRootChain(e, QBadRequestException.class);
      if(badRequestException != null)
      {
         statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.BAD_REQUEST); // 400
         respondWithError(context, statusCode, badRequestException.getMessage(), apiLog);
         return;
      }

      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         if(userFacingException instanceof QNotFoundException)
         {
            statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.NOT_FOUND); // 404
            respondWithError(context, statusCode, userFacingException.getMessage(), apiLog);
            return;
         }
         else
         {
            LOG.info("User-facing exception", e);
            statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.INTERNAL_SERVER_ERROR); // 500
            respondWithError(context, statusCode, userFacingException.getMessage(), apiLog);
            return;
         }
      }
      else
      {
         if(e instanceof QAuthenticationException)
         {
            respondWithError(context, HttpStatus.Code.UNAUTHORIZED, e.getMessage(), apiLog); // 401
            return;
         }

         if(e instanceof QPermissionDeniedException)
         {
            respondWithError(context, HttpStatus.Code.FORBIDDEN, e.getMessage(), apiLog); // 403
            return;
         }

         ////////////////////////////////
         // default exception handling //
         ////////////////////////////////
         LOG.warn("Exception in javalin request", e);
         respondWithError(context, HttpStatus.Code.INTERNAL_SERVER_ERROR, e.getClass().getSimpleName() + " (" + e.getMessage() + ")", apiLog); // 500
         return;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void respondWithError(Context context, HttpStatus.Code statusCode, String errorMessage, APILog apiLog)
   {
      context.status(statusCode.getCode());

      try
      {
         String accept = context.header("accept");
         if(accept != null && accept.toLowerCase().startsWith(ContentType.HTML))
         {
            context.contentType(ContentType.HTML);
            context.result("Error: " + errorMessage);
            return;
         }
      }
      catch(Exception e)
      {
         ///////////////////////////
         // just do default thing //
         ///////////////////////////
      }

      String responseBody = JsonUtils.toJson(Map.of("error", errorMessage));
      context.result(responseBody);

      if(apiLog != null)
      {
         if(QContext.getQSession() != null && QContext.getQInstance() != null)
         {
            apiLog.withStatusCode(statusCode.getCode()).withResponseBody(responseBody);
            storeApiLog(apiLog);
         }
      }
   }

}
