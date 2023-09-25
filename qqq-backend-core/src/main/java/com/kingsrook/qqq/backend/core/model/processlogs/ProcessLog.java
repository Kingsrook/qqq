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
import java.util.List;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.session.QQQUser;


/*******************************************************************************
 ** QRecord Entity for ProcessLog table - e.g., table to store results of running
 ** a process.
 *******************************************************************************/
public class ProcessLog extends QRecordEntity
{
   public static final String TABLE_NAME = "processLog";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false, possibleValueSourceName = QQQProcess.TABLE_NAME, label = "Process")
   private Integer qqqProcessId;

   @QField(isEditable = false)
   private Instant startTime;

   @QField(isEditable = false)
   private Instant endTime;

   @QField(isEditable = false, possibleValueSourceName = QQQUser.TABLE_NAME, label = "User")
   private Integer qqqUserId;

   @QAssociation(name = "processLogValues")
   private List<ProcessLogValue> processLogValueList;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public ProcessLog()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public ProcessLog(QRecord record)
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
   public ProcessLog withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for qqqProcessId
    *******************************************************************************/
   public Integer getQqqProcessId()
   {
      return (this.qqqProcessId);
   }



   /*******************************************************************************
    ** Setter for qqqProcessId
    *******************************************************************************/
   public void setQqqProcessId(Integer qqqProcessId)
   {
      this.qqqProcessId = qqqProcessId;
   }



   /*******************************************************************************
    ** Fluent setter for qqqProcessId
    *******************************************************************************/
   public ProcessLog withQqqProcessId(Integer qqqProcessId)
   {
      this.qqqProcessId = qqqProcessId;
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
   public ProcessLog withStartTime(Instant startTime)
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
   public ProcessLog withEndTime(Instant endTime)
   {
      this.endTime = endTime;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processLogValueList
    *******************************************************************************/
   public List<ProcessLogValue> getProcessLogValueList()
   {
      return (this.processLogValueList);
   }



   /*******************************************************************************
    ** Setter for processLogValueList
    *******************************************************************************/
   public void setProcessLogValueList(List<ProcessLogValue> processLogValueList)
   {
      this.processLogValueList = processLogValueList;
   }



   /*******************************************************************************
    ** Fluent setter for processLogValueList
    *******************************************************************************/
   public ProcessLog withProcessLogValueList(List<ProcessLogValue> processLogValueList)
   {
      this.processLogValueList = processLogValueList;
      return (this);
   }



   /*******************************************************************************
    ** Getter for qqqUserId
    *******************************************************************************/
   public Integer getQqqUserId()
   {
      return (this.qqqUserId);
   }



   /*******************************************************************************
    ** Setter for qqqUserId
    *******************************************************************************/
   public void setQqqUserId(Integer qqqUserId)
   {
      this.qqqUserId = qqqUserId;
   }



   /*******************************************************************************
    ** Fluent setter for qqqUserId
    *******************************************************************************/
   public ProcessLog withQqqUserId(Integer qqqUserId)
   {
      this.qqqUserId = qqqUserId;
      return (this);
   }

}
