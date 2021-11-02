package com.kingsrook.qqq.backend.core.actions;


import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataRequest;
import com.kingsrook.qqq.backend.core.model.actions.MetaDataResult;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendTableMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public class MetaDataAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public MetaDataResult execute(MetaDataRequest metaDataRequest) throws QException
   {
      // todo pre-customization - just get to modify the request?
      MetaDataResult metaDataResult = new MetaDataResult();

      Map<String, QFrontendTableMetaData> tables = new LinkedHashMap<>();
      for(Map.Entry<String, QTableMetaData> entry : metaDataRequest.getInstance().getTables().entrySet())
      {
         tables.put(entry.getKey(), new QFrontendTableMetaData(entry.getValue(), false));
      }

      metaDataResult.setTables(tables);
      // todo post-customization - can do whatever w/ the result if you want

      return metaDataResult;
   }
}
