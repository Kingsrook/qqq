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

package com.kingsrook.qqq.api.utils;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.actions.ApiImplementation;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Object injected into script context, for interfacing with a QQQ API.
 *******************************************************************************/
public class ApiScriptUtils implements Serializable
{
   private String apiName;
   private String apiVersion;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiScriptUtils(String apiName, String apiVersion)
   {
      setApiName(apiName);
      setApiVersion(apiVersion);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ArrayList<Map<String, Serializable>> qRecordListToApiRecordList(List<QRecord> qRecordList, String tableName, String apiName, String apiVersion) throws QException
   {
      if(qRecordList == null)
      {
         return (null);
      }

      ArrayList<Map<String, Serializable>> rs = new ArrayList<>();
      for(QRecord qRecord : qRecordList)
      {
         rs.add(QRecordApiAdapter.qRecordToApiMap(qRecord, tableName, apiName, apiVersion));
      }
      return (rs);
   }



   /*******************************************************************************
    ** Setter for apiName
    **
    *******************************************************************************/
   public void setApiName(String apiName)
   {
      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
      if(apiInstanceMetaDataContainer.getApis().containsKey(apiName))
      {
         this.apiName = apiName;
      }
      else
      {
         throw (new IllegalArgumentException("[" + apiName + "] is not a valid API name.  Valid values are: " + apiInstanceMetaDataContainer.getApis().keySet()));
      }
   }



   /*******************************************************************************
    ** Setter for apiVersion
    **
    *******************************************************************************/
   public void setApiVersion(String apiVersion)
   {
      if(apiName == null)
      {
         throw (new IllegalArgumentException("You must set apiName before setting apiVersion."));
      }

      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
      ApiInstanceMetaData          apiInstanceMetaData          = apiInstanceMetaDataContainer.getApis().get(apiName);
      if(apiInstanceMetaData.getSupportedVersions().contains(new APIVersion(apiVersion)))
      {
         this.apiVersion = apiVersion;
      }
      else
      {
         throw (new IllegalArgumentException("[" + apiVersion + "] is not a supported version for this API.  Supported versions are: " + apiInstanceMetaData.getSupportedVersions()));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void validateApiNameAndVersion(String description)
   {
      if(apiName == null || apiVersion == null)
      {
         throw (new IllegalStateException("Both apiName and apiVersion must be set before calling this method (" + description + ")."));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, Serializable> get(String tableApiName, Object primaryKey) throws QException
   {
      validateApiNameAndVersion("get(" + tableApiName + "," + primaryKey + ")");
      return (ApiImplementation.get(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(primaryKey)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, Serializable> query(String urlPart) throws QException
   {
      validateApiNameAndVersion("query(" + urlPart + ")");
      String[]                  urlParts = urlPart.split("\\?", 2);
      Map<String, List<String>> paramMap = parseQueryString(urlParts.length > 1 ? urlParts[1] : null);
      return (ApiImplementation.query(getApiInstanceMetaData(), apiVersion, urlParts[0], paramMap));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Map<String, Serializable> insert(String tableApiName, Object body) throws QException
   {
      validateApiNameAndVersion("insert(" + tableApiName + ")");
      return (ApiImplementation.insert(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(body)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<Map<String, Serializable>> bulkInsert(String tableApiName, Object body) throws QException
   {
      validateApiNameAndVersion("bulkInsert(" + tableApiName + ")");
      return (ApiImplementation.bulkInsert(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(body)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void update(String tableApiName, Object primaryKey, Object body) throws QException
   {
      validateApiNameAndVersion("update(" + tableApiName + "," + primaryKey + ")");
      ApiImplementation.update(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(primaryKey), String.valueOf(body));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<Map<String, Serializable>> bulkUpdate(String tableApiName, Object body) throws QException
   {
      validateApiNameAndVersion("bulkUpdate(" + tableApiName + ")");
      return (ApiImplementation.bulkUpdate(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(body)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void delete(String tableApiName, Object primaryKey) throws QException
   {
      validateApiNameAndVersion("delete(" + tableApiName + "," + primaryKey + ")");
      ApiImplementation.delete(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(primaryKey));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<Map<String, Serializable>> bulkDelete(String tableApiName, Object body) throws QException
   {
      validateApiNameAndVersion("bulkDelete(" + tableApiName + ")");
      return (ApiImplementation.bulkDelete(getApiInstanceMetaData(), apiVersion, tableApiName, String.valueOf(body)));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private ApiInstanceMetaData getApiInstanceMetaData()
   {
      ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
      ApiInstanceMetaData          apiInstanceMetaData          = apiInstanceMetaDataContainer.getApiInstanceMetaData(apiName);
      return apiInstanceMetaData;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Map<String, List<String>> parseQueryString(String queryString)
   {
      Map<String, List<String>> paramMap = new LinkedHashMap<>();
      if(queryString != null)
      {
         for(String nameValuePair : queryString.split("&"))
         {
            String[] nameValue = nameValuePair.split("=", 2);
            if(nameValue.length == 2)
            {
               paramMap.computeIfAbsent(nameValue[0], (k) -> new ArrayList<>());
               paramMap.get(nameValue[0]).add(nameValue[1]);
            }
         }
      }
      return paramMap;
   }
}
