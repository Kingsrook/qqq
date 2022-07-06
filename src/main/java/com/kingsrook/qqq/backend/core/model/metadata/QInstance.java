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

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidationKey;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
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

   private Map<String, QTableMetaData>          tables               = new HashMap<>();
   private Map<String, QPossibleValueSource<?>> possibleValueSources = new HashMap<>();
   private Map<String, QProcessMetaData>        processes            = new HashMap<>();

   // todo - lock down the object (no more changes allowed) after it's been validated?

   @JsonIgnore
   private boolean hasBeenValidated = false;



   /*******************************************************************************
    ** Get the backend for a given table name
    *******************************************************************************/
   public QBackendMetaData getBackendForTable(String tableName)
   {
      QTableMetaData table = tables.get(tableName);
      if(table == null)
      {
         throw (new IllegalArgumentException("No table with name [" + tableName + "] found in this instance."));
      }

      //////////////////////////////////////////////////////////////////////////////////////////////
      // validation should already let us know that this is valid, so no need to check/throw here //
      //////////////////////////////////////////////////////////////////////////////////////////////
      return (backends.get(table.getBackendName()));
   }



   /*******************************************************************************
    ** Get the list of processes associated with a given table name
    *******************************************************************************/
   public List<QProcessMetaData> getProcessesForTable(String tableName)
   {
      List<QProcessMetaData> rs = new ArrayList<>();
      for(QProcessMetaData process : processes.values())
      {
         if(tableName.equals(process.getTableName()))
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
      addBackend(backend.getName(), backend);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addBackend(String name, QBackendMetaData backend)
   {
      if(this.backends.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second backend with name: " + name));
      }
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
      addTable(table.getName(), table);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addTable(String name, QTableMetaData table)
   {
      if(this.tables.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second table with name: " + name));
      }
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
   public void addPossibleValueSource(QPossibleValueSource<?> possibleValueSource)
   {
      this.addPossibleValueSource(possibleValueSource.getName(), possibleValueSource);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addPossibleValueSource(String name, QPossibleValueSource possibleValueSource)
   {
      if(this.possibleValueSources.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second possibleValueSource with name: " + name));
      }
      this.possibleValueSources.put(name, possibleValueSource);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QPossibleValueSource getPossibleValueSource(String name)
   {
      return (this.possibleValueSources.get(name));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QStepMetaData getProcessStep(String processName, String functionName)
   {
      QProcessMetaData qProcessMetaData = this.processes.get(processName);
      if(qProcessMetaData == null)
      {
         return (null);
      }

      return (qProcessMetaData.getStep(functionName));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addProcess(QProcessMetaData process)
   {
      this.addProcess(process.getName(), process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void addProcess(String name, QProcessMetaData process)
   {
      if(this.processes.containsKey(name))
      {
         throw (new IllegalArgumentException("Attempted to add a second process with name: " + name));
      }
      this.processes.put(name, process);
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
    ** Getter for possibleValueSources
    **
    *******************************************************************************/
   public Map<String, QPossibleValueSource<?>> getPossibleValueSources()
   {
      return possibleValueSources;
   }



   /*******************************************************************************
    ** Setter for possibleValueSources
    **
    *******************************************************************************/
   public void setPossibleValueSources(Map<String, QPossibleValueSource<?>> possibleValueSources)
   {
      this.possibleValueSources = possibleValueSources;
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
