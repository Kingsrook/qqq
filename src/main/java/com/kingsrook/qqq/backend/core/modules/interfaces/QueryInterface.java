/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.query.QueryResult;


/*******************************************************************************
 ** Interface for the Query action.
 **
 *******************************************************************************/
public interface QueryInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   QueryResult execute(QueryRequest queryRequest) throws QException;
}
