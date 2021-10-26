package com.kingsrook.qqq.backend.core.model;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class QInstance
{
   private Map<String, QBackendMetaData> backends = new HashMap<>();
   private Map<String, QTableMetaData> tables = new HashMap<>();



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
      if(backend == null)
      {
         throw (new IllegalArgumentException("Table [" + tableName + "] specified a backend name [" + table.getBackendName() + "] which was found in this instance."));
      }

      return (backend);
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
}
