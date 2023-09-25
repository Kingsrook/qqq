/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.model.processlogs;


import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** QRecord Entity for ProcessLogValue table
 *******************************************************************************/
public class ProcessLogValue extends QRecordEntity
{
   public static final String TABLE_NAME = "processLogValue";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false, possibleValueSourceName = ProcessLog.TABLE_NAME)
   private Integer processLogId;

   @QField(isEditable = false)
   private String name;

   @QField(isEditable = false)
   private String value;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public ProcessLogValue()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public ProcessLogValue(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public ProcessLogValue withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processLogId
    *******************************************************************************/
   public Integer getProcessLogId()
   {
      return (this.processLogId);
   }



   /*******************************************************************************
    ** Setter for processLogId
    *******************************************************************************/
   public void setProcessLogId(Integer processLogId)
   {
      this.processLogId = processLogId;
   }



   /*******************************************************************************
    ** Fluent setter for processLogId
    *******************************************************************************/
   public ProcessLogValue withProcessLogId(Integer processLogId)
   {
      this.processLogId = processLogId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public ProcessLogValue withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for value
    *******************************************************************************/
   public String getValue()
   {
      return (this.value);
   }



   /*******************************************************************************
    ** Setter for value
    *******************************************************************************/
   public void setValue(String value)
   {
      this.value = value;
   }



   /*******************************************************************************
    ** Fluent setter for value
    *******************************************************************************/
   public ProcessLogValue withValue(String value)
   {
      this.value = value;
      return (this);
   }

}
