/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidationKey;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;


/*******************************************************************************
 ** Container for all meta-data in a running instance of a QQQ application.
 **
 *******************************************************************************/
public class QInstance
{
   ///////////////////////////////////////////////////////////////////////////////
   // Do not let the backend data be serialized - e.g., sent to a frontend user //
   ///////////////////////////////////////////////////////////////////////////////
   @JsonIgnore
   private Map<String, QBackendMetaData> backends = new HashMap<>();

   private QAuthenticationMetaData authentication = null;

   private Map<String, QTableMetaData> tables = new HashMap<>();
   private Map<String, QProcessMetaData> processes = new HashMap<>();

   // todo - lock down the object (no more changes allowed) after it's been validated?
   @JsonIgnore
   private boolean hasBeenValidated = false;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData getBackendForTable(String tableName)
   {
      QTableMetaData table = tables.get(tableName);
      if(table == null)
      {
         throw (new IllegalArgumentException("No table with name [" + tableName + "] found in this instance."));
      }

      QBackendMetaData backend = backends.get(table.getBackendName());

      //////////////////////////////////////////////////////////////////////////////////////////////
      // validation should already let us know that this is valid, so no need to check/throw here //
      //////////////////////////////////////////////////////////////////////////////////////////////

      return (backend);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QProcessMetaData> getProcessesForTable(String tableName)
   {
      List<QProcessMetaData> rs = new ArrayList<>();
      for(QProcessMetaData process : processes.values())
      {
         if (tableName.equals(process.getTableName()))
         {
            rs.add(process);
         }
      }
      return (rs);
   }


   /*******************************************************************************
    ** Setter for hasBeenValidated
    **
    *******************************************************************************/
   public void setHasBeenValidated(QInstanceValidationKey key)
   {
      this.hasBeenValidated = true;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBackend(QBackendMetaData backend)
   {
      this.backends.put(backend.getName(), backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBackend(String name, QBackendMetaData backend)
   {
      this.backends.put(name, backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData getBackend(String name)
   {
      return (this.backends.get(name));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTable(QTableMetaData table)
   {
      this.tables.put(table.getName(), table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addProcess(QProcessMetaData process)
   {
      this.processes.put(process.getName(), process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTable(String name, QTableMetaData table)
   {
      this.tables.put(name, table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData getTable(String name)
   {
      return (this.tables.get(name));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QFunctionMetaData getFunction(String processName, String functionName)
   {
      QProcessMetaData qProcessMetaData = this.processes.get(processName);
      if(qProcessMetaData == null)
      {
         return (null);
      }

      return (qProcessMetaData.getFunction(functionName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QProcessMetaData getProcess(String name)
   {
      return (this.processes.get(name));
   }



   /*******************************************************************************
    ** Getter for backends
    **
    *******************************************************************************/
   public Map<String, QBackendMetaData> getBackends()
   {
      return backends;
   }



   /*******************************************************************************
    ** Setter for backends
    **
    *******************************************************************************/
   public void setBackends(Map<String, QBackendMetaData> backends)
   {
      this.backends = backends;
   }



   /*******************************************************************************
    ** Getter for tables
    **
    *******************************************************************************/
   public Map<String, QTableMetaData> getTables()
   {
      return tables;
   }



   /*******************************************************************************
    ** Setter for tables
    **
    *******************************************************************************/
   public void setTables(Map<String, QTableMetaData> tables)
   {
      this.tables = tables;
   }



   /*******************************************************************************
    ** Getter for processes
    **
    *******************************************************************************/
   public Map<String, QProcessMetaData> getProcesses()
   {
      return processes;
   }



   /*******************************************************************************
    ** Setter for processes
    **
    *******************************************************************************/
   public void setProcesses(Map<String, QProcessMetaData> processes)
   {
      this.processes = processes;
   }



   /*******************************************************************************
    ** Getter for hasBeenValidated
    **
    *******************************************************************************/
   public boolean getHasBeenValidated()
   {
      return hasBeenValidated;
   }



   /*******************************************************************************
    ** Getter for authentication
    **
    *******************************************************************************/
   public QAuthenticationMetaData getAuthentication()
   {
      return authentication;
   }



   /*******************************************************************************
    ** Setter for authentication
    **
    *******************************************************************************/
   public void setAuthentication(QAuthenticationMetaData authentication)
   {
      this.authentication = authentication;
   }
}
