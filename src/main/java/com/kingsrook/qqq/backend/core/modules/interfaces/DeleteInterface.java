package com.kingsrook.qqq.backend.core.modules.interfaces;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.DeleteRequest;
import com.kingsrook.qqq.backend.core.model.actions.DeleteResult;


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
