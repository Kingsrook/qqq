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
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Output data container for the RunBackendStep action
 **
 *******************************************************************************/
public class RunBackendStepOutput extends AbstractActionOutput
{
   private ProcessState processState;
   private Exception    exception; // todo - make optional



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "RunBackendStepOutput{exception?='" + (exception == null ? "null" : exception.getMessage())
         + ",records.size()=" + (processState == null ? null : processState.getRecords().size())
         + ",values=" + (processState == null ? null : processState.getValues())
         + "}";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunBackendStepOutput()
   {
      this.processState = new ProcessState();
   }



   /*******************************************************************************
    ** e.g., populate the process state (records, values) in this result object.
    **
    *******************************************************************************/
   public void seedFromRequest(RunBackendStepInput runBackendStepInput)
   {
      this.processState = runBackendStepInput.getProcessState();
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
   public RunBackendStepOutput withRecords(List<QRecord> records)
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
   public RunBackendStepOutput withValues(Map<String, Serializable> values)
   {
      this.processState.setValues(values);
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunBackendStepOutput addValue(String fieldName, Serializable value)
   {
      this.processState.getValues().put(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    ** Accessor for processState
    **
    *******************************************************************************/
   public ProcessState getProcessState()
   {
      return processState;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setException(Exception exception)
   {
      this.exception = exception;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Exception getException()
   {
      return exception;
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
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public String getValueString(String fieldName)
   {
      return ((String) getValue(fieldName));
   }



   /*******************************************************************************
    ** Getter for a single field's value
    **
    *******************************************************************************/
   public Integer getValueInteger(String fieldName)
   {
      return (ValueUtils.getValueAsInteger(getValue(fieldName)));
   }

}
