/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

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
