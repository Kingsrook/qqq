package com.kingsrook.qqq.backend.module.rdbms.actions;


import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractRDBMSAction
{

   /*******************************************************************************
    **
    *******************************************************************************/
   protected String getColumnName(QFieldMetaData field)
   {
      if(field.getBackendName() != null)
      {
         return (field.getBackendName());
      }
      return (field.getName());
   }

}
