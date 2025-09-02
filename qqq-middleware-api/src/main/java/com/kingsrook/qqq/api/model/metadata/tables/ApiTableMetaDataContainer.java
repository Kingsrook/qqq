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

package com.kingsrook.qqq.api.model.metadata.tables;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.api.ApiSupplementType;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QNotFoundException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QSupplementalTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiTableMetaDataContainer extends QSupplementalTableMetaData
{
   private static final QLogger LOG = QLogger.getLogger(ApiTableMetaDataContainer.class);

   private Map<String, ApiTableMetaData> apis;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ApiTableMetaDataContainer of(QTableMetaData table)
   {
      return ((ApiTableMetaDataContainer) table.getSupplementalMetaData(ApiSupplementType.NAME));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getType()
   {
      return (ApiSupplementType.NAME);
   }



   /*******************************************************************************
    ** either get the container attached to a table - or create a new one and attach
    ** it to the table, and return that.
    *******************************************************************************/
   public static ApiTableMetaDataContainer ofOrWithNew(QTableMetaData table)
   {
      ApiTableMetaDataContainer apiTableMetaDataContainer = (ApiTableMetaDataContainer) table.getSupplementalMetaData(ApiSupplementType.NAME);
      if(apiTableMetaDataContainer == null)
      {
         apiTableMetaDataContainer = new ApiTableMetaDataContainer();
         table.withSupplementalMetaData(apiTableMetaDataContainer);
      }
      return (apiTableMetaDataContainer);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void enrich(QInstance qInstance, QTableMetaData table)
   {
      super.enrich(qInstance, table);

      for(Map.Entry<String, ApiTableMetaData> entry : CollectionUtils.nonNullMap(apis).entrySet())
      {
         entry.getValue().enrich(qInstance, entry.getKey(), table);
      }
   }



   /*******************************************************************************
    ** Getter for apis
    *******************************************************************************/
   public Map<String, ApiTableMetaData> getApis()
   {
      return (this.apis);
   }



   /*******************************************************************************
    ** Getter for apis
    *******************************************************************************/
   public ApiTableMetaData getApiTableMetaData(String apiName)
   {
      if(this.apis == null)
      {
         return (null);
      }

      return (this.apis.get(apiName));
   }



   /*******************************************************************************
    ** Getter for api
    *******************************************************************************/
   public ApiTableMetaData getOrWithNewApiTableMetaData(String apiName)
   {
      if(this.apis == null)
      {
         this.apis = new LinkedHashMap<>();
      }

      if(!this.apis.containsKey(apiName))
      {
         this.apis.put(apiName, new ApiTableMetaData());
      }

      return (this.apis.get(apiName));
   }



   /*******************************************************************************
    ** Setter for apis
    *******************************************************************************/
   public void setApis(Map<String, ApiTableMetaData> apis)
   {
      this.apis = apis;
   }



   /*******************************************************************************
    ** Fluent setter for apis
    *******************************************************************************/
   public ApiTableMetaDataContainer withApis(Map<String, ApiTableMetaData> apis)
   {
      this.apis = apis;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for apis
    *******************************************************************************/
   public ApiTableMetaDataContainer withApiTableMetaData(String apiName, ApiTableMetaData apiTableMetaData)
   {
      if(this.apis == null)
      {
         this.apis = new LinkedHashMap<>();
      }
      this.apis.put(apiName, apiTableMetaData);
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, QTableMetaData tableMetaData, QInstanceValidator qInstanceValidator)
   {
      super.validate(qInstance, tableMetaData, qInstanceValidator);

      ////////////////////////////////////////
      // iterate over apis this table is in //
      ////////////////////////////////////////
      for(String apiName : CollectionUtils.nonNullMap(apis).keySet())
      {
         ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaDataContainer.of(qInstance).getApis().get(apiName);

         //////////////////////////////////////////////////
         // iterate over supported versions for this api //
         //////////////////////////////////////////////////
         for(APIVersion version : apiInstanceMetaData.getSupportedVersions())
         {
            CapturedContext capturedContext = QContext.capture();
            try
            {
               QContext.setQInstance(qInstance);

               ///////////////////////////////////////////////////////////////////////////////////////////////////
               // try to get the field-map for this table.  note that this will (implicitly) throw an exception //
               // if we have the same field name more than once, which can happen if a field is both in the     //
               // removed-list and the table's normal field list.                                               //
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, version.toString(), tableMetaData.getName()));
            }
            catch(QNotFoundException qnfe)
            {
               /////////////////////////////
               // skip tables not in apis //
               /////////////////////////////
            }
            catch(Exception e)
            {
               String message = "Error validating ApiTableMetaData for table: " + tableMetaData.getName() + ", api: " + apiName + ", version: " + version;
               LOG.warn(message, e);
               qInstanceValidator.getErrors().add(message + ": " + e.getMessage());
            }
            finally
            {
               QContext.init(capturedContext);
            }
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   protected ApiTableMetaDataContainer finishClone(QSupplementalTableMetaData abstractClone)
   {
      ApiTableMetaDataContainer clone = (ApiTableMetaDataContainer) abstractClone;
      if(apis != null)
      {
         clone.apis = new LinkedHashMap<>();
         for(Map.Entry<String, ApiTableMetaData> entry : apis.entrySet())
         {
            clone.apis.put(entry.getKey(), entry.getValue().clone());
         }
      }
      return null;
   }
}
