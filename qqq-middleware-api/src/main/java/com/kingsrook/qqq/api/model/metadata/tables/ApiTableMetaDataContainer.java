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
import com.kingsrook.qqq.backend.core.model.metadata.tables.QSupplementalTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiTableMetaDataContainer extends QSupplementalTableMetaData
{
   private Map<String, ApiTableMetaData> apis;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiTableMetaDataContainer()
   {
      setType("api");
   }



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
   @Override
   public void enrich(QTableMetaData table)
   {
      super.enrich(table);

      for(Map.Entry<String, ApiTableMetaData> entry : CollectionUtils.nonNullMap(apis).entrySet())
      {
         entry.getValue().enrich(entry.getKey(), table);
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

}
