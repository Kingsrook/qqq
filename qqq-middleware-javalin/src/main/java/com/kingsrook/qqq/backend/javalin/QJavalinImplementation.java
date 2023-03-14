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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobManager;
import com.kingsrook.qqq.backend.core.actions.dashboard.RenderWidgetAction;
import com.kingsrook.qqq.backend.core.actions.metadata.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.metadata.ProcessMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.metadata.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionCheckResult;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.actions.reporting.ExportAction;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QAuthenticationException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QPermissionDeniedException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.ProcessMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataInput;
import com.kingsrook.qqq.backend.core.model.actions.metadata.TableMetaDataOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportInput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ExportOutput;
import com.kingsrook.qqq.backend.core.model.actions.reporting.ReportFormat;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ExceptionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeConsumer;
import com.kingsrook.qqq.backend.core.utils.lambdas.UnsafeFunction;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;
import static com.kingsrook.qqq.backend.javalin.QJavalinAccessLogger.logPairIfSlow;
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
   private static final QLogger LOG = QLogger.getLogger(QJavalinImplementation.class);

   private static final int    SESSION_COOKIE_AGE     = 60 * 60 * 24;
   private static final String SESSION_ID_COOKIE_NAME = "sessionId";
   private static final String BASIC_AUTH_NAME        = "basicAuthString";

   static QInstance        qInstance;
   static QJavalinMetaData javalinMetaData;

   private static Supplier<QInstance> qInstanceHotSwapSupplier;
   private static long                lastQInstanceHotSwapMillis;

   private static final long MILLIS_BETWEEN_HOT_SWAPS = 2500;
   private static final long SLOW_LOG_THRESHOLD_MS    = 1000;

   private static int DEFAULT_PORT = 8001;

   private static Javalin service;

   private static long startTime = 0;



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
      this(qInstance, new QJavalinMetaData());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(QInstance qInstance, QJavalinMetaData javalinMetaData) throws QInstanceValidationException
   {
      QJavalinImplementation.qInstance = qInstance;
      QJavalinImplementation.javalinMetaData = javalinMetaData;
      new QInstanceValidator().validate(qInstance);
      this.startTime = System.currentTimeMillis();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(String qInstanceFilePath) throws IOException
   {
      LOG.info("Loading qInstance from file (assuming json): " + qInstanceFilePath);
      String qInstanceJson = FileUtils.readFileToString(new File(qInstanceFilePath));
      QJavalinImplementation.qInstance = new QInstanceAdapter().jsonToQInstanceIncludingBackends(qInstanceJson);
      QJavalinImplementation.javalinMetaData = new QJavalinMetaData();
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
      service.before(QJavalinImplementation::hotSwapQInstance);
      service.before((Context context) -> context.header("Content-Type", "application/json"));
      service.after(QJavalinImplementation::clearQContext);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void clearQContext(Context context)
   {
      QContext.clear();
   }



   /*******************************************************************************
    ** If there's a qInstanceHotSwapSupplier, and its been a little while, replace
    ** the qInstance with a new one from the supplier.  Meant to be used while doing
    ** development.
    *******************************************************************************/
   public static void hotSwapQInstance(Context context)
   {
      if(qInstanceHotSwapSupplier != null)
      {
         long now = System.currentTimeMillis();
         if(now - lastQInstanceHotSwapMillis < MILLIS_BETWEEN_HOT_SWAPS)
         {
            return;
         }

         lastQInstanceHotSwapMillis = now;

         try
         {
            QInstance newQInstance = qInstanceHotSwapSupplier.get();
            if(newQInstance == null)
            {
               LOG.warn("Got a null qInstance from hotSwapSupplier.  Not hot-swapping.");
               return;
            }

            new QInstanceValidator().validate(newQInstance);
            QJavalinImplementation.qInstance = newQInstance;
            LOG.info("Swapped qInstance");
         }
         catch(QInstanceValidationException e)
         {
            LOG.warn(e.getMessage());
         }
         catch(Exception e)
         {
            LOG.error("Error swapping QInstance", e);
         }
      }
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
            get("/authentication", QJavalinImplementation::authenticationMetaData);
         });

         /////////////////////////
         // table (data) routes //
         /////////////////////////
         path("/data/{table}", () ->
         {
            get("/", QJavalinImplementation::dataQuery);
            post("/query", QJavalinImplementation::dataQuery);
            post("/", QJavalinImplementation::dataInsert);
            get("/count", QJavalinImplementation::dataCount);
            post("/count", QJavalinImplementation::dataCount);
            get("/export", QJavalinImplementation::dataExportWithoutFilename);
            post("/export", QJavalinImplementation::dataExportWithoutFilename);
            get("/export/{filename}", QJavalinImplementation::dataExportWithFilename);
            post("/export/{filename}", QJavalinImplementation::dataExportWithFilename);
            get("/possibleValues/{fieldName}", QJavalinImplementation::possibleValues);
            post("/possibleValues/{fieldName}", QJavalinImplementation::possibleValues);

            // todo - add put and/or patch at this level (without a primaryKey) to do a bulk update based on primaryKeys in the records.
            path("/{primaryKey}", () ->
            {
               get("", QJavalinImplementation::dataGet);
               patch("", QJavalinImplementation::dataUpdate);
               put("", QJavalinImplementation::dataUpdate); // todo - want different semantics??
               delete("", QJavalinImplementation::dataDelete);

               QJavalinScriptsHandler.defineRecordRoutes();
            });
         });

         get("/widget/{name}", QJavalinImplementation::widget); // todo - can we just do a slow log here?

         get("/serverInfo", QJavalinImplementation::serverInfo);

         ////////////////////
         // process routes //
         ////////////////////
         path("", QJavalinProcessHandler.getRoutes());
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void serverInfo(Context context)
   {
      JSONObject serverInfo = new JSONObject();
      serverInfo.put("startTimeMillis", startTime);
      serverInfo.put("startTimeHuman", Instant.ofEpochMilli(startTime));

      long uptime = System.currentTimeMillis() - startTime;
      serverInfo.put("uptimeMillis", uptime);
      serverInfo.put("uptimeHuman", Duration.ofMillis(uptime));

      serverInfo.put("buildId", System.getProperty("buildId", "Unspecified"));

      context.result(serverInfo.toString());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void authenticationMetaData(Context context)
   {
      context.result(JsonUtils.toJson(qInstance.getAuthentication()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void setupSession(Context context, AbstractActionInput input) throws QModuleDispatchException, QAuthenticationException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());

      try
      {
         Map<String, String> authenticationContext = new HashMap<>();

         String sessionIdCookieValue     = context.cookie(SESSION_ID_COOKIE_NAME);
         String authorizationHeaderValue = context.header("Authorization");

         if(StringUtils.hasContent(sessionIdCookieValue))
         {
            ////////////////////////////////////////
            // first, look for a sessionId cookie //
            ////////////////////////////////////////
            authenticationContext.put(SESSION_ID_COOKIE_NAME, sessionIdCookieValue);
         }
         else if(authorizationHeaderValue != null)
         {
            /////////////////////////////////////////////////////////////////////////////////////////////////
            // second, look for the authorization header:                                                  //
            // either with a "Basic " prefix (for a username:password pair)                                //
            // or with a "Bearer " prefix (for a token that can be handled the same as a sessionId cookie) //
            /////////////////////////////////////////////////////////////////////////////////////////////////
            processAuthorizationValue(authenticationContext, authorizationHeaderValue);
         }
         else
         {
            String authorizationFormValue = context.formParam("Authorization");
            if(StringUtils.hasContent(authorizationFormValue))
            {
               processAuthorizationValue(authenticationContext, authorizationFormValue);
            }
            else
            {
               LOG.debug("Neither [" + SESSION_ID_COOKIE_NAME + "] cookie nor [Authorization] header was present in request.");
            }
         }

         QContext.init(qInstance, null); // hmm...
         QSession session = authenticationModule.createSession(qInstance, authenticationContext);
         QContext.init(qInstance, session, null, input);

         /////////////////////////////////////////////////////////////////////////////////
         // if we got a session id cookie in, then send it back with updated cookie age //
         /////////////////////////////////////////////////////////////////////////////////
         if(authenticationModule.usesSessionIdCookie())
         {
            context.cookie(SESSION_ID_COOKIE_NAME, session.getIdReference(), SESSION_COOKIE_AGE);
         }

         setUserTimezoneOffsetMinutesInSession(context, session);
         setUserTimezoneInSession(context, session);
      }
      catch(QAuthenticationException qae)
      {
         ////////////////////////////////////////////////////////////////////////////////
         // if exception caught, clear out the cookie so the frontend will reauthorize //
         ////////////////////////////////////////////////////////////////////////////////
         if(authenticationModule.usesSessionIdCookie())
         {
            context.removeCookie(SESSION_ID_COOKIE_NAME);
         }

         throw (qae);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void processAuthorizationValue(Map<String, String> authenticationContext, String authorizationHeaderValue)
   {
      String basicPrefix  = "Basic ";
      String bearerPrefix = "Bearer ";
      if(authorizationHeaderValue.startsWith(basicPrefix))
      {
         authorizationHeaderValue = authorizationHeaderValue.replaceFirst(basicPrefix, "");
         authenticationContext.put(BASIC_AUTH_NAME, authorizationHeaderValue);
      }
      else if(authorizationHeaderValue.startsWith(bearerPrefix))
      {
         authorizationHeaderValue = authorizationHeaderValue.replaceFirst(bearerPrefix, "");
         authenticationContext.put(SESSION_ID_COOKIE_NAME, authorizationHeaderValue);
      }
      else
      {
         LOG.debug("Authorization value did not have Basic or Bearer prefix. [" + authorizationHeaderValue + "]");
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setUserTimezoneOffsetMinutesInSession(Context context, QSession session)
   {
      String userTimezoneOffsetMinutes = context.header("X-QQQ-UserTimezoneOffsetMinutes");
      if(StringUtils.hasContent(userTimezoneOffsetMinutes))
      {
         try
         {
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // even though we're putting it in the session as a string, go through parse int, to make sure it's a valid int. //
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            session.setValue(QSession.VALUE_KEY_USER_TIMEZONE_OFFSET_MINUTES, String.valueOf(Integer.parseInt(userTimezoneOffsetMinutes)));
         }
         catch(Exception e)
         {
            LOG.debug("Received non-integer value for X-QQQ-UserTimezoneOffsetMinutes header: " + userTimezoneOffsetMinutes);
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void setUserTimezoneInSession(Context context, QSession session)
   {
      String userTimezone = context.header("X-QQQ-UserTimezone");
      if(StringUtils.hasContent(userTimezone))
      {
         session.setValue(QSession.VALUE_KEY_USER_TIMEZONE, userTimezone);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataDelete(Context context)
   {
      String table      = context.pathParam("table");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         List<Serializable> primaryKeys = new ArrayList<>();
         primaryKeys.add(primaryKey);

         DeleteInput deleteInput = new DeleteInput();
         setupSession(context, deleteInput);

         QJavalinAccessLogger.logStart("delete", logPair("table", table), logPair("primaryKey", primaryKey));

         deleteInput.setTableName(table);
         deleteInput.setPrimaryKeys(primaryKeys);

         PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);

         DeleteAction deleteAction = new DeleteAction();
         DeleteOutput deleteResult = deleteAction.execute(deleteInput);

         QJavalinAccessLogger.logEndSuccess();
         context.result(JsonUtils.toJson(deleteResult));
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
   private static void dataUpdate(Context context)
   {
      String table      = context.pathParam("table");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         UpdateInput updateInput = new UpdateInput();
         setupSession(context, updateInput);
         updateInput.setTableName(table);

         PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

         List<QRecord> recordList = new ArrayList<>();
         QRecord       record     = new QRecord();
         record.setTableName(table);
         recordList.add(record);

         Map<?, ?> map = context.bodyAsClass(Map.class);
         for(Map.Entry<?, ?> entry : map.entrySet())
         {
            String fieldName = ValueUtils.getValueAsString(entry.getKey());
            Object value     = entry.getValue();

            if(StringUtils.hasContent(String.valueOf(value)))
            {
               record.setValue(fieldName, (Serializable) value);
            }
            else if("".equals(value))
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               // if frontend sent us an empty string - put a null in the record's value map.                   //
               // this could potentially be changed to be type-specific (e.g., store an empty-string for STRING //
               // fields, but null for INTEGER, etc) - but, who really wants empty-string in database anyway?   //
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               record.setValue(fieldName, null);
            }
         }

         QTableMetaData tableMetaData = qInstance.getTable(table);
         record.setValue(tableMetaData.getPrimaryKeyField(), primaryKey);

         QJavalinAccessLogger.logStart("update", logPair("table", table), logPair("primaryKey", primaryKey));

         updateInput.setRecords(recordList);

         UpdateAction updateAction = new UpdateAction();
         UpdateOutput updateResult = updateAction.execute(updateInput);

         QJavalinAccessLogger.logEndSuccess();
         context.result(JsonUtils.toJson(updateResult));
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
   private static void dataInsert(Context context)
   {
      String tableName = context.pathParam("table");
      try
      {
         InsertInput insertInput = new InsertInput();
         setupSession(context, insertInput);
         insertInput.setTableName(tableName);
         QJavalinAccessLogger.logStart("insert", logPair("table", tableName));

         PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

         List<QRecord> recordList = new ArrayList<>();
         QRecord       record     = new QRecord();
         record.setTableName(tableName);
         recordList.add(record);

         Map<?, ?> map = context.bodyAsClass(Map.class);
         for(Map.Entry<?, ?> entry : map.entrySet())
         {
            if(StringUtils.hasContent(String.valueOf(entry.getValue())))
            {
               record.setValue(String.valueOf(entry.getKey()), (Serializable) entry.getValue());
            }
         }
         insertInput.setRecords(recordList);

         InsertAction insertAction = new InsertAction();
         InsertOutput insertOutput = insertAction.execute(insertInput);

         if(CollectionUtils.nullSafeHasContents(insertOutput.getRecords().get(0).getErrors()))
         {
            throw (new QUserFacingException("Error inserting " + qInstance.getTable(tableName).getLabel() + ": " + insertOutput.getRecords().get(0).getErrors().get(0)));
         }

         QJavalinAccessLogger.logEndSuccess(logPair("primaryKey", () -> (insertOutput.getRecords().get(0).getValue(qInstance.getTable(tableName).getPrimaryKeyField()))));
         context.result(JsonUtils.toJson(insertOutput));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    ********************************************************************************/
   private static void dataGet(Context context)
   {
      String tableName  = context.pathParam("table");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table    = qInstance.getTable(tableName);
         GetInput       getInput = new GetInput();

         setupSession(context, getInput);
         QJavalinAccessLogger.logStart("get", logPair("table", tableName), logPair("primaryKey", primaryKey));

         getInput.setTableName(tableName);
         getInput.setShouldGenerateDisplayValues(true);
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
         if(getOutput.getRecord() == null)
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         QJavalinAccessLogger.logEndSuccess();
         context.result(JsonUtils.toJson(getOutput.getRecord()));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e);
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
      String table  = context.pathParam("table");
      String filter = null;

      try
      {
         CountInput countInput = new CountInput();
         setupSession(context, countInput);
         countInput.setTableName(table);
         QJavalinAccessLogger.logStartSilent("count");

         PermissionsHelper.checkTablePermissionThrowing(countInput, TablePermissionSubType.READ);

         filter = stringQueryParam(context, "filter");
         if(!StringUtils.hasContent(filter))
         {
            filter = context.formParam("filter");
         }
         if(filter != null)
         {
            countInput.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         countInput.setQueryJoins(processQueryJoinsParam(context));

         CountAction countAction = new CountAction();
         CountOutput countOutput = countAction.execute(countInput);

         QJavalinAccessLogger.logEndSuccessIfSlow(SLOW_LOG_THRESHOLD_MS, logPair("table", table), logPair("count", countOutput.getCount()), logPair("filter", filter));
         context.result(JsonUtils.toJson(countOutput));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e, logPair("table", table), logPair("filter", filter));
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
    *
    * queryJoins parameter is a JSONArray of objects which represent a QueryJoin object.  e.g.,
    * <pre>
    *   queryJoins=
    *    [
    *       {"joinTable":"orderLine","select":true,"type":"INNER"},
    *       {"joinTable":"customer","select":true,"type":"LEFT"}
    *    }
    * </pre>
    * Additional field names in the JSONObjects there are: baseTableOrAlias, alias, joinName.
    *******************************************************************************/
   static void dataQuery(Context context)
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
         queryInput.setSkip(integerQueryParam(context, "skip"));
         queryInput.setLimit(integerQueryParam(context, "limit"));

         PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);

         filter = stringQueryParam(context, "filter");
         if(!StringUtils.hasContent(filter))
         {
            filter = context.formParam("filter");
         }
         if(filter != null)
         {
            queryInput.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         queryInput.setQueryJoins(processQueryJoinsParam(context));

         QueryAction queryAction = new QueryAction();
         QueryOutput queryOutput = queryAction.execute(queryInput);

         QJavalinAccessLogger.logEndSuccess(logPair("recordCount", queryOutput.getRecords().size()), logPairIfSlow("filter", filter, SLOW_LOG_THRESHOLD_MS));
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
   private static List<QueryJoin> processQueryJoinsParam(Context context)
   {
      List<QueryJoin> queryJoins = null;

      String queryJoinsParam = stringQueryParam(context, "queryJoins");
      if(StringUtils.hasContent(queryJoinsParam))
      {
         queryJoins = new ArrayList<>();

         JSONArray queryJoinsJSON = new JSONArray(queryJoinsParam);
         for(int i = 0; i < queryJoinsJSON.length(); i++)
         {
            QueryJoin queryJoin = new QueryJoin();
            queryJoins.add(queryJoin);

            JSONObject jsonObject = queryJoinsJSON.getJSONObject(i);
            queryJoin.setJoinTable(jsonObject.optString("joinTable"));

            if(jsonObject.has("baseTableOrAlias") && !jsonObject.isNull("baseTableOrAlias"))
            {
               queryJoin.setBaseTableOrAlias(jsonObject.optString("baseTableOrAlias"));
            }

            if(jsonObject.has("alias") && !jsonObject.isNull("alias"))
            {
               queryJoin.setAlias(jsonObject.optString("alias"));
            }

            queryJoin.setSelect(jsonObject.optBoolean("select"));

            if(jsonObject.has("type") && !jsonObject.isNull("type"))
            {
               queryJoin.setType(QueryJoin.Type.valueOf(jsonObject.getString("type")));
            }

            if(jsonObject.has("joinName") && !jsonObject.isNull("joinName"))
            {
               queryJoin.setJoinMetaData(QContext.getQInstance().getJoin(jsonObject.getString("joinName")));
            }
         }
      }

      return (queryJoins);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void metaData(Context context)
   {
      try
      {
         MetaDataInput metaDataInput = new MetaDataInput();
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
         TableMetaDataInput tableMetaDataInput = new TableMetaDataInput();
         setupSession(context, tableMetaDataInput);

         String         tableName = context.pathParam("table");
         QTableMetaData table     = qInstance.getTable(tableName);
         if(table == null)
         {
            throw (new QNotFoundException("Table [" + tableName + "] was not found."));
         }

         PermissionCheckResult permissionCheckResult = PermissionsHelper.getPermissionCheckResult(tableMetaDataInput, table);
         if(permissionCheckResult.equals(PermissionCheckResult.DENY_HIDE))
         {
            // not found?  or permission denied... hmm
            throw (new QNotFoundException("Table [" + tableName + "] was not found."));
         }

         tableMetaDataInput.setTableName(tableName);
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
         ProcessMetaDataInput processMetaDataInput = new ProcessMetaDataInput();
         setupSession(context, processMetaDataInput);

         String           processName = context.pathParam("processName");
         QProcessMetaData process     = qInstance.getProcess(processName);
         if(process == null)
         {
            throw (new QNotFoundException("Process [" + processName + "] was not found."));
         }
         PermissionsHelper.checkProcessPermissionThrowing(processMetaDataInput, processName);

         processMetaDataInput.setProcessName(processName);
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
    ** Load the data for a widget of a given name.
    *******************************************************************************/
   private static void widget(Context context)
   {
      String widgetName = context.pathParam("name");

      RenderWidgetInput input = new RenderWidgetInput()
         .withWidgetMetaData(qInstance.getWidget(widgetName));

      try
      {
         setupSession(context, input);
         QJavalinAccessLogger.logStartSilent("widget");

         // todo permission?

         //////////////////////////
         // process query string //
         //////////////////////////
         for(Map.Entry<String, List<String>> queryParam : context.queryParamMap().entrySet())
         {
            String       fieldName = queryParam.getKey();
            List<String> values    = queryParam.getValue();
            if(CollectionUtils.nullSafeHasContents(values))
            {
               input.addQueryParam(fieldName, values.get(0));
            }
         }

         RenderWidgetOutput output = new RenderWidgetAction().execute(input);
         QJavalinAccessLogger.logEndSuccessIfSlow(SLOW_LOG_THRESHOLD_MS, logPair("widgetName", widgetName), logPair("inputParams", input.getQueryParams()));
         context.result(JsonUtils.toJson(output.getWidgetData()));
      }
      catch(Exception e)
      {
         QJavalinAccessLogger.logEndFail(e, logPair("widgetName", widgetName), logPair("inputParams", input.getQueryParams()));
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
      String tableName = context.pathParam("table");

      try
      {
         //////////////////////////////////////////
         // read params from the request context //
         //////////////////////////////////////////
         String  format = context.queryParam("format");
         String  filter = context.queryParam("filter");
         Integer limit  = integerQueryParam(context, "limit");

         ReportFormat reportFormat = getReportFormat(context, optionalFilename, format);
         if(reportFormat == null)
         {
            return;
         }

         String filename = optionalFilename.orElse(tableName + "." + reportFormat.toString().toLowerCase(Locale.ROOT));

         /////////////////////////////////////////////
         // set up the report action's input object //
         /////////////////////////////////////////////
         ExportInput exportInput = new ExportInput();
         setupSession(context, exportInput);

         exportInput.setTableName(tableName);
         exportInput.setReportFormat(reportFormat);
         exportInput.setFilename(filename);
         exportInput.setLimit(limit);

         PermissionsHelper.checkTablePermissionThrowing(exportInput, TablePermissionSubType.READ);

         String fields = stringQueryParam(context, "fields");
         if(StringUtils.hasContent(fields))
         {
            exportInput.setFieldNames(List.of(fields.split(",")));
         }

         if(filter != null)
         {
            exportInput.setQueryFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         UnsafeFunction<PipedOutputStream, ExportAction, Exception> preAction = (PipedOutputStream pos) ->
         {
            exportInput.setReportOutputStream(pos);

            ExportAction exportAction = new ExportAction();
            exportAction.preExecute(exportInput);
            return (exportAction);
         };

         UnsafeConsumer<ExportAction, Exception> execute = (ExportAction exportAction) ->
         {
            QJavalinAccessLogger.logStart("export", logPair("table", tableName));
            try
            {
               ExportOutput exportOutput = exportAction.execute(exportInput);
               QJavalinAccessLogger.logEndSuccess(logPair("recordCount", exportOutput.getRecordCount()), logPairIfSlow("filter", filter, SLOW_LOG_THRESHOLD_MS));
            }
            catch(Exception e)
            {
               QJavalinAccessLogger.logEndFail(e, logPair("filter", filter));
               throw (e);
            }
         };

         runStreamedExportOrReport(context, reportFormat, filename, preAction, execute);
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static <T> void runStreamedExportOrReport(Context context, ReportFormat reportFormat, String filename, UnsafeFunction<PipedOutputStream, T, Exception> preAction, UnsafeConsumer<T, Exception> executor) throws Exception
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up the I/O pipe streams.                                                                      //
      // Critically, we must NOT open the outputStream in a try-with-resources.  The thread that writes to //
      // the stream must close it when it's done writing.                                                  //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////
      PipedOutputStream pipedOutputStream = new PipedOutputStream();
      PipedInputStream  pipedInputStream  = new PipedInputStream();
      pipedOutputStream.connect(pipedInputStream);

      T t = preAction.apply(pipedOutputStream);

      /////////////////////////////////////////////////////////////////////////////////////////////////////
      // start the async job.                                                                            //
      // Critically, this must happen before the pipedInputStream is passed to the javalin result method //
      /////////////////////////////////////////////////////////////////////////////////////////////////////
      new AsyncJobManager().startJob("Javalin>ExportAction", (o) ->
      {
         try
         {
            executor.run(t);
            return (true);
         }
         catch(Exception e)
         {
            handleExportOrReportException(context, pipedOutputStream, e);
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



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void handleExportOrReportException(Context context, PipedOutputStream pipedOutputStream, Exception e) throws IOException
   {
      HttpStatus.Code statusCode = HttpStatus.Code.INTERNAL_SERVER_ERROR; // 500
      String          message    = e.getMessage();

      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         LOG.info("User-facing exception", e);
         statusCode = HttpStatus.Code.BAD_REQUEST; // 400
         message = userFacingException.getMessage();
      }
      else
      {
         QAuthenticationException authenticationException = ExceptionUtils.findClassInRootChain(e, QAuthenticationException.class);
         if(authenticationException != null)
         {
            statusCode = HttpStatus.Code.UNAUTHORIZED; // 401
         }
         else
         {
            LOG.warn("Unexpected exception in javalin report or export request", e);
         }
      }

      context.status(statusCode.getCode());
      pipedOutputStream.write(("Error generating report: " + message).getBytes());
      pipedOutputStream.close();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ReportFormat getReportFormat(Context context, Optional<String> optionalFilename, String format)
   {
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
         return null;
      }
      return reportFormat;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void possibleValues(Context context)
   {
      try
      {
         String tableName = context.pathParam("table");
         String fieldName = context.pathParam("fieldName");

         QTableMetaData table = qInstance.getTable(tableName);
         if(table == null)
         {
            throw (new QNotFoundException("Could not find table named " + tableName + " in this instance."));
         }

         QFieldMetaData field;
         try
         {
            field = table.getField(fieldName);
         }
         catch(Exception e)
         {
            throw (new QNotFoundException("Could not find field named " + fieldName + " in table " + tableName + "."));
         }

         if(!StringUtils.hasContent(field.getPossibleValueSourceName()))
         {
            throw (new QNotFoundException("Field " + fieldName + " in table " + tableName + " is not associated with a possible value source."));
         }

         finishPossibleValuesRequest(context, field);
      }
      catch(Exception e)
      {
         handleException(context, e);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void finishPossibleValuesRequest(Context context, QFieldMetaData field) throws IOException, QException
   {
      String searchTerm = context.queryParam("searchTerm");
      String ids        = context.queryParam("ids");

      Map<String, Serializable> values = new HashMap<>();
      if(context.formParamMap().containsKey("values"))
      {
         List<String> valuesParamList = context.formParamMap().get("values");
         if(CollectionUtils.nullSafeHasContents(valuesParamList))
         {
            String valuesParam = valuesParamList.get(0);
            values = JsonUtils.toObject(valuesParam, Map.class);
         }
      }

      SearchPossibleValueSourceInput input = new SearchPossibleValueSourceInput();
      setupSession(context, input);
      input.setPossibleValueSourceName(field.getPossibleValueSourceName());
      input.setSearchTerm(searchTerm);

      if(field.getPossibleValueSourceFilter() != null)
      {
         field.getPossibleValueSourceFilter().interpretValues(values);
         input.setDefaultQueryFilter(field.getPossibleValueSourceFilter());
      }

      if(StringUtils.hasContent(ids))
      {
         List<Serializable> idList = new ArrayList<>(Arrays.asList(ids.split(",")));
         input.setIdList(idList);
      }

      SearchPossibleValueSourceOutput output = new SearchPossibleValueSourceAction().execute(input);

      Map<String, Object> result = new HashMap<>();
      result.put("options", output.getResults());
      context.result(JsonUtils.toJson(result));
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
    ** Returns Integer if context has a valid int form parameter by the given name,
    **  Returns null if no param (or empty value).
    **  Throws QValueException for malformed numbers.
    *******************************************************************************/
   public static Integer integerFormParam(Context context, String name) throws QValueException
   {
      String value = context.formParam(name);
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



   /*******************************************************************************
    ** Setter for qInstanceHotSwapSupplier
    *******************************************************************************/
   public static void setQInstanceHotSwapSupplier(Supplier<QInstance> qInstanceHotSwapSupplier)
   {
      QJavalinImplementation.qInstanceHotSwapSupplier = qInstanceHotSwapSupplier;
   }



   /*******************************************************************************
    ** Getter for javalinMetaData
    **
    *******************************************************************************/
   public QJavalinMetaData getJavalinMetaData()
   {
      return javalinMetaData;
   }



   /*******************************************************************************
    ** Setter for javalinMetaData
    **
    *******************************************************************************/
   public void setJavalinMetaData(QJavalinMetaData javalinMetaData)
   {
      QJavalinImplementation.javalinMetaData = javalinMetaData;
   }
}
