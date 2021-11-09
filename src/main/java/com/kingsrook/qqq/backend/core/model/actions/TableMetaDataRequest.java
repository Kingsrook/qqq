package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 **
 *******************************************************************************/
public class TableMetaDataRequest extends AbstractQRequest
{
   private String tableName;



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableMetaDataRequest()
   {
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableMetaDataRequest(QInstance instance)
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
