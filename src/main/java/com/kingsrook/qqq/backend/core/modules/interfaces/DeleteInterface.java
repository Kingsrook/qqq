/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.delete.DeleteResult;


/*******************************************************************************
 ** Interface for the Delete action.
 **
 *******************************************************************************/
public interface DeleteInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   DeleteResult execute(DeleteRequest deleteRequest) throws QException;
}
