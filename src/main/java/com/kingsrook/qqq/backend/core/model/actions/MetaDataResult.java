package com.kingsrook.qqq.backend.core.model.actions;


import java.util.Map;
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
