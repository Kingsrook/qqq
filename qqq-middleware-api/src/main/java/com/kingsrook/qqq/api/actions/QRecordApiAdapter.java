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
import com.kingsrook.qqq.api.actions.output.ApiOutputMapWrapper;
import com.kingsrook.qqq.api.actions.output.ApiOutputQRecordWrapper;
import com.kingsrook.qqq.api.actions.output.ApiOutputRecordWrapperInterface;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.ApiFieldCustomValueMapper;
import com.kingsrook.qqq.api.model.actions.ApiFieldCustomValueMapperBulkSupportInterface;
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
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.apache.commons.lang.BooleanUtils;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 ** Methods for going back and forth from QRecords to API-versions of objects
 *******************************************************************************/
public class QRecordApiAdapter
{
   private static final QLogger LOG = QLogger.getLogger(QRecordApiAdapter.class);



   /*******************************************************************************
    ** Simple/short form of convert a QRecord to a map for the API - e.g., meant for
    ** public consumption.
    *******************************************************************************/
   public static Map<String, Serializable> qRecordToApiMap(QRecord record, String tableName, String apiName, String apiVersion) throws QException
   {
      return qRecordsToApiMapList(ListBuilder.of(record), tableName, apiName, apiVersion).get(0);
   }



   /*******************************************************************************
    ** bulk-version of the qRecordToApiMap - will use
    ** ApiFieldCustomValueMapperBulkSupportInterface's in the bulky way.
    *******************************************************************************/
   public static ArrayList<Map<String, Serializable>> qRecordsToApiMapList(List<QRecord> records, String tableName, String apiName, String apiVersion) throws QException
   {
      Map<String, ApiFieldCustomValueMapper> fieldValueMappers = getFieldValueMappers(records, tableName, apiName, apiVersion);

      ArrayList<Map<String, Serializable>> rs = new ArrayList<>();
      for(QRecord record : records)
      {
         ApiOutputMapWrapper apiOutputMap = qRecordToApiMap(record, tableName, apiName, apiVersion, fieldValueMappers, new ApiOutputMapWrapper(new LinkedHashMap<>()));
         rs.add(apiOutputMap == null ? null : apiOutputMap.getContents());
      }

      return (rs);
   }



   /*******************************************************************************
    ** version of the qRecordToApiMap that returns QRecords, not maps.
    ** useful for cases where we're staying inside QQQ, but working with an api-
    ** versioned application.
    *******************************************************************************/
   public static List<QRecord> qRecordsToApiVersionedQRecordList(List<QRecord> records, String tableName, String apiName, String apiVersion) throws QException
   {
      Map<String, ApiFieldCustomValueMapper> fieldValueMappers = getFieldValueMappers(records, tableName, apiName, apiVersion);

      List<QRecord> rs = new ArrayList<>();
      for(QRecord record : records)
      {
         ApiOutputQRecordWrapper apiOutputQRecord = qRecordToApiMap(record, tableName, apiName, apiVersion, fieldValueMappers, new ApiOutputQRecordWrapper(new QRecord().withTableName(tableName)));
         rs.add(apiOutputQRecord.getContents());
      }

      return (rs);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static Map<String, ApiFieldCustomValueMapper> getFieldValueMappers(List<QRecord> records, String tableName, String apiName, String apiVersion) throws QException
   {
      Map<String, ApiFieldCustomValueMapper> fieldValueMappers = new HashMap<>();

      List<QFieldMetaData> tableApiFields = GetTableApiFieldsAction.getTableApiFieldList(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, apiVersion, tableName));
      for(QFieldMetaData field : tableApiFields)
      {
         ApiFieldMetaData apiFieldMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
         if(apiFieldMetaData.getCustomValueMapper() != null)
         {
            if(!fieldValueMappers.containsKey(apiFieldMetaData.getCustomValueMapper().getName()))
            {
               ApiFieldCustomValueMapper customValueMapper = QCodeLoader.getAdHoc(ApiFieldCustomValueMapper.class, apiFieldMetaData.getCustomValueMapper());
               fieldValueMappers.put(apiFieldMetaData.getCustomValueMapper().getName(), customValueMapper);

               if(customValueMapper instanceof ApiFieldCustomValueMapperBulkSupportInterface bulkMapper)
               {
                  bulkMapper.prepareToProduceApiValues(records);
               }
            }
         }
      }
      return fieldValueMappers;
   }



   /*******************************************************************************
    ** private version of convert a QRecord to a map for the API (or, another
    ** QRecord - whatever object is in the `O output` param). Takes params to
    ** support working in bulk w/ customizers much better.
    *******************************************************************************/
   private static <C, O extends ApiOutputRecordWrapperInterface<C, O>> O qRecordToApiMap(QRecord record, String tableName, String apiName, String apiVersion, Map<String, ApiFieldCustomValueMapper> fieldValueMappers, O output) throws QException
   {
      if(record == null)
      {
         return (null);
      }

      List<QFieldMetaData> tableApiFields = GetTableApiFieldsAction.getTableApiFieldList(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, apiVersion, tableName));

      /////////////////////////////////////////
      // iterate over the table's api fields //
      /////////////////////////////////////////
      for(QFieldMetaData field : tableApiFields)
      {
         ApiFieldMetaData apiFieldMetaData = ObjectUtils.tryAndRequireNonNullElse(() -> ApiFieldMetaDataContainer.of(field).getApiFieldMetaData(apiName), new ApiFieldMetaData());
         String           apiFieldName     = ApiFieldMetaData.getEffectiveApiFieldName(apiName, field);

         Serializable value;
         if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
         {
            value = record.getValue(apiFieldMetaData.getReplacedByFieldName());
         }
         else if(apiFieldMetaData.getCustomValueMapper() != null)
         {
            if(fieldValueMappers == null)
            {
               fieldValueMappers = new HashMap<>();
            }

            String customValueMapperName = apiFieldMetaData.getCustomValueMapper().getName();
            if(!fieldValueMappers.containsKey(customValueMapperName))
            {
               fieldValueMappers.put(customValueMapperName, QCodeLoader.getAdHoc(ApiFieldCustomValueMapper.class, apiFieldMetaData.getCustomValueMapper()));
            }

            ApiFieldCustomValueMapper customValueMapper = fieldValueMappers.get(customValueMapperName);
            value = customValueMapper.produceApiValue(record, apiFieldName);
         }
         else
         {
            value = record.getValue(field.getName());
         }

         if(field.getType().equals(QFieldType.BLOB) && value instanceof byte[] bytes)
         {
            value = Base64.getEncoder().encodeToString(bytes);
         }

         output.putValue(apiFieldName, value);
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

         ArrayList<O> associationList = new ArrayList<>();

         for(QRecord associatedRecord : CollectionUtils.nonNullList(CollectionUtils.nonNullMap(record.getAssociatedRecords()).get(association.getName())))
         {
            ApiOutputRecordWrapperInterface<C, O> apiOutputAssociation = output.newSibling(associatedRecord.getTableName());
            associationList.add(qRecordToApiMap(associatedRecord, association.getAssociatedTableName(), apiName, apiVersion, fieldValueMappers, apiOutputAssociation.unwrap()));
         }

         output.putAssociation(association.getName(), associationList);
      }

      return (output);
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
      Map<String, QFieldMetaData> apiFieldsMap           = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, apiVersion, tableName));
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
               customValueMapper.consumeApiValue(qRecord, value, jsonObject, jsonKey);
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
            Map<String, QFieldMetaData> versionFields = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, supportedVersion.toString(), tableName));
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
}
