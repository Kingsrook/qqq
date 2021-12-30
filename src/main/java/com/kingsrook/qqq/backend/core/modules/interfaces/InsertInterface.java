/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertRequest;
import com.kingsrook.qqq.backend.core.model.actions.insert.InsertResult;


/*******************************************************************************
 ** Interface for the Insert action.
 **
 *******************************************************************************/
public interface InsertInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   InsertResult execute(InsertRequest insertRequest) throws QException;
}
