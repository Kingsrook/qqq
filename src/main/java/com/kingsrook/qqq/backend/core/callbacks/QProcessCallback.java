/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.callbacks;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.model.actions.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;


/*******************************************************************************
 ** When a process/function can't run because it's missing data, this interface
 ** defines how the core framework goes back to a middleware (and possibly to a
 ** frontend) to get the data.
 *******************************************************************************/
public interface QProcessCallback
{
   /*******************************************************************************
    ** Get the filter query for this callback.
    *******************************************************************************/
   QQueryFilter getQueryFilter();

   /*******************************************************************************
    ** Get the field values for this callback.
    *******************************************************************************/
   Map<String, Serializable> getFieldValues(List<QFieldMetaData> fields);
}
