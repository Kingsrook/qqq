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
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 ** Methods for going back and forth from QRecords to API-versions of objects
 *******************************************************************************/
public class QRecordApiAdapter
{
   private static final QLogger LOG = QLogger.getLogger(QRecordApiAdapter.class);

   private static Map<ApiNameVersionAndTableName, List<QFieldMetaData>>        fieldListCache = new HashMap<>();
   private static Map<ApiNameVersionAndTableName, Map<String, QFieldMetaData>> fieldMapCache  = new HashMap<>();



   /*******************************************************************************
    ** Convert a QRecord to a map for the API
    *******************************************************************************/
   public static Map<String, Serializable> qRecordToApiMap(QRecord record, String tableName, String apiName, String apiVersion) throws QException
   {
      if(record == null)
      {
         return (null);
      }

      List<QFieldMetaData>                tableApiFields = getTableApiFieldList(new ApiNameVersionAndTableName(apiName, apiVersion, tableName));
      LinkedHashMap<String, Serializable> outputRecord   = new LinkedHashMap<>();

      /////////////////////////////////////////
      // iterate over the table's api fields //
      /////////////////////////////////////////
      for(QFieldMetaData field : tableApiFields)
      {
         ApiFieldMetaData apiFieldMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
         String           apiFieldName     = ApiFieldMetaData.getEffectiveApiFieldName(apiName, field);

         Serializable value = null;
         if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
         {
            value = record.getValue(apiFieldMetaData.getReplacedByFieldName());
         }
         else
         {
            value = record.getValue(field.getName());
         }

         if(field.getType().equals(QFieldType.BLOB) && value instanceof byte[] bytes)
         {
            value = Base64.getEncoder().encodeToString(bytes);
         }

         outputRecord.put(apiFieldName, value);
      }

      //////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - should probably define in meta-data if an association is included in the api or not!! //
      //  and what its name is too...                                                                 //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      QTableMetaData table = QContext.getQInstance().getTable(tableName);
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         ArrayList<Map<String, Serializable>> associationList = new ArrayList<>();
         outputRecord.put(association.getName(), associationList);

         for(QRecord associatedRecord : CollectionUtils.nonNullList(CollectionUtils.nonNullMap(record.getAssociatedRecords()).get(association.getName())))
         {
            associationList.add(qRecordToApiMap(associatedRecord, association.getAssociatedTableName(), apiName, apiVersion));
         }
      }

      return (outputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord apiJsonObjectToQRecord(JSONObject jsonObject, String tableName, String apiName, String apiVersion, boolean includePrimaryKey) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // make map of apiFieldNames (e.g., names as api uses them) to QFieldMetaData //
      ////////////////////////////////////////////////////////////////////////////////
      Map<String, QFieldMetaData> apiFieldsMap           = getTableApiFieldMap(new ApiNameVersionAndTableName(apiName, apiVersion, tableName));
      List<String>                unrecognizedFieldNames = new ArrayList<>();
      QRecord                     qRecord                = new QRecord();

      Map<String, Association> associationMap = new HashMap<>();
      QTableMetaData           table          = QContext.getQInstance().getTable(tableName);
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         associationMap.put(association.getName(), association);
      }

      //////////////////////////////////////////
      // iterate over keys in the json object //
      //////////////////////////////////////////
      for(String jsonKey : jsonObject.keySet())
      {
         ////////////////////////////////////////////////
         // if it's a valid api field name, process it //
         ////////////////////////////////////////////////
         if(apiFieldsMap.containsKey(jsonKey))
         {
            QFieldMetaData field = apiFieldsMap.get(jsonKey);
            Object         value = jsonObject.isNull(jsonKey) ? null : jsonObject.get(jsonKey);

            if(field.getType().equals(QFieldType.BLOB) && value instanceof String s)
            {
               value = Base64.getDecoder().decode(s);
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            // generally, omit non-editable fields -                                                              //
            // however - if we're asked to include the primary key (and this is the primary key), then include it //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(!field.getIsEditable())
            {
               if(includePrimaryKey && field.getName().equals(table.getPrimaryKeyField()))
               {
                  LOG.trace("Even though field [" + field.getName() + "] is not editable, we'll use it, because it's the primary key, and we've been asked to include primary keys");
               }
               else
               {
                  continue;
               }
            }

            ApiFieldMetaData apiFieldMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
            if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
            {
               qRecord.setValue(apiFieldMetaData.getReplacedByFieldName(), value);
            }
            else
            {
               qRecord.setValue(field.getName(), value);
            }
         }
         else if(associationMap.containsKey(jsonKey))
         {
            //////////////////////////////////////////////////////////////////////////////////////////////////
            // else, if it's an association - process that (recursively as a list of other records)         //
            // todo - should probably define in meta-data if an association is included in the api or not!! //
            // and what its name is too...                                                                  //
            //////////////////////////////////////////////////////////////////////////////////////////////////
            Association association = associationMap.get(jsonKey);
            Object      value       = jsonObject.get(jsonKey);
            if(value instanceof JSONArray jsonArray)
            {
               for(Object subObject : jsonArray)
               {
                  if(subObject instanceof JSONObject subJsonObject)
                  {
                     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                     // make sure to always include primary keys (boolean param) on calls for children - to help determine insert/update cases //
                     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                     QRecord subRecord = apiJsonObjectToQRecord(subJsonObject, association.getAssociatedTableName(), apiName, apiVersion, true);
                     qRecord.withAssociatedRecord(association.getName(), subRecord);
                  }
                  else
                  {
                     throw (new QBadRequestException("Found a " + value.getClass().getSimpleName() + " in the array under key " + jsonKey + ", but a JSON object is required here."));
                  }
               }
            }
            else
            {
               throw (new QBadRequestException("Found a " + value.getClass().getSimpleName() + " at key " + jsonKey + ", but a JSON array is required here."));
            }
         }
         else
         {
            ///////////////////////////////////////////////////
            // else add it to the list of unrecognized names //
            ///////////////////////////////////////////////////
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
   private static Map<String, QFieldMetaData> getTableApiFieldMap(ApiNameVersionAndTableName apiNameVersionAndTableName) throws QException
   {
      if(!fieldMapCache.containsKey(apiNameVersionAndTableName))
      {
         Map<String, QFieldMetaData> map = getTableApiFieldList(apiNameVersionAndTableName).stream().collect(Collectors.toMap(f -> (ApiFieldMetaData.getEffectiveApiFieldName(apiNameVersionAndTableName.apiName(), f)), f -> f));
         fieldMapCache.put(apiNameVersionAndTableName, map);
      }

      return (fieldMapCache.get(apiNameVersionAndTableName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QFieldMetaData> getTableApiFieldList(ApiNameVersionAndTableName apiNameVersionAndTableName) throws QException
   {
      if(!fieldListCache.containsKey(apiNameVersionAndTableName))
      {
         List<QFieldMetaData> value = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput()
            .withTableName(apiNameVersionAndTableName.tableName())
            .withVersion(apiNameVersionAndTableName.apiVersion())
            .withApiName(apiNameVersionAndTableName.apiName())).getFields();
         fieldListCache.put(apiNameVersionAndTableName, value);
      }
      return (fieldListCache.get(apiNameVersionAndTableName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private record ApiNameVersionAndTableName(String apiName, String apiVersion, String tableName)
   {

   }

}
