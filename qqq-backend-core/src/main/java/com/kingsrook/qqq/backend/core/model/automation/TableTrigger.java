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

package com.kingsrook.qqq.backend.core.model.automation;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.savedfilters.SavedFilter;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;


/*******************************************************************************
 ** Definition of in-app/user/data-defined triggers (aka, automations).
 *******************************************************************************/
public class TableTrigger extends QRecordEntity
{
   public static final String TABLE_NAME = "tableTrigger";

   @QField(isEditable = false)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(possibleValueSourceName = QQQTable.TABLE_NAME)
   private Integer tableId;

   @QField(possibleValueSourceName = SavedFilter.TABLE_NAME)
   private Integer filterId;

   @QField(possibleValueSourceName = Script.TABLE_NAME)
   private Integer scriptId;

   @QField()
   private Integer priority;

   @QField()
   private Boolean postInsert;

   @QField()
   private Boolean postUpdate;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TableTrigger()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public TableTrigger(QRecord qRecord) throws QException
   {
      populateFromQRecord(qRecord);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Setter for id
    **
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    **
    *******************************************************************************/
   public TableTrigger withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    **
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return createDate;
   }



   /*******************************************************************************
    ** Setter for createDate
    **
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    **
    *******************************************************************************/
   public TableTrigger withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    **
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return modifyDate;
   }



   /*******************************************************************************
    ** Setter for modifyDate
    **
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    **
    *******************************************************************************/
   public TableTrigger withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filterId
    **
    *******************************************************************************/
   public Integer getFilterId()
   {
      return filterId;
   }



   /*******************************************************************************
    ** Setter for filterId
    **
    *******************************************************************************/
   public void setFilterId(Integer filterId)
   {
      this.filterId = filterId;
   }



   /*******************************************************************************
    ** Fluent setter for filterId
    **
    *******************************************************************************/
   public TableTrigger withFilterId(Integer filterId)
   {
      this.filterId = filterId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for scriptId
    **
    *******************************************************************************/
   public Integer getScriptId()
   {
      return scriptId;
   }



   /*******************************************************************************
    ** Setter for scriptId
    **
    *******************************************************************************/
   public void setScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
   }



   /*******************************************************************************
    ** Fluent setter for scriptId
    **
    *******************************************************************************/
   public TableTrigger withScriptId(Integer scriptId)
   {
      this.scriptId = scriptId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for postInsert
    **
    *******************************************************************************/
   public Boolean getPostInsert()
   {
      return postInsert;
   }



   /*******************************************************************************
    ** Setter for postInsert
    **
    *******************************************************************************/
   public void setPostInsert(Boolean postInsert)
   {
      this.postInsert = postInsert;
   }



   /*******************************************************************************
    ** Fluent setter for postInsert
    **
    *******************************************************************************/
   public TableTrigger withPostInsert(Boolean postInsert)
   {
      this.postInsert = postInsert;
      return (this);
   }



   /*******************************************************************************
    ** Getter for postUpdate
    **
    *******************************************************************************/
   public Boolean getPostUpdate()
   {
      return postUpdate;
   }



   /*******************************************************************************
    ** Setter for postUpdate
    **
    *******************************************************************************/
   public void setPostUpdate(Boolean postUpdate)
   {
      this.postUpdate = postUpdate;
   }



   /*******************************************************************************
    ** Fluent setter for postUpdate
    **
    *******************************************************************************/
   public TableTrigger withPostUpdate(Boolean postUpdate)
   {
      this.postUpdate = postUpdate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for priority
    *******************************************************************************/
   public Integer getPriority()
   {
      return (this.priority);
   }



   /*******************************************************************************
    ** Setter for priority
    *******************************************************************************/
   public void setPriority(Integer priority)
   {
      this.priority = priority;
   }



   /*******************************************************************************
    ** Fluent setter for priority
    *******************************************************************************/
   public TableTrigger withPriority(Integer priority)
   {
      this.priority = priority;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableId
    *******************************************************************************/
   public Integer getTableId()
   {
      return (this.tableId);
   }



   /*******************************************************************************
    ** Setter for tableId
    *******************************************************************************/
   public void setTableId(Integer tableId)
   {
      this.tableId = tableId;
   }



   /*******************************************************************************
    ** Fluent setter for tableId
    *******************************************************************************/
   public TableTrigger withTableId(Integer tableId)
   {
      this.tableId = tableId;
      return (this);
   }

}
