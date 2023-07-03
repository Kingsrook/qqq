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

package com.kingsrook.qqq.api.model.metadata.fields;


import java.util.Map;
import com.kingsrook.qqq.api.model.openapi.Example;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiFieldMetaData
{
   private String initialVersion;
   private String finalVersion;

   private String apiFieldName;
   private String description;

   private Boolean        isExcluded;
   private String         replacedByFieldName;
   private QCodeReference customValueMapper;

   private Example              example;
   private Map<String, Example> examples;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getEffectiveApiFieldName(String apiName, QFieldMetaData field)
   {
      ApiFieldMetaDataContainer apiFieldMetaDataContainer = ApiFieldMetaDataContainer.of(field);
      if(apiFieldMetaDataContainer != null)
      {
         ApiFieldMetaData apiFieldMetaData = apiFieldMetaDataContainer.getApiFieldMetaData(apiName);
         if(apiFieldMetaData != null && StringUtils.hasContent(apiFieldMetaData.apiFieldName))
         {
            return (apiFieldMetaData.apiFieldName);
         }
      }

      return (field.getName());
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
   public ApiFieldMetaData withInitialVersion(String initialVersion)
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
   public ApiFieldMetaData withFinalVersion(String finalVersion)
   {
      this.finalVersion = finalVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for replacedByFieldName
    *******************************************************************************/
   public String getReplacedByFieldName()
   {
      return (this.replacedByFieldName);
   }



   /*******************************************************************************
    ** Setter for replacedByFieldName
    *******************************************************************************/
   public void setReplacedByFieldName(String replacedByFieldName)
   {
      this.replacedByFieldName = replacedByFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for replacedByFieldName
    *******************************************************************************/
   public ApiFieldMetaData withReplacedByFieldName(String replacedByFieldName)
   {
      this.replacedByFieldName = replacedByFieldName;
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
   public ApiFieldMetaData withIsExcluded(Boolean isExcluded)
   {
      this.isExcluded = isExcluded;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiFieldName
    *******************************************************************************/
   public String getApiFieldName()
   {
      return (this.apiFieldName);
   }



   /*******************************************************************************
    ** Setter for apiFieldName
    *******************************************************************************/
   public void setApiFieldName(String apiFieldName)
   {
      this.apiFieldName = apiFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for apiFieldName
    *******************************************************************************/
   public ApiFieldMetaData withApiFieldName(String apiFieldName)
   {
      this.apiFieldName = apiFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public ApiFieldMetaData withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for example
    *******************************************************************************/
   public Example getExample()
   {
      return (this.example);
   }



   /*******************************************************************************
    ** Setter for example
    *******************************************************************************/
   public void setExample(Example example)
   {
      this.example = example;
   }



   /*******************************************************************************
    ** Fluent setter for example
    *******************************************************************************/
   public ApiFieldMetaData withExample(Example example)
   {
      this.example = example;
      return (this);
   }



   /*******************************************************************************
    ** Getter for examples
    *******************************************************************************/
   public Map<String, Example> getExamples()
   {
      return (this.examples);
   }



   /*******************************************************************************
    ** Setter for examples
    *******************************************************************************/
   public void setExamples(Map<String, Example> examples)
   {
      this.examples = examples;
   }



   /*******************************************************************************
    ** Fluent setter for examples
    *******************************************************************************/
   public ApiFieldMetaData withExamples(Map<String, Example> examples)
   {
      this.examples = examples;
      return (this);
   }



   /*******************************************************************************
    ** Getter for customValueMapper
    *******************************************************************************/
   public QCodeReference getCustomValueMapper()
   {
      return (this.customValueMapper);
   }



   /*******************************************************************************
    ** Setter for customValueMapper
    *******************************************************************************/
   public void setCustomValueMapper(QCodeReference customValueMapper)
   {
      this.customValueMapper = customValueMapper;
   }



   /*******************************************************************************
    ** Fluent setter for customValueMapper
    *******************************************************************************/
   public ApiFieldMetaData withCustomValueMapper(QCodeReference customValueMapper)
   {
      this.customValueMapper = customValueMapper;
      return (this);
   }

}
