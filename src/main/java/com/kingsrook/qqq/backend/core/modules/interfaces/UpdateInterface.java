/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateRequest;
import com.kingsrook.qqq.backend.core.model.actions.update.UpdateResult;


/*******************************************************************************
 ** Interface for the update action.
 **
 *******************************************************************************/
public interface UpdateInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   UpdateResult execute(UpdateRequest updateRequest) throws QException;
}
