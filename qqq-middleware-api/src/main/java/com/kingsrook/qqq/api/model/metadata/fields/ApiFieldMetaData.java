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


import com.kingsrook.qqq.api.ApiMiddlewareType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QMiddlewareFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiFieldMetaData extends QMiddlewareFieldMetaData
{
   private String initialVersion;
   private String finalVersion;

   private String apiFieldName;

   private Boolean isExcluded;
   private String  replacedByFieldName;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ApiFieldMetaData()
   {
      setType("api");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static ApiFieldMetaData of(QFieldMetaData field)
   {
      return ((ApiFieldMetaData) field.getMiddlewareMetaData(ApiMiddlewareType.NAME));
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

}
