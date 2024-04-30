/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class DynamicFormWidgetData extends QWidgetData
{
   private List<QFieldMetaData> fieldList;

   /////////////////////////////////////////////////////
   // if there are no fields, what message to display //
   /////////////////////////////////////////////////////
   private String noFieldsMessage;

   ///////////////////////////////////////////////////////////////////////////////////
   // what 1 field do we want to combine the dynamic fields into (as a JSON string) //
   ///////////////////////////////////////////////////////////////////////////////////
   private String mergedDynamicFormValuesIntoFieldName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getType()
   {
      return WidgetType.DYNAMIC_FORM.getType();
   }



   /*******************************************************************************
    ** Getter for fieldList
    *******************************************************************************/
   public List<QFieldMetaData> getFieldList()
   {
      return (this.fieldList);
   }



   /*******************************************************************************
    ** Setter for fieldList
    *******************************************************************************/
   public void setFieldList(List<QFieldMetaData> fieldList)
   {
      this.fieldList = fieldList;
   }



   /*******************************************************************************
    ** Fluent setter for fieldList
    *******************************************************************************/
   public DynamicFormWidgetData withFieldList(List<QFieldMetaData> fieldList)
   {
      this.fieldList = fieldList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for noFieldsMessage
    *******************************************************************************/
   public String getNoFieldsMessage()
   {
      return (this.noFieldsMessage);
   }



   /*******************************************************************************
    ** Setter for noFieldsMessage
    *******************************************************************************/
   public void setNoFieldsMessage(String noFieldsMessage)
   {
      this.noFieldsMessage = noFieldsMessage;
   }



   /*******************************************************************************
    ** Fluent setter for noFieldsMessage
    *******************************************************************************/
   public DynamicFormWidgetData withNoFieldsMessage(String noFieldsMessage)
   {
      this.noFieldsMessage = noFieldsMessage;
      return (this);
   }



   /*******************************************************************************
    ** Getter for mergedDynamicFormValuesIntoFieldName
    *******************************************************************************/
   public String getMergedDynamicFormValuesIntoFieldName()
   {
      return (this.mergedDynamicFormValuesIntoFieldName);
   }



   /*******************************************************************************
    ** Setter for mergedDynamicFormValuesIntoFieldName
    *******************************************************************************/
   public void setMergedDynamicFormValuesIntoFieldName(String mergedDynamicFormValuesIntoFieldName)
   {
      this.mergedDynamicFormValuesIntoFieldName = mergedDynamicFormValuesIntoFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for mergedDynamicFormValuesIntoFieldName
    *******************************************************************************/
   public DynamicFormWidgetData withMergedDynamicFormValuesIntoFieldName(String mergedDynamicFormValuesIntoFieldName)
   {
      this.mergedDynamicFormValuesIntoFieldName = mergedDynamicFormValuesIntoFieldName;
      return (this);
   }

}
