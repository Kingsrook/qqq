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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.kingsrook.qqq.api.javalin.QBadRequestException;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.Pair;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.json.JSONObject;


/*******************************************************************************
 ** Methods for going back and forth from QRecords to API-versions of objects
 *******************************************************************************/
public class QRecordApiAdapter
{
   private static Map<Pair<String, String>, List<QFieldMetaData>>        fieldListCache = new HashMap<>();
   private static Map<Pair<String, String>, Map<String, QFieldMetaData>> fieldMapCache  = new HashMap<>();



   /*******************************************************************************
    ** Convert a QRecord to a map for the API
    *******************************************************************************/
   public static Map<String, Serializable> qRecordToApiMap(QRecord record, String tableName, String apiVersion) throws QException
   {
      List<QFieldMetaData>                tableApiFields = getTableApiFieldList(tableName, apiVersion);
      LinkedHashMap<String, Serializable> outputRecord   = new LinkedHashMap<>();

      /////////////////////////////////////////
      // iterate over the table's api fields //
      /////////////////////////////////////////
      for(QFieldMetaData field : tableApiFields)
      {
         ApiFieldMetaData apiFieldMetaData = ApiFieldMetaData.of(field);

         // todo - what about display values / possible values?

         String apiFieldName = ApiFieldMetaData.getEffectiveApiFieldName(field);
         if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
         {
            outputRecord.put(apiFieldName, record.getValue(apiFieldMetaData.getReplacedByFieldName()));
         }
         else
         {
            outputRecord.put(apiFieldName, record.getValue(field.getName()));
         }
      }
      return (outputRecord);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QRecord apiJsonObjectToQRecord(JSONObject jsonObject, String tableName, String apiVersion) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////
      // make map of apiFieldNames (e.g., names as api uses them) to QFieldMetaData //
      ////////////////////////////////////////////////////////////////////////////////
      Map<String, QFieldMetaData> apiFieldsMap           = getTableApiFieldMap(tableName, apiVersion);
      List<String>                unrecognizedFieldNames = new ArrayList<>();
      QRecord                     qRecord                = new QRecord();

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
            Object         value = jsonObject.get(jsonKey);

            ApiFieldMetaData apiFieldMetaData = ApiFieldMetaData.of(field);
            if(StringUtils.hasContent(apiFieldMetaData.getReplacedByFieldName()))
            {
               qRecord.setValue(apiFieldMetaData.getReplacedByFieldName(), value);
            }
            else
            {
               qRecord.setValue(field.getName(), value);
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
   private static Map<String, QFieldMetaData> getTableApiFieldMap(String tableName, String apiVersion) throws QException
   {
      Pair<String, String> key = new Pair<>(tableName, apiVersion);
      if(!fieldMapCache.containsKey(key))
      {
         Map<String, QFieldMetaData> map = getTableApiFieldList(tableName, apiVersion).stream().collect(Collectors.toMap(f -> (ApiFieldMetaData.getEffectiveApiFieldName(f)), f -> f));
         fieldMapCache.put(key, map);
      }

      return (fieldMapCache.get(key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<QFieldMetaData> getTableApiFieldList(String tableName, String apiVersion) throws QException
   {
      Pair<String, String> key = new Pair<>(tableName, apiVersion);
      if(!fieldListCache.containsKey(key))
      {
         List<QFieldMetaData> value = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(apiVersion)).getFields();
         fieldListCache.put(key, value);
      }
      return (fieldListCache.get(key));
   }
}
