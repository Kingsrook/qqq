/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.processes;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.callbacks.QProcessCallback;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQRequest;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Request data container for the RunProcess action
 **
 *******************************************************************************/
public class RunProcessRequest extends AbstractQRequest
{
   private String processName;
   private List<QRecord> records;
   private Map<String, Serializable> values;
   private QProcessCallback callback;



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public RunProcessRequest(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData getProcessMetaData()
   {
      return (instance.getProcess(getProcessName()));
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
   public RunProcessRequest withProcessName(String processName)
   {
      this.processName = processName;
      return (this);
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
   public RunProcessRequest withRecords(List<QRecord> records)
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
   public RunProcessRequest withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Setter for values
    **
    *******************************************************************************/
   public RunProcessRequest addValue(String fieldName, Serializable value)
   {
      if(this.values == null)
      {
         this.values = new HashMap<>();
      }
      this.values.put(fieldName, value);
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
   public RunProcessRequest withCallback(QProcessCallback callback)
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
      if(values == null)
      {
         return (null);
      }
      return (values.get(fieldName));
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
      return ((Integer) getValue(fieldName));
   }

}