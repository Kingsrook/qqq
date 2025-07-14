/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.dashboard.widgets;


import java.util.List;


/*******************************************************************************
 ** Model containing datastructure expected by frontend filter and columns setup widget
 **
 *******************************************************************************/
public class FilterAndColumnsSetupData extends QWidgetData
{
   private String       tableName;
   private Boolean      allowVariables = false;
   private Boolean      hideColumns    = false;
   private Boolean      hidePreview    = false;
   private Boolean      hideSortBy     = false;
   private Boolean      overrideIsEditable;
   private List<String> filterDefaultFieldNames;
   private List<String> omitExposedJoins;

   private Boolean isApiVersioned = false;
   private String  apiName;
   private String  apiPath;
   private String  apiVersion;

   private String filterFieldName = "queryFilterJson";
   private String columnFieldName = "columnsJson";



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilterAndColumnsSetupData()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public FilterAndColumnsSetupData(String tableName, Boolean allowVariables, Boolean hideColumns, List<String> filterDefaultFieldNames)
   {
      this.tableName = tableName;
      this.allowVariables = allowVariables;
      this.hideColumns = hideColumns;
      this.filterDefaultFieldNames = filterDefaultFieldNames;
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.FILTER_AND_COLUMNS_SETUP.getType();
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public FilterAndColumnsSetupData withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hideColumns
    *******************************************************************************/
   public Boolean getHideColumns()
   {
      return (this.hideColumns);
   }



   /*******************************************************************************
    ** Setter for hideColumns
    *******************************************************************************/
   public void setHideColumns(Boolean hideColumns)
   {
      this.hideColumns = hideColumns;
   }



   /*******************************************************************************
    ** Fluent setter for hideColumns
    *******************************************************************************/
   public FilterAndColumnsSetupData withHideColumns(Boolean hideColumns)
   {
      this.hideColumns = hideColumns;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filterDefaultFieldNames
    *******************************************************************************/
   public List<String> getFilterDefaultFieldNames()
   {
      return (this.filterDefaultFieldNames);
   }



   /*******************************************************************************
    ** Setter for filterDefaultFieldNames
    *******************************************************************************/
   public void setFilterDefaultFieldNames(List<String> filterDefaultFieldNames)
   {
      this.filterDefaultFieldNames = filterDefaultFieldNames;
   }



   /*******************************************************************************
    ** Fluent setter for filterDefaultFieldNames
    *******************************************************************************/
   public FilterAndColumnsSetupData withFilterDefaultFieldNames(List<String> filterDefaultFieldNames)
   {
      this.filterDefaultFieldNames = filterDefaultFieldNames;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allowVariables
    *******************************************************************************/
   public Boolean getAllowVariables()
   {
      return (this.allowVariables);
   }



   /*******************************************************************************
    ** Setter for allowVariables
    *******************************************************************************/
   public void setAllowVariables(Boolean allowVariables)
   {
      this.allowVariables = allowVariables;
   }



   /*******************************************************************************
    ** Fluent setter for allowVariables
    *******************************************************************************/
   public FilterAndColumnsSetupData withAllowVariables(Boolean allowVariables)
   {
      this.allowVariables = allowVariables;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hidePreview
    *******************************************************************************/
   public Boolean getHidePreview()
   {
      return (this.hidePreview);
   }



   /*******************************************************************************
    ** Setter for hidePreview
    *******************************************************************************/
   public void setHidePreview(Boolean hidePreview)
   {
      this.hidePreview = hidePreview;
   }



   /*******************************************************************************
    ** Fluent setter for hidePreview
    *******************************************************************************/
   public FilterAndColumnsSetupData withHidePreview(Boolean hidePreview)
   {
      this.hidePreview = hidePreview;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filterFieldName
    *******************************************************************************/
   public String getFilterFieldName()
   {
      return (this.filterFieldName);
   }



   /*******************************************************************************
    ** Setter for filterFieldName
    *******************************************************************************/
   public void setFilterFieldName(String filterFieldName)
   {
      this.filterFieldName = filterFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for filterFieldName
    *******************************************************************************/
   public FilterAndColumnsSetupData withFilterFieldName(String filterFieldName)
   {
      this.filterFieldName = filterFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for columnFieldName
    *******************************************************************************/
   public String getColumnFieldName()
   {
      return (this.columnFieldName);
   }



   /*******************************************************************************
    ** Setter for columnFieldName
    *******************************************************************************/
   public void setColumnFieldName(String columnFieldName)
   {
      this.columnFieldName = columnFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for columnFieldName
    *******************************************************************************/
   public FilterAndColumnsSetupData withColumnFieldName(String columnFieldName)
   {
      this.columnFieldName = columnFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for overrideIsEditable
    *******************************************************************************/
   public Boolean getOverrideIsEditable()
   {
      return (this.overrideIsEditable);
   }



   /*******************************************************************************
    ** Setter for overrideIsEditable
    *******************************************************************************/
   public void setOverrideIsEditable(Boolean overrideIsEditable)
   {
      this.overrideIsEditable = overrideIsEditable;
   }



   /*******************************************************************************
    ** Fluent setter for overrideIsEditable
    *******************************************************************************/
   public FilterAndColumnsSetupData withOverrideIsEditable(Boolean overrideIsEditable)
   {
      this.overrideIsEditable = overrideIsEditable;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hideSortBy
    *******************************************************************************/
   public Boolean getHideSortBy()
   {
      return (this.hideSortBy);
   }



   /*******************************************************************************
    ** Setter for hideSortBy
    *******************************************************************************/
   public void setHideSortBy(Boolean hideSortBy)
   {
      this.hideSortBy = hideSortBy;
   }



   /*******************************************************************************
    ** Fluent setter for hideSortBy
    *******************************************************************************/
   public FilterAndColumnsSetupData withHideSortBy(Boolean hideSortBy)
   {
      this.hideSortBy = hideSortBy;
      return (this);
   }


   /*******************************************************************************
    ** Getter for isApiVersioned
    *******************************************************************************/
   public Boolean getIsApiVersioned()
   {
      return (this.isApiVersioned);
   }



   /*******************************************************************************
    ** Setter for isApiVersioned
    *******************************************************************************/
   public void setIsApiVersioned(Boolean isApiVersioned)
   {
      this.isApiVersioned = isApiVersioned;
   }



   /*******************************************************************************
    ** Fluent setter for isApiVersioned
    *******************************************************************************/
   public FilterAndColumnsSetupData withIsApiVersioned(Boolean isApiVersioned)
   {
      this.isApiVersioned = isApiVersioned;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiName
    *******************************************************************************/
   public String getApiName()
   {
      return (this.apiName);
   }



   /*******************************************************************************
    ** Setter for apiName
    *******************************************************************************/
   public void setApiName(String apiName)
   {
      this.apiName = apiName;
   }



   /*******************************************************************************
    ** Fluent setter for apiName
    *******************************************************************************/
   public FilterAndColumnsSetupData withApiName(String apiName)
   {
      this.apiName = apiName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiPath
    *******************************************************************************/
   public String getApiPath()
   {
      return (this.apiPath);
   }



   /*******************************************************************************
    ** Setter for apiPath
    *******************************************************************************/
   public void setApiPath(String apiPath)
   {
      this.apiPath = apiPath;
   }



   /*******************************************************************************
    ** Fluent setter for apiPath
    *******************************************************************************/
   public FilterAndColumnsSetupData withApiPath(String apiPath)
   {
      this.apiPath = apiPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiVersion
    *******************************************************************************/
   public String getApiVersion()
   {
      return (this.apiVersion);
   }



   /*******************************************************************************
    ** Setter for apiVersion
    *******************************************************************************/
   public void setApiVersion(String apiVersion)
   {
      this.apiVersion = apiVersion;
   }



   /*******************************************************************************
    ** Fluent setter for apiVersion
    *******************************************************************************/
   public FilterAndColumnsSetupData withApiVersion(String apiVersion)
   {
      this.apiVersion = apiVersion;
      return (this);
   }



   /*******************************************************************************
    * Getter for omitExposedJoins
    * @see #withOmitExposedJoins(List)
    *******************************************************************************/
   public List<String> getOmitExposedJoins()
   {
      return (this.omitExposedJoins);
   }



   /*******************************************************************************
    * Setter for omitExposedJoins
    * @see #withOmitExposedJoins(List)
    *******************************************************************************/
   public void setOmitExposedJoins(List<String> omitExposedJoins)
   {
      this.omitExposedJoins = omitExposedJoins;
   }



   /*******************************************************************************
    * Fluent setter for omitExposedJoins
    *
    * @param omitExposedJoins
    * list of tableNames of exposed joins that shouldn't be available in the filter.
    * @return this
    *******************************************************************************/
   public FilterAndColumnsSetupData withOmitExposedJoins(List<String> omitExposedJoins)
   {
      this.omitExposedJoins = omitExposedJoins;
      return (this);
   }


}
