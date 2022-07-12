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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.CountAction;
import com.kingsrook.qqq.backend.core.actions.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.InsertAction;
import com.kingsrook.qqq.backend.core.actions.MetaDataAction;
import com.kingsrook.qqq.backend.core.actions.ProcessMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.QueryAction;
import com.kingsrook.qqq.backend.core.actions.TableMetaDataAction;
import com.kingsrook.qqq.backend.core.actions.UpdateAction;
import com.kingsrook.qqq.backend.core.adapters.QInstanceAdapter;
import com.kingsrook.qqq.backend.core.exceptions.QModuleDispatchException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.exceptions.QValueException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQRequest;
import com.kingsrook.qqq.backend.core.model.actions.count.CountRequest;
import com.kingsrook.qqq.backend.core.model.actions.count.CountResult;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.MetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.metadata.process.ProcessMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.process.ProcessMetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.model.actions.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QAuthenticationModuleInterface;
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

   static QInstance qInstance;

   private static int DEFAULT_PORT = 8001;

   private static Javalin service;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args)
   {
      QInstance qInstance = new QInstance();
      // todo - parse args to look up metaData and prime instance
      // qInstance.addBackend(QMetaDataProvider.getQBackend());

      new QJavalinImplementation(qInstance).startJavalinServer(DEFAULT_PORT);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QJavalinImplementation(QInstance qInstance)
   {
      QJavalinImplementation.qInstance = qInstance;
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
         path("/metaData", () ->
         {
            get("/", QJavalinImplementation::metaData);
            path("/table/:table", () ->
            {
               get("", QJavalinImplementation::tableMetaData);
            });
            path("/process/:processName", () ->
            {
               get("", QJavalinImplementation::processMetaData);
            });
         });
         path("/data", () ->
         {
            path("/:table", () ->
            {
               get("/", QJavalinImplementation::dataQuery);
               post("/", QJavalinImplementation::dataInsert); // todo - internal to that method, if input is a list, do a bulk - else, single.
               get("/count", QJavalinImplementation::dataCount);

               // todo - add put and/or patch at this level (without a primaryKey) to do a bulk update based on primaryKeys in the records.
               path("/:primaryKey", () ->
               {
                  get("", QJavalinImplementation::dataGet);
                  patch("", QJavalinImplementation::dataUpdate);
                  put("", QJavalinImplementation::dataUpdate); // todo - want different semantics??
                  delete("", QJavalinImplementation::dataDelete);
               });
            });
         });
         path("", QJavalinProcessHandler.getRoutes());
      });
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   static void setupSession(Context context, AbstractQRequest request) throws QModuleDispatchException
   {
      QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
      QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(request.getAuthenticationMetaData());

      // todo - does this need some per-provider logic actually?  mmm...
      Map<String, String> authenticationContext = new HashMap<>();
      authenticationContext.put("sessionId", context.cookie("sessionId"));
      QSession session = authenticationModule.createSession(authenticationContext);
      request.setSession(session);

      context.cookie("sessionId", session.getIdReference(), SESSION_COOKIE_AGE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void dataDelete(Context context)
   {
      try
      {
         String             table       = context.pathParam("table");
         List<Serializable> primaryKeys = new ArrayList<>();
         primaryKeys.add(context.pathParam("primaryKey"));

         DeleteRequest deleteRequest = new DeleteRequest(qInstance);
         setupSession(context, deleteRequest);
         deleteRequest.setTableName(table);
         deleteRequest.setPrimaryKeys(primaryKeys);

         DeleteAction deleteAction = new DeleteAction();
         DeleteResult deleteResult = deleteAction.execute(deleteRequest);

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
         String        table      = context.pathParam("table");
         List<QRecord> recordList = new ArrayList<>();
         QRecord       record     = new QRecord();
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

         UpdateRequest updateRequest = new UpdateRequest(qInstance);
         setupSession(context, updateRequest);
         updateRequest.setTableName(table);
         updateRequest.setRecords(recordList);

         UpdateAction updateAction = new UpdateAction();
         UpdateResult updateResult = updateAction.execute(updateRequest);

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
         String        table      = context.pathParam("table");
         List<QRecord> recordList = new ArrayList<>();
         QRecord       record     = new QRecord();
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

         InsertRequest insertRequest = new InsertRequest(qInstance);
         setupSession(context, insertRequest);
         insertRequest.setTableName(table);
         insertRequest.setRecords(recordList);

         InsertAction insertAction = new InsertAction();
         InsertResult insertResult = insertAction.execute(insertRequest);

         context.result(JsonUtils.toJson(insertResult));
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
         String         tableName    = context.pathParam("table");
         QTableMetaData table        = qInstance.getTable(tableName);
         String         primaryKey   = context.pathParam("primaryKey");
         QueryRequest   queryRequest = new QueryRequest(qInstance);

         setupSession(context, queryRequest);
         queryRequest.setTableName(tableName);

         // todo - validate that the primary key is of the proper type (e.g,. not a string for an id field)
         //  and throw a 400-series error (tell the user bad-request), rather than, we're doing a 500 (server error)

         ///////////////////////////////////////////////////////
         // setup a filter for the primaryKey = the path-pram //
         ///////////////////////////////////////////////////////
         queryRequest.setFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria()
               .withFieldName(table.getPrimaryKeyField())
               .withOperator(QCriteriaOperator.EQUALS)
               .withValues(List.of(primaryKey))));

         QueryAction queryAction = new QueryAction();
         QueryResult queryResult = queryAction.execute(queryRequest);

         ///////////////////////////////////////////////////////
         // throw a not found error if the record isn't found //
         ///////////////////////////////////////////////////////
         if(queryResult.getRecords().isEmpty())
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with "
               + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }

         context.result(JsonUtils.toJson(queryResult.getRecords().get(0)));
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
         CountRequest countRequest = new CountRequest(qInstance);
         setupSession(context, countRequest);
         countRequest.setTableName(context.pathParam("table"));

         String filter = stringQueryParam(context, "filter");
         if(filter != null)
         {
            countRequest.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         CountAction countAction = new CountAction();
         CountResult countResult = countAction.execute(countRequest);

         context.result(JsonUtils.toJson(countResult));
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
         QueryRequest queryRequest = new QueryRequest(qInstance);
         setupSession(context, queryRequest);
         queryRequest.setTableName(context.pathParam("table"));
         queryRequest.setSkip(integerQueryParam(context, "skip"));
         queryRequest.setLimit(integerQueryParam(context, "limit"));

         String filter = stringQueryParam(context, "filter");
         if(filter != null)
         {
            queryRequest.setFilter(JsonUtils.toObject(filter, QQueryFilter.class));
         }

         QueryAction queryAction = new QueryAction();
         QueryResult queryResult = queryAction.execute(queryRequest);

         context.result(JsonUtils.toJson(queryResult));
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
         MetaDataRequest metaDataRequest = new MetaDataRequest(qInstance);
         setupSession(context, metaDataRequest);
         MetaDataAction metaDataAction = new MetaDataAction();
         MetaDataResult metaDataResult = metaDataAction.execute(metaDataRequest);

         context.result(JsonUtils.toJson(metaDataResult));
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
         TableMetaDataRequest tableMetaDataRequest = new TableMetaDataRequest(qInstance);
         setupSession(context, tableMetaDataRequest);
         tableMetaDataRequest.setTableName(context.pathParam("table"));
         TableMetaDataAction tableMetaDataAction = new TableMetaDataAction();
         TableMetaDataResult tableMetaDataResult = tableMetaDataAction.execute(tableMetaDataRequest);

         context.result(JsonUtils.toJson(tableMetaDataResult));
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
         ProcessMetaDataRequest processMetaDataRequest = new ProcessMetaDataRequest(qInstance);
         setupSession(context, processMetaDataRequest);
         processMetaDataRequest.setProcessName(context.pathParam("processName"));
         ProcessMetaDataAction processMetaDataAction = new ProcessMetaDataAction();
         ProcessMetaDataResult processMetaDataResult = processMetaDataAction.execute(processMetaDataRequest);

         context.result(JsonUtils.toJson(processMetaDataResult));
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
      QUserFacingException userFacingException = ExceptionUtils.findClassInRootChain(e, QUserFacingException.class);
      if(userFacingException != null)
      {
         if(userFacingException instanceof QNotFoundException)
         {
            context.status(HttpStatus.NOT_FOUND_404)
               .result("{\"error\":\"" + userFacingException.getMessage() + "\"}");
         }
         else
         {
            LOG.info("User-facing exception", e);
            context.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
               .result("{\"error\":\"" + userFacingException.getMessage() + "\"}");
         }
      }
      else
      {
         LOG.warn("Exception in javalin request", e);
         context.status(HttpStatus.INTERNAL_SERVER_ERROR_500)
            .result("{\"error\":\"" + e.getClass().getSimpleName() + " (" + e.getMessage() + ")\"}");
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
