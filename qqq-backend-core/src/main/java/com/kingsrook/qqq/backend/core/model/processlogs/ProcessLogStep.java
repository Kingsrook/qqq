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


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** QRecord Entity for ProcessLogStep table - e.g., details about a particular step
 ** in a process
 *******************************************************************************/
public class ProcessLogStep extends QRecordEntity
{
   public static final String TABLE_NAME = "processLogStep";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false, possibleValueSourceName = ProcessLog.TABLE_NAME)
   private Integer processLogId;

   @QField(isEditable = false)
   private String name;

   @QField(isEditable = false)
   private Instant startTime;

   @QField(isEditable = false)
   private Instant endTime;

   @QField(isEditable = false)
   private Integer recordCount;

   @QField(isEditable = false)
   private Integer runTimeMillis;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public ProcessLogStep()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public ProcessLogStep(QRecord record)
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
   public ProcessLogStep withId(Integer id)
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
   public ProcessLogStep withProcessLogId(Integer processLogId)
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
   public ProcessLogStep withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startTime
    *******************************************************************************/
   public Instant getStartTime()
   {
      return (this.startTime);
   }



   /*******************************************************************************
    ** Setter for startTime
    *******************************************************************************/
   public void setStartTime(Instant startTime)
   {
      this.startTime = startTime;
   }



   /*******************************************************************************
    ** Fluent setter for startTime
    *******************************************************************************/
   public ProcessLogStep withStartTime(Instant startTime)
   {
      this.startTime = startTime;
      return (this);
   }



   /*******************************************************************************
    ** Getter for endTime
    *******************************************************************************/
   public Instant getEndTime()
   {
      return (this.endTime);
   }



   /*******************************************************************************
    ** Setter for endTime
    *******************************************************************************/
   public void setEndTime(Instant endTime)
   {
      this.endTime = endTime;
   }



   /*******************************************************************************
    ** Fluent setter for endTime
    *******************************************************************************/
   public ProcessLogStep withEndTime(Instant endTime)
   {
      this.endTime = endTime;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordCount
    *******************************************************************************/
   public Integer getRecordCount()
   {
      return (this.recordCount);
   }



   /*******************************************************************************
    ** Setter for recordCount
    *******************************************************************************/
   public void setRecordCount(Integer recordCount)
   {
      this.recordCount = recordCount;
   }



   /*******************************************************************************
    ** Fluent setter for recordCount
    *******************************************************************************/
   public ProcessLogStep withRecordCount(Integer recordCount)
   {
      this.recordCount = recordCount;
      return (this);
   }



   /*******************************************************************************
    ** Getter for runTimeMillis
    *******************************************************************************/
   public Integer getRunTimeMillis()
   {
      return (this.runTimeMillis);
   }



   /*******************************************************************************
    ** Setter for runTimeMillis
    *******************************************************************************/
   public void setRunTimeMillis(Integer runTimeMillis)
   {
      this.runTimeMillis = runTimeMillis;
   }



   /*******************************************************************************
    ** Fluent setter for runTimeMillis
    *******************************************************************************/
   public ProcessLogStep withRunTimeMillis(Integer runTimeMillis)
   {
      this.runTimeMillis = runTimeMillis;
      return (this);
   }

}
