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

package com.kingsrook.qqq.backend.core.model.metadata.tables;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.kingsrook.qqq.backend.core.instances.QInstanceHelpContentManager;
import com.kingsrook.qqq.backend.core.model.metadata.help.HelpRole;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.utils.collections.MutableList;


/*******************************************************************************
 ** A section of fields - a logical grouping.
 ** TODO - this class should be named QTableSection!
 *******************************************************************************/
public class QFieldSection
{
   private String name;
   private String label;
   private Tier   tier;

   private List<String> fieldNames;
   private String       widgetName;
   private QIcon        icon;

   private boolean isHidden = false;
   private Integer gridColumns;

   private List<QHelpContent> helpContents;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldSection()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldSection(String name, String label, QIcon icon, Tier tier, List<String> fieldNames)
   {
      this.name = name;
      this.label = label;
      this.icon = icon;
      this.tier = tier;
      this.fieldNames = fieldNames == null ? null : new MutableList<>(fieldNames);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldSection(String name, QIcon icon, Tier tier, List<String> fieldNames)
   {
      this.name = name;
      this.icon = icon;
      this.tier = tier;
      this.fieldNames = fieldNames == null ? null : new MutableList<>(fieldNames);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFieldSection(String name, QIcon icon, Tier tier)
   {
      this.name = name;
      this.icon = icon;
      this.tier = tier;
   }



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
   public QFieldSection withName(String name)
   {
      this.name = name;
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
   public QFieldSection withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tier
    **
    *******************************************************************************/
   public Tier getTier()
   {
      return tier;
   }



   /*******************************************************************************
    ** Setter for tier
    **
    *******************************************************************************/
   public void setTier(Tier tier)
   {
      this.tier = tier;
   }



   /*******************************************************************************
    ** Fluent setter for tier
    **
    *******************************************************************************/
   public QFieldSection withTier(Tier tier)
   {
      this.tier = tier;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldNames
    **
    *******************************************************************************/
   public List<String> getFieldNames()
   {
      return fieldNames;
   }



   /*******************************************************************************
    ** Setter for fieldNames
    **
    *******************************************************************************/
   public void setFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames == null ? null : new MutableList<>(fieldNames);
   }



   /*******************************************************************************
    ** Fluent setter for fieldNames
    **
    *******************************************************************************/
   public QFieldSection withFieldNames(List<String> fieldNames)
   {
      this.fieldNames = fieldNames;
      return (this);
   }



   /*******************************************************************************
    ** Getter for icon
    **
    *******************************************************************************/
   public QIcon getIcon()
   {
      return icon;
   }



   /*******************************************************************************
    ** Setter for icon
    **
    *******************************************************************************/
   public void setIcon(QIcon icon)
   {
      this.icon = icon;
   }



   /*******************************************************************************
    ** Fluent setter for icon
    **
    *******************************************************************************/
   public QFieldSection withIcon(QIcon icon)
   {
      this.icon = icon;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isHidden
    **
    *******************************************************************************/
   public boolean getIsHidden()
   {
      return (isHidden);
   }



   /*******************************************************************************
    ** Setter for isHidden
    **
    *******************************************************************************/
   public void setIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
   }



   /*******************************************************************************
    ** Fluent Setter for isHidden
    **
    *******************************************************************************/
   public QFieldSection withIsHidden(boolean isHidden)
   {
      this.isHidden = isHidden;
      return (this);
   }



   /*******************************************************************************
    ** Getter for widgetName
    **
    *******************************************************************************/
   public String getWidgetName()
   {
      return widgetName;
   }



   /*******************************************************************************
    ** Setter for widgetName
    **
    *******************************************************************************/
   public void setWidgetName(String widgetName)
   {
      this.widgetName = widgetName;
   }



   /*******************************************************************************
    ** Fluent setter for widgetName
    **
    *******************************************************************************/
   public QFieldSection withWidgetName(String widgetName)
   {
      this.widgetName = widgetName;
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
   public QFieldSection withGridColumns(Integer gridColumns)
   {
      this.gridColumns = gridColumns;
      return (this);
   }



   /*******************************************************************************
    ** Getter for helpContents
    *******************************************************************************/
   public List<QHelpContent> getHelpContents()
   {
      return (this.helpContents);
   }



   /*******************************************************************************
    ** Setter for helpContents
    *******************************************************************************/
   public void setHelpContents(List<QHelpContent> helpContents)
   {
      this.helpContents = helpContents;
   }



   /*******************************************************************************
    ** Fluent setter for helpContents
    *******************************************************************************/
   public QFieldSection withHelpContents(List<QHelpContent> helpContents)
   {
      this.helpContents = helpContents;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for adding 1 helpContent
    *******************************************************************************/
   public QFieldSection withHelpContent(QHelpContent helpContent)
   {
      if(this.helpContents == null)
      {
         this.helpContents = new ArrayList<>();
      }

      QInstanceHelpContentManager.putHelpContentInList(helpContent, this.helpContents);
      return (this);
   }



   /*******************************************************************************
    ** remove a single helpContent based on its set of roles
    *******************************************************************************/
   public void removeHelpContent(Set<HelpRole> roles)
   {
      QInstanceHelpContentManager.removeHelpContentByRoleSetFromList(roles, this.helpContents);
   }

}
