/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;
import com.kingsrook.qqq.backend.core.modules.QBackendModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.interfaces.QBackendModuleInterface;


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
      ActionHelper.validateSession(queryRequest);

      QBackendModuleDispatcher qBackendModuleDispatcher = new QBackendModuleDispatcher();
      QBackendModuleInterface qModule = qBackendModuleDispatcher.getQModule(queryRequest.getBackend());
      // todo pre-customization - just get to modify the request?
      QueryResult queryResult = qModule.getQueryInterface().execute(queryRequest);
      // todo post-customization - can do whatever w/ the result if you want
      return queryResult;
   }
}
