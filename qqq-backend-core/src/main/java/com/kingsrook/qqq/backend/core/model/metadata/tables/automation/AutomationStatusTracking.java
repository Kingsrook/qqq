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

package com.kingsrook.qqq.backend.core.model.metadata.tables.automation;


import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;


/*******************************************************************************
 ** Table-automation meta-data to define how this table's per-record automation
 ** status is tracked.
 *******************************************************************************/
public class AutomationStatusTracking implements QMetaDataObject
{
   private AutomationStatusTrackingType type;

   private String fieldName; // used when type is FIELD_IN_TABLE

   // todo - fields for additional types (e.g., 1-1 table, shared-table)



   /*******************************************************************************
    ** Getter for type
    **
    *******************************************************************************/
   public AutomationStatusTrackingType getType()
   {
      return type;
   }



   /*******************************************************************************
    ** Setter for type
    **
    *******************************************************************************/
   public void setType(AutomationStatusTrackingType type)
   {
      this.type = type;
   }



   /*******************************************************************************
    ** Fluent setter for type
    **
    *******************************************************************************/
   public AutomationStatusTracking withType(AutomationStatusTrackingType type)
   {
      this.type = type;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fieldName
    **
    *******************************************************************************/
   public String getFieldName()
   {
      return fieldName;
   }



   /*******************************************************************************
    ** Setter for fieldName
    **
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    **
    *******************************************************************************/
   public AutomationStatusTracking withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }

}
