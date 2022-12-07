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


import java.util.List;


/*******************************************************************************
 ** Specific meta data for frontend parent widget
 **
 *******************************************************************************/
public class ParentWidgetMetaData extends QWidgetMetaData implements QWidgetMetaDataInterface
{
   private String             title;
   private List<String>       possibleValueNameList;
   private List<String>       childWidgetNameList;
   private List<String>       childProcessNameList;
   private List<DropdownData> dropdowns;



   /*******************************************************************************
    ** Getter for title
    **
    *******************************************************************************/
   public String getTitle()
   {
      return title;
   }



   /*******************************************************************************
    ** Setter for title
    **
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title
    **
    *******************************************************************************/
   public ParentWidgetMetaData withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for possibleValueNameList
    **
    *******************************************************************************/
   public List<String> getPossibleValueNameList()
   {
      return possibleValueNameList;
   }



   /*******************************************************************************
    ** Setter for possibleValueNameList
    **
    *******************************************************************************/
   public void setPossibleValueNameList(List<String> possibleValueNameList)
   {
      this.possibleValueNameList = possibleValueNameList;
   }



   /*******************************************************************************
    ** Fluent setter for possibleValueNameList
    **
    *******************************************************************************/
   public ParentWidgetMetaData withPossibleValueNameList(List<String> possibleValueNameList)
   {
      this.possibleValueNameList = possibleValueNameList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for childWidgetNameList
    **
    *******************************************************************************/
   public List<String> getChildWidgetNameList()
   {
      return childWidgetNameList;
   }



   /*******************************************************************************
    ** Setter for childWidgetNameList
    **
    *******************************************************************************/
   public void setChildWidgetNameList(List<String> childWidgetNameList)
   {
      this.childWidgetNameList = childWidgetNameList;
   }



   /*******************************************************************************
    ** Fluent setter for childWidgetNameList
    **
    *******************************************************************************/
   public ParentWidgetMetaData withChildWidgetNameList(List<String> childWidgetNameList)
   {
      this.childWidgetNameList = childWidgetNameList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for childProcessNameList
    **
    *******************************************************************************/
   public List<String> getChildProcessNameList()
   {
      return childProcessNameList;
   }



   /*******************************************************************************
    ** Setter for childProcessNameList
    **
    *******************************************************************************/
   public void setChildProcessNameList(List<String> childProcessNameList)
   {
      this.childProcessNameList = childProcessNameList;
   }



   /*******************************************************************************
    ** Fluent setter for childProcessNameList
    **
    *******************************************************************************/
   public ParentWidgetMetaData withChildProcessNameList(List<String> childProcessNameList)
   {
      this.childProcessNameList = childProcessNameList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdowns
    **
    *******************************************************************************/
   public List<DropdownData> getDropdowns()
   {
      return dropdowns;
   }



   /*******************************************************************************
    ** Setter for dropdowns
    **
    *******************************************************************************/
   public void setDropdowns(List<DropdownData> dropdowns)
   {
      this.dropdowns = dropdowns;
   }



   /*******************************************************************************
    ** Fluent setter for dropdowns
    **
    *******************************************************************************/
   public ParentWidgetMetaData withDropdowns(List<DropdownData> dropdowns)
   {
      this.dropdowns = dropdowns;
      return (this);
   }



   /*******************************************************************************
    ** inner class for specifying details about dropdown fields on a parent widget
    **
    *******************************************************************************/
   public static class DropdownData
   {
      private String possibleValueSourceName;
      private String foreignKeyFieldName;
      private String label;



      /*******************************************************************************
       ** Getter for possibleValueSourceName
       **
       *******************************************************************************/
      public String getPossibleValueSourceName()
      {
         return possibleValueSourceName;
      }



      /*******************************************************************************
       ** Setter for possibleValueSourceName
       **
       *******************************************************************************/
      public void setPossibleValueSourceName(String possibleValueSourceName)
      {
         this.possibleValueSourceName = possibleValueSourceName;
      }



      /*******************************************************************************
       ** Fluent setter for possibleValueSourceName
       **
       *******************************************************************************/
      public DropdownData withPossibleValueSourceName(String possibleValueSourceName)
      {
         this.possibleValueSourceName = possibleValueSourceName;
         return (this);
      }



      /*******************************************************************************
       ** Getter for foreignKeyFieldName
       **
       *******************************************************************************/
      public String getForeignKeyFieldName()
      {
         return foreignKeyFieldName;
      }



      /*******************************************************************************
       ** Setter for foreignKeyFieldName
       **
       *******************************************************************************/
      public void setForeignKeyFieldName(String foreignKeyFieldName)
      {
         this.foreignKeyFieldName = foreignKeyFieldName;
      }



      /*******************************************************************************
       ** Fluent setter for foreignKeyFieldName
       **
       *******************************************************************************/
      public DropdownData withForeignKeyFieldName(String foreignKeyFieldName)
      {
         this.foreignKeyFieldName = foreignKeyFieldName;
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
      public DropdownData withLabel(String label)
      {
         this.label = label;
         return (this);
      }

   }

}
