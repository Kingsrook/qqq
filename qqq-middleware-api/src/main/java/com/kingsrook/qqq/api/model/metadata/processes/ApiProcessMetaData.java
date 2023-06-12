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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.ApiSupplementType;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.openapi.HttpMethod;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessMetaData
{
   private String initialVersion;
   private String finalVersion;

   private String  apiProcessName;
   private Boolean isExcluded;

   private String     path;
   private HttpMethod method;

   private List<QFieldMetaData> inputFields;
   private List<QFieldMetaData> outputFields;

   private Map<String, QCodeReference> customizers;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ApiProcessMetaData withInferredInputFields(QProcessMetaData processMetaData)
   {
      inputFields = new ArrayList<>();
      for(QStepMetaData stepMetaData : CollectionUtils.nonNullList(processMetaData.getStepList()))
      {
         if(stepMetaData instanceof QFrontendStepMetaData frontendStep)
         {
            inputFields.addAll(frontendStep.getInputFields());
         }
      }

      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ApiProcessMetaData withInferredOutputFields(QProcessMetaData processMetaData)
   {
      outputFields = new ArrayList<>();
      for(QStepMetaData stepMetaData : CollectionUtils.nonNullList(processMetaData.getStepList()))
      {
         if(stepMetaData instanceof QFrontendStepMetaData frontendStep)
         {
            outputFields.addAll(frontendStep.getOutputFields());
         }
      }

      return (this);
   }



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
   @SuppressWarnings("unchecked")
   public void enrich(String apiName, QProcessMetaData process)
   {
      if(!StringUtils.hasContent(getApiProcessName()))
      {
         setApiProcessName(process.getName());
      }

      if(initialVersion != null)
      {
         ///////////////////////////////////////////////////////////////
         // make sure all fields have at least an initial version set //
         ///////////////////////////////////////////////////////////////
         for(QFieldMetaData field : CollectionUtils.mergeLists(getInputFields(), getOutputFields()))
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
    ** Fluent setter for a single outputField
    *******************************************************************************/
   public ApiProcessMetaData withOutputField(QFieldMetaData outputField)
   {
      if(this.outputFields == null)
      {
         this.outputFields = new ArrayList<>();
      }
      this.outputFields.add(outputField);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single inputField
    *******************************************************************************/
   public ApiProcessMetaData withInputField(QFieldMetaData inputField)
   {
      if(this.inputFields == null)
      {
         this.inputFields = new ArrayList<>();
      }
      this.inputFields.add(inputField);
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
   public ApiProcessMetaData withInitialVersion(String initialVersion)
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
   public ApiProcessMetaData withFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiProcessName
    *******************************************************************************/
   public String getApiProcessName()
   {
      return (this.apiProcessName);
   }



   /*******************************************************************************
    ** Setter for apiProcessName
    *******************************************************************************/
   public void setApiProcessName(String apiProcessName)
   {
      this.apiProcessName = apiProcessName;
   }



   /*******************************************************************************
    ** Fluent setter for apiProcessName
    *******************************************************************************/
   public ApiProcessMetaData withApiProcessName(String apiProcessName)
   {
      this.apiProcessName = apiProcessName;
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
   public ApiProcessMetaData withIsExcluded(Boolean isExcluded)
   {
      this.isExcluded = isExcluded;
      return (this);
   }



   /*******************************************************************************
    ** Getter for method
    *******************************************************************************/
   public HttpMethod getMethod()
   {
      return (this.method);
   }



   /*******************************************************************************
    ** Setter for method
    *******************************************************************************/
   public void setMethod(HttpMethod method)
   {
      this.method = method;
   }



   /*******************************************************************************
    ** Fluent setter for method
    *******************************************************************************/
   public ApiProcessMetaData withMethod(HttpMethod method)
   {
      this.method = method;
      return (this);
   }



   /*******************************************************************************
    ** Getter for path
    *******************************************************************************/
   public String getPath()
   {
      return (this.path);
   }



   /*******************************************************************************
    ** Setter for path
    *******************************************************************************/
   public void setPath(String path)
   {
      this.path = path;
   }



   /*******************************************************************************
    ** Fluent setter for path
    *******************************************************************************/
   public ApiProcessMetaData withPath(String path)
   {
      this.path = path;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputFields
    *******************************************************************************/
   public List<QFieldMetaData> getInputFields()
   {
      return (this.inputFields);
   }



   /*******************************************************************************
    ** Setter for inputFields
    *******************************************************************************/
   public void setInputFields(List<QFieldMetaData> inputFields)
   {
      this.inputFields = inputFields;
   }



   /*******************************************************************************
    ** Fluent setter for inputFields
    *******************************************************************************/
   public ApiProcessMetaData withInputFields(List<QFieldMetaData> inputFields)
   {
      this.inputFields = inputFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputFields
    *******************************************************************************/
   public List<QFieldMetaData> getOutputFields()
   {
      return (this.outputFields);
   }



   /*******************************************************************************
    ** Setter for outputFields
    *******************************************************************************/
   public void setOutputFields(List<QFieldMetaData> outputFields)
   {
      this.outputFields = outputFields;
   }



   /*******************************************************************************
    ** Fluent setter for outputFields
    *******************************************************************************/
   public ApiProcessMetaData withOutputFields(List<QFieldMetaData> outputFields)
   {
      this.outputFields = outputFields;
      return (this);
   }



   /*******************************************************************************
    ** Getter for customizers
    *******************************************************************************/
   public Map<String, QCodeReference> getCustomizers()
   {
      return (this.customizers);
   }



   /*******************************************************************************
    ** Setter for customizers
    *******************************************************************************/
   public void setCustomizers(Map<String, QCodeReference> customizers)
   {
      this.customizers = customizers;
   }



   /*******************************************************************************
    ** Fluent setter for customizers
    *******************************************************************************/
   public ApiProcessMetaData withCustomizers(Map<String, QCodeReference> customizers)
   {
      this.customizers = customizers;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public ApiProcessMetaData withCustomizer(String role, QCodeReference customizer)
   {
      if(this.customizers == null)
      {
         this.customizers = new HashMap<>();
      }

      if(this.customizers.containsKey(role))
      {
         throw (new IllegalArgumentException("Attempt to add a second customizer with role [" + role + "] to apiProcess [" + apiProcessName + "]."));
      }
      this.customizers.put(role, customizer);
      return (this);
   }

}
