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
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.ApiFieldCustomValueMapper;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.tables.ApiAssociationMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
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
import org.apache.commons.lang.BooleanUtils;
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
    ** Allow tests (that manipulate meta-data) to clear field caches.
    *******************************************************************************/
   public static void clearCaches()
   {
      fieldListCache.clear();
      fieldMapCache.clear();
   }



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
         else if(apiFieldMetaData.getCustomValueMapper() != null)
         {
            ApiFieldCustomValueMapper customValueMapper = QCodeLoader.getAdHoc(ApiFieldCustomValueMapper.class, apiFieldMetaData.getCustomValueMapper());
            value = customValueMapper.produceApiValue(record);
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
         if(isAssociationOmitted(apiName, apiVersion, table, association))
         {
            continue;
         }

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
   private static boolean isAssociationOmitted(String apiName, String apiVersion, QTableMetaData table, Association association)
   {
      ApiTableMetaData       thisApiTableMetaData   = ObjectUtils.tryAndRequireNonNullElse(() -> ApiTableMetaDataContainer.of(table).getApiTableMetaData(apiName), new ApiTableMetaData());
      ApiAssociationMetaData apiAssociationMetaData = thisApiTableMetaData.getApiAssociationMetaData().get(association.getName());
      if(apiAssociationMetaData != null)
      {
         if(BooleanUtils.isTrue(apiAssociationMetaData.getIsExcluded()))
         {
            return (true);
         }

         APIVersionRange apiVersionRange = apiAssociationMetaData.getApiVersionRange();
         if(!apiVersionRange.includes(new APIVersion(apiVersion)))
         {
            return true;
         }
      }
      return false;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord apiJsonObjectToQRecord(JSONObject jsonObject, String tableName, String apiName, String apiVersion, boolean includeNonEditableFields) throws QException
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
         if(!isAssociationOmitted(apiName, apiVersion, table, association))
         {
            associationMap.put(association.getName(), association);
         }
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
               if(includeNonEditableFields)
               {
                  LOG.trace("Even though field [" + field.getName() + "] is not editable, we'll use it, because we've been asked to include non-editable fields");
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
            else if(apiFieldMetaData.getCustomValueMapper() != null)
            {
               ApiFieldCustomValueMapper customValueMapper = QCodeLoader.getAdHoc(ApiFieldCustomValueMapper.class, apiFieldMetaData.getCustomValueMapper());
               customValueMapper.consumeApiValue(qRecord, value, jsonObject);
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
         List<String> otherVersionHints = new ArrayList<>();
         try
         {
            for(String unrecognizedFieldName : unrecognizedFieldNames)
            {
               String hint = lookForFieldInOtherVersions(unrecognizedFieldName, tableName, apiName, apiVersion);
               if(hint != null)
               {
                  otherVersionHints.add(hint);
               }
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error looking for unrecognized field names in other api versions", e);
         }

         throw (new QBadRequestException("Request body contained "
            + (unrecognizedFieldNames.size() + " unrecognized field name" + StringUtils.plural(unrecognizedFieldNames) + ": " + StringUtils.joinWithCommasAndAnd(unrecognizedFieldNames) + ". ")
            + (CollectionUtils.nullSafeIsEmpty(otherVersionHints) ? "" : StringUtils.join(" ", otherVersionHints))
         ));
      }

      return (qRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static String lookForFieldInOtherVersions(String unrecognizedFieldName, String tableName, String apiName, String apiVersion) throws QException
   {
      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
      ApiInstanceMetaData          apiInstanceMetaData          = apiInstanceMetaDataContainer.getApiInstanceMetaData(apiName);

      List<String> versionsWithThisField = new ArrayList<>();
      for(APIVersion supportedVersion : apiInstanceMetaData.getSupportedVersions())
      {
         if(!supportedVersion.toString().equals(apiVersion))
         {
            Map<String, QFieldMetaData> versionFields = getTableApiFieldMap(new ApiNameVersionAndTableName(apiName, supportedVersion.toString(), tableName));
            if(versionFields.containsKey(unrecognizedFieldName))
            {
               versionsWithThisField.add(supportedVersion.toString());
            }
         }
      }

      if(CollectionUtils.nullSafeHasContents(versionsWithThisField))
      {
         return (unrecognizedFieldName + " does not exist in version " + apiVersion + ", but does exist in versions: " + StringUtils.joinWithCommasAndAnd(versionsWithThisField) + ". ");
      }

      return (null);
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
