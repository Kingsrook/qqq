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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.kingsrook.qqq.api.ApiSupplementType;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.metadata.ApiOperation;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiTableMetaData implements ApiOperation.EnabledOperationsProvider
{
   private String initialVersion;
   private String finalVersion;

   private String  apiTableName;
   private Boolean isExcluded;

   private List<QFieldMetaData> removedApiFields;

   private Set<ApiOperation> enabledOperations  = new HashSet<>();
   private Set<ApiOperation> disabledOperations = new HashSet<>();

   private Map<String, ApiAssociationMetaData> apiAssociationMetaData = new HashMap<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   public APIVersionRange getApiVersionRange()
   {
      if(getInitialVersion() == null)
      {
         return APIVersionRange.none();
      }

      return (getFinalVersion() != null
         ? APIVersionRange.betweenAndIncluding(getInitialVersion(), getFinalVersion())
         : APIVersionRange.afterAndIncluding(getInitialVersion()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void enrich(String apiName, QTableMetaData table)
   {
      if(initialVersion != null)
      {
         for(QFieldMetaData field : table.getFields().values())
         {
            ApiFieldMetaData apiFieldMetaData = ensureFieldHasApiSupplementalMetaData(apiName, field);
            if(apiFieldMetaData.getInitialVersion() == null)
            {
               apiFieldMetaData.setInitialVersion(initialVersion);
            }
         }

         for(QFieldMetaData field : CollectionUtils.nonNullList(removedApiFields))
         {
            ApiFieldMetaData apiFieldMetaData = ensureFieldHasApiSupplementalMetaData(apiName, field);
            if(apiFieldMetaData.getInitialVersion() == null)
            {
               apiFieldMetaData.setInitialVersion(initialVersion);
            }
         }
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static ApiFieldMetaData ensureFieldHasApiSupplementalMetaData(String apiName, QFieldMetaData field)
   {
      if(field.getSupplementalMetaData(ApiSupplementType.NAME) == null)
      {
         field.withSupplementalMetaData(new ApiFieldMetaDataContainer());
      }

      ApiFieldMetaDataContainer apiFieldMetaDataContainer = ApiFieldMetaDataContainer.of(field);
      if(apiFieldMetaDataContainer.getApiFieldMetaData(apiName) == null)
      {
         apiFieldMetaDataContainer.withApiFieldMetaData(apiName, new ApiFieldMetaData());
      }

      return (apiFieldMetaDataContainer.getApiFieldMetaData(apiName));
   }



   /*******************************************************************************
    ** Getter for removedApiFields
    *******************************************************************************/
   public List<QFieldMetaData> getRemovedApiFields()
   {
      return (this.removedApiFields);
   }



   /*******************************************************************************
    ** Setter for removedApiFields
    *******************************************************************************/
   public void setRemovedApiFields(List<QFieldMetaData> removedApiFields)
   {
      this.removedApiFields = removedApiFields;
   }



   /*******************************************************************************
    ** Fluent setter for removedApiFields
    *******************************************************************************/
   public ApiTableMetaData withRemovedApiFields(List<QFieldMetaData> removedApiFields)
   {
      this.removedApiFields = removedApiFields;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single removedApiField
    *******************************************************************************/
   public ApiTableMetaData withRemovedApiField(QFieldMetaData removedApiField)
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



   /*******************************************************************************
    ** Getter for apiTableName
    *******************************************************************************/
   public String getApiTableName()
   {
      return (this.apiTableName);
   }



   /*******************************************************************************
    ** Setter for apiTableName
    *******************************************************************************/
   public void setApiTableName(String apiTableName)
   {
      this.apiTableName = apiTableName;
   }



   /*******************************************************************************
    ** Fluent setter for apiTableName
    *******************************************************************************/
   public ApiTableMetaData withApiTableName(String apiTableName)
   {
      this.apiTableName = apiTableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isExcluded
    *******************************************************************************/
   public Boolean getIsExcluded()
   {
      return (this.isExcluded);
   }



   /*******************************************************************************
    ** Setter for isExcluded
    *******************************************************************************/
   public void setIsExcluded(Boolean isExcluded)
   {
      this.isExcluded = isExcluded;
   }



   /*******************************************************************************
    ** Fluent setter for isExcluded
    *******************************************************************************/
   public ApiTableMetaData withIsExcluded(Boolean isExcluded)
   {
      this.isExcluded = isExcluded;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Set<ApiOperation> getEnabledOperations()
   {
      return (enabledOperations);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Set<ApiOperation> getDisabledOperations()
   {
      return (disabledOperations);
   }



   /*******************************************************************************
    ** Setter for enabledOperations
    *******************************************************************************/
   public void setEnabledOperations(Set<ApiOperation> enabledOperations)
   {
      this.enabledOperations = enabledOperations;
   }



   /*******************************************************************************
    ** Fluent setter for enabledOperations
    *******************************************************************************/
   public ApiTableMetaData withEnabledOperations(Set<ApiOperation> enabledOperations)
   {
      this.enabledOperations = enabledOperations;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for enabledOperations
    *******************************************************************************/
   public ApiTableMetaData withEnabledOperation(ApiOperation operation)
   {
      return withEnabledOperations(operation);
   }



   /*******************************************************************************
    ** Fluent setter for enabledOperations
    *******************************************************************************/
   public ApiTableMetaData withEnabledOperations(ApiOperation... operations)
   {
      if(this.enabledOperations == null)
      {
         this.enabledOperations = new HashSet<>();
      }
      if(operations != null)
      {
         Collections.addAll(this.enabledOperations, operations);
      }
      return (this);
   }



   /*******************************************************************************
    ** Setter for disabledOperations
    *******************************************************************************/
   public void setDisabledOperations(Set<ApiOperation> disabledOperations)
   {
      this.disabledOperations = disabledOperations;
   }



   /*******************************************************************************
    ** Fluent setter for disabledOperations
    *******************************************************************************/
   public ApiTableMetaData withDisabledOperations(Set<ApiOperation> disabledOperations)
   {
      this.disabledOperations = disabledOperations;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for disabledOperations
    *******************************************************************************/
   public ApiTableMetaData withDisabledOperation(ApiOperation operation)
   {
      return withDisabledOperations(operation);
   }



   /*******************************************************************************
    ** Fluent setter for disabledOperations
    *******************************************************************************/
   public ApiTableMetaData withDisabledOperations(ApiOperation... operations)
   {
      if(this.disabledOperations == null)
      {
         this.disabledOperations = new HashSet<>();
      }
      if(operations != null)
      {
         Collections.addAll(this.disabledOperations, operations);
      }
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiAssociationMetaData
    *******************************************************************************/
   public Map<String, ApiAssociationMetaData> getApiAssociationMetaData()
   {
      return (this.apiAssociationMetaData);
   }



   /*******************************************************************************
    ** Setter for apiAssociationMetaData
    *******************************************************************************/
   public void setApiAssociationMetaData(Map<String, ApiAssociationMetaData> apiAssociationMetaData)
   {
      this.apiAssociationMetaData = apiAssociationMetaData;
   }



   /*******************************************************************************
    ** Fluent setter for apiAssociationMetaData
    *******************************************************************************/
   public ApiTableMetaData withApiAssociationMetaData(Map<String, ApiAssociationMetaData> apiAssociationMetaData)
   {
      this.apiAssociationMetaData = apiAssociationMetaData;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for apiAssociationMetaData
    *******************************************************************************/
   public ApiTableMetaData withApiAssociationMetaData(String associationName, ApiAssociationMetaData apiAssociationMetaData)
   {
      if(this.apiAssociationMetaData == null)
      {
         this.apiAssociationMetaData = new HashMap<>();
      }
      this.apiAssociationMetaData.put(associationName, apiAssociationMetaData);
      return (this);
   }

}
