/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.metadata.table.TableMetaDataResult;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;


/*******************************************************************************
 ** Action to fetch meta-data for a table.
 **
 *******************************************************************************/
public class TableMetaDataAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public TableMetaDataResult execute(TableMetaDataRequest tableMetaDataRequest) throws QException
   {
      // todo pre-customization - just get to modify the request?
      TableMetaDataResult tableMetaDataResult = new TableMetaDataResult();

      QTableMetaData table = tableMetaDataRequest.getInstance().getTable(tableMetaDataRequest.getTableName());
      if(table == null)
      {
         throw (new QUserFacingException("Table [" + tableMetaDataRequest.getTableName() + "] was not found."));
      }
      tableMetaDataResult.setTable(new QFrontendTableMetaData(table, true));

      // todo post-customization - can do whatever w/ the result if you want

      return tableMetaDataResult;
   }
}
