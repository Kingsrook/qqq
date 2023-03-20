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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.RemovedApiFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QMiddlewareTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiTableMetaData extends QMiddlewareTableMetaData
{
   private String initialVersion;
   private String finalVersion;

   private List<RemovedApiFieldMetaData> removedApiFields;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void enrich(QTableMetaData table)
   {
      super.enrich(table);

      if(initialVersion != null)
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            if(field.getMiddlewareMetaData(ApiMiddlewareType.NAME) == null)
            {
               field.withMiddlewareMetaData(new ApiFieldMetaData());
            }

            ApiFieldMetaData apiFieldMetaData = (ApiFieldMetaData) field.getMiddlewareMetaData(ApiMiddlewareType.NAME);
            if(apiFieldMetaData.getInitialVersion() == null)
            {
               apiFieldMetaData.setInitialVersion(initialVersion);
            }
         }
      }
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiTableMetaData()
   {
      setType("api");
   }



   /*******************************************************************************
    ** Getter for removedApiFields
    *******************************************************************************/
   public List<RemovedApiFieldMetaData> getRemovedApiFields()
   {
      return (this.removedApiFields);
   }



   /*******************************************************************************
    ** Setter for removedApiFields
    *******************************************************************************/
   public void setRemovedApiFields(List<RemovedApiFieldMetaData> removedApiFields)
   {
      this.removedApiFields = removedApiFields;
   }



   /*******************************************************************************
    ** Fluent setter for removedApiFields
    *******************************************************************************/
   public ApiTableMetaData withRemovedApiFields(List<RemovedApiFieldMetaData> removedApiFields)
   {
      this.removedApiFields = removedApiFields;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single removedApiField
    *******************************************************************************/
   public ApiTableMetaData withRemovedApiField(RemovedApiFieldMetaData removedApiField)
   {
      if(this.removedApiFields == null)
      {
         this.removedApiFields = new ArrayList<>();
      }
      this.removedApiFields.add(removedApiField);
      return (this);
   }



   /*******************************************************************************
    ** Getter for initialVersion
    *******************************************************************************/
   public String getInitialVersion()
   {
      return (this.initialVersion);
   }



   /*******************************************************************************
    ** Setter for initialVersion
    *******************************************************************************/
   public void setInitialVersion(String initialVersion)
   {
      this.initialVersion = initialVersion;
   }



   /*******************************************************************************
    ** Fluent setter for initialVersion
    *******************************************************************************/
   public ApiTableMetaData withInitialVersion(String initialVersion)
   {
      this.initialVersion = initialVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for finalVersion
    *******************************************************************************/
   public String getFinalVersion()
   {
      return (this.finalVersion);
   }



   /*******************************************************************************
    ** Setter for finalVersion
    *******************************************************************************/
   public void setFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
   }



   /*******************************************************************************
    ** Fluent setter for finalVersion
    *******************************************************************************/
   public ApiTableMetaData withFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
      return (this);
   }

}
