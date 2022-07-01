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
import com.kingsrook.qqq.backend.core.model.actions.AbstractQResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Result data container for the RunProcess action
 **
 *******************************************************************************/
public class RunProcessResult extends AbstractQResult
{
   private ProcessState processState;
   private String       error;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String toString()
   {
      return "RunProcessResult{error='" + error
         + ",records.size()=" + (processState == null ? null : processState.getRecords().size())
         + ",values=" + (processState == null ? null : processState.getValues())
         + "}";
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessResult()
   {
      processState = new ProcessState();
   }



   /*******************************************************************************
    ** e.g., populate the process state (records, values) in this result object from
    ** the final function result
    **
    *******************************************************************************/
   public void seedFromLastFunctionResult(RunFunctionResult runFunctionResult)
   {
      this.processState = runFunctionResult.getProcessState();
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
   public RunProcessResult withRecords(List<QRecord> records)
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
   public RunProcessResult withValues(Map<String, Serializable> values)
   {
      this.processState.setValues(values);
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunProcessResult addValue(String fieldName, Serializable value)
   {
      this.processState.getValues().put(fieldName, value);
      return (this);
   }



   /*******************************************************************************
    ** Getter for error
    **
    *******************************************************************************/
   public String getError()
   {
      return error;
   }



   /*******************************************************************************
    ** Setter for error
    **
    *******************************************************************************/
   public void setError(String error)
   {
      this.error = error;
   }
}
