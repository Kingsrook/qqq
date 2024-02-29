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
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.ParentWidgetMetaData;


/*******************************************************************************
 ** Model containing datastructure expected by frontend parent widget
 **
 *******************************************************************************/
public class ParentWidgetData extends QWidgetData
{
   private List<String> childWidgetNameList;
   private ParentWidgetMetaData.LayoutType layoutType = ParentWidgetMetaData.LayoutType.GRID;

   private boolean isLabelPageTitle = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ParentWidgetData()
   {
   }



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public String getType()
   {
      return WidgetType.PARENT_WIDGET.getType();
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
   public ParentWidgetData withChildWidgetNameList(List<String> childWidgetNameList)
   {
      this.childWidgetNameList = childWidgetNameList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for layoutType
    *******************************************************************************/
   public ParentWidgetMetaData.LayoutType getLayoutType()
   {
      return (this.layoutType);
   }



   /*******************************************************************************
    ** Setter for layoutType
    *******************************************************************************/
   public void setLayoutType(ParentWidgetMetaData.LayoutType layoutType)
   {
      this.layoutType = layoutType;
   }



   /*******************************************************************************
    ** Fluent setter for layoutType
    *******************************************************************************/
   public ParentWidgetData withLayoutType(ParentWidgetMetaData.LayoutType layoutType)
   {
      this.layoutType = layoutType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for isLabelPageTitle
    *******************************************************************************/
   public boolean getIsLabelPageTitle()
   {
      return (this.isLabelPageTitle);
   }



   /*******************************************************************************
    ** Setter for isLabelPageTitle
    *******************************************************************************/
   public void setIsLabelPageTitle(boolean isLabelPageTitle)
   {
      this.isLabelPageTitle = isLabelPageTitle;
   }



   /*******************************************************************************
    ** Fluent setter for isLabelPageTitle
    *******************************************************************************/
   public ParentWidgetData withIsLabelPageTitle(boolean isLabelPageTitle)
   {
      this.isLabelPageTitle = isLabelPageTitle;
      return (this);
   }

}
