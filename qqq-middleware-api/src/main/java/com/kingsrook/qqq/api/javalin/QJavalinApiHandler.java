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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
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
import com.kingsrook.qqq.backend.javalin.QJavalinAccessLogger;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import com.kingsrook.qqq.backend.javalin.QJavalinUtils;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.apibuilder.EndpointGroup;
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
   public static EndpointGroup getRoutes()
   {
      return (() ->
      {
         ApiBuilder.path("/api/{version}", () -> // todo - configurable, that /api/ bit?
         {
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
      String tableName  = context.pathParam("table");
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

         List<? extends QFieldMetaData> tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();
         Map<String, Serializable>      outputRecord   = new LinkedHashMap<>();
         for(QFieldMetaData tableApiField : tableApiFields)
         {
            // todo - what about display values / possible values
            // todo - handle removed-from-this-version fields!!
            outputRecord.put(tableApiField.getName(), record.getValue(tableApiField.getName()));
         }

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
      String table  = context.pathParam("table");
      String filter = null;

      try
      {
         QueryInput queryInput = new QueryInput();
         setupSession(context, queryInput);
         QJavalinAccessLogger.logStart("query", logPair("table", table));

         queryInput.setTableName(table);
         queryInput.setShouldGenerateDisplayValues(true);
         queryInput.setShouldTranslatePossibleValues(true);
         queryInput.setSkip(QJavalinUtils.integerQueryParam(context, "skip"));
         queryInput.setLimit(QJavalinUtils.integerQueryParam(context, "limit"));

         PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);

         filter = QJavalinUtils.stringQueryParam(context, "filter");
         if(!StringUtils.hasContent(filter))
         {
            filter = context.formParam("filter");
         }
         if(filter != null)
         {
            queryInput.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         // queryInput.setQueryJoins(processQueryJoinsParam(context));

         QueryAction queryAction = new QueryAction();
         QueryOutput queryOutput = queryAction.execute(queryInput);

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", queryOutput.getRecords().size()), QJavalinAccessLogger.logPairIfSlow("filter", filter, SLOW_LOG_THRESHOLD_MS));
         context.result(JsonUtils.toJson(queryOutput));
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
   public static void handleException(Context context, Exception e)
   {
      handleException(null, context, e);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void handleException(HttpStatus.Code statusCode, Context context, Exception e)
   {
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
