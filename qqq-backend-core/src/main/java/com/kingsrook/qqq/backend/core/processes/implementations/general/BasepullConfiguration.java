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

package com.kingsrook.qqq.backend.core.processes.implementations.general;


import java.io.Serializable;


/*******************************************************************************
 ** Class for storing all basepull configuration data
 **
 *******************************************************************************/
public class BasepullConfiguration implements Serializable
{
   private String tableName;
   private String keyField;
   private String keyValue;

   private String  lastRunTimeFieldName;
   private Integer hoursBackForInitialTimestamp;



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    **
    *******************************************************************************/
   public BasepullConfiguration withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for keyField
    **
    *******************************************************************************/
   public String getKeyField()
   {
      return keyField;
   }



   /*******************************************************************************
    ** Setter for keyField
    **
    *******************************************************************************/
   public void setKeyField(String keyField)
   {
      this.keyField = keyField;
   }



   /*******************************************************************************
    ** Fluent setter for keyField
    **
    *******************************************************************************/
   public BasepullConfiguration withKeyField(String keyField)
   {
      this.keyField = keyField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for keyValue
    **
    *******************************************************************************/
   public String getKeyValue()
   {
      return keyValue;
   }



   /*******************************************************************************
    ** Setter for keyValue
    **
    *******************************************************************************/
   public void setKeyValue(String keyValue)
   {
      this.keyValue = keyValue;
   }



   /*******************************************************************************
    ** Fluent setter for keyValue
    **
    *******************************************************************************/
   public BasepullConfiguration withKeyValue(String keyValue)
   {
      this.keyValue = keyValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for lastRunTimeFieldName
    **
    *******************************************************************************/
   public String getLastRunTimeFieldName()
   {
      return lastRunTimeFieldName;
   }



   /*******************************************************************************
    ** Setter for lastRunTimeFieldName
    **
    *******************************************************************************/
   public void setLastRunTimeFieldName(String lastRunTimeFieldName)
   {
      this.lastRunTimeFieldName = lastRunTimeFieldName;
   }



   /*******************************************************************************
    ** Fluent setter for lastRunTimeFieldName
    **
    *******************************************************************************/
   public BasepullConfiguration withLastRunTimeFieldName(String lastRunTimeFieldName)
   {
      this.lastRunTimeFieldName = lastRunTimeFieldName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hoursBackForInitialTimestamp
    **
    *******************************************************************************/
   public Integer getHoursBackForInitialTimestamp()
   {
      return hoursBackForInitialTimestamp;
   }



   /*******************************************************************************
    ** Setter for hoursBackForInitialTimestamp
    **
    *******************************************************************************/
   public void setHoursBackForInitialTimestamp(Integer hoursBackForInitialTimestamp)
   {
      this.hoursBackForInitialTimestamp = hoursBackForInitialTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for hoursBackForInitialTimestamp
    **
    *******************************************************************************/
   public BasepullConfiguration withHoursBackForInitialTimestamp(Integer hoursBackForInitialTimestamp)
   {
      this.hoursBackForInitialTimestamp = hoursBackForInitialTimestamp;
      return (this);
   }

}
