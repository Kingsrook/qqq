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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qqq.api.actions.GenerateOpenApiSpecAction;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.javalin.QJavalinAccessLogger;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
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

   private static Map<String, Map<String, QTableMetaData>> tableApiNameMap = new HashMap<>();



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

         ApiBuilder.path("/api/{version}", () -> // todo - configurable, that /api/ bit?
         {
            ApiBuilder.get("/openapi.yaml", QJavalinApiHandler::doSpecYaml);
            ApiBuilder.get("/openapi.json", QJavalinApiHandler::doSpecJson);

            ApiBuilder.path("/{tableName}", () ->
            {
               ApiBuilder.get("/openapi.yaml", QJavalinApiHandler::doSpecYaml);
               ApiBuilder.get("/openapi.json", QJavalinApiHandler::doSpecJson);

               ApiBuilder.post("/", QJavalinApiHandler::doInsert);

               ApiBuilder.get("/query", QJavalinApiHandler::doQuery);
               // ApiBuilder.post("/query", QJavalinApiHandler::doQuery);

               ApiBuilder.post("/bulk", QJavalinApiHandler::bulkInsert);
               ApiBuilder.patch("/bulk", QJavalinApiHandler::bulkUpdate);
               ApiBuilder.delete("/bulk", QJavalinApiHandler::bulkDelete);

               //////////////////////////////////////////////////////////////////
               // remember to keep the wildcard paths after the specific paths //
               //////////////////////////////////////////////////////////////////
               ApiBuilder.get("/{primaryKey}", QJavalinApiHandler::doGet);
               ApiBuilder.patch("/{primaryKey}", QJavalinApiHandler::doUpdate);
               ApiBuilder.delete("/{primaryKey}", QJavalinApiHandler::doDelete);
            });
         });

         ApiBuilder.get("/api/versions.json", QJavalinApiHandler::doVersions);

         ApiBuilder.before("/*", QJavalinApiHandler::setupCORS);

         //////////////////////////////////////////////////////////////////////////////////////////////
         // default all other /api/ requests (for the methods we support) to a standard 404 response //
         //////////////////////////////////////////////////////////////////////////////////////////////
         ApiBuilder.get("/api/*", QJavalinApiHandler::doPathNotFound);
         ApiBuilder.delete("/api/*", QJavalinApiHandler::doPathNotFound);
         ApiBuilder.patch("/api/*", QJavalinApiHandler::doPathNotFound);
         ApiBuilder.post("/api/*", QJavalinApiHandler::doPathNotFound);
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doVersions(Context context)
   {
      ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaData.of(qInstance);

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
      handleException(context, new QNotFoundException("Could not find any resources at path " + context.path()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpecYaml(Context context)
   {
      try
      {
         QContext.init(qInstance, null);
         String                    version = context.pathParam("version");
         GenerateOpenApiSpecOutput output  = new GenerateOpenApiSpecAction().execute(new GenerateOpenApiSpecInput().withVersion(version));
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
   private static void handleAuthorization(Context context)
   {
      try
      {
         //////////////////////////////
         // validate required inputs //
         //////////////////////////////
         String clientId = context.formParam("client_id");
         if(clientId == null)
         {
            context.status(HttpStatus.BAD_REQUEST_400);
            context.result("'client_id' must be provided.");
            return;
         }
         String clientSecret = context.formParam("client_secret");
         if(clientSecret == null)
         {
            context.status(HttpStatus.BAD_REQUEST_400);
            context.result("'client_secret' must be provided.");
            return;
         }

         ////////////////////////////////////////////////////////
         // get the auth0 authentication module from qInstance //
         ////////////////////////////////////////////////////////
         Auth0AuthenticationMetaData     metaData                        = (Auth0AuthenticationMetaData) qInstance.getAuthentication();
         QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
         QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());

         try
         {
            /////////////////////////////////////////////////////////////////////////////////////////
            // make call to get access token data, if no exception thrown, assume 200OK and return //
            /////////////////////////////////////////////////////////////////////////////////////////
            QContext.init(qInstance, null); // hmm...
            String accessToken = authenticationModule.createAccessToken(metaData, clientId, clientSecret);
            context.status(io.javalin.http.HttpStatus.OK);
            context.result(accessToken);
            QJavalinAccessLogger.logEndSuccess();
            return;
         }
         catch(AccessTokenException aae)
         {
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
   private static void doSpecJson(Context context)
   {
      try
      {
         QContext.init(qInstance, null);
         String                   version = context.pathParam("version");
         GenerateOpenApiSpecInput input   = new GenerateOpenApiSpecInput().withVersion(version);

         if(StringUtils.hasContent(context.pathParam("tableName")))
         {
            input.setTableName(context.pathParam("tableName"));
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
   public static void setupSession(Context context, AbstractActionInput input, String version) throws QModuleDispatchException, QAuthenticationException
   {
      QSession session = QJavalinImplementation.setupSession(context, input);
      session.setValue("apiVersion", version);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doGet(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         GetInput getInput = new GetInput();

         setupSession(context, getInput, version);
         QJavalinAccessLogger.logStart("apiGet", logPair("table", tableName), logPair("primaryKey", primaryKey));

         getInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(getInput, TablePermissionSubType.READ);

         // todo - validate that the primary key is of the proper type (e.g,. not a string for an id field)
         //  and throw a 400-series error (tell the user bad-request), rather than, we're doing a 500 (server error)

         getInput.setPrimaryKey(primaryKey);

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

         Map<String, Serializable> outputRecord = QRecordApiAdapter.qRecordToApiMap(record, tableName, version);

         QJavalinAccessLogger.logEndSuccess();
         context.result(JsonUtils.toJson(outputRecord));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doQuery(Context context)
   {
      String       version      = context.pathParam("version");
      String       tableApiName = context.pathParam("tableName");
      QQueryFilter filter       = null;

      try
      {
         List<String> badRequestMessages = new ArrayList<>();

         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         QueryInput queryInput = new QueryInput();
         setupSession(context, queryInput, version);
         QJavalinAccessLogger.logStart("apiQuery", logPair("table", tableName));

         queryInput.setTableName(tableName);

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

         Map<String, Serializable> output = new HashMap<>();
         output.put("pageSize", pageSize);
         output.put("pageNo", pageNo);

         ///////////////////////////////
         // map record fields for api //
         ///////////////////////////////
         ArrayList<Map<String, Serializable>> records = new ArrayList<>();
         for(QRecord record : queryOutput.getRecords())
         {
            records.add(QRecordApiAdapter.qRecordToApiMap(record, tableName, version));
         }
         output.put("records", records);

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

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", queryOutput.getRecords().size()), QJavalinAccessLogger.logPairIfSlow("filter", filter, SLOW_LOG_THRESHOLD_MS));
         context.result(JsonUtils.toJson(output));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e, logPair("filter", filter));
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData validateTableAndVersion(Context context, String version, String tableApiName) throws QNotFoundException
   {
      QNotFoundException qNotFoundException = new QNotFoundException("Could not find any resources at path " + context.path());

      QTableMetaData table = getTableByApiName(version, tableApiName);

      if(table == null)
      {
         throw (qNotFoundException);
      }

      if(BooleanUtils.isTrue(table.getIsHidden()))
      {
         throw (qNotFoundException);
      }

      ApiTableMetaData apiTableMetaData = ApiTableMetaData.of(table);
      if(apiTableMetaData == null)
      {
         throw (qNotFoundException);
      }

      if(BooleanUtils.isTrue(apiTableMetaData.getIsExcluded()))
      {
         throw (qNotFoundException);
      }

      APIVersion       requestApiVersion = new APIVersion(version);
      List<APIVersion> supportedVersions = ApiInstanceMetaData.of(qInstance).getSupportedVersions();
      if(CollectionUtils.nullSafeIsEmpty(supportedVersions) || !supportedVersions.contains(requestApiVersion))
      {
         throw (qNotFoundException);
      }

      if(!apiTableMetaData.getApiVersionRange().includes(requestApiVersion))
      {
         throw (qNotFoundException);
      }

      return (table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QTableMetaData getTableByApiName(String version, String tableApiName)
   {
      if(tableApiNameMap.get(version) == null)
      {
         Map<String, QTableMetaData> map = new HashMap<>();

         for(QTableMetaData table : qInstance.getTables().values())
         {
            ApiTableMetaData apiTableMetaData = ApiTableMetaData.of(table);
            String           name             = table.getName();
            if(apiTableMetaData != null && StringUtils.hasContent(apiTableMetaData.getApiTableName()))
            {
               name = apiTableMetaData.getApiTableName();
            }
            map.put(name, table);
         }

         tableApiNameMap.put(version, map);
      }

      return (tableApiNameMap.get(version).get(tableApiName));
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
   private static void doInsert(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         InsertInput insertInput = new InsertInput();

         setupSession(context, insertInput, version);
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

            insertInput.setRecords(List.of(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, version)));

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

         LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
         outputRecord.put(table.getPrimaryKeyField(), insertOutput.getRecords().get(0).getValue(table.getPrimaryKeyField()));

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.CREATED.getCode());
         context.result(JsonUtils.toJson(outputRecord));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkInsert(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         InsertInput insertInput = new InsertInput();

         setupSession(context, insertInput, version);
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
               recordList.add(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, version));
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

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         context.result(JsonUtils.toJson(response));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkUpdate(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         UpdateInput updateInput = new UpdateInput();

         setupSession(context, updateInput, version);
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
               recordList.add(QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, version));
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
         for(QRecord record : updateOutput.getRecords())
         {
            LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
            response.add(outputRecord);

            List<String> errors = record.getErrors();
            if(CollectionUtils.nullSafeHasContents(errors))
            {
               outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
               outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
               outputRecord.put("error", "Error updating " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors));
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.NO_CONTENT.getCode());
               outputRecord.put("statusText", HttpStatus.Code.NO_CONTENT.getMessage());
            }
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         context.result(JsonUtils.toJson(response));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void bulkDelete(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         DeleteInput deleteInput = new DeleteInput();

         setupSession(context, deleteInput, version);
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

         List<QRecord>       recordsWithErrors    = deleteOutput.getRecordsWithErrors();
         Map<String, String> primaryKeyToErrorMap = new HashMap<>();
         for(QRecord recordWithError : CollectionUtils.nonNullList(recordsWithErrors))
         {
            String primaryKey = recordWithError.getValueString(table.getPrimaryKeyField());
            primaryKeyToErrorMap.put(primaryKey, StringUtils.join(", ", recordWithError.getErrors()));
         }

         for(Serializable primaryKey : deleteInput.getPrimaryKeys())
         {
            LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
            response.add(outputRecord);

            String primaryKeyString = ValueUtils.getValueAsString((primaryKey));
            if(primaryKeyToErrorMap.containsKey(primaryKeyString))
            {
               outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
               outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
               outputRecord.put("error", "Error deleting " + table.getLabel() + ": " + primaryKeyToErrorMap.get(primaryKeyString));
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.NO_CONTENT.getCode());
               outputRecord.put("statusText", HttpStatus.Code.NO_CONTENT.getMessage());
            }
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.MULTI_STATUS.getCode());
         context.result(JsonUtils.toJson(response));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doUpdate(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         UpdateInput updateInput = new UpdateInput();

         setupSession(context, updateInput, version);
         QJavalinAccessLogger.logStart("apiUpdate", logPair("table", tableName));

         updateInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

         ///////////////////////////////////////////////////////
         // throw a not found error if the record isn't found //
         ///////////////////////////////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName(tableName);
         getInput.setPrimaryKey(primaryKey);
         GetAction getAction = new GetAction();
         GetOutput getOutput = getAction.execute(getInput);
         if(getOutput.getRecord() == null)
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required PATCH body"));
            }

            JSONTokener jsonTokener = new JSONTokener(context.body().trim());
            JSONObject  jsonObject  = new JSONObject(jsonTokener);

            QRecord qRecord = QRecordApiAdapter.apiJsonObjectToQRecord(jsonObject, tableName, version);
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
            throw (new QException("Error updating " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(errors)));
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.NO_CONTENT.getCode());
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doDelete(Context context)
   {
      String version      = context.pathParam("version");
      String tableApiName = context.pathParam("tableName");
      String primaryKey   = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table     = validateTableAndVersion(context, version, tableApiName);
         String         tableName = table.getName();

         DeleteInput deleteInput = new DeleteInput();

         setupSession(context, deleteInput, version);
         QJavalinAccessLogger.logStart("apiDelete", logPair("table", tableName));

         deleteInput.setTableName(tableName);
         deleteInput.setPrimaryKeys(List.of(primaryKey));

         PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);

         ///////////////////////////////////////////////////////
         // throw a not found error if the record isn't found //
         ///////////////////////////////////////////////////////
         GetInput getInput = new GetInput();
         getInput.setTableName(tableName);
         getInput.setPrimaryKey(primaryKey);
         GetAction getAction = new GetAction();
         GetOutput getOutput = getAction.execute(getInput);
         if(getOutput.getRecord() == null)
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         ///////////////////
         // do the delete //
         ///////////////////
         DeleteAction deleteAction = new DeleteAction();
         DeleteOutput deleteOutput = deleteAction.execute(deleteInput);
         if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
         {
            throw (new QException("Error deleting " + table.getLabel() + ": " + StringUtils.joinWithCommasAndAnd(deleteOutput.getRecordsWithErrors().get(0).getErrors())));
         }

         QJavalinAccessLogger.logEndSuccess();
         context.status(HttpStatus.Code.NO_CONTENT.getCode());
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(Context context, Exception e)
   {
      handleException(null, context, e);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(HttpStatus.Code statusCode, Context context, Exception e)
   {
      QBadRequestException badRequestException = ExceptionUtils.findClassInRootChain(e, QBadRequestException.class);
      if(badRequestException != null)
      {
         statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.BAD_REQUEST); // 400
         respondWithError(context, statusCode, badRequestException.getMessage());
         return;
      }

      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         if(userFacingException instanceof QNotFoundException)
         {
            statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.NOT_FOUND); // 404
            respondWithError(context, statusCode, userFacingException.getMessage());
         }
         else
         {
            LOG.info("User-facing exception", e);
            statusCode = Objects.requireNonNullElse(statusCode, HttpStatus.Code.INTERNAL_SERVER_ERROR); // 500
            respondWithError(context, statusCode, userFacingException.getMessage());
         }
      }
      else
      {
         if(e instanceof QAuthenticationException)
         {
            respondWithError(context, HttpStatus.Code.UNAUTHORIZED, e.getMessage()); // 401
            return;
         }

         if(e instanceof QPermissionDeniedException)
         {
            respondWithError(context, HttpStatus.Code.FORBIDDEN, e.getMessage()); // 403
            return;
         }

         ////////////////////////////////
         // default exception handling //
         ////////////////////////////////
         LOG.warn("Exception in javalin request", e);
         respondWithError(context, HttpStatus.Code.INTERNAL_SERVER_ERROR, e.getClass().getSimpleName() + " (" + e.getMessage() + ")"); // 500
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void respondWithError(Context context, HttpStatus.Code statusCode, String errorMessage)
   {
      context.status(statusCode.getCode());
      context.result(JsonUtils.toJson(Map.of("error", errorMessage)));
   }

}
