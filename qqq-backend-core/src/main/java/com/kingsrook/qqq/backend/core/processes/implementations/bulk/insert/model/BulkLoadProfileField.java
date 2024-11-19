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

package com.kingsrook.qqq.backend.core.processes.implementations.bulk.insert.model;


import java.io.Serializable;
import java.util.Map;


/***************************************************************************
 **
 ***************************************************************************/
public class BulkLoadProfileField
{
   private String                    fieldName;
   private Integer                   columnIndex;
   private Serializable              defaultValue;
   private Boolean                   doValueMapping;
   private Map<String, Serializable> valueMappings;



   /*******************************************************************************
    ** Getter for fieldName
    *******************************************************************************/
   public String getFieldName()
   {
      return (this.fieldName);
   }



   /*******************************************************************************
    ** Setter for fieldName
    *******************************************************************************/
   public void setFieldName(String fieldName)
   {
      this.fieldName = fieldName;
   }



   /*******************************************************************************
    ** Fluent setter for fieldName
    *******************************************************************************/
   public BulkLoadProfileField withFieldName(String fieldName)
   {
      this.fieldName = fieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for columnIndex
    *******************************************************************************/
   public Integer getColumnIndex()
   {
      return (this.columnIndex);
   }



   /*******************************************************************************
    ** Setter for columnIndex
    *******************************************************************************/
   public void setColumnIndex(Integer columnIndex)
   {
      this.columnIndex = columnIndex;
   }



   /*******************************************************************************
    ** Fluent setter for columnIndex
    *******************************************************************************/
   public BulkLoadProfileField withColumnIndex(Integer columnIndex)
   {
      this.columnIndex = columnIndex;
      return (this);
   }



   /*******************************************************************************
    ** Getter for defaultValue
    *******************************************************************************/
   public Serializable getDefaultValue()
   {
      return (this.defaultValue);
   }



   /*******************************************************************************
    ** Setter for defaultValue
    *******************************************************************************/
   public void setDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
   }



   /*******************************************************************************
    ** Fluent setter for defaultValue
    *******************************************************************************/
   public BulkLoadProfileField withDefaultValue(Serializable defaultValue)
   {
      this.defaultValue = defaultValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for doValueMapping
    *******************************************************************************/
   public Boolean getDoValueMapping()
   {
      return (this.doValueMapping);
   }



   /*******************************************************************************
    ** Setter for doValueMapping
    *******************************************************************************/
   public void setDoValueMapping(Boolean doValueMapping)
   {
      this.doValueMapping = doValueMapping;
   }



   /*******************************************************************************
    ** Fluent setter for doValueMapping
    *******************************************************************************/
   public BulkLoadProfileField withDoValueMapping(Boolean doValueMapping)
   {
      this.doValueMapping = doValueMapping;
      return (this);
   }



   /*******************************************************************************
    ** Getter for valueMappings
    *******************************************************************************/
   public Map<String, Serializable> getValueMappings()
   {
      return (this.valueMappings);
   }



   /*******************************************************************************
    ** Setter for valueMappings
    *******************************************************************************/
   public void setValueMappings(Map<String, Serializable> valueMappings)
   {
      this.valueMappings = valueMappings;
   }



   /*******************************************************************************
    ** Fluent setter for valueMappings
    *******************************************************************************/
   public BulkLoadProfileField withValueMappings(Map<String, Serializable> valueMappings)
   {
      this.valueMappings = valueMappings;
      return (this);
   }

}
