/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.modules.QModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QModuleInterface;


/*******************************************************************************
 ** Action to run a query against a table.
 **
 *******************************************************************************/
public class QueryAction
{
   /*******************************************************************************
    **
    *******************************************************************************/
   public QueryResult execute(QueryRequest queryRequest) throws QException
   {
      QModuleDispatcher qModuleDispatcher = new QModuleDispatcher();

      QBackendMetaData backend = queryRequest.getBackend();

      QModuleInterface qModule = qModuleDispatcher.getQModule(backend);
      // todo pre-customization - just get to modify the request?
      QueryResult queryResult = qModule.getQueryInterface().execute(queryRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return queryResult;
   }
}
