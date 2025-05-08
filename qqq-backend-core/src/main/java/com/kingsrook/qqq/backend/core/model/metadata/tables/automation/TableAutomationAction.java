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


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 ** Definition of a specific action to run against a table
 *******************************************************************************/
public class TableAutomationAction implements QMetaDataObject
{
   private String       name;
   private TriggerEvent triggerEvent;
   private Integer      priority = 500;
   private QQueryFilter filter;
   private Serializable shardId;

   ////////////////////////////////////////////////////////////////////////
   // flag that will cause the records to cause their associations to be //
   // fetched, when they are looked up for passing into the action       //
   ////////////////////////////////////////////////////////////////////////
   private boolean includeRecordAssociations = false;

   private Map<String, Serializable> values;

   ////////////////////////////////
   // mutually-exclusive options //
   ////////////////////////////////
   private QCodeReference codeReference;
   private String         processName;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "TableAutomationAction{name='" + name + "'}";
   }



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    **
    *******************************************************************************/
   public TableAutomationAction withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for triggerEvent
    **
    *******************************************************************************/
   public TriggerEvent getTriggerEvent()
   {
      return triggerEvent;
   }



   /*******************************************************************************
    ** Setter for triggerEvent
    **
    *******************************************************************************/
   public void setTriggerEvent(TriggerEvent triggerEvent)
   {
      this.triggerEvent = triggerEvent;
   }



   /*******************************************************************************
    ** Fluent setter for triggerEvent
    **
    *******************************************************************************/
   public TableAutomationAction withTriggerEvent(TriggerEvent triggerEvent)
   {
      this.triggerEvent = triggerEvent;
      return (this);
   }



   /*******************************************************************************
    ** Getter for priority
    **
    *******************************************************************************/
   public Integer getPriority()
   {
      return priority;
   }



   /*******************************************************************************
    ** Setter for priority
    **
    *******************************************************************************/
   public void setPriority(Integer priority)
   {
      this.priority = priority;
   }



   /*******************************************************************************
    ** Fluent setter for priority
    **
    *******************************************************************************/
   public TableAutomationAction withPriority(Integer priority)
   {
      this.priority = priority;
      return (this);
   }



   /*******************************************************************************
    ** Getter for filter
    **
    *******************************************************************************/
   public QQueryFilter getFilter()
   {
      return filter;
   }



   /*******************************************************************************
    ** Setter for filter
    **
    *******************************************************************************/
   public void setFilter(QQueryFilter filter)
   {
      this.filter = filter;
   }



   /*******************************************************************************
    ** Fluent setter for filter
    **
    *******************************************************************************/
   public TableAutomationAction withFilter(QQueryFilter filter)
   {
      this.filter = filter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for codeReference
    **
    *******************************************************************************/
   public QCodeReference getCodeReference()
   {
      return codeReference;
   }



   /*******************************************************************************
    ** Setter for codeReference
    **
    *******************************************************************************/
   public void setCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
   }



   /*******************************************************************************
    ** Fluent setter for codeReference
    **
    *******************************************************************************/
   public TableAutomationAction withCodeReference(QCodeReference codeReference)
   {
      this.codeReference = codeReference;
      return (this);
   }



   /*******************************************************************************
    ** Getter for processName
    **
    *******************************************************************************/
   public String getProcessName()
   {
      return processName;
   }



   /*******************************************************************************
    ** Setter for processName
    **
    *******************************************************************************/
   public void setProcessName(String processName)
   {
      this.processName = processName;
   }



   /*******************************************************************************
    ** Fluent setter for processName
    **
    *******************************************************************************/
   public TableAutomationAction withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public TableAutomationAction withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Getter for includeRecordAssociations
    *******************************************************************************/
   public boolean getIncludeRecordAssociations()
   {
      return (this.includeRecordAssociations);
   }



   /*******************************************************************************
    ** Setter for includeRecordAssociations
    *******************************************************************************/
   public void setIncludeRecordAssociations(boolean includeRecordAssociations)
   {
      this.includeRecordAssociations = includeRecordAssociations;
   }



   /*******************************************************************************
    ** Fluent setter for includeRecordAssociations
    *******************************************************************************/
   public TableAutomationAction withIncludeRecordAssociations(boolean includeRecordAssociations)
   {
      this.includeRecordAssociations = includeRecordAssociations;
      return (this);
   }



   /*******************************************************************************
    ** Getter for shardId
    *******************************************************************************/
   public Serializable getShardId()
   {
      return (this.shardId);
   }



   /*******************************************************************************
    ** Setter for shardId
    *******************************************************************************/
   public void setShardId(Serializable shardId)
   {
      this.shardId = shardId;
   }



   /*******************************************************************************
    ** Fluent setter for shardId
    *******************************************************************************/
   public TableAutomationAction withShardId(Serializable shardId)
   {
      this.shardId = shardId;
      return (this);
   }

}
