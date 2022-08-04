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

package com.kingsrook.qqq.backend.javalin;


import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobManager;
import com.kingsrook.qqq.backend.core.actions.metadata.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.metadata.ProcessMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.metadata.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.reporting.ReportAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
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
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.Auth0AuthenticationModule;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.patch;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;


/*******************************************************************************
 ** QQQ Javalin implementation.  Given a QInstance, defines all routes needed
 ** to respond to http requests and route down into the qqq backend.
 **
 *******************************************************************************/
public class QJavalinImplementation
{
   private static final Logger LOG = LogManager.getLogger(QJavalinImplementation.class);

   private static final int SESSION_COOKIE_AGE = 60 * 60 * 24;
   private static final String SESSION_ID_COOKIE_NAME = "sessionId";

   static QInstance qInstance;

   private static int DEFAULT_PORT = 8001;

   private static Javalin service;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws QInstanceValidationException
   {
      QInstance qInstance = new QInstance();
      // todo - parse args to look up metaData and prime instance
      // qInstance.addBackend(QMetaDataProvider.getQBackend());

      new QJavalinImplementation(qInstance).startJavalinServer(DEFAULT_PORT);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(QInstance qInstance) throws QInstanceValidationException
   {
      QJavalinImplementation.qInstance = qInstance;
      new QInstanceValidator().validate(qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(String qInstanceFilePath) throws IOException
   {
      LOG.info("Loading qInstance from file (assuming json): " + qInstanceFilePath);
      String qInstanceJson = FileUtils.readFileToString(new File(qInstanceFilePath));
      QJavalinImplementation.qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(qInstanceJson);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void startJavalinServer(int port)
   {
      // todo port from arg
      // todo base path from arg? - and then potentially multiple instances too (chosen based on the root path??)
      service = Javalin.create().start(port);
      service.routes(getRoutes());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   void stopJavalinServer()
   {
      service.stop();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setDefaultPort(int port)
   {
      QJavalinImplementation.DEFAULT_PORT = port;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public EndpointGroup getRoutes()
   {
      return (() ->
      {
         /////////////////////
         // metadata routes //
         /////////////////////
         path("/metaData", () ->
         {
            get("/", QJavalinImplementation::metaData);
            path("/table/{table}", () ->
            {
               get("", QJavalinImplementation::tableMetaData);
            });
            path("/process/{processName}", () ->
            {
               get("", QJavalinImplementation::processMetaData);
            });
         });

         /////////////////////////
         // table (data) routes //
         /////////////////////////
         path("/data/{table}", () ->
         {
            get("/", QJavalinImplementation::dataQuery);
            post("/", QJavalinImplementation::dataInsert); // todo - internal to that method, if input is a list, do a bulk - else, single.
            get("/count", QJavalinImplementation::dataCount);
            get("/export", QJavalinImplementation::dataExportWithoutFilename);
            get("/export/{filename}", QJavalinImplementation::dataExportWithFilename);

            // todo - add put and/or patch at this level (without a primaryKey) to do a bulk update based on primaryKeys in the records.
            path("/{primaryKey}", () ->
            {
               get("", QJavalinImplementation::dataGet);
               patch("", QJavalinImplementation::dataUpdate);
               put("", QJavalinImplementation::dataUpdate); // todo - want different semantics??
               delete("", QJavalinImplementation::dataDelete);
            });
         });

         ////////////////////
         // process routes //
         ////////////////////
         path("", QJavalinProcessHandler.getRoutes());
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void setupSession(Context context, AbstractActionInput input) throws QModuleDispatchException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface authenticationModule = qAuthenticationModuleDispatcher.getQModule(input.getAuthenticationMetaData());

      try
      {
         Map<String, String> authenticationContext = new HashMap<>();

         /////////////////////////////////////////////////////////////////////////////////
         // look for a token in either the sessionId cookie, or an Authorization header //
         /////////////////////////////////////////////////////////////////////////////////
         String sessionIdCookieValue = context.cookie(SESSION_ID_COOKIE_NAME);
         if(StringUtils.hasContent(sessionIdCookieValue))
         {
            authenticationContext.put(SESSION_ID_COOKIE_NAME, sessionIdCookieValue);
         }
         else
         {
            String authorizationHeaderValue = context.header("Authorization");
            if (authorizationHeaderValue != null)
            {
               String bearerPrefix = "Bearer ";
               if(authorizationHeaderValue.startsWith(bearerPrefix))
               {
                  authorizationHeaderValue = authorizationHeaderValue.replaceFirst(bearerPrefix, "");
               }
               authenticationContext.put(SESSION_ID_COOKIE_NAME, authorizationHeaderValue);
            }
         }

         QSession session = authenticationModule.createSession(qInstance, authenticationContext);
         input.setSession(session);

         /////////////////////////////////////////////////////////////////////////////////
         // if we got a session id cookie in, then send it back with updated cookie age //
         /////////////////////////////////////////////////////////////////////////////////
         if(StringUtils.hasContent(sessionIdCookieValue))
         {
            context.cookie(SESSION_ID_COOKIE_NAME, session.getIdReference(), SESSION_COOKIE_AGE);
         }
      }
      catch(QAuthenticationException qae)
      {
         ////////////////////////////////////////////////////////////////////////////////
         // if exception caught, clear out the cookie so the frontend will reauthorize //
         ////////////////////////////////////////////////////////////////////////////////
         if(authenticationModule instanceof Auth0AuthenticationModule)
         {
            context.removeCookie(SESSION_ID_COOKIE_NAME);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataDelete(Context context)
   {
      try
      {
         String table = context.pathParam("table");
         List<Serializable> primaryKeys = new ArrayList<>();
         primaryKeys.add(context.pathParam("primaryKey"));

         DeleteInput deleteInput = new DeleteInput(qInstance);
         setupSession(context, deleteInput);
         deleteInput.setTableName(table);
         deleteInput.setPrimaryKeys(primaryKeys);

         DeleteAction deleteAction = new DeleteAction();
         DeleteOutput deleteResult = deleteAction.execute(deleteInput);

         context.result(JsonUtils.toJson(deleteResult));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataUpdate(Context context)
   {
      try
      {
         String table = context.pathParam("table");
         List<QRecord> recordList = new ArrayList<>();
         QRecord record = new QRecord();
         record.setTableName(table);
         recordList.add(record);

         Map<?, ?> map = context.bodyAsClass(Map.class);
         for(Map.Entry<?, ?> entry : map.entrySet())
         {
            if(StringUtils.hasContent(String.valueOf(entry.getValue())))
            {
               record.setValue(String.valueOf(entry.getKey()), (Serializable) entry.getValue());
            }
         }

         QTableMetaData tableMetaData = qInstance.getTable(table);

         record.setValue(tableMetaData.getPrimaryKeyField(), context.pathParam("primaryKey"));

         UpdateInput updateInput = new UpdateInput(qInstance);
         setupSession(context, updateInput);
         updateInput.setTableName(table);
         updateInput.setRecords(recordList);

         UpdateAction updateAction = new UpdateAction();
         UpdateOutput updateResult = updateAction.execute(updateInput);

         context.result(JsonUtils.toJson(updateResult));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataInsert(Context context)
   {
      try
      {
         String table = context.pathParam("table");
         List<QRecord> recordList = new ArrayList<>();
         QRecord record = new QRecord();
         record.setTableName(table);
         recordList.add(record);

         Map<?, ?> map = context.bodyAsClass(Map.class);
         for(Map.Entry<?, ?> entry : map.entrySet())
         {
            if(StringUtils.hasContent(String.valueOf(entry.getValue())))
            {
               record.setValue(String.valueOf(entry.getKey()), (Serializable) entry.getValue());
            }
         }

         InsertInput insertInput = new InsertInput(qInstance);
         setupSession(context, insertInput);
         insertInput.setTableName(table);
         insertInput.setRecords(recordList);

         InsertAction insertAction = new InsertAction();
         InsertOutput insertOutput = insertAction.execute(insertInput);

         context.result(JsonUtils.toJson(insertOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    ********************************************************************************/
   private static void dataGet(Context context)
   {
      try
      {
         String         tableName  = context.pathParam("table");
         QTableMetaData table      = qInstance.getTable(tableName);
         String         primaryKey = context.pathParam("primaryKey");
         QueryInput     queryInput = new QueryInput(qInstance);

         setupSession(context, queryInput);
         queryInput.setTableName(tableName);

         // todo - validate that the primary key is of the proper type (e.g,. not a string for an id field)
         //  and throw a 400-series error (tell the user bad-request), rather than, we're doing a 500 (server error)

         ///////////////////////////////////////////////////////
         // setup a filter for the primaryKey = the path-pram //
         ///////////////////////////////////////////////////////
         queryInput.setFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria()
               .withFieldName(table.getPrimaryKeyField())
               .withOperator(QCriteriaOperator.EQUALS)
               .withValues(List.of(primaryKey))));

         QueryAction queryAction = new QueryAction();
         QueryOutput queryOutput = queryAction.execute(queryInput);

         ///////////////////////////////////////////////////////
         // throw a not found error if the record isn't found //
         ///////////////////////////////////////////////////////
         if(queryOutput.getRecords().isEmpty())
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         context.result(JsonUtils.toJson(queryOutput.getRecords().get(0)));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    *
    * Filter parameter is a serialized QQueryFilter object, that is to say:
    * <pre>
    *   filter=
    *    {"criteria":[
    *       {"fieldName":"id","operator":"EQUALS","values":[1]},
    *       {"fieldName":"name","operator":"IN","values":["Darin","James"]}
    *     ]
    *    }
    * </pre>
    *******************************************************************************/
   static void dataCount(Context context)
   {
      try
      {
         CountInput countInput = new CountInput(qInstance);
         setupSession(context, countInput);
         countInput.setTableName(context.pathParam("table"));

         String filter = stringQueryParam(context, "filter");
         if(filter != null)
         {
            countInput.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         CountAction countAction = new CountAction();
         CountOutput countOutput = countAction.execute(countInput);

         context.result(JsonUtils.toJson(countOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    *
    * Filter parameter is a serialized QQueryFilter object, that is to say:
    * <pre>
    *   filter=
    *    {"criteria":[
    *       {"fieldName":"id","operator":"EQUALS","values":[1]},
    *       {"fieldName":"name","operator":"IN","values":["Darin","James"]}
    *     ],
    *     "orderBys":[
    *       {"fieldName":"age","isAscending":true}
    *     ]}
    * </pre>
    *******************************************************************************/
   static void dataQuery(Context context)
   {
      try
      {
         QueryInput queryInput = new QueryInput(qInstance);
         setupSession(context, queryInput);
         queryInput.setTableName(context.pathParam("table"));
         queryInput.setSkip(integerQueryParam(context, "skip"));
         queryInput.setLimit(integerQueryParam(context, "limit"));

         String filter = stringQueryParam(context, "filter");
         if(filter != null)
         {
            queryInput.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         QueryAction queryAction = new QueryAction();
         QueryOutput queryOutput = queryAction.execute(queryInput);

         context.result(JsonUtils.toJson(queryOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void metaData(Context context)
   {
      try
      {
         MetaDataInput metaDataInput = new MetaDataInput(qInstance);
         setupSession(context, metaDataInput);
         MetaDataAction metaDataAction = new MetaDataAction();
         MetaDataOutput metaDataOutput = metaDataAction.execute(metaDataInput);

         context.result(JsonUtils.toJson(metaDataOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void tableMetaData(Context context)
   {
      try
      {
         TableMetaDataInput tableMetaDataInput = new TableMetaDataInput(qInstance);
         setupSession(context, tableMetaDataInput);
         tableMetaDataInput.setTableName(context.pathParam("table"));
         TableMetaDataAction tableMetaDataAction = new TableMetaDataAction();
         TableMetaDataOutput tableMetaDataOutput = tableMetaDataAction.execute(tableMetaDataInput);

         context.result(JsonUtils.toJson(tableMetaDataOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void processMetaData(Context context)
   {
      try
      {
         ProcessMetaDataInput processMetaDataInput = new ProcessMetaDataInput(qInstance);
         setupSession(context, processMetaDataInput);
         processMetaDataInput.setProcessName(context.pathParam("processName"));
         ProcessMetaDataAction processMetaDataAction = new ProcessMetaDataAction();
         ProcessMetaDataOutput processMetaDataOutput = processMetaDataAction.execute(processMetaDataInput);

         context.result(JsonUtils.toJson(processMetaDataOutput));
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataExportWithFilename(Context context)
   {
      String filename = context.pathParam("filename");
      dataExport(context, Optional.of(filename));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataExportWithoutFilename(Context context)
   {
      dataExport(context, Optional.empty());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataExport(Context context, Optional<String> optionalFilename)
   {
      try
      {
         //////////////////////////////////////////
         // read params from the request context //
         //////////////////////////////////////////
         String  tableName = context.pathParam("table");
         String  format    = context.queryParam("format");
         String  filter    = context.queryParam("filter");
         Integer limit     = integerQueryParam(context, "limit");

         /////////////////////////////////////////////////////////////////////////////////////////
         // if a format query param wasn't given, then try to get file extension from file name //
         /////////////////////////////////////////////////////////////////////////////////////////
         if(!StringUtils.hasContent(format) && optionalFilename.isPresent() && StringUtils.hasContent(optionalFilename.get()))
         {
            String filename = optionalFilename.get();
            if(filename.contains("."))
            {
               format = filename.substring(filename.lastIndexOf(".") + 1);
            }
         }

         ReportFormat reportFormat;
         try
         {
            reportFormat = ReportFormat.fromString(format);
         }
         catch(QUserFacingException e)
         {
            handleException(HttpStatus.Code.BAD_REQUEST, context, e);
            return;
         }

         String filename = optionalFilename.orElse(tableName + "." + reportFormat.toString().toLowerCase(Locale.ROOT));

         /////////////////////////////////////////////
         // set up the report action's input object //
         /////////////////////////////////////////////
         ReportInput reportInput = new ReportInput(qInstance);
         setupSession(context, reportInput);
         reportInput.setTableName(tableName);
         reportInput.setReportFormat(reportFormat);
         reportInput.setFilename(filename);
         reportInput.setLimit(limit);

         String fields = stringQueryParam(context, "fields");
         if(StringUtils.hasContent(fields))
         {
            reportInput.setFieldNames(List.of(fields.split(",")));
         }

         if(filter != null)
         {
            reportInput.setQueryFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         // set up the I/O pipe streams.                                                                      //
         // Critically, we must NOT open the outputStream in a try-with-resources.  The thread that writes to //
         // the stream must close it when it's done writing.                                                  //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         PipedOutputStream pipedOutputStream = new PipedOutputStream();
         PipedInputStream  pipedInputStream  = new PipedInputStream();
         pipedOutputStream.connect(pipedInputStream);
         reportInput.setReportOutputStream(pipedOutputStream);

         ReportAction reportAction = new ReportAction();
         reportAction.preExecute(reportInput);

         /////////////////////////////////////////////////////////////////////////////////////////////////////
         // start the async job.                                                                            //
         // Critically, this must happen before the pipedInputStream is passed to the javalin result method //
         /////////////////////////////////////////////////////////////////////////////////////////////////////
         new AsyncJobManager().startJob("Javalin>ReportAction", (o) ->
         {
            try
            {
               reportAction.execute(reportInput);
               return (true);
            }
            catch(Exception e)
            {
               pipedOutputStream.write(("Error generating report: " + e.getMessage()).getBytes());
               pipedOutputStream.close();
               return (false);
            }
         });

         ////////////////////////////////////////////
         // set the response content type & stream //
         ////////////////////////////////////////////
         context.contentType(reportFormat.getMimeType());
         context.header("Content-Disposition", "filename=" + filename);
         context.result(pipedInputStream);

         ////////////////////////////////////////////////////////////////////////////////////////////
         // we'd like to check to see if the job failed, and if so, to give the user an error...   //
         // but if we "block" here, then piped streams seem to never flush, so we deadlock things. //
         ////////////////////////////////////////////////////////////////////////////////////////////
         // AsyncJobStatus asyncJobStatus = asyncJobManager.waitForJob(jobUUID);
         // if(asyncJobStatus.getState().equals(AsyncJobState.ERROR))
         // {
         //    System.out.println("Well, here we are...");
         //    throw (new QUserFacingException("Error running report: " + asyncJobStatus.getCaughtException().getMessage()));
         // }
      }
      catch(Exception e)
      {
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
      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         if(userFacingException instanceof QNotFoundException)
         {
            int code = Objects.requireNonNullElse(statusCode, HttpStatus.Code.NOT_FOUND).getCode();
            context.status(code).result("{\"error\":\"" + userFacingException.getMessage() + "\"}");
         }
         else
         {
            LOG.info("User-facing exception", e);
            int code = Objects.requireNonNullElse(statusCode, HttpStatus.Code.INTERNAL_SERVER_ERROR).getCode();
            context.status(code).result("{\"error\":\"" + userFacingException.getMessage() + "\"}");
         }
      }
      else
      {
         if(e instanceof QAuthenticationException)
         {
            context.status(HttpStatus.UNAUTHORIZED_401).result("{\"error\":\"" + e.getMessage() + "\"}");
            return;
         }

         ////////////////////////////////
         // default exception handling //
         ////////////////////////////////
         LOG.warn("Exception in javalin request", e);
         int code = Objects.requireNonNullElse(statusCode, HttpStatus.Code.INTERNAL_SERVER_ERROR).getCode();
         context.status(code).result("{\"error\":\"" + e.getClass().getSimpleName() + " (" + e.getMessage() + ")\"}");
      }
   }



   /*******************************************************************************
    ** Returns Integer if context has a valid int query parameter by the given name,
    **  Returns null if no param (or empty value).
    **  Throws QValueException for malformed numbers.
    *******************************************************************************/
   public static Integer integerQueryParam(Context context, String name) throws QValueException
   {
      String value = context.queryParam(name);
      if(StringUtils.hasContent(value))
      {
         return (ValueUtils.getValueAsInteger(value));
      }

      return (null);
   }



   /*******************************************************************************
    ** Returns String if context has a valid query parameter by the given name,
    *  Returns null if no param (or empty value).
    *******************************************************************************/
   private static String stringQueryParam(Context context, String name)
   {
      String value = context.queryParam(name);
      if(StringUtils.hasContent(value))
      {
         return (value);
      }

      return (null);
   }

}
