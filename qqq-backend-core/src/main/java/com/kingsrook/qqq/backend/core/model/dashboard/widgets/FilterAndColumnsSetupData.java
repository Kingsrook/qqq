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
   private List<String> filterDefaultFieldNames;



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

}
