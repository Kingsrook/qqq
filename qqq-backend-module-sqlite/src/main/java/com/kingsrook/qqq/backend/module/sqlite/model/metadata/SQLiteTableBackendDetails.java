package com.kingsrook.qqq.backend.module.sqlite.model.metadata;


import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableBackendDetails;


/*******************************************************************************
 **
 *******************************************************************************/
public class SQLiteTableBackendDetails extends QTableBackendDetails
{
   private String tableName;



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public SQLiteTableBackendDetails withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }

}
