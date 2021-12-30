/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.HashMap;
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
   private List<QRecord> records;
   private Map<String, Serializable> values;
   private String error;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessResult()
   {
   }



   /*******************************************************************************
    ** Getter for records
    **
    *******************************************************************************/
   public List<QRecord> getRecords()
   {
      return records;
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public void setRecords(List<QRecord> records)
   {
      this.records = records;
   }



   /*******************************************************************************
    ** Setter for records
    **
    *******************************************************************************/
   public RunProcessResult withRecords(List<QRecord> records)
   {
      this.records = records;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    **
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunProcessResult withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunProcessResult addValue(String fieldName, Serializable value)
   {
      if(this.values == null)
      {
         this.values = new HashMap<>();
      }
      this.values.put(fieldName, value);
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
