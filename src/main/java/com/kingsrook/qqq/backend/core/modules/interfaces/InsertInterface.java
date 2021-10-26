package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.InsertResult;


/*******************************************************************************
 **
 *******************************************************************************/
public interface InsertInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   InsertResult execute(InsertRequest insertRequest) throws QException;
}
