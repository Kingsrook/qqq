/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Result data container for the RunFunction action
 **
 *******************************************************************************/
public class RunFunctionResult extends AbstractQResult
{
   private ProcessState processState;
   private String error;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunFunctionResult()
   {
      this.processState = new ProcessState();
   }



   /*******************************************************************************
    ** e.g., populate the process state (records, values) in this result object.
    **
    *******************************************************************************/
   public void seedFromRequest(RunFunctionRequest runFunctionRequest)
   {
      this.processState = runFunctionRequest.getProcessState();
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
   public RunFunctionResult withRecords(List<QRecord> records)
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
   public RunFunctionResult withValues(Map<String, Serializable> values)
   {
      this.processState.setValues(values);
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunFunctionResult addValue(String fieldName, Serializable value)
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



   /*******************************************************************************
    ** Accessor for processState
    **
    *******************************************************************************/
   public ProcessState getProcessState()
   {
      return processState;
   }
}
