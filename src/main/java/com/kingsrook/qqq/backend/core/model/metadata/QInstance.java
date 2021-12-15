/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata;


import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidationKey;


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

   private Map<String, QTableMetaData> tables = new HashMap<>();

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
    ** Getter for hasBeenValidated
    **
    *******************************************************************************/
   public boolean getHasBeenValidated()
   {
      return hasBeenValidated;
   }

}
