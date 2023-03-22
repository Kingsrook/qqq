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
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
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
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
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
               // ApiBuilder.post("/query", QJavalinApiHandler::doQuery);

               ApiBuilder.get("/{primaryKey}", QJavalinApiHandler::doGet);
               ApiBuilder.patch("/{primaryKey}", QJavalinApiHandler::doUpdate);
               ApiBuilder.delete("/{primaryKey}", QJavalinApiHandler::doDelete);

               // post("/bulk", QJavalinApiHandler::bulkInsert);
               // patch("/bulk", QJavalinApiHandler::bulkUpdate);
               // delete("/bulk", QJavalinApiHandler::bulkDelete);
            });
         });

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
   private static void doPathNotFound(Context context)
   {
      handleException(context, new QNotFoundException("Could not find any resources at path " + context.path()));
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
         handleException(context, e);
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
         QTableMetaData table = qInstance.getTable(tableName);
         validateTableAndVersion(context, version, table);

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

         LinkedHashMap<String, Serializable> outputRecord = toApiRecord(record, tableName, version);

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
      String       version   = context.pathParam("version");
      String       tableName = context.pathParam("tableName");
      QQueryFilter filter    = null;

      try
      {
         List<String> badRequestMessages = new ArrayList<>();

         QTableMetaData table = qInstance.getTable(tableName);
         validateTableAndVersion(context, version, table);

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
            records.add(toApiRecord(record, tableName, version));
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
   private static void validateTableAndVersion(Context context, String version, QTableMetaData table) throws QNotFoundException
   {
      if(table == null)
      {
         throw (new QNotFoundException("Could not find any resources at path " + context.path()));
      }

      APIVersion requestApiVersion = new APIVersion(version);
      if(!ApiMiddlewareType.getApiInstanceMetaData(qInstance).getSupportedVersions().contains(requestApiVersion))
      {
         throw (new QNotFoundException("This version of this API does not contain the resource path " + context.path()));
      }

      if(!getApiVersionRange(table).includes(requestApiVersion))
      {
         throw (new QNotFoundException("This version of this API does not contain the resource path " + context.path()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private enum Operator
   {
      ///////////////////////////////////////////////////////////////////////////////////
      // order of these is important (e.g., because some are a sub-string of others!!) //
      ///////////////////////////////////////////////////////////////////////////////////
      EQ("=", QCriteriaOperator.EQUALS, QCriteriaOperator.NOT_EQUALS, true, 1),
      LTE("<=", QCriteriaOperator.LESS_THAN_OR_EQUALS, QCriteriaOperator.GREATER_THAN, false, 1),
      GTE(">=", QCriteriaOperator.GREATER_THAN_OR_EQUALS, QCriteriaOperator.LESS_THAN, false, 1),
      LT("<", QCriteriaOperator.LESS_THAN, QCriteriaOperator.GREATER_THAN_OR_EQUALS, false, 1),
      GT(">", QCriteriaOperator.GREATER_THAN, QCriteriaOperator.LESS_THAN_OR_EQUALS, false, 1),
      EMPTY("EMPTY", QCriteriaOperator.IS_BLANK, QCriteriaOperator.IS_NOT_BLANK, true, 0),
      BETWEEN("BETWEEN ", QCriteriaOperator.BETWEEN, QCriteriaOperator.NOT_BETWEEN, true, 2),
      IN("IN ", QCriteriaOperator.IN, QCriteriaOperator.NOT_IN, true, null),
      // todo MATCHES
      ;


      private final String            prefix;
      private final QCriteriaOperator positiveOperator;
      private final QCriteriaOperator negativeOperator;
      private final boolean           supportsNot;
      private final Integer           noOfValues; // null means many (IN)



      /*******************************************************************************
       **
       *******************************************************************************/
      Operator(String prefix, QCriteriaOperator positiveOperator, QCriteriaOperator negativeOperator, boolean supportsNot, Integer noOfValues)
      {
         this.prefix = prefix;
         this.positiveOperator = positiveOperator;
         this.negativeOperator = negativeOperator;
         this.supportsNot = supportsNot;
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
            if(!selectedOperator.supportsNot && isNot)
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
      String version   = context.pathParam("version");
      String tableName = context.pathParam("tableName");

      try
      {
         QTableMetaData table = qInstance.getTable(tableName);
         validateTableAndVersion(context, version, table);

         InsertInput insertInput = new InsertInput();

         setupSession(context, insertInput);
         QJavalinAccessLogger.logStart("insert", logPair("table", tableName));

         insertInput.setTableName(tableName);

         PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

         try
         {
            if(!StringUtils.hasContent(context.body()))
            {
               throw (new QBadRequestException("Missing required POST body"));
            }

            JSONObject jsonObject = new JSONObject(context.body());
            insertInput.setRecords(List.of(toQRecord(jsonObject, tableName, version)));
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
   private static void doUpdate(Context context)
   {
      String version    = context.pathParam("version");
      String tableName  = context.pathParam("tableName");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table = qInstance.getTable(tableName);
         validateTableAndVersion(context, version, table);

         UpdateInput updateInput = new UpdateInput();

         setupSession(context, updateInput);
         QJavalinAccessLogger.logStart("update", logPair("table", tableName));

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
               throw (new QBadRequestException("Missing required POST body"));
            }

            JSONObject jsonObject = new JSONObject(context.body());
            QRecord    qRecord    = toQRecord(jsonObject, tableName, version);
            qRecord.setValue(table.getPrimaryKeyField(), primaryKey);
            updateInput.setRecords(List.of(qRecord));
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
      String version    = context.pathParam("version");
      String tableName  = context.pathParam("tableName");
      String primaryKey = context.pathParam("primaryKey");

      try
      {
         QTableMetaData table = qInstance.getTable(tableName);
         validateTableAndVersion(context, version, table);

         DeleteInput deleteInput = new DeleteInput();

         setupSession(context, deleteInput);
         QJavalinAccessLogger.logStart("delete", logPair("table", tableName));

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
   private static LinkedHashMap<String, Serializable> toApiRecord(QRecord record, String tableName, String apiVersion) throws QException
   {
      List<? extends QFieldMetaData>      tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(apiVersion)).getFields();
      LinkedHashMap<String, Serializable> outputRecord   = new LinkedHashMap<>();
      for(QFieldMetaData tableApiField : tableApiFields)
      {
         // todo - what about display values / possible values
         // todo - handle removed-from-this-version fields!!
         outputRecord.put(tableApiField.getName(), record.getValue(tableApiField.getName()));
      }
      return (outputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QRecord toQRecord(JSONObject jsonObject, String tableName, String apiVersion) throws QException
   {
      List<String> unrecognizedFieldNames = new ArrayList<>();

      List<? extends QFieldMetaData>        tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(apiVersion)).getFields();
      Map<String, ? extends QFieldMetaData> apiFieldsMap   = tableApiFields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));

      QRecord qRecord = new QRecord();

      for(String jsonKey : jsonObject.keySet())
      {
         if(apiFieldsMap.containsKey(jsonKey))
         {
            QFieldMetaData field = apiFieldsMap.get(jsonKey);
            qRecord.setValue(field.getName(), jsonObject.get(jsonKey));
         }
         else
         {
            unrecognizedFieldNames.add(jsonKey);
         }
      }

      if(!unrecognizedFieldNames.isEmpty())
      {
         throw (new QBadRequestException("Request body contained " + unrecognizedFieldNames.size() + " unrecognized field name" + StringUtils.plural(unrecognizedFieldNames) + ": " + StringUtils.joinWithCommasAndAnd(unrecognizedFieldNames)));
      }

      return (qRecord);
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
         if(e instanceof ApiPathNotFoundException)
         {
            respondWithError(context, HttpStatus.Code.NOT_FOUND, e.getMessage()); // 404
            return;
         }

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
