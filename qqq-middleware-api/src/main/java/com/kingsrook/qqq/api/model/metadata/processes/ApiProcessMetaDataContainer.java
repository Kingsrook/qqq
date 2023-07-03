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

package com.kingsrook.qqq.api.model.metadata.processes;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.api.ApiSupplementType;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QSupplementalProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessMetaDataContainer extends QSupplementalProcessMetaData
{
   private Map<String, ApiProcessMetaData> apis;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiProcessMetaDataContainer()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ApiProcessMetaDataContainer of(QProcessMetaData process)
   {
      return ((ApiProcessMetaDataContainer) process.getSupplementalMetaData(ApiSupplementType.NAME));
   }



   /*******************************************************************************
    ** either get the container attached to a field - or create a new one and attach
    ** it to the field, and return that.
    *******************************************************************************/
   public static ApiProcessMetaDataContainer ofOrWithNew(QProcessMetaData process)
   {
      ApiProcessMetaDataContainer apiProcessMetaDataContainer = (ApiProcessMetaDataContainer) process.getSupplementalMetaData(ApiSupplementType.NAME);
      if(apiProcessMetaDataContainer == null)
      {
         apiProcessMetaDataContainer = new ApiProcessMetaDataContainer();
         process.withSupplementalMetaData(apiProcessMetaDataContainer);
      }
      return (apiProcessMetaDataContainer);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getType()
   {
      return (ApiSupplementType.NAME);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void enrich(QInstanceEnricher qInstanceEnricher, QProcessMetaData process)
   {
      super.enrich(qInstanceEnricher, process);

      for(Map.Entry<String, ApiProcessMetaData> entry : CollectionUtils.nonNullMap(apis).entrySet())
      {
         entry.getValue().enrich(qInstanceEnricher, entry.getKey(), process);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, QProcessMetaData process, QInstanceValidator qInstanceValidator)
   {
      super.validate(qInstance, process, qInstanceValidator);

      for(Map.Entry<String, ApiProcessMetaData> entry : CollectionUtils.nonNullMap(apis).entrySet())
      {
         entry.getValue().validate(qInstance, process, qInstanceValidator, entry.getKey());
      }
   }



   /*******************************************************************************
    ** Getter for apis
    *******************************************************************************/
   public Map<String, ApiProcessMetaData> getApis()
   {
      return (this.apis);
   }



   /*******************************************************************************
    ** Getter for apis
    *******************************************************************************/
   public ApiProcessMetaData getApiProcessMetaData(String apiName)
   {
      if(this.apis == null)
      {
         return (null);
      }

      return (this.apis.get(apiName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ApiProcessMetaData getApiProcessMetaDataOrWithNew(String apiName)
   {
      ApiProcessMetaData apiProcessMetaData = getApiProcessMetaData(apiName);
      if(apiProcessMetaData == null)
      {
         apiProcessMetaData = new ApiProcessMetaData();
         withApiProcessMetaData(apiName, apiProcessMetaData);
      }
      return (apiProcessMetaData);
   }



   /*******************************************************************************
    ** Setter for apis
    *******************************************************************************/
   public void setApis(Map<String, ApiProcessMetaData> apis)
   {
      this.apis = apis;
   }



   /*******************************************************************************
    ** Fluent setter for apis
    *******************************************************************************/
   public ApiProcessMetaDataContainer withApis(Map<String, ApiProcessMetaData> apis)
   {
      this.apis = apis;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for apis
    *******************************************************************************/
   public ApiProcessMetaDataContainer withApiProcessMetaData(String apiName, ApiProcessMetaData apiProcessMetaData)
   {
      if(this.apis == null)
      {
         this.apis = new LinkedHashMap<>();
      }
      this.apis.put(apiName, apiProcessMetaData);
      return (this);
   }
}
