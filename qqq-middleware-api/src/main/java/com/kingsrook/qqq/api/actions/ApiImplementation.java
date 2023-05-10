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

package com.kingsrook.qqq.api.actions;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
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
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.logging.LogPair;
import com.kingsrook.qqq.backend.core.logging.QLogger;
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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.NotFoundStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.PermissionDeniedMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QErrorMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QWarningMessage;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiImplementation
{
   private static final QLogger LOG = QLogger.getLogger(ApiImplementation.class);

   /////////////////////////////////////
   // key:  Pair<apiName, apiVersion> //
   /////////////////////////////////////
   private static Map<Pair<String, String>, Map<String, QTableMetaData>> tableApiNameMap = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, Serializable> query(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, Map<String, List<String>> paramMap) throws QException
   {
      List<String> badRequestMessages = new ArrayList<>();

      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.QUERY_BY_QUERY_STRING);
      String         tableName = table.getName();

      QueryInput queryInput = new QueryInput();
      queryInput.setTableName(tableName);
      queryInput.setIncludeAssociations(true);

      PermissionsHelper.checkTablePermissionThrowing(queryInput, TablePermissionSubType.READ);

      String pageSizeParam     = getSingleParam(paramMap, "pageSize");
      String pageNoParam       = getSingleParam(paramMap, "pageNo");
      String booleanOperator   = getSingleParam(paramMap, "booleanOperator");
      String includeCountParam = getSingleParam(paramMap, "includeCount");
      String orderBy           = getSingleParam(paramMap, "orderBy");

      Integer pageSize = 50;
      if(StringUtils.hasContent(pageSizeParam))
      {
         try
         {
            pageSize = ValueUtils.getValueAsInteger(pageSizeParam);
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
      if(StringUtils.hasContent(pageNoParam))
      {
         try
         {
            pageNo = ValueUtils.getValueAsInteger(pageNoParam);
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

      QQueryFilter filter = new QQueryFilter();
      filter.setLimit(pageSize);
      filter.setSkip((pageNo - 1) * pageSize);

      // queryInput.setQueryJoins(processQueryJoinsParam(context));

      if("and".equalsIgnoreCase(booleanOperator))
      {
         filter.setBooleanOperator(QQueryFilter.BooleanOperator.AND);
      }
      else if("or".equalsIgnoreCase(booleanOperator))
      {
         filter.setBooleanOperator(QQueryFilter.BooleanOperator.OR);
      }
      else if(StringUtils.hasContent(booleanOperator))
      {
         badRequestMessages.add("booleanOperator must be either AND or OR.");
      }

      boolean includeCount = true;
      if("true".equalsIgnoreCase(includeCountParam))
      {
         includeCount = true;
      }
      else if("false".equalsIgnoreCase(includeCountParam))
      {
         includeCount = false;
      }
      else if(StringUtils.hasContent(includeCountParam))
      {
         badRequestMessages.add("includeCount must be either true or false");
      }

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
      for(Map.Entry<String, List<String>> entry : paramMap.entrySet())
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

      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, Serializable> insert(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String body) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.INSERT);
      String         tableName = table.getName();

      InsertInput insertInput = new InsertInput();

      insertInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

      try
      {
         if(!StringUtils.hasContent(body))
         {
            throw (new QBadRequestException("Missing required POST body"));
         }

         JSONTokener jsonTokener = new JSONTokener(body.trim());
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

      List<QErrorMessage> errors = insertOutput.getRecords().get(0).getErrors();
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         boolean isBadRequest = areAnyErrorsBadRequest(errors);

         String message = "Error inserting " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors);
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

      List<QWarningMessage> warnings = insertOutput.getRecords().get(0).getWarnings();
      if(CollectionUtils.nullSafeHasContents(warnings))
      {
         outputRecord.put("warning", "Warning inserting " + table.getLabel() + ", some data may have been inserted: " + joinErrorsWithCommasAndAnd(warnings));
      }

      return (outputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<Map<String, Serializable>> bulkInsert(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String body) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_INSERT);
      String         tableName = table.getName();

      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(insertInput, TablePermissionSubType.INSERT);

      /////////////////
      // build input //
      /////////////////
      try
      {
         if(!StringUtils.hasContent(body))
         {
            throw (new QBadRequestException("Missing required POST body"));
         }

         ArrayList<QRecord> recordList = new ArrayList<>();
         insertInput.setRecords(recordList);

         JSONTokener jsonTokener = new JSONTokener(body.trim());
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

         List<QErrorMessage>   errors   = record.getErrors();
         List<QWarningMessage> warnings = record.getWarnings();
         if(CollectionUtils.nullSafeHasContents(errors))
         {
            if(areAnyErrorsBadRequest(errors))
            {
               outputRecord.put("statusCode", HttpStatus.Code.BAD_REQUEST.getCode());
               outputRecord.put("statusText", HttpStatus.Code.BAD_REQUEST.getMessage());
            }
            else
            {
               outputRecord.put("statusCode", HttpStatus.Code.INTERNAL_SERVER_ERROR.getCode());
               outputRecord.put("statusText", HttpStatus.Code.INTERNAL_SERVER_ERROR.getMessage());
            }
            outputRecord.put("error", "Error inserting " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors));
         }
         else if(CollectionUtils.nullSafeHasContents(warnings))
         {
            outputRecord.put("statusCode", HttpStatus.Code.CREATED.getCode());
            outputRecord.put("statusText", HttpStatus.Code.CREATED.getMessage());
            outputRecord.put("warning", "Warning inserting " + table.getLabel() + ", some data may have been inserted: " + joinErrorsWithCommasAndAnd(warnings));
            outputRecord.put(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
         }
         else
         {
            outputRecord.put("statusCode", HttpStatus.Code.CREATED.getCode());
            outputRecord.put("statusText", HttpStatus.Code.CREATED.getMessage());
            outputRecord.put(table.getPrimaryKeyField(), record.getValue(table.getPrimaryKeyField()));
         }
      }

      return (response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static Map<String, Serializable> get(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String primaryKey) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.GET);
      String         tableName = table.getName();

      GetInput getInput = new GetInput();
      getInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(getInput, TablePermissionSubType.READ);

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
      return (outputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void update(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String primaryKey, String body) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.UPDATE);
      String         tableName = table.getName();

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

      try
      {
         if(!StringUtils.hasContent(body))
         {
            throw (new QBadRequestException("Missing required PATCH body"));
         }

         JSONTokener jsonTokener = new JSONTokener(body.trim());
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

      List<QErrorMessage> errors = updateOutput.getRecords().get(0).getErrors();
      // todo - do we want, if there were warnings, to return a 200 w/ a body w/ the warnings?  maybe...
      if(CollectionUtils.nullSafeHasContents(errors))
      {
         if(areAnyErrorsNotFound(errors))
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with " + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }
         else
         {
            boolean isBadRequest = areAnyErrorsBadRequest(errors);

            String message = "Error updating " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors);
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
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<Map<String, Serializable>> bulkUpdate(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String body) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_UPDATE);
      String         tableName = table.getName();

      UpdateInput updateInput = new UpdateInput();
      updateInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(updateInput, TablePermissionSubType.EDIT);

      /////////////////
      // build input //
      /////////////////
      try
      {
         if(!StringUtils.hasContent(body))
         {
            throw (new QBadRequestException("Missing required PATCH body"));
         }

         ArrayList<QRecord> recordList = new ArrayList<>();
         updateInput.setRecords(recordList);

         JSONTokener jsonTokener = new JSONTokener(body.trim());
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

         List<QErrorMessage> errors = record.getErrors();

         HttpStatus.Code statusCode;
         if(CollectionUtils.nullSafeHasContents(errors))
         {
            outputRecord.put("error", "Error updating " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors));
            if(areAnyErrorsNotFound(errors))
            {
               statusCode = HttpStatus.Code.NOT_FOUND;
            }
            else if(areAnyErrorsBadRequest(errors))
            {
               statusCode = HttpStatus.Code.BAD_REQUEST;
            }
            else
            {
               statusCode = HttpStatus.Code.INTERNAL_SERVER_ERROR;
            }
         }
         else
         {
            statusCode = HttpStatus.Code.NO_CONTENT;

            List<QWarningMessage> warnings = record.getWarnings();
            if(CollectionUtils.nullSafeHasContents(warnings))
            {
               outputRecord.put("warning", "Warning updating " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(warnings));
            }
         }

         outputRecord.put("statusCode", statusCode.getCode());
         outputRecord.put("statusText", statusCode.getMessage());

         i++;
      }

      return (response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void delete(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String primaryKey) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.DELETE);
      String         tableName = table.getName();

      DeleteInput deleteInput = new DeleteInput();
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
         List<QErrorMessage> errors = deleteOutput.getRecordsWithErrors().get(0).getErrors();
         if(areAnyErrorsNotFound(errors))
         {
            throw (new QNotFoundException("Could not find " + table.getLabel() + " with " + table.getFields().get(table.getPrimaryKeyField()).getLabel() + " of " + primaryKey));
         }
         else if(areAnyErrorsBadRequest(errors))
         {
            throw (new QBadRequestException("Error deleting " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors)));
         }
         // todo - do we want, if there were warnings, to return a 200 w/ a body w/ the warnings?  maybe...
         else
         {
            throw (new QException("Error deleting " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors)));
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<Map<String, Serializable>> bulkDelete(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, String body) throws QException
   {
      QTableMetaData table     = validateTableAndVersion(apiInstanceMetaData, version, tableApiName, ApiOperation.BULK_DELETE);
      String         tableName = table.getName();

      DeleteInput deleteInput = new DeleteInput();
      deleteInput.setTableName(tableName);

      PermissionsHelper.checkTablePermissionThrowing(deleteInput, TablePermissionSubType.DELETE);

      /////////////////
      // build input //
      /////////////////
      try
      {
         if(!StringUtils.hasContent(body))
         {
            throw (new QBadRequestException("Missing required DELETE body"));
         }

         ArrayList<Serializable> primaryKeyList = new ArrayList<>();
         deleteInput.setPrimaryKeys(primaryKeyList);

         JSONTokener jsonTokener = new JSONTokener(body.trim());
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

      List<QRecord>                    recordsWithErrors     = deleteOutput.getRecordsWithErrors();
      Map<String, List<QErrorMessage>> primaryKeyToErrorsMap = new HashMap<>();
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

         String              primaryKeyString = ValueUtils.getValueAsString(primaryKey);
         List<QErrorMessage> errors           = primaryKeyToErrorsMap.get(primaryKeyString);
         if(CollectionUtils.nullSafeHasContents(errors))
         {
            outputRecord.put("error", "Error deleting " + table.getLabel() + ": " + joinErrorsWithCommasAndAnd(errors));
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

      return (response);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String joinErrorsWithCommasAndAnd(List<? extends QStatusMessage> errors)
   {
      return StringUtils.joinWithCommasAndAnd(errors.stream().map(QStatusMessage::getMessage).toList());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String getSingleParam(Map<String, List<String>> paramMap, String name)
   {
      if(CollectionUtils.nullSafeHasContents(paramMap.get(name)))
      {
         return (paramMap.get(name).get(0));
      }

      return (null);
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
   public static QTableMetaData validateTableAndVersion(ApiInstanceMetaData apiInstanceMetaData, String version, String tableApiName, ApiOperation operation) throws QNotFoundException
   {
      QTableMetaData table    = getTableByApiName(apiInstanceMetaData.getName(), version, tableApiName);
      LogPair[]      logPairs = new LogPair[] { logPair("apiName", apiInstanceMetaData.getName()), logPair("version", version), logPair("tableApiName", tableApiName), logPair("operation", operation) };

      if(table == null)
      {
         LOG.info("404 because table is null (tableApiName=" + tableApiName + ")", logPairs);
         throw (new QNotFoundException("Could not find a table named " + tableApiName + " in this api."));
      }

      if(BooleanUtils.isTrue(table.getIsHidden()))
      {
         LOG.info("404 because table isHidden", logPairs);
         throw (new QNotFoundException("Could not find a table named " + tableApiName + " in this api."));
      }

      ApiTableMetaDataContainer apiTableMetaDataContainer = ApiTableMetaDataContainer.of(table);
      if(apiTableMetaDataContainer == null)
      {
         LOG.info("404 because table apiMetaDataContainer is null", logPairs);
         throw (new QNotFoundException("Could not find a table named " + tableApiName + " in this api."));
      }

      ApiTableMetaData apiTableMetaData = apiTableMetaDataContainer.getApiTableMetaData(apiInstanceMetaData.getName());
      if(apiTableMetaData == null)
      {
         LOG.info("404 because table apiMetaData is null", logPairs);
         throw (new QNotFoundException("Could not find a table named " + tableApiName + " in this api."));
      }

      if(BooleanUtils.isTrue(apiTableMetaData.getIsExcluded()))
      {
         LOG.info("404 because table is excluded", logPairs);
         throw (new QNotFoundException("Could not find a table named " + tableApiName + " in this api."));
      }

      if(!operation.isOperationEnabled(List.of(apiInstanceMetaData, apiTableMetaData)))
      {
         LOG.info("404 because api operation is not enabled", logPairs);
         throw (new QNotFoundException("Cannot perform operation [" + operation + "] on table named " + tableApiName + " in this api."));
      }

      if(!table.isCapabilityEnabled(QContext.getQInstance().getBackendForTable(table.getName()), operation.getCapability()))
      {
         LOG.info("404 because table capability is not enabled", logPairs);
         throw (new QNotFoundException("Cannot perform operation [" + operation + "] on table named " + tableApiName + " in this api."));
      }

      APIVersion       requestApiVersion = new APIVersion(version);
      List<APIVersion> supportedVersions = apiInstanceMetaData.getSupportedVersions();
      if(CollectionUtils.nullSafeIsEmpty(supportedVersions) || !supportedVersions.contains(requestApiVersion))
      {
         LOG.info("404 because requested version is not supported", logPairs);
         throw (new QNotFoundException(version + " is not a supported version in this api."));
      }

      if(!apiTableMetaData.getApiVersionRange().includes(requestApiVersion))
      {
         LOG.info("404 because table version range does not include requested version", logPairs);
         throw (new QNotFoundException(version + " is not a supported version for table " + tableApiName + " in this api."));
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

         for(QTableMetaData table : QContext.getQInstance().getTables().values())
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
   private static boolean areAnyErrorsBadRequest(List<QErrorMessage> errors)
   {
      return errors.stream().anyMatch(e -> (e instanceof BadInputStatusMessage) || (e instanceof PermissionDeniedMessage));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean areAnyErrorsNotFound(List<QErrorMessage> errors)
   {
      return errors.stream().anyMatch(e -> (e instanceof NotFoundStatusMessage));
   }

}