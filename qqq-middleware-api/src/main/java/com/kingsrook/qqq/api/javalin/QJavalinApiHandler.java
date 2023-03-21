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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.api.actions.GenerateOpenApiSpecAction;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinAccessLogger;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.backend.javalin.QJavalinUtils;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;
import static com.kingsrook.qqq.backend.javalin.QJavalinImplementation.SLOW_LOG_THRESHOLD_MS;


/*******************************************************************************
 ** methods for handling qqq API requests in javalin.
 *******************************************************************************/
public class QJavalinApiHandler
{
   private static final QLogger LOG = QLogger.getLogger(QJavalinApiHandler.class);

   static QInstance qInstance;



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
         ApiBuilder.path("/api/{version}", () -> // todo - configurable, that /api/ bit?
         {
            ApiBuilder.get("/openapi.yaml", QJavalinApiHandler::doSpec);

            ApiBuilder.path("/{tableName}", () ->
            {
               ApiBuilder.post("/", QJavalinApiHandler::doInsert);

               ApiBuilder.get("/query", QJavalinApiHandler::doQuery);
               ApiBuilder.post("/query", QJavalinApiHandler::doQuery);

               ApiBuilder.get("/{primaryKey}", QJavalinApiHandler::doGet);
               ApiBuilder.patch("/{primaryKey}", QJavalinApiHandler::doUpdate);
               ApiBuilder.delete("/{primaryKey}", QJavalinApiHandler::doDelete);

               // post("/bulk", QJavalinApiHandler::bulkInsert);
               // patch("/bulk", QJavalinApiHandler::bulkUpdate);
               // delete("/bulk", QJavalinApiHandler::bulkDelete);
            });
         });
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doSpec(Context context)
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
         QJavalinImplementation.handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setupSession(Context context, AbstractActionInput input) throws QModuleDispatchException, QAuthenticationException
   {
      QJavalinImplementation.setupSession(context, input);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static APIVersionRange getApiVersionRange(QTableMetaData table)
   {
      ApiTableMetaData middlewareMetaData = ApiMiddlewareType.getApiTableMetaData(table);
      if(middlewareMetaData != null && middlewareMetaData.getInitialVersion() != null)
      {
         return (APIVersionRange.afterAndIncluding(middlewareMetaData.getInitialVersion()));
      }

      return (APIVersionRange.none());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doGet(Context context)
   {
      String version    = context.pathParam("version");
      String tableName  = context.pathParam("tableName");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         // todo - make sure version is known in this instance
         // todo - make sure table is supported in this version

         QTableMetaData table = qInstance.getTable(tableName);

         if(table == null)
         {
            throw (new QNotFoundException("Could not find any resources at path " + context.path()));
         }

         if(!getApiVersionRange(table).includes(new APIVersion(version)))
         {
            throw (new QNotFoundException("This version of this API does not contain the resource path " + context.path()));
         }

         GetInput getInput = new GetInput();

         setupSession(context, getInput);
         QJavalinAccessLogger.logStart("get", logPair("table", tableName), logPair("primaryKey", primaryKey));

         getInput.setTableName(tableName);
         // i think not for api... getInput.setShouldGenerateDisplayValues(true);
         getInput.setShouldTranslatePossibleValues(true);

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

         List<? extends QFieldMetaData>      tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();
         LinkedHashMap<String, Serializable> outputRecord   = toApiRecord(record, tableApiFields);

         QJavalinAccessLogger.logEndSuccess();
         context.result(JsonUtils.toJson(outputRecord));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         QJavalinImplementation.handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doQuery(Context context)
   {
      String       version   = context.pathParam("version");
      String       tableName = context.pathParam("tableName");
      QQueryFilter filter    = null;

      try
      {
         List<String> badRequestMessages = new ArrayList<>();

         // todo - make sure version is known in this instance
         // todo - make sure table is supported in this version

         QTableMetaData table = qInstance.getTable(tableName);

         if(table == null)
         {
            throw (new QNotFoundException("Could not find any resources at path " + context.path()));
         }

         if(!getApiVersionRange(table).includes(new APIVersion(version)))
         {
            throw (new QNotFoundException("This version of this API does not contain the resource path " + context.path()));
         }

         QueryInput queryInput = new QueryInput();
         setupSession(context, queryInput);
         QJavalinAccessLogger.logStart("apiQuery", logPair("table", tableName));

         queryInput.setTableName(tableName);
         //? queryInput.setShouldGenerateDisplayValues(true);
         //? queryInput.setShouldTranslatePossibleValues(true);

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

         Integer pageNo = Objects.requireNonNullElse(QJavalinUtils.integerQueryParam(context, "pageNo"), 1);
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
            filter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);
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
                     QCriteriaOperator  operator       = getCriteriaOperator(value);
                     List<Serializable> criteriaValues = getCriteriaValues(field, value);
                     filter.addCriteria(new QFilterCriteria(name, operator, criteriaValues));
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
               throw (new QBadRequestException("Requested failed with " + badRequestMessages.size() + " reasons: " + StringUtils.join(" \n", badRequestMessages)));
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
         List<? extends QFieldMetaData>       tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();
         ArrayList<Map<String, Serializable>> records        = new ArrayList<>();
         for(QRecord record : queryOutput.getRecords())
         {
            records.add(toApiRecord(record, tableApiFields));
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
   private static QCriteriaOperator getCriteriaOperator(String value)
   {
      // todo - all other operators
      return (QCriteriaOperator.EQUALS);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<Serializable> getCriteriaValues(QFieldMetaData field, String value)
   {
      // todo - parse the thing, do stuff
      return (List.of(value));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doInsert(Context context)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doUpdate(Context context)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void doDelete(Context context)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static LinkedHashMap<String, Serializable> toApiRecord(QRecord record, List<? extends QFieldMetaData> tableApiFields)
   {
      LinkedHashMap<String, Serializable> outputRecord = new LinkedHashMap<>();
      for(QFieldMetaData tableApiField : tableApiFields)
      {
         // todo - what about display values / possible values
         // todo - handle removed-from-this-version fields!!
         outputRecord.put(tableApiField.getName(), record.getValue(tableApiField.getName()));
      }
      return outputRecord;
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
