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

package com.kingsrook.qqq.backend.core.model.metadata.dashboard;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;


/*******************************************************************************
 ** Base metadata for frontend dashboard widgets
 **
 *******************************************************************************/
public class QWidgetMetaData implements QWidgetMetaDataInterface
{
   protected String         name;
   protected String         icon;
   protected String         label;
   protected String         type;
   protected boolean        isCard;
   protected Integer        gridColumns;
   protected QCodeReference codeReference;

   private QPermissionRules permissionRules;

   private   List<WidgetDropdownData>  dropdowns;
   protected Map<String, Serializable> defaultValues = new LinkedHashMap<>();



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public QWidgetMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public String getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(String icon)
   {
      this.icon = icon;
   }



   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QWidgetMetaData withIcon(String icon)
   {
      this.icon = icon;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    **
    *******************************************************************************/
   public QWidgetMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeReference
    **
    *******************************************************************************/
   public QCodeReference getCodeReference()
   {
      return codeReference;
   }



   /*******************************************************************************
    ** Setter for codeReference
    **
    *******************************************************************************/
   public void setCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    **
    *******************************************************************************/
   public QWidgetMetaData withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(String type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public QWidgetMetaData withType(String type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for gridColumns
    **
    *******************************************************************************/
   public Integer getGridColumns()
   {
      return gridColumns;
   }



   /*******************************************************************************
    ** Setter for gridColumns
    **
    *******************************************************************************/
   public void setGridColumns(Integer gridColumns)
   {
      this.gridColumns = gridColumns;
   }



   /*******************************************************************************
    ** Fluent setter for gridColumns
    **
    *******************************************************************************/
   public QWidgetMetaData withGridColumns(Integer gridColumns)
   {
      this.gridColumns = gridColumns;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultValues
    **
    *******************************************************************************/
   public Map<String, Serializable> getDefaultValues()
   {
      return defaultValues;
   }



   /*******************************************************************************
    ** Setter for defaultValues
    **
    *******************************************************************************/
   public void setDefaultValues(Map<String, Serializable> defaultValues)
   {
      this.defaultValues = defaultValues;
   }



   /*******************************************************************************
    ** Fluent setter for defaultValues
    **
    *******************************************************************************/
   public QWidgetMetaData withDefaultValues(Map<String, Serializable> defaultValues)
   {
      this.defaultValues = defaultValues;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single defaultValue
    **
    *******************************************************************************/
   public QWidgetMetaData withDefaultValue(String key, Serializable value)
   {
      if(this.defaultValues == null)
      {
         this.defaultValues = new LinkedHashMap<>();
      }

      this.defaultValues.put(key, value);

      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdowns
    **
    *******************************************************************************/
   public List<WidgetDropdownData> getDropdowns()
   {
      return dropdowns;
   }



   /*******************************************************************************
    ** Setter for dropdowns
    **
    *******************************************************************************/
   public void setDropdowns(List<WidgetDropdownData> dropdowns)
   {
      this.dropdowns = dropdowns;
   }



   /*******************************************************************************
    ** Fluent setter for dropdowns
    **
    *******************************************************************************/
   public QWidgetMetaData withDropdowns(List<WidgetDropdownData> dropdowns)
   {
      this.dropdowns = dropdowns;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for dropdowns
    **
    *******************************************************************************/
   public QWidgetMetaData withDropdown(WidgetDropdownData dropdown)
   {
      if(this.dropdowns == null)
      {
         this.dropdowns = new ArrayList<>();
      }
      this.dropdowns.add(dropdown);
      return (this);
   }



   /*******************************************************************************
    ** Getter for isCard
    **
    *******************************************************************************/
   public boolean getIsCard()
   {
      return isCard;
   }



   /*******************************************************************************
    ** Setter for isCard
    **
    *******************************************************************************/
   public void setIsCard(boolean isCard)
   {
      this.isCard = isCard;
   }



   /*******************************************************************************
    ** Fluent setter for isCard
    **
    *******************************************************************************/
   public QWidgetMetaData withIsCard(boolean isCard)
   {
      this.isCard = isCard;
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionRules
    *******************************************************************************/
   @Override
   public QPermissionRules getPermissionRules()
   {
      return (this.permissionRules);
   }



   /*******************************************************************************
    ** Setter for permissionRules
    *******************************************************************************/
   @Override
   public void setPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
   }



   /*******************************************************************************
    ** Fluent setter for permissionRules
    *******************************************************************************/
   public QWidgetMetaData withPermissionRules(QPermissionRules permissionRules)
   {
      this.permissionRules = permissionRules;
      return (this);
   }

}
