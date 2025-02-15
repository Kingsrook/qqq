/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.metadata.variants;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;


/*******************************************************************************
 ** Configs for how a backend that uses variants works.  Specifically:
 **
 ** - the variant "type key" - e.g., key for variants map in session.
 ** - what table supplies the variant options (optionsTableName
 ** - an optional filter to apply to that options table
 ** - a map of the settings that a backend gets from its variant table to the
 ** field names in that table that they come from.  e.g., a backend may have a
 ** username attribute, whose value comes from a field named "theUser" in the
 ** variant options table.
 *******************************************************************************/
public class BackendVariantsConfig
{
   private String variantTypeKey;

   private String       optionsTableName;
   private QQueryFilter optionsFilter;

   private Map<BackendVariantSetting, String> backendSettingSourceFieldNameMap;



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getOptionsTableName()
   {
      return (this.optionsTableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setOptionsTableName(String optionsTableName)
   {
      this.optionsTableName = optionsTableName;
   }



   /*******************************************************************************
    ** Getter for filter
    *******************************************************************************/
   public QQueryFilter getOptionsFilter()
   {
      return (this.optionsFilter);
   }



   /*******************************************************************************
    ** Setter for filter
    *******************************************************************************/
   public void setOptionsFilter(QQueryFilter optionsFilter)
   {
      this.optionsFilter = optionsFilter;
   }



   /*******************************************************************************
    ** Getter for backendSettingSourceFieldNameMap
    *******************************************************************************/
   public Map<BackendVariantSetting, String> getBackendSettingSourceFieldNameMap()
   {
      return (this.backendSettingSourceFieldNameMap);
   }



   /*******************************************************************************
    ** Setter for backendSettingSourceFieldNameMap
    *******************************************************************************/
   public void setBackendSettingSourceFieldNameMap(Map<BackendVariantSetting, String> backendSettingSourceFieldNameMap)
   {
      this.backendSettingSourceFieldNameMap = backendSettingSourceFieldNameMap;
   }



   /*******************************************************************************
    ** Fluent setter for backendSettingSourceFieldNameMap
    *******************************************************************************/
   public BackendVariantsConfig withBackendSettingSourceFieldName(BackendVariantSetting backendVariantSetting, String sourceFieldName)
   {
      if(this.backendSettingSourceFieldNameMap == null)
      {
         this.backendSettingSourceFieldNameMap = new HashMap<>();
      }
      this.backendSettingSourceFieldNameMap.put(backendVariantSetting, sourceFieldName);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for backendSettingSourceFieldNameMap
    *******************************************************************************/
   public BackendVariantsConfig withBackendSettingSourceFieldNameMap(Map<BackendVariantSetting, String> backendSettingSourceFieldNameMap)
   {
      this.backendSettingSourceFieldNameMap = backendSettingSourceFieldNameMap;
      return (this);
   }



   /*******************************************************************************
    ** Getter for variantTypeKey
    *******************************************************************************/
   public String getVariantTypeKey()
   {
      return (this.variantTypeKey);
   }



   /*******************************************************************************
    ** Setter for variantTypeKey
    *******************************************************************************/
   public void setVariantTypeKey(String variantTypeKey)
   {
      this.variantTypeKey = variantTypeKey;
   }



   /*******************************************************************************
    ** Fluent setter for variantTypeKey
    *******************************************************************************/
   public BackendVariantsConfig withVariantTypeKey(String variantTypeKey)
   {
      this.variantTypeKey = variantTypeKey;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for optionsTableName
    *******************************************************************************/
   public BackendVariantsConfig withOptionsTableName(String optionsTableName)
   {
      this.optionsTableName = optionsTableName;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for optionsFilter
    *******************************************************************************/
   public BackendVariantsConfig withOptionsFilter(QQueryFilter optionsFilter)
   {
      this.optionsFilter = optionsFilter;
      return (this);
   }

}
