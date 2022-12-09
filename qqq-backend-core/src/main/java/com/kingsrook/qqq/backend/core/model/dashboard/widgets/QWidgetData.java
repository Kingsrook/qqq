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
import java.util.Map;


/*******************************************************************************
 ** Base class for the data returned by rendering a Widget.
 **
 *******************************************************************************/
public abstract class QWidgetData
{
   private List<String> dropdownNameList;
   private List<String> dropdownLabelList;

   /////////////////////////////////////////////////////////////////////////////////////////
   // this is a list of lists, the outer list corresponds to each dropdown (parallel list //
   // with the above dropdownLabelList) - the inner list is the list of actual dropdown   //
   // options                                                                             //
   /////////////////////////////////////////////////////////////////////////////////////////
   private List<List<Map<String, String>>> dropdownDataList;
   private String                          dropdownNeedsSelectedText;



   /*******************************************************************************
    ** Getter for type
    *******************************************************************************/
   public abstract String getType();



   /*******************************************************************************
    ** Getter for dropdownLabelList
    **
    *******************************************************************************/
   public List<String> getDropdownLabelList()
   {
      return dropdownLabelList;
   }



   /*******************************************************************************
    ** Setter for dropdownLabelList
    **
    *******************************************************************************/
   public void setDropdownLabelList(List<String> dropdownLabelList)
   {
      this.dropdownLabelList = dropdownLabelList;
   }



   /*******************************************************************************
    ** Fluent setter for dropdownLabelList
    **
    *******************************************************************************/
   public QWidgetData withDropdownLabelList(List<String> dropdownLabelList)
   {
      this.dropdownLabelList = dropdownLabelList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdownNameList
    **
    *******************************************************************************/
   public List<String> getDropdownNameList()
   {
      return dropdownNameList;
   }



   /*******************************************************************************
    ** Setter for dropdownNameList
    **
    *******************************************************************************/
   public void setDropdownNameList(List<String> dropdownNameList)
   {
      this.dropdownNameList = dropdownNameList;
   }



   /*******************************************************************************
    ** Fluent setter for dropdownNameList
    **
    *******************************************************************************/
   public QWidgetData withDropdownNameList(List<String> dropdownNameList)
   {
      this.dropdownNameList = dropdownNameList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdownDataList
    **
    *******************************************************************************/
   public List<List<Map<String, String>>> getDropdownDataList()
   {
      return dropdownDataList;
   }



   /*******************************************************************************
    ** Setter for dropdownDataList
    **
    *******************************************************************************/
   public void setDropdownDataList(List<List<Map<String, String>>> dropdownDataList)
   {
      this.dropdownDataList = dropdownDataList;
   }



   /*******************************************************************************
    ** Fluent setter for dropdownDataList
    **
    *******************************************************************************/
   public QWidgetData withDropdownDataList(List<List<Map<String, String>>> dropdownDataList)
   {
      this.dropdownDataList = dropdownDataList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for dropdownNeedsSelectedText
    **
    *******************************************************************************/
   public String getDropdownNeedsSelectedText()
   {
      return dropdownNeedsSelectedText;
   }



   /*******************************************************************************
    ** Setter for dropdownNeedsSelectedText
    **
    *******************************************************************************/
   public void setDropdownNeedsSelectedText(String dropdownNeedsSelectedText)
   {
      this.dropdownNeedsSelectedText = dropdownNeedsSelectedText;
   }



   /*******************************************************************************
    ** Fluent setter for dropdownNeedsSelectedText
    **
    *******************************************************************************/
   public QWidgetData withDropdownNeedsSelectedText(String dropdownNeedsSelectedText)
   {
      this.dropdownNeedsSelectedText = dropdownNeedsSelectedText;
      return (this);
   }
}