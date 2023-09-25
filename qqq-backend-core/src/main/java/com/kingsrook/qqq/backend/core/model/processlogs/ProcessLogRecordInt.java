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
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;


/*******************************************************************************
 ** QRecord Entity for ProcessLogRecordInt table
 *******************************************************************************/
public class ProcessLogRecordInt extends QRecordEntity
{
   public static final String TABLE_NAME = "processLogRecordInt";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false, possibleValueSourceName = ProcessLog.TABLE_NAME)
   private Integer processLogId;

   @QField(isEditable = false, possibleValueSourceName = QQQTable.TABLE_NAME, label = "Table")
   private Integer qqqTableId;

   @QField(isEditable = false)
   private String recordId;

   @QField(isEditable = false) // todo pvs
   private String status;

   @QField(isEditable = false)
   private String message;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public ProcessLogRecordInt()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public ProcessLogRecordInt(QRecord record)
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
   public ProcessLogRecordInt withId(Integer id)
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
   public ProcessLogRecordInt withProcessLogId(Integer processLogId)
   {
      this.processLogId = processLogId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for qqqTableId
    *******************************************************************************/
   public Integer getQqqTableId()
   {
      return (this.qqqTableId);
   }



   /*******************************************************************************
    ** Setter for qqqTableId
    *******************************************************************************/
   public void setQqqTableId(Integer qqqTableId)
   {
      this.qqqTableId = qqqTableId;
   }



   /*******************************************************************************
    ** Fluent setter for qqqTableId
    *******************************************************************************/
   public ProcessLogRecordInt withQqqTableId(Integer qqqTableId)
   {
      this.qqqTableId = qqqTableId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for status
    *******************************************************************************/
   public String getStatus()
   {
      return (this.status);
   }



   /*******************************************************************************
    ** Setter for status
    *******************************************************************************/
   public void setStatus(String status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Fluent setter for status
    *******************************************************************************/
   public ProcessLogRecordInt withStatus(String status)
   {
      this.status = status;
      return (this);
   }



   /*******************************************************************************
    ** Getter for message
    *******************************************************************************/
   public String getMessage()
   {
      return (this.message);
   }



   /*******************************************************************************
    ** Setter for message
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    ** Fluent setter for message
    *******************************************************************************/
   public ProcessLogRecordInt withMessage(String message)
   {
      this.message = message;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordId
    *******************************************************************************/
   public String getRecordId()
   {
      return (this.recordId);
   }



   /*******************************************************************************
    ** Setter for recordId
    *******************************************************************************/
   public void setRecordId(String recordId)
   {
      this.recordId = recordId;
   }



   /*******************************************************************************
    ** Fluent setter for recordId
    *******************************************************************************/
   public ProcessLogRecordInt withRecordId(String recordId)
   {
      this.recordId = recordId;
      return (this);
   }

}
