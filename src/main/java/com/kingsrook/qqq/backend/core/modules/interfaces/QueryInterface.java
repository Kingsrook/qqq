package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.QueryRequest;
import com.kingsrook.qqq.backend.core.model.actions.QueryResult;


/*******************************************************************************
 **
 *******************************************************************************/
public interface QueryInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   QueryResult execute(QueryRequest queryRequest) throws QException;
}
