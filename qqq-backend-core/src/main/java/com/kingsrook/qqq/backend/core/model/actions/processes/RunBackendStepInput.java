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

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobCallback;
import com.kingsrook.qqq.backend.core.actions.async.AsyncJobStatus;
import com.kingsrook.qqq.backend.core.actions.processes.QProcessCallback;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Input data container for the RunBackendStep action
 **
 *******************************************************************************/
public class RunBackendStepInput extends AbstractActionInput
{
   private ProcessState                         processState;
   private String                               processName;
   private String                               tableName;
   private String                               stepName;
   private QProcessCallback                     callback;
   private AsyncJobCallback                     asyncJobCallback;
   private RunProcessInput.FrontendStepBehavior frontendStepBehavior;
   private Instant                              basepullLastRunTime;

   ////////////////////////////////////////////////////////////////////////////
   // note - new fields should generally be added in method: cloneFieldsInto //
   ////////////////////////////////////////////////////////////////////////////



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunBackendStepInput()
   {
      processState = new ProcessState();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunBackendStepInput(ProcessState processState)
   {
      this.processState = processState;
   }



   /*******************************************************************************
    ** Kinda like a reverse copy-constructor -- for a subclass that wants all the
    ** field values from this object.  Keep this in sync with the fields in this class!
    **
    ** Of note - the processState does NOT get cloned - because...  well, in our first
    ** use-case (a subclass that doesn't WANT the same/full state), that's what we needed.
    *******************************************************************************/
   public void cloneFieldsInto(RunBackendStepInput target)
   {
      target.setStepName(getStepName());
      target.setTableName(getTableName());
      target.setProcessName(getProcessName());
      target.setAsyncJobCallback(getAsyncJobCallback());
      target.setFrontendStepBehavior(getFrontendStepBehavior());
      target.setValues(getValues());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QStepMetaData getStepMetaData()
   {
      return (QContext.getQInstance().getProcessStep(getProcessName(), getStepName()));
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
    ** Setter for processName
    **
    *******************************************************************************/
   public RunBackendStepInput withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
   }



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
   public RunBackendStepInput withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData getTable()
   {
      if(tableName == null)
      {
         return (null);
      }

      return (QContext.getQInstance().getTable(tableName));
   }



   /*******************************************************************************
    ** Getter for functionName
    **
    *******************************************************************************/
   public String getStepName()
   {
      return stepName;
   }



   /*******************************************************************************
    ** Setter for functionName
    **
    *******************************************************************************/
   public void setStepName(String stepName)
   {
      this.stepName = stepName;
   }



   /*******************************************************************************
    ** Setter for functionName
    **
    *******************************************************************************/
   public RunBackendStepInput withFunctionName(String functionName)
   {
      this.stepName = functionName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for records
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return processState.getRecords();
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.processState.setRecords(records);
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public RunBackendStepInput withRecords(List<QRecord> records)
   {
      this.processState.setRecords(records);
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return processState.getValues();
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.processState.setValues(values);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunBackendStepInput withValues(Map<String, Serializable> values)
   {
      this.processState.setValues(values);
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunBackendStepInput addValue(String fieldName, Serializable value)
   {
      this.processState.getValues().put(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for callback
    **
    *******************************************************************************/
   public QProcessCallback getCallback()
   {
      return callback;
   }



   /*******************************************************************************
    ** Setter for callback
    **
    *******************************************************************************/
   public void setCallback(QProcessCallback callback)
   {
      this.callback = callback;
   }



   /*******************************************************************************
    ** Setter for callback
    **
    *******************************************************************************/
   public RunBackendStepInput withCallback(QProcessCallback callback)
   {
      this.callback = callback;
      return (this);
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Serializable getValue(String fieldName)
   {
      return (processState.getValues().get(fieldName));
   }



   /*******************************************************************************
    ** Getter for a single field's date value
    **
    *******************************************************************************/
   public LocalDate getValueLocalDate(String fieldName)
   {
      return (ValueUtils.getValueAsLocalDate(getValue(fieldName)));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getValueString(String fieldName)
   {
      return (ValueUtils.getValueAsString(getValue(fieldName)));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Boolean getValueBoolean(String fieldName)
   {
      return (ValueUtils.getValueAsBoolean(getValue(fieldName)));
   }



   /*******************************************************************************
    ** Getter for a single field's value as a primitive boolean - with null => false.
    **
    *******************************************************************************/
   public boolean getValuePrimitiveBoolean(String fieldName)
   {
      Boolean valueAsBoolean = ValueUtils.getValueAsBoolean(getValue(fieldName));
      return (valueAsBoolean != null && valueAsBoolean);
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Integer getValueInteger(String fieldName)
   {
      return (ValueUtils.getValueAsInteger(getValue(fieldName)));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Instant getValueInstant(String fieldName)
   {
      return (ValueUtils.getValueAsInstant(getValue(fieldName)));
   }



   /*******************************************************************************
    ** Accessor for processState - protected, because we generally want to access
    ** its members through wrapper methods, we think
    **
    *******************************************************************************/
   protected ProcessState getProcessState()
   {
      return processState;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setAsyncJobCallback(AsyncJobCallback asyncJobCallback)
   {
      this.asyncJobCallback = asyncJobCallback;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AsyncJobCallback getAsyncJobCallback()
   {
      if(asyncJobCallback == null)
      {
         /////////////////////////////////////////////////////////////////////////
         // avoid NPE in case we didn't have one of these!  create a new one... //
         /////////////////////////////////////////////////////////////////////////
         asyncJobCallback = new AsyncJobCallback(UUID.randomUUID(), new AsyncJobStatus());
      }
      return (asyncJobCallback);
   }



   /*******************************************************************************
    ** Getter for frontendStepBehavior
    **
    *******************************************************************************/
   public RunProcessInput.FrontendStepBehavior getFrontendStepBehavior()
   {
      return frontendStepBehavior;
   }



   /*******************************************************************************
    ** Setter for frontendStepBehavior
    **
    *******************************************************************************/
   public void setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior frontendStepBehavior)
   {
      this.frontendStepBehavior = frontendStepBehavior;
   }



   /*******************************************************************************
    ** Fluent setter for frontendStepBehavior
    **
    *******************************************************************************/
   public RunBackendStepInput withFrontendStepBehavior(RunProcessInput.FrontendStepBehavior frontendStepBehavior)
   {
      this.frontendStepBehavior = frontendStepBehavior;
      return (this);
   }



   /*******************************************************************************
    ** Getter for basepullLastRunTime
    **
    *******************************************************************************/
   public Instant getBasepullLastRunTime()
   {
      return basepullLastRunTime;
   }



   /*******************************************************************************
    ** Setter for basepullLastRunTime
    **
    *******************************************************************************/
   public void setBasepullLastRunTime(Instant basepullLastRunTime)
   {
      this.basepullLastRunTime = basepullLastRunTime;
   }



   /*******************************************************************************
    ** Fluent setter for basepullLastRunTime
    **
    *******************************************************************************/
   public RunBackendStepInput withBasepullLastRunTime(Instant basepullLastRunTime)
   {
      this.basepullLastRunTime = basepullLastRunTime;
      return (this);
   }

}
