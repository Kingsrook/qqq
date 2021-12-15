/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions;


import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;


/*******************************************************************************
 * Result for a table-metaData action
 *
 *******************************************************************************/
public class TableMetaDataResult extends AbstractQResult
{
   QFrontendTableMetaData table;



   /*******************************************************************************
    ** Getter for table
    **
    *******************************************************************************/
   public QFrontendTableMetaData getTable()
   {
      return table;
   }



   /*******************************************************************************
    ** Setter for table
    **
    *******************************************************************************/
   public void setTable(QFrontendTableMetaData table)
   {
      this.table = table;
   }
}
