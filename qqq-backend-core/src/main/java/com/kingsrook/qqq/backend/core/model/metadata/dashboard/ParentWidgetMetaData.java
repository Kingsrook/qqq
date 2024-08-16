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
public class ParentWidgetMetaData extends QWidgetMetaData
{
   private String       title;
   private List<String> childWidgetNameList;
   private List<String> childProcessNameList;

   private LayoutType layoutType = LayoutType.GRID;



   /***************************************************************************
    **
    ***************************************************************************/
   public enum LayoutType
   {
      GRID,
      TABS
   }



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
    ** Getter for layoutType
    *******************************************************************************/
   public LayoutType getLayoutType()
   {
      return (this.layoutType);
   }



   /*******************************************************************************
    ** Setter for layoutType
    *******************************************************************************/
   public void setLayoutType(LayoutType layoutType)
   {
      this.layoutType = layoutType;
   }



   /*******************************************************************************
    ** Fluent setter for layoutType
    *******************************************************************************/
   public ParentWidgetMetaData withLayoutType(LayoutType layoutType)
   {
      this.layoutType = layoutType;
      return (this);
   }


}
