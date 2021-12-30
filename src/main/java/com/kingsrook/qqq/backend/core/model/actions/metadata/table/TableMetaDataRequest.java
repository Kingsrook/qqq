/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.metadata.table;


import com.kingsrook.qqq.backend.core.model.actions.AbstractQRequest;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;


/*******************************************************************************
 ** Request for meta-data for a table.
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
