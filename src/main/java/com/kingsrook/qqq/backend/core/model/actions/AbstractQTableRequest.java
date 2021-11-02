package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractQTableRequest extends AbstractQRequest
{
   private String tableName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public QBackendMetaData getBackend()
   {
      return (instance.getBackendForTable(getTableName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QTableMetaData getTable()
   {
      return (instance.getTable(getTableName()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQTableRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public AbstractQTableRequest(QInstance instance)
   {
      super(instance);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Setter for tableName
    **
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }
}
