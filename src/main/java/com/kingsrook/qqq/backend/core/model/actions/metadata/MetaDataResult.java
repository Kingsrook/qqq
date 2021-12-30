/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.actions.metadata;


import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.AbstractQResult;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;


/*******************************************************************************
 * Result for a metaData action
 *
 *******************************************************************************/
public class MetaDataResult extends AbstractQResult
{
   Map<String, QFrontendTableMetaData> tables;



   /*******************************************************************************
    ** Getter for tables
    **
    *******************************************************************************/
   public Map<String, QFrontendTableMetaData> getTables()
   {
      return tables;
   }



   /*******************************************************************************
    ** Setter for tables
    **
    *******************************************************************************/
   public void setTables(Map<String, QFrontendTableMetaData> tables)
   {
      this.tables = tables;
   }
}
